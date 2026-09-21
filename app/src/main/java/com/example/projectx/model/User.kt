package com.example.projectx.model

data class User(
    val uid: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val rollNumber: String? = null,
    val role: UserRole = UserRole.STUDENT,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
