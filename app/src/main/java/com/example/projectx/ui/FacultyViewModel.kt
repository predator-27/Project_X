package com.projectx.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.auth.AuthRepository
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.data.firestore.FacultyRepository
import com.projectx.app.model.Teacher
import com.projectx.app.model.TeacherStatus
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FacultyViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val facultyRepository: FacultyRepository = FacultyRepository()
) : ViewModel() {

    private val _facultyListState = MutableStateFlow<Resource<List<Teacher>>>(Resource.Loading)
    val facultyListState: StateFlow<Resource<List<Teacher>>> = _facultyListState.asStateFlow()

    private val _activeFacultyProfile = MutableStateFlow<Resource<Teacher?>>(Resource.Empty)
    val activeFacultyProfile: StateFlow<Resource<Teacher?>> = _activeFacultyProfile.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDepartment = MutableStateFlow("All")
    val selectedDepartment: StateFlow<String> = _selectedDepartment.asStateFlow()

    init {
        loadFacultyDirectory()
    }

    private fun isDemoSession(): Boolean {
        return BuildConfig.DEBUG &&
                (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid == DemoCampusData.DEMO_STUDENT_UID
    }

    fun loadFacultyDirectory() {
        viewModelScope.launch {
            _facultyListState.value = Resource.Loading
            if (isDemoSession()) {
                _facultyListState.value = Resource.Success(DemoCampusData.demoFaculty)
                return@launch
            }
            facultyRepository.getFacultyDirectory().fold(
                onSuccess = { list ->
                    _facultyListState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _facultyListState.value = Resource.Error(error.message ?: "Failed to load faculty directory.")
                }
            )
        }
    }

    fun loadActiveFacultyProfile(facultyUid: String) {
        if (facultyUid.isBlank()) return
        viewModelScope.launch {
            _activeFacultyProfile.value = Resource.Loading
            facultyRepository.getFacultyByUid(facultyUid).fold(
                onSuccess = { teacher ->
                    _activeFacultyProfile.value = Resource.Success(teacher)
                },
                onFailure = { error ->
                    _activeFacultyProfile.value = Resource.Error(error.message ?: "Failed to read faculty profile.")
                }
            )
        }
    }

    val filteredTeachersState: StateFlow<Resource<List<Teacher>>> = combine(
        _facultyListState,
        _searchQuery,
        _selectedDepartment
    ) { resource, query, dept ->
        when (resource) {
            is Resource.Success -> {
                val trimmedQuery = query.trim().lowercase()
                val filtered = resource.data.filter { teacher ->
                    val matchesQuery = trimmedQuery.isEmpty() ||
                            teacher.name.lowercase().contains(trimmedQuery) ||
                            teacher.department.lowercase().contains(trimmedQuery) ||
                            teacher.deskNumber.lowercase().contains(trimmedQuery) ||
                            teacher.title.lowercase().contains(trimmedQuery)

                    val matchesDept = dept == "All" || teacher.department.equals(dept, ignoreCase = true)
                    matchesQuery && matchesDept
                }
                if (filtered.isEmpty()) Resource.Empty else Resource.Success(filtered)
            }
            else -> resource
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDepartmentFilter(dept: String) {
        _selectedDepartment.value = dept
    }

    fun updateOwnStatus(facultyUid: String, status: TeacherStatus) {
        if (facultyUid.isBlank()) return
        viewModelScope.launch {
            facultyRepository.updateOwnFacultyStatus(facultyUid, status).fold(
                onSuccess = {
                    loadActiveFacultyProfile(facultyUid)
                    loadFacultyDirectory()
                },
                onFailure = { /* Handled via toast / log */ }
            )
        }
    }

    fun updateOwnTimings(facultyUid: String, timings: String) {
        if (facultyUid.isBlank()) return
        viewModelScope.launch {
            facultyRepository.updateOwnFacultyTimings(facultyUid, timings).fold(
                onSuccess = {
                    loadActiveFacultyProfile(facultyUid)
                    loadFacultyDirectory()
                },
                onFailure = { /* Handled via toast / log */ }
            )
        }
    }

    fun updateOwnBio(facultyUid: String, bio: String) {
        if (facultyUid.isBlank()) return
        viewModelScope.launch {
            facultyRepository.updateOwnFacultyBio(facultyUid, bio).fold(
                onSuccess = {
                    loadActiveFacultyProfile(facultyUid)
                    loadFacultyDirectory()
                },
                onFailure = { /* Handled via toast / log */ }
            )
        }
    }
}
