package com.example.projectx.model

enum class AppointmentStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class Appointment(
    val id: String,
    val teacherId: String,
    val teacherName: String,
    val studentName: String,
    val studentEmail: String,
    val date: String,
    val timeSlot: String,
    val purpose: String,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)
