package com.example.projectx.data

import com.example.projectx.model.Appointment
import com.example.projectx.model.AppointmentStatus
import com.example.projectx.model.Teacher
import com.example.projectx.model.TeacherStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TeacherRepository private constructor() {

    private val _teachers = MutableStateFlow<List<Teacher>>(initialTeachers())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(initialAppointments())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    fun updateTeacherStatus(teacherId: String, newStatus: TeacherStatus) {
        _teachers.value = _teachers.value.map { teacher ->
            if (teacher.id == teacherId) {
                teacher.copy(status = newStatus)
            } else {
                teacher
            }
        }
    }

    fun updateTeacherDeskAndTimings(teacherId: String, newDesk: String, newTimings: String) {
        _teachers.value = _teachers.value.map { teacher ->
            if (teacher.id == teacherId) {
                teacher.copy(deskNumber = newDesk, timings = newTimings)
            } else {
                teacher
            }
        }
    }

    fun updateTeacherFullProfile(
        teacherId: String,
        name: String,
        title: String,
        department: String,
        email: String,
        deskNumber: String,
        timings: String,
        institution: String,
        institutionDomain: String
    ) {
        _teachers.value = _teachers.value.map { teacher ->
            if (teacher.id == teacherId) {
                teacher.copy(
                    name = name,
                    title = title,
                    department = department,
                    email = email,
                    deskNumber = deskNumber,
                    timings = timings,
                    institution = institution,
                    institutionDomain = institutionDomain
                )
            } else {
                teacher
            }
        }
    }

    fun registerNewTeacher(
        name: String,
        title: String,
        department: String,
        email: String,
        deskNumber: String,
        timings: String,
        institution: String,
        institutionDomain: String
    ): Teacher {
        val newTeacher = Teacher(
            id = "t_" + UUID.randomUUID().toString().take(6),
            name = name,
            title = title,
            department = department,
            email = email,
            deskNumber = deskNumber.ifBlank { "Desk #101" },
            timings = timings.ifBlank { "Mon-Fri: 10:00 AM - 02:00 PM" },
            institution = institution,
            institutionDomain = institutionDomain,
            status = TeacherStatus.AT_DESK
        )
        _teachers.value = _teachers.value + newTeacher
        return newTeacher
    }

    fun bookAppointment(
        teacherId: String,
        teacherName: String,
        studentName: String,
        studentEmail: String,
        date: String,
        timeSlot: String,
        purpose: String
    ): Appointment {
        val newAppointment = Appointment(
            id = UUID.randomUUID().toString().take(8),
            teacherId = teacherId,
            teacherName = teacherName,
            studentName = studentName,
            studentEmail = studentEmail,
            date = date,
            timeSlot = timeSlot,
            purpose = purpose,
            status = AppointmentStatus.PENDING
        )
        _appointments.value = listOf(newAppointment) + _appointments.value
        return newAppointment
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        _appointments.value = _appointments.value.map { appointment ->
            if (appointment.id == appointmentId) {
                appointment.copy(status = newStatus)
            } else {
                appointment
            }
        }
    }

    companion object {
        @Volatile
        private var instance: TeacherRepository? = null

        fun getInstance(): TeacherRepository {
            return instance ?: synchronized(this) {
                instance ?: TeacherRepository().also { instance = it }
            }
        }

        private fun initialTeachers() = listOf(
            Teacher(
                id = "t1",
                name = "Dr. Sarah Jenkins",
                title = "Associate Professor",
                department = "Computer Science",
                deskNumber = "Desk #304, Block B",
                timings = "Mon-Fri: 10:00 AM - 01:00 PM",
                email = "s.jenkins@university.edu",
                institution = "Global Tech University",
                institutionDomain = "university.edu",
                status = TeacherStatus.AT_DESK,
                bio = "Specializes in Artificial Intelligence and Machine Learning mentorship."
            ),
            Teacher(
                id = "t2",
                name = "Prof. David Miller",
                title = "Senior Mentor",
                department = "Data Science",
                deskNumber = "Stall #102, Innovation Lab",
                timings = "Mon-Thu: 02:00 PM - 05:00 PM",
                email = "d.miller@university.edu",
                institution = "Global Tech University",
                institutionDomain = "university.edu",
                status = TeacherStatus.BUSY,
                bio = "Data Analytics advisor and research mentor."
            ),
            Teacher(
                id = "t3",
                name = "Dr. Emily Carter",
                title = "Department Chair",
                department = "Software Engineering",
                deskNumber = "Desk #412, Tech Tower",
                timings = "Tue-Fri: 11:00 AM - 03:00 PM",
                email = "e.carter@university.edu",
                institution = "Global Tech University",
                institutionDomain = "university.edu",
                status = TeacherStatus.IN_CLASS,
                bio = "Software Architecture and Distributed Systems lead."
            ),
            Teacher(
                id = "t4",
                name = "Prof. Robert Chen",
                title = "Assistant Professor",
                department = "Cybersecurity",
                deskNumber = "Desk #208, Block C",
                timings = "Mon-Wed: 09:30 AM - 12:30 PM",
                email = "r.chen@university.edu",
                institution = "Global Tech University",
                institutionDomain = "university.edu",
                status = TeacherStatus.AWAY,
                bio = "Network Security and Cryptography mentor."
            )
        )

        private fun initialAppointments() = listOf(
            Appointment(
                id = "a101",
                teacherId = "t1",
                teacherName = "Dr. Sarah Jenkins",
                studentName = "Alex Rivera",
                studentEmail = "alex.r@student.edu",
                date = "2026-09-18",
                timeSlot = "10:30 AM - 11:00 AM",
                purpose = "Project Architecture Review & AI Model Selection",
                status = AppointmentStatus.CONFIRMED
            ),
            Appointment(
                id = "a102",
                teacherId = "t2",
                teacherName = "Prof. David Miller",
                studentName = "Jordan Lee",
                studentEmail = "jordan.l@student.edu",
                date = "2026-09-19",
                timeSlot = "02:30 PM - 03:00 PM",
                purpose = "Data Science Internship Guidance",
                status = AppointmentStatus.PENDING
            )
        )
    }
}
