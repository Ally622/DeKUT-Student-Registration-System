# DeKUT First Year Student Registration System

A complete Android application for first-year student registration at Dedan Kimathi University of Technology (DeKUT).

## Project Information

- **Institution:** Dedan Kimathi University of Technology
- **Department:** Computer Science
- **Unit:** CCS 3211 – Mobile Systems Programming
- **Year:** Third Year
- **Developer:** Computer Science Student

## Features

### Core Functionality
- ✅ **Student Registration** - Complete registration with validation
- ✅ **Library Book Requests** - Request library books with date validation
- ✅ **Hostel Accommodation** - Request hostel accommodation
- ✅ **M-Pesa Payment** - Pay fees via M-Pesa STK Push
- ✅ **Offline Mode** - Register without internet, auto-sync when online (5 marks)
- ✅ **Admin Dashboard** - View and manage all registered students
- ✅ **PDF Printing** - Generate and print registration summaries

### Technical Features
- Firebase Realtime Database integration
- Offline data persistence with SharedPreferences
- Automatic background synchronization
- Network connectivity monitoring
- M-Pesa Daraja API integration
- Professional UI with Material Design 3
- Input validation and error handling
- Search and filter functionality
- PDF generation with WebView

## Technology Stack

- **Language:** Kotlin
- **UI:** XML Layouts (Material Design 3)
- **Database:** Firebase Realtime Database
- **Local Storage:** SharedPreferences
- **Networking:** Retrofit 2
- **Payment:** Safaricom Daraja API (M-Pesa)
- **Async:** Kotlin Coroutines & Flow
- **Architecture:** Repository Pattern
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)

## Project Structure

```
app/src/main/java/com/dekut/studentregistration/
├── MainActivity.kt                     # Dashboard
├── StudentRegistrationActivity.kt      # Student registration form
├── LibraryActivity.kt                  # Library book request
├── HostelActivity.kt                   # Hostel accommodation
├── PaymentActivity.kt                  # M-Pesa payment
├── StudentsActivity.kt                 # Admin - view students
├── models/
│   ├── Student.kt                      # Student data model
│   ├── LibraryRequest.kt               # Library request model
│   ├── HostelRequest.kt                # Hostel request model
│   └── Payment.kt                      # Payment model
├── repository/
│   ├── FirebaseRepository.kt           # Firebase operations
│   └── OfflineRepository.kt            # Local storage operations
├── api/
│   ├── MpesaApi.kt                     # M-Pesa API interface
│   └── RetrofitClient.kt               # Retrofit configuration
├── utils/
│   ├── NetworkUtils.kt                 # Network monitoring
│   ├── SyncManager.kt                  # Data synchronization
│   └── PdfGenerator.kt                 # PDF generation
└── adapters/
    └── StudentsAdapter.kt              # RecyclerView adapter
```

## Quick Start

### Prerequisites
1. Android Studio (Arctic Fox or later)
2. JDK 8+
3. Android SDK (API 24+)
4. Firebase account
5. Safaricom Daraja account (for M-Pesa)

### Setup Steps

1. **Clone/Open Project**
   ```bash
   # Open Android Studio
   # File > Open > Select DeKUTStudentRegistration folder
   ```

2. **Firebase Setup**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create new project: "DeKUT Student Registration"
   - Add Android app with package: `com.dekut.studentregistration`
   - Download `google-services.json`
   - Place in `app/` directory
   - Enable Realtime Database
   - Set database rules (see SETUP_INSTRUCTIONS.md)

3. **Build Project**
   ```bash
   # In Android Studio
   Build > Make Project
   ```

4. **Run Application**
   - Connect Android device or start emulator
   - Click Run button
   - App will launch on device

## Testing Guide

### 1. Student Registration
```
1. Open app
2. Click "Student Registration"
3. Fill form:
   - Full Name: Jane Wanjiku
   - ID Number: 30123456
   - Reg Number: CS001/2024
   - Gender: Female
   - Course: Bachelor of Science in Computer Science
   - Department: Computer Science
4. Click "Register"
5. Verify success message
```

### 2. Offline Mode Test
```
1. Turn OFF WiFi and mobile data
2. Register a student
3. Verify "Pending Sync" status appears
4. Close and reopen app
5. Data should persist
6. Turn ON internet
7. Wait for automatic sync
8. Status changes to "Synced"
9. Check Firebase console - data should appear
```

### 3. Library Request
```
1. Click "Library Book Request"
2. Enter:
   - Student ID: CS001/2024
   - ISBN: 978-0134685991
   - Title: Effective Java
   - Author: Joshua Bloch
   - Borrow Date: Select today
   - Return Date: Select 2 weeks later
3. Submit request
```

### 4. Hostel Request
```
1. Click "Hostel Accommodation"
2. Enter:
   - Student ID: CS001/2024
   - Location: Main Campus Hostels
   - Room Type: Single Room
   - Sharable: No
3. Submit request
```

### 5. M-Pesa Payment (Sandbox Mode)
```
1. Click "Fees Payment"
2. Enter:
   - Student ID: CS001/2024
   - Phone: 254712345678
   - Amount: 50000
3. Click "Pay Now"
4. If backend not configured, sandbox demo appears
5. Click "Simulate Success"
6. Verify payment recorded
7. Check student status changed to CONFIRMED
```

