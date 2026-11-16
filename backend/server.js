const express = require('express');
const axios = require('axios');
const cors = require('cors');
require('dotenv').config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Configuration
const getEnv = (key, fallback = '') => (process.env[key] || fallback).trim();

const CONSUMER_KEY = getEnv('CONSUMER_KEY');
const CONSUMER_SECRET = getEnv('CONSUMER_SECRET');
const BUSINESS_SHORT_CODE = getEnv('BUSINESS_SHORT_CODE', '174379');
const PASSKEY = getEnv('PASSKEY');
const CALLBACK_URL = getEnv('CALLBACK_URL');
const PORT = process.env.PORT || 3000;

// Daraja API URLs
const SANDBOX_URL = 'https://sandbox.safaricom.co.ke';
const PRODUCTION_URL = 'https://api.safaricom.co.ke';
const BASE_URL = SANDBOX_URL; // Change to PRODUCTION_URL for live

// In-memory store for completed payment webhooks (CheckoutRequestID -> status object)
const completedPayments = new Map();
// Cache for query results to prevent rate limiting
const queryCache = new Map();

// Token caching variables
let cachedToken = null;
let tokenExpiry = 0;

/** Helper: Generate Kenya (EAT - UTC+3) timestamp format YYYYMMDDHHmmss required by Safaricom Daraja API */
function getFormattedTimestamp() {
  const now = new Date();
  const eatOffset = 3 * 60 * 60 * 1000;
  const eatDate = new Date(now.getTime() + eatOffset);

  const YYYY = eatDate.getUTCFullYear();
  const MM = String(eatDate.getUTCMonth() + 1).padStart(2, '0');
  const DD = String(eatDate.getUTCDate()).padStart(2, '0');
  const HH = String(eatDate.getUTCHours()).padStart(2, '0');
  const mm = String(eatDate.getUTCMinutes()).padStart(2, '0');
  const ss = String(eatDate.getUTCSeconds()).padStart(2, '0');

  return `${YYYY}${MM}${DD}${HH}${mm}${ss}`;
}

/** Helper: Ensure phone number is formatted as 2547XXXXXXXX or 2541XXXXXXXX */
function formatPhoneNumber(phone) {
  let cleaned = String(phone).replace(/\D/g, '');
  if (cleaned.startsWith('0')) {
    cleaned = '254' + cleaned.substring(1);
  } else if (cleaned.startsWith('7') || cleaned.startsWith('1')) {
    cleaned = '254' + cleaned;
  }
  return cleaned;
}

// Get OAuth Access Token (with 1-hour token caching to avoid Safaricom 403 & SpikeArrest violations)
async function getAccessToken() {
  try {
    const now = Date.now();
    if (cachedToken && now < tokenExpiry) {
      return cachedToken;
    }

    if (!CONSUMER_KEY || !CONSUMER_SECRET) {
      throw new Error('CONSUMER_KEY or CONSUMER_SECRET is missing in backend .env configuration.');
    }

    const auth = Buffer.from(`${CONSUMER_KEY}:${CONSUMER_SECRET}`).toString('base64');
    const response = await axios.get(
      `${BASE_URL}/oauth/v1/generate?grant_type=client_credentials`,
      {
        headers: {
          Authorization: `Basic ${auth}`
        }
      }
    );

    cachedToken = response.data.access_token;
    const expiresInSeconds = parseInt(response.data.expires_in || '3599', 10);
    // Refresh token 5 minutes before expiration
    tokenExpiry = now + (expiresInSeconds - 300) * 1000;

    console.log('✅ New Daraja OAuth access token obtained & cached.');
    return cachedToken;

  } catch (error) {
    if (cachedToken) {
      console.warn('⚠️ Daraja OAuth refresh failed, using cached token');
      return cachedToken;
    }
    const errorDetails = error.response?.data?.errorMessage || error.response?.data?.error_description || error.message;
    console.error('Error getting Daraja access token:', errorDetails);
    throw new Error(`Daraja OAuth token failed: ${errorDetails}`);
  }
}

