package com.projectx.app.ui.lms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.auth.AuthRepository
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.data.firestore.AssignmentRepository
import com.projectx.app.model.lms.Assignment
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssignmentDetailViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val assignmentRepository: AssignmentRepository = AssignmentRepository()
) : ViewModel() {

    private val _currentAssignmentId = MutableStateFlow("")
    val currentAssignmentId: StateFlow<String> = _currentAssignmentId.asStateFlow()

    private val _assignmentState = MutableStateFlow<Resource<Assignment>>(Resource.Empty)
    val assignmentState: StateFlow<Resource<Assignment>> = _assignmentState.asStateFlow()

    private fun isDemoSession(): Boolean {
        return BuildConfig.DEBUG &&
                (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid == DemoCampusData.DEMO_STUDENT_UID
    }

    fun loadAssignment(assignmentId: String) {
        val trimmedId = assignmentId.trim()
        _currentAssignmentId.value = trimmedId

        if (trimmedId.isBlank()) {
            _assignmentState.value = Resource.Empty
            return
        }

        if (isDemoSession()) {
            val demoAsgn = DemoCampusData.demoAssignments.values.flatten().find { it.assignmentId == trimmedId }
            if (demoAsgn != null) {
                _assignmentState.value = Resource.Success(demoAsgn)
                return
            }
        }

        viewModelScope.launch {
            _assignmentState.value = Resource.Loading
            assignmentRepository.getAssignment(trimmedId).fold(
                onSuccess = { assignment ->
                    _assignmentState.value = if (assignment != null) {
                        Resource.Success(assignment)
                    } else {
                        Resource.Empty
                    }
                },
                onFailure = { error ->
                    _assignmentState.value = Resource.Error(error.message ?: "Failed to load assignment details.")
                }
            )
        }
    }

    fun refreshAssignment() {
        val id = _currentAssignmentId.value
        if (id.isNotBlank()) {
            loadAssignment(id)
        }
    }
}
