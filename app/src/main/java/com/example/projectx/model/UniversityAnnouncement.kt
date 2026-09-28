package com.example.projectx.model

import androidx.compose.runtime.Immutable
import com.example.projectx.model.lms.AnnouncementPriority

@Immutable
data class UniversityAnnouncement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val authorUid: String = "",
    val targetDepartment: String = "ALL",
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val createdAt: Long = 0L
)
