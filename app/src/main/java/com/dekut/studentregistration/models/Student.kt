package com.dekut.studentregistration.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    var id: String = "",
    var uid: String = "", // Firebase Authentication UID
    var fullName: String = "",
    var idNumber: String = "",
    var registrationNumber: String = "",
    var gender: String = "",
    var course: String = "",
    var department: String = "",
    var registrationStatus: String = "PENDING", // PENDING, CONFIRMED
    var dateRegistered: Long = System.currentTimeMillis(),
    var syncStatus: String = "PENDING", // PENDING, SYNCED
    var paymentStatus: String = "UNPAID", // UNPAID, PAID
    var role: String = "STUDENT", // STUDENT, ADMIN (only set server-side)
    var totalFeesPayable: Double = 0.0 // Configure per student or course
) : Parcelable {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", "", "")
}
