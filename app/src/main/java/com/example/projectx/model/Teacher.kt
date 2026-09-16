package com.example.projectx.model

enum class TeacherStatus(val label: String) {
    AT_DESK("At Desk"),
    BUSY("Busy / In Meeting"),
    IN_CLASS("In Class"),
    AWAY("Away")
}

data class Teacher(
    val id: String,
    val name: String,
    val title: String,
    val department: String,
    val deskNumber: String,
    val timings: String,
    val email: String,
    val status: TeacherStatus = TeacherStatus.AT_DESK,
    val isAvailableForAppointments: Boolean = true,
    val bio: String = ""
)
