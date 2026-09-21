package com.example.projectx.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectx.data.auth.AuthRepository
import com.example.projectx.model.AcademicProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val sessionState: StateFlow<AuthSessionState> = authRepository.sessionState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val state = authRepository.restoreSession()
            if (state is AuthSessionState.Error) {
                _errorMessage.value = state.message
            }
            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val state = authRepository.signIn(email, password)
            if (state is AuthSessionState.Error) {
                _errorMessage.value = state.message
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val state = authRepository.register(email, password)
            if (state is AuthSessionState.Error) {
                _errorMessage.value = state.message
            }
            _isLoading.value = false
        }
    }

    fun completeRegistrationAfterVerification(
        displayName: String,
        academicProfile: AcademicProfile? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val state = authRepository.completeRegistrationAfterVerification(displayName, academicProfile)
            if (state is AuthSessionState.Error) {
                _errorMessage.value = state.message
            }
            _isLoading.value = false
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val state = authRepository.reloadVerificationAndCheckState()
            if (state is AuthSessionState.Error) {
                _errorMessage.value = state.message
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _errorMessage.value = null
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
