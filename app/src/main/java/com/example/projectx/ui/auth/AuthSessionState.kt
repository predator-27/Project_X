package com.projectx.app.ui.auth

import com.projectx.app.model.PublicProfile
import com.projectx.app.model.User
import com.projectx.app.model.UserRole

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
