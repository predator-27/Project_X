package com.projectx.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.data.firestore.AppointmentRepository
import com.projectx.app.model.Appointment
import com.projectx.app.model.AppointmentStatus
import com.projectx.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(
    private val appointmentRepository: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    private val _studentAppointmentsState = MutableStateFlow<Resource<List<Appointment>>>(Resource.Empty)
    val studentAppointmentsState: StateFlow<Resource<List<Appointment>>> = _studentAppointmentsState.asStateFlow()

    private val _facultyAppointmentsState = MutableStateFlow<Resource<List<Appointment>>>(Resource.Empty)
    val facultyAppointmentsState: StateFlow<Resource<List<Appointment>>> = _facultyAppointmentsState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    fun loadStudentAppointments(studentUid: String) {
        if (studentUid.isBlank()) {
            _studentAppointmentsState.value = Resource.Empty
            return
        }

        viewModelScope.launch {
            _studentAppointmentsState.value = Resource.Loading
            appointmentRepository.getStudentAppointments(studentUid).fold(
                onSuccess = { list ->
                    _studentAppointmentsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _studentAppointmentsState.value = Resource.Error(error.message ?: "Failed to load appointments.")
                }
            )
        }
    }

    fun loadFacultyAppointments(facultyUid: String) {
        if (facultyUid.isBlank()) {
            _facultyAppointmentsState.value = Resource.Empty
            return
        }

        viewModelScope.launch {
            _facultyAppointmentsState.value = Resource.Loading
            appointmentRepository.getFacultyAppointments(facultyUid).fold(
                onSuccess = { list ->
                    _facultyAppointmentsState.value = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                },
                onFailure = { error ->
                    _facultyAppointmentsState.value = Resource.Error(error.message ?: "Failed to load faculty appointment requests.")
                }
            )
        }
    }

    fun bookAppointment(
        appointment: Appointment,
        studentUid: String
    ) {
        if (studentUid.isBlank() || appointment.teacherId.isBlank()) {
            _actionState.value = Resource.Error("Authentication error: Missing student or faculty UID.")
            return
        }

        if (appointment.date.isBlank() || appointment.timeSlot.isBlank() || appointment.purpose.isBlank()) {
            _actionState.value = Resource.Error("Please fill in all appointment details (date, time slot, and discussion purpose).")
            return
        }

        viewModelScope.launch {
            _actionState.value = Resource.Loading
            appointmentRepository.createAppointment(appointment, studentUid).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadStudentAppointments(studentUid)
                },
                onFailure = { error ->
                    _actionState.value = Resource.Error(error.message ?: "Failed to request appointment.")
                }
            )
        }
    }

    fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: AppointmentStatus,
        studentUid: String? = null,
        facultyUid: String? = null,
        facultyNotes: String? = null
    ) {
        if (appointmentId.isBlank()) return

        viewModelScope.launch {
            _actionState.value = Resource.Loading
            appointmentRepository.updateAppointmentStatus(appointmentId, newStatus, facultyNotes).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    if (!studentUid.isNullOrBlank()) loadStudentAppointments(studentUid)
                    if (!facultyUid.isNullOrBlank()) loadFacultyAppointments(facultyUid)
                },
                onFailure = { error ->
                    _actionState.value = Resource.Error(error.message ?: "Failed to update appointment status.")
                }
            )
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Empty
    }
}
