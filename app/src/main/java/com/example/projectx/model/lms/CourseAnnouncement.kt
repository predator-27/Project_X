package com.projectx.app.model.lms

import androidx.compose.runtime.Immutable

enum class AnnouncementPriority {
    NORMAL,
    URGENT
}

@Immutable
data class CourseAnnouncement(
    val announcementId: String = "",
    val courseCode: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val authorUid: String = "",
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val createdAt: Long = System.currentTimeMillis()
)
