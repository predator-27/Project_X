package com.projectx.app.model

import androidx.compose.runtime.Immutable

enum class FeedbackCategory(val label: String) {
    ACADEMICS("Academics"),
    FACULTY("Faculty"),
    HOSTEL("Hostel"),
    CAFETERIA("Cafeteria"),
    INFRASTRUCTURE("Infrastructure"),
    OTHER("Other"),
}

@Immutable
data class FeedbackItem(
    val id: String,
    val category: FeedbackCategory,
    val rating: Int,            // 1..5
    val comment: String,
    val submittedByName: String?, // null = anonymous
    val createdAt: Long,
)