// Initiate STK Push
app.post('/api/initiate-payment', async (req, res) => {
  try {
    const { phoneNumber, amount, accountReference, transactionDesc } = req.body;

    if (!phoneNumber || !amount || !accountReference) {
      return res.status(400).json({
        success: false,
        message: 'Missing required fields: phoneNumber, amount, or accountReference'
      });
    }

    const formattedPhone = formatPhoneNumber(phoneNumber);
    if (!formattedPhone.startsWith('254') || formattedPhone.length !== 12) {
      return res.status(400).json({
        success: false,
        message: `Invalid M-Pesa phone number format (${formattedPhone}). Must be 12 digits starting with 254.`
      });
    }

    if (!PASSKEY) {
      return res.status(400).json({
        success: false,
        message: 'PASSKEY is missing in backend .env file. Please configure PASSKEY from Safaricom Daraja Developer portal.'
      });
    }

    const token = await getAccessToken();
    const timestamp = getFormattedTimestamp();
    const password = Buffer.from(
      `${BUSINESS_SHORT_CODE}${PASSKEY}${timestamp}`
    ).toString('base64');

    const stkPushData = {
      BusinessShortCode: BUSINESS_SHORT_CODE,
      Password: password,
      Timestamp: timestamp,
      TransactionType: 'CustomerPayBillOnline',
      Amount: Math.round(amount),
      PartyA: formattedPhone,
      PartyB: BUSINESS_SHORT_CODE,
      PhoneNumber: formattedPhone,
      CallBackURL: CALLBACK_URL || 'https://icy-tables-notice.loca.lt/api/mpesa-callback',
      AccountReference: accountReference,
      TransactionDesc: transactionDesc || 'DeKUT Fees Payment'
    };

    console.log(`Initiating STK Push for ${formattedPhone}, Amount: ${amount}, Ref: ${accountReference}...`);

    const response = await axios.post(
      `${BASE_URL}/mpesa/stkpush/v1/processrequest`,
      stkPushData,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );

    console.log('STK Push Success Response:', response.data);

    res.json({
      success: true,
      message: 'STK Push prompt sent to user phone successfully',
      checkoutRequestID: response.data.CheckoutRequestID,
      merchantRequestID: response.data.MerchantRequestID,
      responseCode: response.data.ResponseCode,
      responseDescription: response.data.ResponseDescription
    });

  } catch (error) {
    const errorDetails = error.response?.data?.errorMessage
      || error.response?.data?.ResponseDescription
      || error.response?.data?.error_description
      || error.message;

    console.error('STK Push Initiation Error:', error.response?.data || error.message);

    res.status(500).json({
      success: false,
      message: `M-Pesa Initiation Error: ${errorDetails}`,
      error: error.response?.data || error.message
    });
  }
});

