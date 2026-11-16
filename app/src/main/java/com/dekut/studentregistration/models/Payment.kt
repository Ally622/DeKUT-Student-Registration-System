package com.dekut.studentregistration.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(
    var id: String = "",
    var uid: String = "",          // Firebase Auth UID of the paying student
    var studentId: String = "",    // Registration number
    var phoneNumber: String = "",
    var amount: Double = 0.0,
    var transactionReference: String = "",
    var paymentStatus: String = "PENDING", // PENDING, SUCCESS, FAILED
    var transactionDate: Long = System.currentTimeMillis(),
    var syncStatus: String = "PENDING" // PENDING, SYNCED
) : Parcelable {
    constructor() : this("", "", "", "", 0.0, "", "PENDING")
}
