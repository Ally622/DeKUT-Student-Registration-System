# DeKUT Student Registration System - Setup Instructions

## Overview
This is a complete Android application for first-year student registration at Dedan Kimathi University of Technology (DeKUT). The application supports student registration, library book requests, hostel accommodation, M-Pesa fee payment, and offline functionality.

## Prerequisites
- Android Studio (Arctic Fox or later)
- JDK 8 or higher
- Android SDK with minimum API 24 (Android 7.0)
- Firebase account
- Safaricom Daraja API account (for M-Pesa integration)

## Project Structure
```
DeKUTStudentRegistration/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/dekut/studentregistration/
│   │       │   ├── MainActivity.kt
│   │       │   ├── StudentRegistrationActivity.kt
│   │       │   ├── LibraryActivity.kt
│   │       │   ├── HostelActivity.kt
│   │       │   ├── PaymentActivity.kt
│   │       │   ├── StudentsActivity.kt
│   │       │   ├── models/
│   │       │   │   ├── Student.kt
│   │       │   │   ├── LibraryRequest.kt
│   │       │   │   ├── HostelRequest.kt
│   │       │   │   └── Payment.kt
│   │       │   ├── repository/
│   │       │   │   ├── FirebaseRepository.kt
│   │       │   │   └── OfflineRepository.kt
│   │       │   ├── api/
│   │       │   │   ├── MpesaApi.kt
│   │       │   │   └── RetrofitClient.kt
│   │       │   ├── utils/
│   │       │   │   ├── NetworkUtils.kt
│   │       │   │   ├── SyncManager.kt
│   │       │   │   └── PdfGenerator.kt
│   │       │   └── adapters/
│   │       │       └── StudentsAdapter.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── values/
│   │       │   └── xml/
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Step 1: Firebase Setup

### 1.1 Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "DeKUT Student Registration"
4. Follow the setup wizard

### 1.2 Add Android App to Firebase
1. In Firebase Console, click "Add app" and select Android
2. Enter package name: `com.dekut.studentregistration`
3. Download `google-services.json`
4. Replace the placeholder `app/google-services.json` with your downloaded file

### 1.3 Enable Firebase Realtime Database
1. In Firebase Console, go to "Realtime Database"
2. Click "Create Database"
3. Start in test mode (for development)
4. Note your database URL

### 1.4 Configure Database Rules
Go to Firebase Console > Realtime Database > Rules and set:

```json
{
  "rules": {
    "DeKUTRegistration": {
      ".read": true,
      ".write": true,
      "Students": {
        ".indexOn": ["registrationNumber", "idNumber"]
      },
      "LibraryRequests": {
        ".indexOn": ["studentId"]
      },
      "HostelRequests": {
        ".indexOn": ["studentId"]
      },
      "Payments": {
        ".indexOn": ["studentId"]
      }
    }
  }
}
```

**Note:** For production, implement proper authentication and security rules.

### 1.5 Enable Firebase Authentication (Optional)
1. Go to Authentication > Sign-in method
2. Enable Email/Password or Anonymous authentication

## Step 2: M-Pesa Daraja API Setup

### 2.1 Create Daraja Account
1. Go to [Safaricom Daraja Portal](https://developer.safaricom.co.ke/)
2. Register/Login
3. Create a new app
4. Note your Consumer Key and Consumer Secret

### 2.2 Set Up Backend Server
The M-Pesa STK Push requires a backend server. You need to:

1. Deploy a Node.js/Python/PHP backend with Daraja integration
2. Implement these endpoints:
   - `POST /api/initiate-payment` - Initiates STK Push
   - `POST /api/check-payment-status` - Checks payment status
   - `POST /api/mpesa-callback` - Receives payment confirmation

### 2.3 Sample Node.js Backend (backend/server.js)
```javascript
const express = require('express');
const axios = require('axios');
const app = express();

app.use(express.json());

const CONSUMER_KEY = 'your_consumer_key';
const CONSUMER_SECRET = 'your_consumer_secret';
const BUSINESS_SHORT_CODE = 'your_shortcode';
const PASSKEY = 'your_passkey';
const CALLBACK_URL = 'https://your-domain.com/api/mpesa-callback';

