package com.projectx.app.model.career

import androidx.compose.runtime.Immutable

@Immutable
data class Achievement(
    val achievementId: String = "",
    val ownerUid: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val issuingOrganization: String = "",
    val documentStoragePath: String? = null,
    val createdAt: Long = 0L
)
