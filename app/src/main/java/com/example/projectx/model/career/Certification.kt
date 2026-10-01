package com.projectx.app.model.career

import androidx.compose.runtime.Immutable

@Immutable
data class Certification(
    val certificationId: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val issuingOrganization: String = "",
    val issueDate: String = "",
    val expiryDate: String? = null,
    val credentialId: String? = null,
    val credentialUrl: String? = null,
    val category: String = "General",
    val skills: List<String> = emptyList(),
    val description: String = "",
    val certificateStoragePath: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
