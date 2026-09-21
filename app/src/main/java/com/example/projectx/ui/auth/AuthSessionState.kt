package com.example.projectx.ui.auth

import com.example.projectx.model.PublicProfile
import com.example.projectx.model.User
import com.example.projectx.model.UserRole

sealed class AuthSessionState {
    object Unauthenticated : AuthSessionState()
    object Loading : AuthSessionState()

    data class EmailVerificationRequired(
        val email: String
    ) : AuthSessionState()

    data class ProfileMissing(
        val uid: String,
        val email: String
    ) : AuthSessionState()

    data class AccountInactive(
        val email: String
    ) : AuthSessionState()

    data class Authenticated(
        val user: User,
        val publicProfile: PublicProfile?
    ) : AuthSessionState() {
        val role: UserRole get() = user.role
        val isActive: Boolean get() = user.isActive
    }

    data class Error(
        val message: String
    ) : AuthSessionState()
}
