package com.projectx.app.model.career

import androidx.compose.runtime.Immutable

@Immutable
data class SkillItem(
    val skillId: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val category: String = "General",
    val proficiency: String? = "Intermediate",
    val createdAt: Long = 0L
)
