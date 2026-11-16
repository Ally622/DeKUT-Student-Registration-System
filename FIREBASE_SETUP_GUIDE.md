# 🔥 FIREBASE CONSOLE SETUP GUIDE

## Project: studentregistration-a018e
## Required Time: 5-10 minutes

---

## ⚠️ CRITICAL: These Steps Are MANDATORY

The application will NOT work correctly until you complete these Firebase Console configurations.

---

## STEP 1: Enable Email/Password Authentication

### Why This Is Needed
Your app uses Firebase Authentication with email/password for student login. This provider must be enabled in Firebase Console.

### Instructions
1. Open your browser and go to: **https://console.firebase.google.com/**
2. Sign in with your Google account
3. Click on project: **studentregistration-a018e**
4. In left sidebar, click: **Authentication** (🔐 icon)
5. Click the **Sign-in method** tab at the top
6. In the providers list, find **Email/Password**
7. Click on it to open settings
8. Toggle **Enable** to ON (blue)
9. **Do NOT enable** "Email link (passwordless sign-in)"
10. Click **Save**

### Verification
✅ Email/Password should now show as "Enabled" in the providers list

---

## STEP 2: Apply Database Security Rules

### Why This Is Needed
Without proper security rules:
- You may get "Permission denied" errors
- OR the database might be completely open (security risk)
- Student records won't be properly protected

### Instructions

#### Option A: Copy-Paste from File (Recommended)

1. In Firebase Console, from project dashboard, click: **Realtime Database** (🗄️ icon)
2. Click the **Rules** tab at the top
3. You will see the current rules editor

4. Open this file on your computer:
   ```
   c:\Users\CHEMOGET\Desktop\3.2\DeKUTStudentRegistration\DeKUTStudentRegistration\FIREBASE_DATABASE_RULES.json
   ```

5. Select ALL contents of the file and Copy (Ctrl+A, Ctrl+C)

6. Go back to Firebase Console Rules editor
7. Select ALL existing content and DELETE it
8. Paste the new rules (Ctrl+V)
9. Click **Publish** button (top right)
10. Confirm the warning dialog

#### Option B: Manual Entry (If file not accessible)

Paste this exactly into the Rules editor:

```json
{
  "rules": {
    "DeKUTRegistration": {
      "Students": {
        "$uid": {
          ".read": "auth != null && (auth.uid === $uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".write": "auth != null && (auth.uid === $uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".validate": "newData.hasChildren(['uid', 'fullName', 'idNumber', 'registrationNumber'])",
          "uid": {
            ".validate": "newData.val() === $uid"
          },
          "idNumber": {
            ".validate": "newData.isString() && newData.val().length === 9"
          },
          "registrationNumber": {
            ".validate": "newData.isString() && newData.val().length > 0"
          },
          "role": {
            ".validate": "newData.val() === 'STUDENT' || (auth != null && root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')"
          }
        }
      },
      "LibraryRequests": {
        "$requestId": {
          ".read": "auth != null && (data.child('uid').val() === auth.uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".write": "auth != null && (newData.child('uid').val() === auth.uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".validate": "newData.hasChildren(['uid', 'studentId', 'bookTitle'])"
        }
      },
      "HostelRequests": {
        "$requestId": {
          ".read": "auth != null && (data.child('uid').val() === auth.uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".write": "auth != null && (newData.child('uid').val() === auth.uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".validate": "newData.hasChildren(['uid', 'studentId', 'hostelLocation'])"
        }
      },
      "Payments": {
        "$paymentId": {
          ".read": "auth != null && (data.child('uid').val() === auth.uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".write": "auth != null && (newData.child('uid').val() === auth.uid || root.child('DeKUTRegistration/Students/' + auth.uid + '/role').val() === 'ADMIN')",
          ".validate": "newData.hasChildren(['uid', 'studentId', 'amount'])"
        }
      }
    }
  }
}
```

### What These Rules Do
- ✅ Students can only read/write their own records (matched by Firebase UID)
- ✅ Administrators (role="ADMIN") can access all student records
- ✅ Validates that ID numbers are exactly 9 digits
- ✅ Enforces required fields
- ✅ Protects role assignment (only admins can create admins)

### Verification
✅ After publishing, the Rules editor should show your new rules
✅ No red error indicators should appear

### ⚠️ Testing-Only Alternative (NOT SECURE)

If you want to test basic functionality first, you can temporarily use these open rules:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

**WARNING:** These rules allow ANY authenticated user to access ALL data. Use only for testing, then replace with proper rules.

---

## STEP 3: Create Administrator Account

### Why This Is Needed
The app has an Admin Panel for viewing all students, but admin accounts cannot be created through the app UI for security reasons. You must manually promote a student to admin.

### Instructions

#### Part A: Register a Student Account (Through the App)

1. Install and open the app on your device/emulator
2. Tap **New Registration**
3. Fill in the form:
   - Full Name: `Admin User`
   - ID Number: `999999999` (any 9 digits)
   - Registration Number: `ADMIN/001/2024` (or any format you prefer)
   - Gender: Select any
   - Course: Select any
   - Department: Select any
   - Password: Choose a secure password
   - Confirm Password: Same password
