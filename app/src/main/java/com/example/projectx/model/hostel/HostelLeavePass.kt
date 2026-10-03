package com.projectx.app.model.hostel

import androidx.compose.runtime.Immutable

@Immutable
data class HostelLeavePass(
    val leaveId: String = "",
    val studentUid: String = "",
    val leaveType: String = "Home Leave",
    val startDate: String = "",
    val endDate: String = "",
    val reason: String = "",
    val status: String = "SUBMITTED",
    val approvedBy: String? = null,
    val appliedAt: Long = 0L
)
