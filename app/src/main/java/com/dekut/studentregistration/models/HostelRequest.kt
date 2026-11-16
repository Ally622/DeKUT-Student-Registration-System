package com.dekut.studentregistration.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HostelRequest(
    var id: String = "",
    var uid: String = "",          // Firebase Auth UID
    var studentId: String = "",    // Registration number
    var hostelLocation: String = "",
    var roomType: String = "",
    var sharable: String = "",
    var requestDate: Long = System.currentTimeMillis(),
    var syncStatus: String = "PENDING" // PENDING, SYNCED
) : Parcelable {
    constructor() : this("", "", "", "", "", "")
}
