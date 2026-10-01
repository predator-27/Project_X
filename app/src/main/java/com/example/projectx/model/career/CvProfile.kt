package com.projectx.app.model.career

import androidx.compose.runtime.Immutable

@Immutable
data class CvProfile(
    val cvId: String = "",
    val ownerUid: String = "",
    val title: String = "Master CV",
    val selectedCertificationIds: List<String> = emptyList(),
    val selectedProjectIds: List<String> = emptyList(),
    val selectedSkillIds: List<String> = emptyList(),
    val selectedAchievementIds: List<String> = emptyList(),
    val personalSummary: String = "",
    val targetRole: String = "Software Engineer",
    val selectedTemplate: String = "Classic",
    val updatedAt: Long = 0L
)
