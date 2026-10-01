package com.projectx.app.ui.lms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.auth.AuthRepository
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.data.firestore.AssignmentRepository
import com.projectx.app.data.firestore.CourseAnnouncementRepository
import com.projectx.app.data.firestore.CourseMaterialRepository
import com.projectx.app.model.lms.Assignment
import com.projectx.app.model.lms.CourseAnnouncement
import com.projectx.app.model.lms.CourseMaterial
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val materialRepository: CourseMaterialRepository = CourseMaterialRepository(),
    private val assignmentRepository: AssignmentRepository = AssignmentRepository(),
    private val announcementRepository: CourseAnnouncementRepository = CourseAnnouncementRepository()
) : ViewModel() {

    private val _currentCourseCode = MutableStateFlow("")
    val currentCourseCode: StateFlow<String> = _currentCourseCode.asStateFlow()

    private val _materialsState = MutableStateFlow<Resource<List<CourseMaterial>>>(Resource.Empty)
    val materialsState: StateFlow<Resource<List<CourseMaterial>>> = _materialsState.asStateFlow()

    private val _assignmentsState = MutableStateFlow<Resource<List<Assignment>>>(Resource.Empty)
    val assignmentsState: StateFlow<Resource<List<Assignment>>> = _assignmentsState.asStateFlow()

    private val _announcementsState = MutableStateFlow<Resource<List<CourseAnnouncement>>>(Resource.Empty)
    val announcementsState: StateFlow<Resource<List<CourseAnnouncement>>> = _announcementsState.asStateFlow()

    private fun isDemoSession(): Boolean {
        return BuildConfig.DEBUG &&
                (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid == DemoCampusData.DEMO_STUDENT_UID
    }

    fun loadCourseLmsData(courseCode: String) {
        val trimmedCode = courseCode.trim()
        _currentCourseCode.value = trimmedCode

        if (trimmedCode.isBlank()) {
            _materialsState.value = Resource.Empty
            _assignmentsState.value = Resource.Empty
            _announcementsState.value = Resource.Empty
            return
        }

        if (isDemoSession()) {
            val mats = DemoCampusData.demoMaterials[trimmedCode] ?: emptyList()
            val asgns = DemoCampusData.demoAssignments[trimmedCode] ?: emptyList()
            val anns = DemoCampusData.demoCourseAnnouncements[trimmedCode] ?: emptyList()

            _materialsState.value = if (mats.isEmpty()) Resource.Empty else Resource.Success(mats)
            _assignmentsState.value = if (asgns.isEmpty()) Resource.Empty else Resource.Success(asgns)
            _announcementsState.value = if (anns.isEmpty()) Resource.Empty else Resource.Success(anns)
            return
        }

        viewModelScope.launch {
            _materialsState.value = Resource.Loading
            _assignmentsState.value = Resource.Loading
            _announcementsState.value = Resource.Loading

            val materialsDeferred = async { materialRepository.getMaterialsForCourse(trimmedCode) }
            val assignmentsDeferred = async { assignmentRepository.getAssignmentsForCourse(trimmedCode) }
            val announcementsDeferred = async { announcementRepository.getAnnouncementsForCourse(trimmedCode) }

            val materialsResult = materialsDeferred.await()
            val assignmentsResult = assignmentsDeferred.await()
            val announcementsResult = announcementsDeferred.await()

            materialsResult.fold(
                onSuccess = { list ->
                    _materialsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _materialsState.value = Resource.Error(error.message ?: "Failed to load course materials.")
                }
            )

            assignmentsResult.fold(
                onSuccess = { list ->
                    _assignmentsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _assignmentsState.value = Resource.Error(error.message ?: "Failed to load assignments.")
                }
            )

            announcementsResult.fold(
                onSuccess = { list ->
                    _announcementsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _announcementsState.value = Resource.Error(error.message ?: "Failed to load course announcements.")
                }
            )
        }
    }

    fun refreshMaterials() {
        val code = _currentCourseCode.value
        if (code.isBlank()) return
        if (isDemoSession()) {
            val mats = DemoCampusData.demoMaterials[code] ?: emptyList()
            _materialsState.value = if (mats.isEmpty()) Resource.Empty else Resource.Success(mats)
            return
        }
        viewModelScope.launch {
            _materialsState.value = Resource.Loading
            materialRepository.getMaterialsForCourse(code).fold(
                onSuccess = { list ->
                    _materialsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _materialsState.value = Resource.Error(error.message ?: "Failed to load course materials.")
                }
            )
        }
    }

    fun refreshAssignments() {
        val code = _currentCourseCode.value
        if (code.isBlank()) return
        if (isDemoSession()) {
            val asgns = DemoCampusData.demoAssignments[code] ?: emptyList()
            _assignmentsState.value = if (asgns.isEmpty()) Resource.Empty else Resource.Success(asgns)
            return
        }
        viewModelScope.launch {
            _assignmentsState.value = Resource.Loading
            assignmentRepository.getAssignmentsForCourse(code).fold(
                onSuccess = { list ->
                    _assignmentsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _assignmentsState.value = Resource.Error(error.message ?: "Failed to load assignments.")
                }
            )
        }
    }

    fun refreshAnnouncements() {
        val code = _currentCourseCode.value
        if (code.isBlank()) return
        if (isDemoSession()) {
            val anns = DemoCampusData.demoCourseAnnouncements[code] ?: emptyList()
            _announcementsState.value = if (anns.isEmpty()) Resource.Empty else Resource.Success(anns)
            return
        }
        viewModelScope.launch {
            _announcementsState.value = Resource.Loading
            announcementRepository.getAnnouncementsForCourse(code).fold(
                onSuccess = { list ->
                    _announcementsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _announcementsState.value = Resource.Error(error.message ?: "Failed to load course announcements.")
                }
            )
        }
    }
}
