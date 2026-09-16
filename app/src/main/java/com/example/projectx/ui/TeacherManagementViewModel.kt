package com.example.projectx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectx.data.TeacherRepository
import com.example.projectx.model.Appointment
import com.example.projectx.model.AppointmentStatus
import com.example.projectx.model.Teacher
import com.example.projectx.model.TeacherStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class UserRole {
    STUDENT,
    ADMIN_TEACHER,
    WEB_VIEW
}

class TeacherManagementViewModel(
    private val repository: TeacherRepository = TeacherRepository.getInstance()
) : ViewModel() {

    private val _currentRole = MutableStateFlow(UserRole.ADMIN_TEACHER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _selectedTeacherId = MutableStateFlow("t1")
    val selectedTeacherId: StateFlow<String> = _selectedTeacherId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDepartment = MutableStateFlow("All")
    val selectedDepartment: StateFlow<String> = _selectedDepartment.asStateFlow()

    val teachers: StateFlow<List<Teacher>> = repository.teachers
    val appointments: StateFlow<List<Appointment>> = repository.appointments

    val filteredTeachers: StateFlow<List<Teacher>> = combine(
        teachers,
        _searchQuery,
        _selectedDepartment
    ) { teacherList, query, department ->
        teacherList.filter { teacher ->
            val matchesQuery = query.isEmpty() ||
                    teacher.name.contains(query, ignoreCase = true) ||
                    teacher.department.contains(query, ignoreCase = true) ||
                    teacher.deskNumber.contains(query, ignoreCase = true)

            val matchesDept = department == "All" || teacher.department.equals(department, ignoreCase = true)

            matchesQuery && matchesDept
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTeacher: StateFlow<Teacher?> = combine(
        teachers,
        _selectedTeacherId
    ) { teacherList, teacherId ->
        teacherList.find { it.id == teacherId } ?: teacherList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectRole(role: UserRole) {
        _currentRole.value = role
    }

    fun selectTeacher(teacherId: String) {
        _selectedTeacherId.value = teacherId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDepartmentFilter(department: String) {
        _selectedDepartment.value = department
    }

    fun updateStatus(teacherId: String, status: TeacherStatus) {
        repository.updateTeacherStatus(teacherId, status)
    }

    fun updateDeskAndTimings(teacherId: String, desk: String, timings: String) {
        repository.updateTeacherDeskAndTimings(teacherId, desk, timings)
    }

    fun bookAppointment(
        teacherId: String,
        teacherName: String,
        studentName: String,
        studentEmail: String,
        date: String,
        timeSlot: String,
        purpose: String
    ) {
        repository.bookAppointment(
            teacherId = teacherId,
            teacherName = teacherName,
            studentName = studentName,
            studentEmail = studentEmail,
            date = date,
            timeSlot = timeSlot,
            purpose = purpose
        )
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        repository.updateAppointmentStatus(appointmentId, status)
    }
}
