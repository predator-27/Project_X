package com.example.projectx.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectx.data.TeacherRepository
import com.example.projectx.model.Appointment
import com.example.projectx.model.AppointmentStatus
import com.example.projectx.model.Teacher
import com.example.projectx.model.TeacherStatus
import com.example.projectx.update.UpdateInfo
import com.example.projectx.update.UpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthState {
    UNAUTHENTICATED,
    TEACHER_ADMIN,
    STUDENT,
    WEB_VIEW
}

class TeacherManagementViewModel(
    private val repository: TeacherRepository = TeacherRepository.getInstance(),
    private val updateManager: UpdateManager = UpdateManager.getInstance()
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "projectx_auth_prefs"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_AUTH_STATE = "auth_state"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_TEACHER_ID = "teacher_id"
    }

    private val _authState = MutableStateFlow(AuthState.UNAUTHENTICATED)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loggedInUserEmail = MutableStateFlow("")
    val loggedInUserEmail: StateFlow<String> = _loggedInUserEmail.asStateFlow()

    private val _selectedTeacherId = MutableStateFlow("t1")
    val selectedTeacherId: StateFlow<String> = _selectedTeacherId.asStateFlow()

    private val _rememberMe = MutableStateFlow(true)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDepartment = MutableStateFlow("All")
    val selectedDepartment: StateFlow<String> = _selectedDepartment.asStateFlow()

    private val _showUpdateBanner = MutableStateFlow(true)
    val showUpdateBanner: StateFlow<Boolean> = _showUpdateBanner.asStateFlow()

    val updateInfo: StateFlow<UpdateInfo?> = updateManager.updateInfo
    val isDownloadingUpdate: StateFlow<Boolean> = updateManager.isDownloading
    val downloadProgress: StateFlow<Float> = updateManager.downloadProgress
    val downloadError: StateFlow<String?> = updateManager.downloadError

    val teachers: StateFlow<List<Teacher>> = repository.teachers
    val appointments: StateFlow<List<Appointment>> = repository.appointments

    fun loadSavedAuth(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isRemembered = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val savedStateStr = prefs.getString(KEY_AUTH_STATE, AuthState.UNAUTHENTICATED.name) ?: AuthState.UNAUTHENTICATED.name
        val savedEmail = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val savedTeacherId = prefs.getString(KEY_TEACHER_ID, "t1") ?: "t1"

        _rememberMe.value = isRemembered
        if (savedEmail.isNotBlank()) {
            _loggedInUserEmail.value = savedEmail
        }
        if (savedTeacherId.isNotBlank()) {
            _selectedTeacherId.value = savedTeacherId
        }

        if (isRemembered) {
            when (savedStateStr) {
                AuthState.TEACHER_ADMIN.name -> _authState.value = AuthState.TEACHER_ADMIN
                AuthState.STUDENT.name -> _authState.value = AuthState.STUDENT
                else -> _authState.value = AuthState.UNAUTHENTICATED
            }
        }
    }

    fun checkForAppUpdates(context: Context) {
        viewModelScope.launch {
            updateManager.checkForUpdates(context)
        }
    }

    fun downloadAndInstallAppUpdate(context: Context) {
        val info = updateInfo.value ?: return
        viewModelScope.launch {
            updateManager.downloadAndInstallUpdate(context, info.downloadUrl)
        }
    }

    fun dismissUpdateNotification(context: Context) {
        updateInfo.value?.latestVersion?.let { version ->
            updateManager.markVersionDismissed(context, version)
        }
        _showUpdateBanner.value = false
    }

    fun isVersionDismissed(context: Context, version: String): Boolean {
        return updateManager.getDismissedVersion(context) == version
    }

    val filteredTeachers: StateFlow<List<Teacher>> = combine(
        teachers,
        _searchQuery,
        _selectedDepartment
    ) { teacherList, query, department ->
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty() && department == "All") {
            teacherList
        } else {
            teacherList.filter { teacher ->
                val matchesQuery = trimmedQuery.isEmpty() ||
                        teacher.name.contains(trimmedQuery, ignoreCase = true) ||
                        teacher.department.contains(trimmedQuery, ignoreCase = true) ||
                        teacher.deskNumber.contains(trimmedQuery, ignoreCase = true) ||
                        teacher.institution.contains(trimmedQuery, ignoreCase = true)

                val matchesDept = department == "All" || teacher.department.equals(department, ignoreCase = true)

                matchesQuery && matchesDept
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTeacher: StateFlow<Teacher?> = combine(
        teachers,
        _selectedTeacherId
    ) { teacherList, teacherId ->
        teacherList.find { it.id == teacherId } ?: teacherList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loginAsTeacher(
        email: String,
        teacherId: String = "t1",
        rememberMe: Boolean = true,
        context: Context? = null
    ) {
        val finalEmail = email.ifBlank { "teacher@university.edu" }
        _loggedInUserEmail.value = finalEmail
        _selectedTeacherId.value = teacherId
        _rememberMe.value = rememberMe
        _authState.value = AuthState.TEACHER_ADMIN

        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean(KEY_REMEMBER_ME, rememberMe)
                putString(KEY_AUTH_STATE, AuthState.TEACHER_ADMIN.name)
                putString(KEY_USER_EMAIL, finalEmail)
                putString(KEY_TEACHER_ID, teacherId)
                apply()
            }
        }
    }

    fun loginAsStudent(
        email: String,
        rememberMe: Boolean = true,
        context: Context? = null
    ) {
        val finalEmail = email.ifBlank { "student@university.edu" }
        _loggedInUserEmail.value = finalEmail
        _rememberMe.value = rememberMe
        _authState.value = AuthState.STUDENT

        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean(KEY_REMEMBER_ME, rememberMe)
                putString(KEY_AUTH_STATE, AuthState.STUDENT.name)
                putString(KEY_USER_EMAIL, finalEmail)
                apply()
            }
        }
    }

    fun openWebView() {
        _authState.value = AuthState.WEB_VIEW
    }

    fun logout(context: Context? = null) {
        _authState.value = AuthState.UNAUTHENTICATED
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_AUTH_STATE, AuthState.UNAUTHENTICATED.name)
                apply()
            }
        }
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

    fun updateFullProfile(
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
        repository.updateTeacherFullProfile(
            teacherId = teacherId,
            name = name,
            title = title,
            department = department,
            email = email,
            deskNumber = deskNumber,
            timings = timings,
            institution = institution,
            institutionDomain = institutionDomain
        )
    }

    fun registerAndLoginTeacher(
        name: String,
        title: String,
        department: String,
        email: String,
        deskNumber: String,
        timings: String,
        institution: String,
        institutionDomain: String,
        rememberMe: Boolean = true,
        context: Context? = null
    ) {
        val newTeacher = repository.registerNewTeacher(
            name = name,
            title = title,
            department = department,
            email = email,
            deskNumber = deskNumber,
            timings = timings,
            institution = institution,
            institutionDomain = institutionDomain
        )
        loginAsTeacher(
            email = newTeacher.email,
            teacherId = newTeacher.id,
            rememberMe = rememberMe,
            context = context
        )
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
            studentEmail = studentEmail.ifBlank { _loggedInUserEmail.value },
            date = date,
            timeSlot = timeSlot,
            purpose = purpose
        )
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        repository.updateAppointmentStatus(appointmentId, status)
    }
}
