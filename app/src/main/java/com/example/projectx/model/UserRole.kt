package com.example.projectx.model

enum class UserRole {
    STUDENT,
    FACULTY,
    LOST_FOUND_STAFF,
    COLLEGE_ADMIN,
    SUPER_ADMIN;

    companion object {
        fun fromString(roleStr: String?): UserRole {
            return entries.find { it.name.equals(roleStr, ignoreCase = true) } ?: STUDENT
        }
    }
}
