package com.dekut.studentregistration.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    var id: String = "",
    var title: String = "",
    var isbn: String = "",
    var author: String = "",
    var department: String = "",
    var availabilityStatus: String = "AVAILABLE", // AVAILABLE, UNAVAILABLE
    var totalCopies: Int = 1,
    var availableCopies: Int = 1
) : Parcelable {
    constructor() : this("", "", "", "", "", "AVAILABLE", 1, 1)
}
