# DeKUT Student Registration - Testing Guide

This guide will help you test all features of the application systematically.

## Pre-Test Setup

### 1. Firebase Configuration
✅ Ensure `google-services.json` is in `app/` folder  
✅ Firebase Realtime Database is created  
✅ Database rules are set correctly  
✅ Internet connection is available

### 2. Device/Emulator Setup
✅ Android device or emulator is running  
✅ Developer options enabled (for physical device)  
✅ USB debugging enabled (for physical device)  
✅ Minimum Android 7.0 (API 24)

## Test Cases

### TEST 1: Student Registration (Online)

**Objective:** Verify student registration works online

**Steps:**
1. Launch app
2. Verify dashboard loads with all cards
3. Click "Student Registration" card
4. Fill form with valid data:
   ```
   Full Name: John Kamau
   ID Number: 12345678
   Reg Number: CS001/2024
   Gender: Male
   Course: Bachelor of Science in Computer Science
   Department: Computer Science
   ```
5. Click "Register" button

**Expected Result:**
- ✅ Success message appears
- ✅ Form clears
- ✅ No "Pending Sync" status
- ✅ Data appears in Firebase Console

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 2: Duplicate Registration Prevention

**Objective:** Verify duplicate registration numbers are rejected

**Steps:**
1. Open Student Registration
2. Enter same registration number as TEST 1:
   ```
   Full Name: Jane Wambui
   ID Number: 87654321
   Reg Number: CS001/2024  ← Same as before
   ```
3. Click "Register"

**Expected Result:**
- ✅ Error message: "Registration number already exists!"
- ✅ Data not saved
- ✅ Form remains filled

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 3: Input Validation

**Objective:** Verify all fields are validated

**Steps:**
1. Open Student Registration
2. Leave Full Name empty
3. Click "Register"
4. Verify error on Full Name field
5. Fill Full Name, leave ID Number empty
6. Click "Register"
7. Verify error on ID Number
8. Repeat for all fields

**Expected Result:**
- ✅ Required fields show error when empty
- ✅ Cursor focuses on error field
- ✅ Appropriate error messages
- ✅ Registration prevented

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 4: Offline Registration (★ 5 MARKS)

**Objective:** Verify offline functionality works correctly

**Phase A: Register Offline**
1. **Turn OFF WiFi and mobile data**
2. Open Student Registration
3. Fill form:
   ```
   Full Name: Mary Njeri
   ID Number: 11223344
   Reg Number: IT001/2024
   Gender: Female
   Course: Bachelor of Science in Information Technology
   Department: Information Technology
   ```
4. Click "Register"

**Expected Result:**
- ✅ Success message: "Will sync when online"
- ✅ "Pending Sync" status visible
- ✅ Form clears

**Phase B: Data Persistence**
5. Close the app completely
6. Reopen the app
7. Navigate to admin section

**Expected Result:**
- ✅ Offline student still shows
- ✅ Status is "Pending Sync"
- ✅ Data not lost

**Phase C: Auto Sync**
8. **Turn ON internet**
9. Wait 5-10 seconds
10. Check Firebase Console

**Expected Result:**
- ✅ "Syncing pending data..." message appears
- ✅ Status changes to "Synced"
- ✅ Data appears in Firebase
- ✅ "Pending Sync" indicator disappears

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 5: Library Book Request

**Objective:** Verify library request functionality

**Steps:**
1. Click "Library Book Request" from dashboard
2. Fill form:
   ```
   Student Reg Number: CS001/2024
   ISBN: 978-0134685991
   Title: Effective Java
   Author: Joshua Bloch
   Date Borrowed: [Select today's date]
   Date of Return: [Select 14 days from today]
   ```
3. Click "Submit Request"

**Expected Result:**
- ✅ Success message appears
- ✅ Form clears
- ✅ Data saved to Firebase under LibraryRequests

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 6: Library Date Validation

**Objective:** Verify return date must be after borrow date

**Steps:**
1. Open Library Book Request
2. Fill all fields
3. Select Date Borrowed: Today
4. Select Date of Return: Yesterday
5. Click "Submit"

**Expected Result:**
- ✅ Error message: "Return date cannot be earlier than borrow date"
- ✅ Request not submitted
- ✅ Form remains filled

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 7: Hostel Accommodation Request

**Objective:** Verify hostel request functionality

**Steps:**
1. Click "Hostel Accommodation" from dashboard
2. Fill form:
   ```
   Student Reg Number: CS001/2024
   Hostel Location: Main Campus Hostels
   Room Type: Single Room
   Sharable: No
   ```
3. Click "Submit Request"

**Expected Result:**
- ✅ Success message appears
- ✅ Form clears
- ✅ Data saved to Firebase under HostelRequests

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 8: M-Pesa Payment (Sandbox Mode)

**Objective:** Verify payment flow in sandbox mode

**Steps:**
1. Click "Fees Payment" from dashboard
2. Fill form:
   ```
   Student Reg Number: CS001/2024
   Phone Number: 254712345678
   Amount: 50000
   ```
3. Click "Pay Now"
4. Read confirmation dialog
5. Click "Proceed"
6. If sandbox mode appears, click "Simulate Success"

**Expected Result:**
- ✅ Payment status card appears
- ✅ Success message shown
- ✅ Transaction reference generated
- ✅ Payment saved to Firebase
- ✅ Student's payment status updated to "PAID"
- ✅ Registration status updated to "CONFIRMED"

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 9: Phone Number Validation

**Objective:** Verify M-Pesa phone number validation

