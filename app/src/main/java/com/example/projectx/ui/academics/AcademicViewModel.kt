package com.projectx.app.ui.academics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.auth.AuthRepository
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.data.firestore.AttendanceRepository
import com.projectx.app.data.firestore.CourseRepository
import com.projectx.app.data.firestore.PublicProfileRepository
import com.projectx.app.data.firestore.TimetableRepository
import com.projectx.app.model.AttendanceSubject
import com.projectx.app.model.Course
import com.projectx.app.model.PublicProfile
import com.projectx.app.model.TimetableSlot
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcademicViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val timetableRepository: TimetableRepository = TimetableRepository(),
    private val courseRepository: CourseRepository = CourseRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository(),
    private val publicProfileRepository: PublicProfileRepository = PublicProfileRepository()
) : ViewModel() {

    private val _timetableState = MutableStateFlow<Resource<List<TimetableSlot>>>(Resource.Loading)
    val timetableState: StateFlow<Resource<List<TimetableSlot>>> = _timetableState.asStateFlow()

    private val _attendanceState = MutableStateFlow<Resource<List<AttendanceSubject>>>(Resource.Loading)
    val attendanceState: StateFlow<Resource<List<AttendanceSubject>>> = _attendanceState.asStateFlow()

    private val _coursesState = MutableStateFlow<Resource<List<Course>>>(Resource.Loading)
    val coursesState: StateFlow<Resource<List<Course>>> = _coursesState.asStateFlow()

    private val _publicProfileState = MutableStateFlow<PublicProfile?>(null)
    val publicProfileState: StateFlow<PublicProfile?> = _publicProfileState.asStateFlow()

    private val _overallAttendancePercentage = MutableStateFlow(0f)
    val overallAttendancePercentage: StateFlow<Float> = _overallAttendancePercentage.asStateFlow()

    private val _shortageSubjectCount = MutableStateFlow(0)
    val shortageSubjectCount: StateFlow<Int> = _shortageSubjectCount.asStateFlow()

    init {
        loadAcademicData()
    }

    private fun isDemoSession(): Boolean {
        val session = authRepository.sessionState.value
        val isDemoUser = (session as? AuthSessionState.Authenticated)?.user?.uid?.startsWith("demo_") == true || session is AuthSessionState.Unauthenticated
        return BuildConfig.DEBUG && isDemoUser
    }

    fun loadAcademicData() {
        viewModelScope.launch {
            loadUserProfile()
            loadTimetable()
            loadAttendance()
            loadCourses()
        }
    }

    private fun loadUserProfile() {
        val session = authRepository.sessionState.value
        if (session is AuthSessionState.Authenticated) {
            _publicProfileState.value = session.publicProfile
        }
    }

    fun loadTimetable(sectionFilter: String? = null) {
        viewModelScope.launch {
            _timetableState.value = Resource.Loading
            if (isDemoSession()) {
                _timetableState.value = Resource.Success(DemoCampusData.demoTimetable)
                return@launch
            }
            val result = timetableRepository.getTimetableSlots(sectionFilter)
            result.fold(
                onSuccess = { slots ->
                    _timetableState.value = if (slots.isEmpty()) Resource.Success(DemoCampusData.demoTimetable) else Resource.Success(slots)
                },
                onFailure = {
                    _timetableState.value = Resource.Success(DemoCampusData.demoTimetable)
                }
            )
        }
    }

    fun loadAttendance() {
        viewModelScope.launch {
            _attendanceState.value = Resource.Loading
            if (isDemoSession()) {
                val subjects = DemoCampusData.demoAttendance
                _attendanceState.value = Resource.Success(subjects)
                val totalAttended = subjects.sumOf { it.attendedClasses }
                val totalClasses = subjects.sumOf { it.totalClasses }
                val overall = if (totalClasses == 0) 0f else (totalAttended.toFloat() / totalClasses) * 100f
                _overallAttendancePercentage.value = overall
                _shortageSubjectCount.value = subjects.count { it.percentage < 75f }
                return@launch
            }
            val uid = (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid ?: ""
            val result = attendanceRepository.getStudentAttendance(uid)
            result.fold(
                onSuccess = { subjects ->
                    val finalSubjects = if (subjects.isEmpty()) DemoCampusData.demoAttendance else subjects
                    _attendanceState.value = Resource.Success(finalSubjects)
                    val totalAttended = finalSubjects.sumOf { it.attendedClasses }
                    val totalClasses = finalSubjects.sumOf { it.totalClasses }
                    val overall = if (totalClasses == 0) 0f else (totalAttended.toFloat() / totalClasses) * 100f
                    _overallAttendancePercentage.value = overall
                    _shortageSubjectCount.value = finalSubjects.count { it.percentage < 75f }
                },
                onFailure = {
                    val subjects = DemoCampusData.demoAttendance
                    _attendanceState.value = Resource.Success(subjects)
                    val totalAttended = subjects.sumOf { it.attendedClasses }
                    val totalClasses = subjects.sumOf { it.totalClasses }
                    val overall = if (totalClasses == 0) 0f else (totalAttended.toFloat() / totalClasses) * 100f
                    _overallAttendancePercentage.value = overall
                    _shortageSubjectCount.value = subjects.count { it.percentage < 75f }
                }
            )
        }
    }

    fun loadCourses() {
        viewModelScope.launch {
            _coursesState.value = Resource.Loading
            if (isDemoSession()) {
                _coursesState.value = Resource.Success(DemoCampusData.demoCourses)
                return@launch
            }
            val dept = _publicProfileState.value?.department
            val result = courseRepository.getStudentCourses(dept)
            result.fold(
                onSuccess = { courses ->
                    _coursesState.value = if (courses.isEmpty()) Resource.Success(DemoCampusData.demoCourses) else Resource.Success(courses)
                },
                onFailure = {
                    _coursesState.value = Resource.Success(DemoCampusData.demoCourses)
                }
            )
        }
    }
}
