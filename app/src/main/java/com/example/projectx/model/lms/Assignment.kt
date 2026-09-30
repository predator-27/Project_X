package com.projectx.app.model.lms

import androidx.compose.runtime.Immutable

@Immutable
data class Assignment(
    val assignmentId: String = "",
    val courseCode: String = "",
    val title: String = "",
    val description: String = "",
    val maxPoints: Int = 100,
    val dueDateTimestamp: Long? = null,
    val attachmentUrl: String? = null,
    val publishedByUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