// Check Payment Status
app.post('/api/check-payment-status', async (req, res) => {
  try {
    const { checkoutRequestID } = req.body;

    if (!checkoutRequestID) {
      return res.status(400).json({
        success: false,
        message: 'CheckoutRequestID is required'
      });
    }

    // 1. Check if we received a webhook callback for this CheckoutRequestID
    if (completedPayments.has(checkoutRequestID)) {
      const saved = completedPayments.get(checkoutRequestID);
      console.log(`✅ Using Webhook Callback result for ${checkoutRequestID}:`, saved);
      return res.json({
        success: true,
        resultCode: saved.resultCode,
        resultDesc: saved.resultDesc,
        transactionId: saved.transactionId
      });
    }

    // 2. Rate-limit status queries per checkoutRequestID (cache for 5 seconds)
    const now = Date.now();
    const lastQuery = queryCache.get(checkoutRequestID);
    if (lastQuery && (now - lastQuery.time) < 5000) {
      return res.json(lastQuery.result);
    }

    // 3. Otherwise query Safaricom's stkpushquery API using cached token
    const token = await getAccessToken();
    const timestamp = getFormattedTimestamp();
    const password = Buffer.from(
      `${BUSINESS_SHORT_CODE}${PASSKEY}${timestamp}`
    ).toString('base64');

    const queryData = {
      BusinessShortCode: BUSINESS_SHORT_CODE,
      Password: password,
      Timestamp: timestamp,
      CheckoutRequestID: checkoutRequestID
    };

    try {
      const response = await axios.post(
        `${BASE_URL}/mpesa/stkpushquery/v1/query`,
        queryData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      console.log('STK Query Result:', response.data);
      const rCode = String(response.data.ResultCode);
      const rDesc = response.data.ResultDesc || 'Success';
      const receipt = response.data.MpesaReceiptNumber || null;

      const resultPayload = {
        success: true,
        resultCode: rCode,
        resultDesc: rDesc,
        transactionId: receipt
      };

      queryCache.set(checkoutRequestID, { time: now, result: resultPayload });

      if (rCode === '0') {
        completedPayments.set(checkoutRequestID, {
          resultCode: '0',
          resultDesc: rDesc,
          transactionId: receipt || checkoutRequestID
        });
      }

      return res.json(resultPayload);

    } catch (queryErr) {
      const errData = queryErr.response?.data;
      const desc = errData?.errorMessage || errData?.ResponseDescription || queryErr.message;
      const rCode = String(errData?.ResultCode || errData?.responseCode || '1');

      console.log(`STK Query In-Progress: Code=${rCode}, Desc=${desc}`);

      // Check if webhook callback arrived during query
      if (completedPayments.has(checkoutRequestID)) {
        const saved = completedPayments.get(checkoutRequestID);
        return res.json({
          success: true,
          resultCode: saved.resultCode,
          resultDesc: saved.resultDesc,
          transactionId: saved.transactionId
        });
      }

      const descLower = desc.toLowerCase();
      // If pending / processing / transaction does not exist yet (Safaricom sandbox delay)
      if (rCode === '1' || rCode === '500.001.1001' || descLower.includes('process') || descLower.includes('pending') || descLower.includes('exist')) {
        const pendingPayload = {
          success: true,
          resultCode: '1',
          resultDesc: 'The transaction is being processed. Please enter your M-Pesa PIN.'
        };
        queryCache.set(checkoutRequestID, { time: now, result: pendingPayload });
        return res.json(pendingPayload);
      }

      if (rCode === '1032' || descLower.includes('cancel')) {
        const cancelPayload = {
          success: true,
          resultCode: '1032',
          resultDesc: 'Payment cancelled by user.'
        };
        queryCache.set(checkoutRequestID, { time: now, result: cancelPayload });
        return res.json(cancelPayload);
      }

      return res.json({
        success: true,
        resultCode: rCode,
        resultDesc: desc
      });
    }

  } catch (error) {
    console.error('Status Query Outer Error:', error.message);
    res.status(500).json({
      success: false,
      message: `Failed to check payment status: ${error.message}`
    });
  }
});

// M-Pesa Callback Webhook
app.post('/api/mpesa-callback', async (req, res) => {
  console.log('M-Pesa Callback Webhook Received:');
  console.log(JSON.stringify(req.body, null, 2));

  try {
    const { Body } = req.body;

    if (Body && Body.stkCallback) {
      const { ResultCode, ResultDesc, CallbackMetadata, CheckoutRequestID } = Body.stkCallback;

      let transactionId = null;
      if (ResultCode === 0 && CallbackMetadata && CallbackMetadata.Item) {
        const receiptItem = CallbackMetadata.Item.find(item => item.Name === 'MpesaReceiptNumber');
        if (receiptItem) {
          transactionId = String(receiptItem.Value);
        }
      }

      if (CheckoutRequestID) {
        completedPayments.set(CheckoutRequestID, {
          resultCode: String(ResultCode),
          resultDesc: ResultDesc || (ResultCode === 0 ? 'Success' : 'Failed'),
          transactionId: transactionId || CheckoutRequestID
        });
        console.log(`✅ Webhook status stored for ${CheckoutRequestID}: Code=${ResultCode}, Ref=${transactionId}`);
      }
    }

    res.json({ ResultCode: 0, ResultDesc: 'Success' });
  } catch (error) {
    console.error('Callback Error:', error.message);
    res.status(500).json({ ResultCode: 1, ResultDesc: 'Error processing callback' });
  }
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    message: 'DeKUT M-Pesa Backend is running',
    environment: BASE_URL.includes('sandbox') ? 'sandbox' : 'production'
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`DeKUT M-Pesa Server running on port ${PORT}`);
  console.log(`Environment: ${BASE_URL.includes('sandbox') ? 'Sandbox' : 'Production'}`);
  console.log(`Callback URL: ${CALLBACK_URL}`);
});

module.exports = app;