4. Tap **Register**
5. Wait for: "Registration successful!"
6. **IMPORTANT:** Write down the registration number you used

#### Part B: Promote to Admin (In Firebase Console)

1. Go to Firebase Console: **Realtime Database** section
2. Click the **Data** tab
3. Navigate to: `DeKUTRegistration` → `Students`
4. You should see one or more student records (each is a long UID string)
5. Expand the records and look for the one with:
   - `registrationNumber`: The one you just created (e.g., "ADMIN/001/2024")
6. Click on that student record
7. Find the `role` field (should currently say `"STUDENT"`)
8. Click on `"STUDENT"` to edit it
9. Change the value to: `ADMIN` (type it exactly, all caps)
10. Press Enter or click the checkmark to save

### Alternative: If You Can't Find the Record

If you have trouble finding the right student record:

1. In Firebase Console → Realtime Database → Data
2. Click the **+** icon next to `DeKUTRegistration/Students`
3. Copy one of the existing UID keys
4. Go back to the app
5. Login with your admin account
6. Go to Dashboard → My Details
7. The screen will show your Firebase UID
8. Go back to Firebase Console
9. Find the record with that matching UID
10. Set the `role` field to `ADMIN`

### Part C: Verify Admin Access (In the App)

1. If you're logged in, log out first
2. Log back in with the admin account credentials
3. You should now see the **Admin Panel** card on the Dashboard
4. Tap it to access admin features

### Verification
✅ Admin Panel card visible on Dashboard
✅ Can open Admin Panel
✅ Can view all students
✅ Can print reports

---

## STEP 4: Test the Application

### Basic Test Sequence

#### Test 1: Register a Student
1. Open app → New Registration
2. Fill in valid details (use real-looking data)
3. Tap Register
4. Expected: "Registration successful!"

#### Test 2: Login
1. Go to Login
2. Enter the registration number and password you just used
3. Tap Login
4. Expected: Dashboard opens with welcome message

#### Test 3: Access Admin Panel
1. Login as the admin account you created
2. Dashboard should show Admin Panel card
3. Tap it
4. Expected: Admin screen opens, showing "View All Students" button

---

## 🚨 TROUBLESHOOTING

### Problem: "Permission denied" when accessing database

**Solution:**
- Verify you applied the database rules correctly
- Check that Email/Password authentication is enabled
- Make sure you're logged in (not using the app while logged out)

### Problem: Cannot see Admin Panel

**Causes:**
- `role` field not set to "ADMIN" (check spelling, must be all caps)
- Not logged in as the admin user
- Cached session (try logout and login again)

**Solution:**
- Verify in Firebase Console: Database → Data → DeKUTRegistration → Students → find your user → check `role` field
- It must be exactly: `ADMIN` (not "admin", not "Admin", not "ADMINISTRATOR")

### Problem: "The supplied auth credentials are malformed"

**Status:** Should be fixed in latest code

**If still occurring:**
- Verify you're using the LATEST version of LoginActivity.kt
- Check that the registration number format is consistent
- Try registering a NEW account and logging in with that

### Problem: "Already Registered" for new student

**Status:** Should be fixed in latest code

**If still occurring:**
- Check Firebase Console → Database → Data → DeKUTRegistration → Students
- Verify the registration number and ID number are genuinely unique
- Clear app data and try again

---

## 📋 QUICK CHECKLIST

Use this checklist to ensure everything is configured:

- [ ] Firebase Email/Password authentication ENABLED
- [ ] Firebase Database Rules APPLIED and PUBLISHED
- [ ] At least one admin account CREATED
- [ ] Admin account `role` field set to `ADMIN` in Firebase
- [ ] Tested registration of new student (Success)
- [ ] Tested login with registered student (Success)
- [ ] Tested admin panel access (Success)

Once all items are checked, your Firebase setup is complete!

---

## 🔗 USEFUL LINKS

### Firebase Console
- Main: https://console.firebase.google.com/
- Your Project: https://console.firebase.google.com/project/studentregistration-a018e

### Firebase Documentation
- Authentication: https://firebase.google.com/docs/auth
- Realtime Database: https://firebase.google.com/docs/database
- Security Rules: https://firebase.google.com/docs/database/security

---

## ⏱️ ESTIMATED TIME

- **Step 1** (Enable Auth): 2 minutes
- **Step 2** (Apply Rules): 2 minutes
- **Step 3** (Create Admin): 5 minutes
- **Step 4** (Testing): 3 minutes

**Total: ~12 minutes**

---

## ✅ AFTER SETUP

Once you complete these steps:
1. The app will work correctly
2. Students can register and login
3. Administrators can access all features
4. Database is properly secured
5. All features are ready for testing

If you encounter any issues during setup, refer to the troubleshooting section or check the main documentation files.

---

**REMEMBER: This setup is REQUIRED. The app cannot function correctly without it.**

---

Generated: November 4, 2026
