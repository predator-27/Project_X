package com.projectx.app.model.career

import androidx.compose.runtime.Immutable

@Immutable
data class ProjectItem(
    val projectId: String = "",
    val ownerUid: String = "",
    val projectName: String = "",
    val description: String = "",
    val technologies: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val projectUrl: String? = null,
    val githubUrl: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
