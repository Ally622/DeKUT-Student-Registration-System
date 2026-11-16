package com.dekut.studentregistration.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LibraryRequest(
    var id: String = "",
    var uid: String = "",          // Firebase Auth UID
    var studentId: String = "",    // Student Registration number
    var bookId: String = "",       // Linked Book ID
    var bookIsbn: String = "",
    var bookTitle: String = "",
    var author: String = "",
    var department: String = "",
    var dateBorrowed: Long = 0L,
    var dateOfReturn: Long = 0L,
    var status: String = "PENDING", // PENDING, APPROVED, REJECTED, RETURNED
    var requestDate: Long = System.currentTimeMillis(),
    var syncStatus: String = "PENDING" // PENDING, SYNCED
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", "", 0L, 0L, "PENDING", System.currentTimeMillis(), "PENDING")
}