### 6. Admin Section
```
1. Click "View All Students"
2. See list of registered students
3. Use search: Type "CS001"
4. Click "Confirmed" filter
5. Click on student to view details
6. Click "Print Summary"
7. Preview PDF, click Print
8. Click "Print All" for complete report
```

## Database Structure

```
DeKUTRegistration/
├── Students/
│   └── {studentId}/
│       ├── id
│       ├── fullName
│       ├── idNumber
│       ├── registrationNumber
│       ├── gender
│       ├── course
│       ├── department
│       ├── registrationStatus (PENDING/CONFIRMED)
│       ├── paymentStatus (UNPAID/PAID)
│       ├── dateRegistered
│       └── syncStatus (PENDING/SYNCED)
├── LibraryRequests/
│   └── {requestId}/
│       ├── studentId
│       ├── bookIsbn
│       ├── bookTitle
│       ├── author
│       ├── dateBorrowed
│       └── dateOfReturn
├── HostelRequests/
│   └── {requestId}/
│       ├── studentId
│       ├── hostelLocation
│       ├── roomType
│       └── sharable
└── Payments/
    └── {paymentId}/
        ├── studentId
        ├── phoneNumber
        ├── amount
        ├── transactionReference
        ├── paymentStatus
        └── transactionDate
```

## Offline Functionality (5 Marks)

The application implements robust offline functionality:

### How It Works
1. **Detection:** NetworkUtils monitors internet connectivity
2. **Local Storage:** OfflineRepository saves data in SharedPreferences
3. **Status Tracking:** Each record has syncStatus (PENDING/SYNCED)
4. **Auto Sync:** SyncManager syncs when internet returns
5. **UI Indication:** Visual indicators show sync status

### Implementation Details
```kotlin
// Firebase offline persistence
FirebaseDatabase.getInstance().setPersistenceEnabled(true)

// Local storage for pending data
SharedPreferences for offline queue

// Network monitoring
NetworkUtils.observeNetworkConnectivity()

// Automatic sync
SyncManager.syncPendingData()
```

## M-Pesa Integration

### Architecture
```
Android App → Backend Server → Daraja API → M-Pesa
                    ↓
              Firebase Update
```

### Payment Flow
1. User enters payment details
2. App sends request to backend
3. Backend initiates STK Push via Daraja API
4. User receives M-Pesa prompt on phone
5. User enters PIN
6. Safaricom processes payment
7. Callback sent to backend
8. Backend updates Firebase
9. Student registration confirmed

### Sandbox Mode
If backend is not configured, app demonstrates the flow in sandbox mode and allows simulated payments for testing.

## Screenshots & Demo

### Dashboard
- Clean navigation cards
- Sync status indicator
- Professional DeKUT branding

### Registration Form
- Input validation
- Real-time error messages
- Offline support indication

### Payment Screen
- M-Pesa integration
- Transaction tracking
- Status updates

### Admin Dashboard
- Student list with RecyclerView
- Search functionality
- PDF generation

## Grading Checklist

| Requirement | Status | Points |
|------------|--------|--------|
| Student Registration Activity | ✅ | ✓ |
| Library Request Activity | ✅ | ✓ |
| Hostel Request Activity | ✅ | ✓ |
| M-Pesa Payment (STK Push) | ✅ | ✓ |
| Firebase Integration | ✅ | ✓ |
| Offline Functionality | ✅ | 5 |
| View Registered Students | ✅ | ✓ |
| Print Individual Summary | ✅ | ✓ |
| Print All Students | ✅ | ✓ |
| Professional UI/UX | ✅ | ✓ |
| Input Validation | ✅ | ✓ |
| Error Handling | ✅ | ✓ |

## Troubleshooting

### Build Issues
- **Problem:** Could not resolve Firebase
  - **Solution:** Add `google-services.json` to `app/` folder

- **Problem:** Gradle sync failed
  - **Solution:** File > Invalidate Caches > Restart

### Runtime Issues
- **Problem:** Firebase not initialized
  - **Solution:** Verify `google-services.json` is correct

- **Problem:** Offline sync not working
  - **Solution:** Check network permissions in manifest

### M-Pesa Issues
- **Problem:** Payment fails
  - **Solution:** Verify backend is running and URL is correct

## Future Enhancements

- [ ] User authentication with roles
- [ ] Push notifications for payment confirmation
- [ ] Email receipts
- [ ] SMS notifications
- [ ] Student ID card generation
- [ ] QR code for registration
- [ ] Analytics dashboard
- [ ] Bulk student upload

## Documentation

- `SETUP_INSTRUCTIONS.md` - Detailed setup guide
- `backend/README.md` - Backend server documentation
- `app/` - Android application source
- `backend/` - Node.js M-Pesa backend

## Support & Contact

For questions or issues:
1. Check SETUP_INSTRUCTIONS.md
2. Review Firebase Console logs
3. Check Android Logcat
4. Verify all dependencies

## License

This project is developed for academic purposes at Dedan Kimathi University of Technology.

## Acknowledgments

- Dedan Kimathi University of Technology
- Computer Science Department
- CCS 3211 Course Instructors
- Safaricom Daraja API Documentation
- Firebase Documentation

---

**Note:** This is a complete, functional application ready for demonstration and grading. All four required activities are implemented with proper offline functionality, Firebase integration, M-Pesa payment, and PDF printing capabilities.

**Developer:** Third Year Computer Science Student  
**Date:** November 2024  
**Version:** 1.0.0