**Steps:**
1. Open Fees Payment
2. Enter invalid phone numbers:
   - Test A: "0712345678" (should be 254...)
   - Test B: "25471234" (too short)
   - Test C: "abcd" (non-numeric)
3. Try to pay

**Expected Result:**
- ✅ Error shown for invalid format
- ✅ Error shown for wrong length
- ✅ Error shown for non-numeric
- ✅ Payment not initiated

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 10: View All Students

**Objective:** Verify admin can view all students

**Steps:**
1. Click "View All Students" from dashboard
2. Observe student list
3. Verify student count displayed
4. Check sync status visible for each student

**Expected Result:**
- ✅ All registered students shown
- ✅ Student count accurate
- ✅ RecyclerView scrolls smoothly
- ✅ Student cards show all info correctly

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 11: Search Students

**Objective:** Verify search functionality

**Steps:**
1. In Students Activity
2. Type in search box:
   - "CS001" (search by reg number)
   - "12345678" (search by ID)
   - "John" (search by name)
3. Verify filtering works

**Expected Result:**
- ✅ List updates in real-time
- ✅ Only matching students shown
- ✅ Empty state if no matches
- ✅ Clear search shows all again

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 12: Filter Confirmed Students

**Objective:** Verify confirmed filter works

**Steps:**
1. In Students Activity
2. Click "Confirmed" button
3. Observe filtered list

**Expected Result:**
- ✅ Only students with CONFIRMED status shown
- ✅ Button highlighted
- ✅ Count updated
- ✅ Click "All" to show all again

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 13: View Student Details

**Objective:** Verify individual student details

**Steps:**
1. In Students Activity
2. Click on any student card
3. Read details in dialog

**Expected Result:**
- ✅ Dialog shows all student info
- ✅ Library requests count visible
- ✅ Hostel info shown if exists
- ✅ Payment total shown if exists
- ✅ "Print Summary" button available

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 14: Print Individual Student Summary

**Objective:** Verify PDF generation for single student

**Steps:**
1. In Students Activity
2. Click on student
3. Click "Print Summary" button
4. Wait for print preview

**Expected Result:**
- ✅ WebView loads with student data
- ✅ Print dialog opens
- ✅ PDF contains:
  - DeKUT header
  - Student information
  - Course and department
  - Payment details
  - Library requests (if any)
  - Hostel info (if any)
  - Professional formatting

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 15: Print All Students Summary

**Objective:** Verify PDF generation for all students

**Steps:**
1. In Students Activity
2. Click "Print All" button
3. Wait for print preview

**Expected Result:**
- ✅ Print dialog opens
- ✅ PDF contains:
  - DeKUT header
  - Summary statistics
  - Table of all students
  - Registration numbers
  - Names, courses, departments
  - Payment statuses
  - Professional formatting

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 16: UI/UX Design

**Objective:** Verify professional design

**Checklist:**
- ✅ DeKUT red color scheme (#8B0000)
- ✅ White backgrounds with cards
- ✅ Rounded corners on cards
- ✅ Consistent spacing
- ✅ Readable typography
- ✅ Material Design 3 components
- ✅ Proper icons/emojis
- ✅ Responsive to different screen sizes
- ✅ Smooth animations
- ✅ Professional splash/header

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 17: Error Handling

**Objective:** Verify graceful error handling

**Test Scenarios:**
1. No internet during online operation
2. Invalid Firebase configuration
3. Invalid input data
4. Backend unavailable (M-Pesa)
5. Database write failure

**Expected Result:**
- ✅ User-friendly error messages
- ✅ App doesn't crash
- ✅ Snackbar notifications
- ✅ Fallback to offline mode
- ✅ Retry options where appropriate

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

### TEST 18: Comprehensive Flow Test

**Objective:** Complete end-to-end workflow

**Steps:**
1. Register new student: "Test Student A"
2. Submit library request for student
3. Submit hostel request for student
4. Make payment for student
5. Verify registration confirmed
6. View in admin section
7. Print student summary
8. Verify all data correct

**Expected Result:**
- ✅ All operations succeed
- ✅ Data correctly linked by student ID
- ✅ Status updates appropriately
- ✅ PDF shows complete information

**Status:** [ ] PASS  [ ] FAIL  
**Notes:** _______________________

---

## Test Summary

| Test # | Test Name | Status | Notes |
|--------|-----------|--------|-------|
| 1 | Student Registration (Online) | [ ] | |
| 2 | Duplicate Prevention | [ ] | |
| 3 | Input Validation | [ ] | |
| 4 | **Offline Functionality** | [ ] | **5 MARKS** |
| 5 | Library Request | [ ] | |
| 6 | Date Validation | [ ] | |
| 7 | Hostel Request | [ ] | |
| 8 | M-Pesa Payment | [ ] | |
| 9 | Phone Validation | [ ] | |
| 10 | View Students | [ ] | |
| 11 | Search Students | [ ] | |
| 12 | Filter Students | [ ] | |
| 13 | Student Details | [ ] | |
| 14 | Print Individual | [ ] | |
| 15 | Print All | [ ] | |
| 16 | UI/UX Design | [ ] | |
| 17 | Error Handling | [ ] | |
| 18 | Complete Flow | [ ] | |

**Total Tests:** 18  
**Passed:** _____  
**Failed:** _____  
**Pass Rate:** _____%

## Critical Issues Found

1. _______________________________________
2. _______________________________________
3. _______________________________________

## Recommendations

1. _______________________________________
2. _______________________________________
3. _______________________________________

## Tester Information

**Name:** _______________________  
**Date:** _______________________  
**Device:** _____________________  
**Android Version:** _____________  
**Build Version:** _______________

**Signature:** _______________________
