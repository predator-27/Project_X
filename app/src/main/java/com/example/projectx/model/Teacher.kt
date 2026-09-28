package com.example.projectx.model

enum class TeacherStatus(val label: String) {
    AT_DESK("At Desk"),
    BUSY("Busy / In Meeting"),
    IN_CLASS("In Class"),
    AWAY("Away")
}

data class Institution(
    val name: String,
    val domain: String
) {
    val displayLabel: String get() = "$name (@$domain)"

    companion object {
        val DEFAULT_LIST = listOf(
            Institution("Bennett University", "bennett.edu.in")
        )
    }
}

data class Teacher(
    val id: String,
    val name: String,
    val title: String,
    val department: String,
    val deskNumber: String,
    val timings: String,
    val email: String,
    val institution: String = "Bennett University",
    val institutionDomain: String = "bennett.edu.in",
    val status: TeacherStatus = TeacherStatus.AT_DESK,
    val isAvailableForAppointments: Boolean = true,
    val bio: String = ""
)
