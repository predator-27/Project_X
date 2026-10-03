package com.projectx.app.model.hostel

import androidx.compose.runtime.Immutable

@Immutable
data class RoomPartnerRequest(
    val requestId: String = "",
    val studentUid: String = "",
    val partnerRollNo: String? = null,
    val partnerName: String? = null,
    val roomType: String = "Triple Sharing",
    val status: String = "NO_REQUEST",
    val submittedAt: Long = 0L
)
