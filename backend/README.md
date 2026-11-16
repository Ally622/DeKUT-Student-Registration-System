# M-Pesa Backend Server for DeKUT Student Registration

This backend server handles M-Pesa Daraja API integration for the DeKUT Student Registration Android application.

## Requirements

- Node.js 14 or higher
- npm or yarn
- Safaricom Daraja API credentials

## Installation

```bash
cd backend
npm install
```

## Configuration

Create a `.env` file in the backend directory:

```env
# Daraja API Credentials (Sandbox)
CONSUMER_KEY=your_consumer_key_here
CONSUMER_SECRET=your_consumer_secret_here
BUSINESS_SHORT_CODE=174379
PASSKEY=your_passkey_here

# Server Configuration
PORT=3000
CALLBACK_URL=https://your-domain.com/api/mpesa-callback

# Firebase Admin SDK
FIREBASE_DATABASE_URL=https://your-firebase-project.firebaseio.com
```

## Getting Daraja Credentials

1. Go to https://developer.safaricom.co.ke/
2. Register/Login
3. Create a new app
4. Copy Consumer Key and Consumer Secret
5. For sandbox, use test credentials
6. For production, complete KYC and get production credentials

## Running the Server

### Development
```bash
npm run dev
```

### Production
```bash
npm start
```

## API Endpoints

### 1. Initiate Payment (STK Push)

**Endpoint:** `POST /api/initiate-payment`

**Request Body:**
```json
{
  "phoneNumber": "254712345678",
  "amount": 50000,
  "accountReference": "CS001/2024",
  "transactionDesc": "DeKUT Fees Payment"
}
```

**Response:**
```json
{
  "success": true,
  "message": "STK Push sent successfully",
  "checkoutRequestID": "ws_CO_DMZ_123456789_12345678",
  "merchantRequestID": "29115-34620561-1"
}
```

### 2. Check Payment Status

**Endpoint:** `POST /api/check-payment-status`

**Request Body:**
```json
{
  "checkoutRequestID": "ws_CO_DMZ_123456789_12345678"
}
```

**Response:**
```json
{
  "success": true,
  "resultCode": "0",
  "resultDesc": "The service request is processed successfully.",
  "transactionId": "QGP7XXXXXX"
}
```

### 3. M-Pesa Callback

**Endpoint:** `POST /api/mpesa-callback`

This endpoint receives payment confirmation from Safaricom.

## Deployment

### Option 1: Heroku

```bash
heroku create dekut-registration-api
heroku config:set CONSUMER_KEY=your_key
heroku config:set CONSUMER_SECRET=your_secret
git push heroku main
```

### Option 2: Vercel/Netlify Functions

Deploy as serverless functions.

### Option 3: VPS (DigitalOcean, Linode, etc.)

1. Set up Node.js on VPS
2. Install PM2: `npm install -g pm2`
3. Run: `pm2 start server.js`
4. Set up Nginx reverse proxy
5. Configure SSL with Let's Encrypt

## Testing

### Test Credentials (Sandbox)

- **Consumer Key:** Get from Daraja portal
- **Consumer Secret:** Get from Daraja portal
- **Business Short Code:** 174379
- **Test Phone:** 254708374149
- **Passkey:** Get from Daraja portal

## Security

- Never commit `.env` file
- Use environment variables in production
- Implement rate limiting
- Add authentication for API endpoints
- Use HTTPS only
- Validate all inputs
- Log all transactions

## Troubleshooting

### Error: "Invalid Access Token"
- Check if Consumer Key and Secret are correct
- Ensure you're using the right environment (sandbox/production)

### Error: "Request timeout"
- Check internet connection
- Verify Safaricom API is accessible
- Increase timeout settings

### Error: "Invalid phone number"
- Use format 254XXXXXXXXX
- Ensure phone number is registered for M-Pesa

## Support

For Daraja API support:
- Email: apisupport@safaricom.co.ke
- Portal: https://developer.safaricom.co.ke/support