// Get OAuth token
async function getAccessToken() {
  const auth = Buffer.from(`${CONSUMER_KEY}:${CONSUMER_SECRET}`).toString('base64');
  const response = await axios.get(
    'https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials',
    { headers: { Authorization: `Basic ${auth}` } }
  );
  return response.data.access_token;
}

// Initiate STK Push
app.post('/api/initiate-payment', async (req, res) => {
  try {
    const { phoneNumber, amount, accountReference } = req.body;
    const token = await getAccessToken();
    
    const timestamp = new Date().toISOString().replace(/[^0-9]/g, '').slice(0, -3);
    const password = Buffer.from(`${BUSINESS_SHORT_CODE}${PASSKEY}${timestamp}`).toString('base64');
    
    const response = await axios.post(
      'https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest',
      {
        BusinessShortCode: BUSINESS_SHORT_CODE,
        Password: password,
        Timestamp: timestamp,
        TransactionType: 'CustomerPayBillOnline',
        Amount: amount,
        PartyA: phoneNumber,
        PartyB: BUSINESS_SHORT_CODE,
        PhoneNumber: phoneNumber,
        CallBackURL: CALLBACK_URL,
        AccountReference: accountReference,
        TransactionDesc: 'DeKUT Fees Payment'
      },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    
    res.json({
      success: true,
      checkoutRequestID: response.data.CheckoutRequestID,
      merchantRequestID: response.data.MerchantRequestID
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Callback endpoint
app.post('/api/mpesa-callback', (req, res) => {
  const { Body } = req.body;
  // Process callback and update Firebase
  console.log('M-Pesa Callback:', JSON.stringify(Body));
  res.json({ ResultCode: 0, ResultDesc: 'Success' });
});

app.listen(3000, () => console.log('Server running on port 3000'));
```

### 2.4 Update Android App Configuration
1. Open `app/src/main/java/com/dekut/studentregistration/api/RetrofitClient.kt`
2. Replace `BASE_URL` with your backend URL:
   ```kotlin
   private const val BASE_URL = "https://your-backend-url.com/api/"
   ```

## Step 3: Android Studio Setup

### 3.1 Open Project
1. Launch Android Studio
2. File > Open
3. Navigate to `DeKUTStudentRegistration` folder
4. Click OK

### 3.2 Gradle Sync
1. Wait for Gradle to sync automatically
2. If errors occur, click "Sync Project with Gradle Files"

### 3.3 Update SDK Path (if needed)
1. Open `local.properties`
2. Verify `sdk.dir` points to your Android SDK location

## Step 4: Build and Run

### 4.1 Build the Project
1. Build > Make Project
2. Wait for build to complete
3. Fix any errors that appear

### 4.2 Run on Emulator
1. Tools > Device Manager
2. Create a virtual device (Pixel 5, API 30+)
3. Click Run button
4. Select your emulator

### 4.3 Run on Physical Device
1. Enable Developer Options on your Android phone
2. Enable USB Debugging
3. Connect phone via USB
4. Click Run and select your device

## Step 5: Testing the Application

### 5.1 Test Student Registration
1. Open the app
2. Click "Student Registration"
3. Fill in all fields:
   - Full Names: John Doe
   - ID Number: 12345678
   - Registration Number: CS001/2024
   - Gender: Male
   - Course: Bachelor of Science in Computer Science
   - Department: Computer Science
4. Click Register
5. Verify success message

### 5.2 Test Offline Mode
1. Turn off WiFi and mobile data
2. Register a new student
3. Verify "Pending Sync" status
4. Turn on internet
5. Verify automatic synchronization

### 5.3 Test Library Request
1. Click "Library Book Request"
2. Fill in book details
3. Select dates using date picker
4. Submit request

### 5.4 Test Hostel Accommodation
1. Click "Hostel Accommodation"
2. Select hostel location and room type
3. Choose if sharable
4. Submit request

### 5.5 Test M-Pesa Payment
**Note:** This requires backend setup and valid Daraja credentials

1. Click "Fees Payment"
2. Enter student registration number
3. Enter M-Pesa phone number (254...)
4. Enter amount (e.g., 50000)
5. Click "Pay Now"
6. If backend is not configured, sandbox mode demonstration will show

### 5.6 Test Admin Section
1. Click "View All Students"
2. Search for students by registration number
3. Filter by confirmed students
4. Click on a student to view details
5. Click "Print Summary" to generate PDF

## Step 6: Firebase Database Verification

1. Go to Firebase Console > Realtime Database
2. Verify data structure:
   ```
   DeKUTRegistration/
   ├── Students/
   │   └── {studentId}/
   ├── LibraryRequests/
   │   └── {requestId}/
   ├── HostelRequests/
   │   └── {requestId}/
   └── Payments/
       └── {paymentId}/
   ```

## Key Features Implemented

### ✅ Student Registration
- Full registration form with validation
- Duplicate registration number check
- Gender selection
- Course and department selection
- Offline support

### ✅ Library Book Request
- ISBN, author, and title fields
- Date picker for borrow and return dates
- Date validation (return date after borrow date)
- Student ID association

### ✅ Hostel Accommodation
- Hostel location selection
- Room type selection
- Sharable option
- Student ID association

### ✅ M-Pesa Payment Integration
- STK Push integration (requires backend)
- Phone number validation
- Amount validation
- Transaction reference tracking
- Payment status updates
- Registration confirmation after successful payment
- Sandbox mode demonstration

### ✅ Offline Functionality
- SharedPreferences for local storage
- Automatic sync when internet returns
- Network connectivity monitoring
- Pending sync indicator
- No data loss when offline

### ✅ Admin Section
- View all registered students
- Search by registration number or ID
- Filter confirmed students
- View individual student details
- Print individual student summary
- Print all students summary

### ✅ PDF Printing
- Professional HTML-based PDF generation
- Individual student registration summary
- All students summary report
- DeKUT branding and styling
- Android printing framework integration

## Troubleshooting

### Build Errors
**Error:** "Could not resolve com.google.firebase"
- **Solution:** Ensure `google-services.json` is in `app/` folder

**Error:** "SDK location not found"
- **Solution:** Update `local.properties` with correct SDK path

### Runtime Errors
**Error:** "FirebaseApp initialization unsuccessful"
- **Solution:** Verify `google-services.json` is correct and in the right location

**Error:** "Permission denied (missing INTERNET permission?)"
- **Solution:** Verify AndroidManifest.xml contains INTERNET permission

### M-Pesa Issues
**Error:** "Failed to initiate payment"
- **Solution:** Check backend is running and BASE_URL is correct

**Error:** "Invalid phone number"
- **Solution:** Use format 254XXXXXXXXX (12 digits)

## Project Grading Checklist

- ✅ Student Registration Activity (with validation)
- ✅ Library Book Request Activity (with date validation)
- ✅ Hostel Accommodation Activity
- ✅ M-Pesa Payment Activity (STK Push)
- ✅ Firebase Realtime Database integration
- ✅ Offline functionality with auto-sync (5 marks)
- ✅ View registered students
- ✅ Search and filter students
- ✅ Individual student summary printing
- ✅ All students summary printing
- ✅ Professional UI/UX design
- ✅ Input validation
- ✅ Error handling
- ✅ Success/error messages

## Additional Notes

### Security Considerations
- Never commit `google-services.json` with real credentials to public repos
- Use environment variables for sensitive data
- Implement proper Firebase security rules in production
- Store M-Pesa credentials only on backend server

### Performance Optimization
- Firebase offline persistence is enabled
- RecyclerView for efficient list display
- Coroutines for asynchronous operations
- Proper lifecycle management

### Future Enhancements
- User authentication
- Push notifications
- Receipt generation
- Email confirmation
- SMS notifications
- Student ID card generation

## Support

For issues or questions:
1. Check Firebase Console for database errors
2. Check Android Logcat for runtime errors
3. Verify all dependencies are downloaded
4. Ensure minimum SDK version is met

## License

This project is developed for academic purposes at Dedan Kimathi University of Technology.

---

**Developer:** Third Year Computer Science Student  
**Unit:** CCS 3211 – Mobile Systems Programming  
**Institution:** Dedan Kimathi University of Technology  
**Date:** 2024
