package com.projectx.app.model.lms

import androidx.compose.runtime.Immutable

enum class SubmissionStatus {
    SUBMITTED,
    GRADED
}

@Immutable
data class AssignmentSubmission(
    val submissionId: String = "",
    val assignmentId: String = "",
    val courseCode: String = "",
    val studentUid: String = "",
    val studentName: String = "",
    val studentRollNumber: String = "",
    val responseText: String? = null,
    val storagePath: String? = null,
    val fileSizeBytes: Long? = null,
    val fileType: String? = null,
    val submittedAtTimestamp: Long = 0L,
    val status: SubmissionStatus = SubmissionStatus.SUBMITTED,
    val isLate: Boolean = false,
    val pointsEarned: Int? = null,
    val facultyFeedback: String? = null,
    val gradedByUid: String? = null,
    val gradedAtTimestamp: Long? = null
)
