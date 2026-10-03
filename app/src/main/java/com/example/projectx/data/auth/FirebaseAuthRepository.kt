package com.projectx.app.data.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import com.projectx.app.util.AuthValidation

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(firebaseAuth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            _isAuthenticated.value = auth.currentUser != null
        }
    }

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val emailError = AuthValidation.getEmailError(email)
        if (emailError != null) {
            return@withContext Result.failure(IllegalArgumentException(emailError))
        }

        val passwordError = AuthValidation.getPasswordError(password)
        if (passwordError != null) {
            return@withContext Result.failure(IllegalArgumentException(passwordError))
        }

        val trimmedEmail = email.trim().lowercase()

        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("Authentication failed: User is null."))
            _currentUser.value = user
            _isAuthenticated.value = true
            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid credentials. Please check your email and password."))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found matching this email address."))
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Sign-in failed. Please check your network connection."
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun registerWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val emailError = AuthValidation.getEmailError(email)
        if (emailError != null) {
            return@withContext Result.failure(IllegalArgumentException(emailError))
        }

        val passwordError = AuthValidation.getPasswordError(password)
        if (passwordError != null) {
            return@withContext Result.failure(IllegalArgumentException(passwordError))
        }

        val trimmedEmail = email.trim().lowercase()

        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("Registration failed: User is null."))

            // Send verification email
            user.sendEmailVerification().await()

            Result.success(user)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account with this Bennett email address already exists."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password does not meet security requirements: Must contain at least 8 characters, 1 capital letter, 1 number, and 1 special character."))
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Registration failed. Please check your network connection."
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun signInWithMicrosoft(activity: Activity): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val provider = OAuthProvider.newBuilder("microsoft.com")
                .addCustomParameter("tenant", "<TENANT_ID>")
                .build()

            val pendingResult = firebaseAuth.pendingAuthResult
            if (pendingResult != null) {
                val authResult = pendingResult.await()
                val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("Microsoft Authentication failed: null user."))
                _currentUser.value = user
                _isAuthenticated.value = true
                return@withContext Result.success(user)
            }

            val authResult = firebaseAuth.startActivityForSignInWithProvider(activity, provider).await()
            val user = authResult.user ?: return@withContext Result.failure(IllegalStateException("Microsoft Authentication failed: null user."))
            _currentUser.value = user
            _isAuthenticated.value = true
            Result.success(user)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Microsoft Sign-In was cancelled or failed."
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun verifyAndRefreshUser(): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("No active user session found."))

        try {
            user.reload().await()
            if (user.isEmailVerified) {
                // Force token refresh to ensure email_verified claim updates in Firebase JWT token for Firestore Rules
                user.getIdToken(true).await()
                Result.success(true)
            } else {
                Result.failure(Exception("Email not verified yet. Please click the link sent to your @bennett.edu.in inbox."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Verification check failed: ${e.localizedMessage}"))
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
        _isAuthenticated.value = false
    }
}
