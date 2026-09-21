package com.example.projectx.model

data class PublicProfile(
    val uid: String = "",
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val schoolName: String? = null,
    val program: String? = null,
    val department: String? = null,
    val specialization: String? = null,
    val admissionYear: Int? = null,
    val currentSemester: Int? = null,
    val section: String? = null,
    val bio: String? = null
)
