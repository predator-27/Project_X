package com.projectx.app.ui.hostel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.auth.AuthRepository
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.model.hostel.*
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HostelViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _selectedDateState = MutableStateFlow("2026-10-04")
    val selectedDateState: StateFlow<String> = _selectedDateState.asStateFlow()

    private val _mealsState = MutableStateFlow<Resource<List<DiningMeal>>>(Resource.Loading)
    val mealsState: StateFlow<Resource<List<DiningMeal>>> = _mealsState.asStateFlow()

    private val _roomPartnerState = MutableStateFlow<Resource<RoomPartnerRequest>>(Resource.Loading)
    val roomPartnerState: StateFlow<Resource<RoomPartnerRequest>> = _roomPartnerState.asStateFlow()

    private val _leavePassesState = MutableStateFlow<Resource<List<HostelLeavePass>>>(Resource.Loading)
    val leavePassesState: StateFlow<Resource<List<HostelLeavePass>>> = _leavePassesState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    init {
        loadHostelData()
    }

    private fun isDemoSession(): Boolean {
        val sessionUid = (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid
        return BuildConfig.DEBUG && sessionUid == DemoCampusData.DEMO_STUDENT_UID
    }

    fun loadHostelData() {
        loadMeals()
        loadRoomPartnerRequest()
        loadLeavePasses()
    }

    fun setSelectedDate(date: String) {
        _selectedDateState.value = date
        loadMeals(date)
    }

    fun loadMeals(date: String = _selectedDateState.value) {
        viewModelScope.launch {
            _mealsState.value = Resource.Loading
            val filteredMeals = DemoCampusData.demoDiningMeals.filter { it.date == date }
            _mealsState.value = Resource.Success(filteredMeals)
        }
    }

    fun loadRoomPartnerRequest() {
        viewModelScope.launch {
            _roomPartnerState.value = Resource.Loading
            if (isDemoSession()) {
                _roomPartnerState.value = Resource.Success(DemoCampusData.demoRoomPartnerRequest)
                return@launch
            }
            _roomPartnerState.value = Resource.Success(DemoCampusData.demoRoomPartnerRequest)
        }
    }

    fun selectRoomTypeAndPartner(roomType: String, partnerRollNo: String?, partnerName: String?) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val currentIdx = DemoCampusData.demoRoomPartnerRequest.requestId.removePrefix("room_demo_").toIntOrNull() ?: 1
            val nextReqId = "room_demo_${currentIdx + 1}"
            val newReq = RoomPartnerRequest(
                requestId = nextReqId,
                studentUid = DemoCampusData.DEMO_STUDENT_UID,
                partnerRollNo = partnerRollNo?.trim()?.ifBlank { null },
                partnerName = partnerName?.trim()?.ifBlank { null },
                roomType = roomType,
                status = if (partnerRollNo.isNullOrBlank()) "NO_REQUEST" else "REQUEST_SENT",
                submittedAt = 1789900800000L
            )
            DemoCampusData.demoRoomPartnerRequest = newReq
            _roomPartnerState.value = Resource.Success(newReq)
            _actionState.value = Resource.Success(Unit)
        }
    }

    fun loadLeavePasses() {
        viewModelScope.launch {
            _leavePassesState.value = Resource.Loading
            if (isDemoSession()) {
                _leavePassesState.value = Resource.Success(DemoCampusData.demoLeavePasses.toList())
                return@launch
            }
            _leavePassesState.value = Resource.Success(DemoCampusData.demoLeavePasses.toList())
        }
    }

    fun applyLeavePass(leaveType: String, startDate: String, endDate: String, reason: String) {
        val cleanStart = startDate.trim()
        val cleanEnd = endDate.trim()
        val cleanReason = reason.trim()

        if (cleanStart.isBlank() || cleanEnd.isBlank() || cleanReason.isBlank()) {
            _actionState.value = Resource.Error("Please fill in start date, end date, and reason for leave.")
            return
        }

        val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        if (!dateRegex.matches(cleanStart) || !dateRegex.matches(cleanEnd)) {
            _actionState.value = Resource.Error("Please enter dates in YYYY-MM-DD format.")
            return
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val startParsed = sdf.parse(cleanStart)
            val endParsed = sdf.parse(cleanEnd)

            if (startParsed == null || endParsed == null) {
                _actionState.value = Resource.Error("Please enter dates in YYYY-MM-DD format.")
                return
            }

            if (endParsed.before(startParsed)) {
                _actionState.value = Resource.Error("End date cannot be earlier than start date.")
                return
            }
        } catch (e: Exception) {
            _actionState.value = Resource.Error("Please enter dates in YYYY-MM-DD format.")
            return
        }

        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val maxIndex = DemoCampusData.demoLeavePasses
                .mapNotNull { it.leaveId.removePrefix("leave_demo_").toIntOrNull() }
                .maxOrNull() ?: 0
            val nextLeaveId = "leave_demo_${maxIndex + 1}"
            val newPass = HostelLeavePass(
                leaveId = nextLeaveId,
                studentUid = DemoCampusData.DEMO_STUDENT_UID,
                leaveType = leaveType,
                startDate = cleanStart,
                endDate = cleanEnd,
                reason = cleanReason,
                status = "SUBMITTED",
                approvedBy = "Pending Warden Review",
                appliedAt = 1789900800000L
            )
            DemoCampusData.demoLeavePasses.add(0, newPass)
            _actionState.value = Resource.Success(Unit)
            loadLeavePasses()
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Empty
    }
}
