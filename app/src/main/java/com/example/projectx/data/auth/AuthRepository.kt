package com.example.projectx.data.auth

import com.example.projectx.data.firestore.PublicProfileRepository
import com.example.projectx.data.firestore.UserRepository
import com.example.projectx.model.AcademicProfile
import com.example.projectx.model.PublicProfile
import com.example.projectx.model.User
import com.example.projectx.model.UserRole
import com.example.projectx.ui.auth.AuthSessionState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val firebaseAuthRepo: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val publicProfileRepository: PublicProfileRepository = PublicProfileRepository()
) {

    private val _sessionState = MutableStateFlow<AuthSessionState>(AuthSessionState.Unauthenticated)
    val sessionState: StateFlow<AuthSessionState> = _sessionState.asStateFlow()

    suspend fun restoreSession(): AuthSessionState = withContext(Dispatchers.IO) {
        _sessionState.value = AuthSessionState.Loading
        val currentFirebaseUser = firebaseAuthRepo.currentUser.value

        if (currentFirebaseUser == null) {
            val state = AuthSessionState.Unauthenticated
            _sessionState.value = state
            return@withContext state
        }

        // Reload user and force-refresh token claim (getIdToken(true))
        val verifyResult = firebaseAuthRepo.verifyAndRefreshUser()
        if (verifyResult.isFailure) {
            val reloadedUser = firebaseAuthRepo.currentUser.value ?: currentFirebaseUser
            val state = if (!reloadedUser.isEmailVerified) {
                AuthSessionState.EmailVerificationRequired(reloadedUser.email ?: "")
            } else {
                AuthSessionState.Error(verifyResult.exceptionOrNull()?.message ?: "Session restoration failed.")
            }
            _sessionState.value = state
            return@withContext state
        }

        val reloadedUser = firebaseAuthRepo.currentUser.value ?: currentFirebaseUser
        val state = loadSessionForFirebaseUser(reloadedUser)
        _sessionState.value = state
        state
    }

    suspend fun signIn(email: String, password: String): AuthSessionState = withContext(Dispatchers.IO) {
        _sessionState.value = AuthSessionState.Loading

        val authResult = firebaseAuthRepo.signInWithEmailAndPassword(email, password)
        val firebaseUser = authResult.getOrElse { error ->
            val errorState = AuthSessionState.Error(error.message ?: "Authentication failed.")
            _sessionState.value = errorState
            return@withContext errorState
        }

        val state = loadSessionForFirebaseUser(firebaseUser)
        _sessionState.value = state
        state
    }

    suspend fun register(
        email: String,
        password: String
    ): AuthSessionState = withContext(Dispatchers.IO) {
        _sessionState.value = AuthSessionState.Loading

        val authResult = firebaseAuthRepo.registerWithEmailAndPassword(email, password)
        val firebaseUser = authResult.getOrElse { error ->
            val errorState = AuthSessionState.Error(error.message ?: "Registration failed.")
            _sessionState.value = errorState
            return@withContext errorState
        }

        // Firebase Auth account created & verification email sent.
        // Do NOT create /users/{uid} or /public_profiles/{uid} before verification!
        val state = AuthSessionState.EmailVerificationRequired(firebaseUser.email ?: email)
        _sessionState.value = state
        state
    }

    suspend fun completeRegistrationAfterVerification(
        displayName: String,
        academicProfile: AcademicProfile? = null
    ): AuthSessionState = withContext(Dispatchers.IO) {
        _sessionState.value = AuthSessionState.Loading

        val verifyResult = firebaseAuthRepo.verifyAndRefreshUser()
        val isVerified = verifyResult.getOrElse { error ->
            val errorState = AuthSessionState.Error(error.message ?: "Verification check failed.")
            _sessionState.value = errorState
            return@withContext errorState
        }

        if (!isVerified) {
            val user = firebaseAuthRepo.currentUser.value
            val state = AuthSessionState.EmailVerificationRequired(user?.email ?: "")
            _sessionState.value = state
            return@withContext state
        }

        val firebaseUser = firebaseAuthRepo.currentUser.value
            ?: run {
                val state = AuthSessionState.Unauthenticated
                _sessionState.value = state
                return@withContext state
            }

        // Step 1: Create private user account document (/users/{uid})
        // Enforces role = STUDENT and isActive = true for self-registration
        val privateUser = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            role = UserRole.STUDENT,
            isActive = true
        )

        val createPrivateResult = userRepository.createPrivateAccount(privateUser)
        createPrivateResult.onFailure { error ->
            val errorState = AuthSessionState.Error("Failed to initialize account: ${error.message}")
            _sessionState.value = errorState
            return@withContext errorState
        }

        // Step 2: Create public profile document (/public_profiles/{uid})
        val publicProfile = PublicProfile(
            uid = firebaseUser.uid,
            displayName = displayName.ifBlank { firebaseUser.email?.substringBefore("@") ?: "Student" },
            schoolName = academicProfile?.schoolName,
            program = academicProfile?.program,
            department = academicProfile?.department,
            specialization = academicProfile?.specialization,
            admissionYear = academicProfile?.admissionYear,
            currentSemester = academicProfile?.currentSemester,
            section = academicProfile?.section
        )

        val createPublicResult = publicProfileRepository.createPublicProfile(publicProfile)
        createPublicResult.onFailure { error ->
            val errorState = AuthSessionState.Error("Account created, but public profile setup failed: ${error.message}. Please retry completing profile setup.")
            _sessionState.value = errorState
            return@withContext errorState
        }

        // Step 3: Load and return authenticated session
        val state = loadSessionForFirebaseUser(firebaseUser)
        _sessionState.value = state
        state
    }

    suspend fun reloadVerificationAndCheckState(): AuthSessionState = withContext(Dispatchers.IO) {
        val firebaseUser = firebaseAuthRepo.currentUser.value
            ?: return@withContext AuthSessionState.Unauthenticated

        val verifyResult = firebaseAuthRepo.verifyAndRefreshUser()
        if (verifyResult.isSuccess) {
            val state = loadSessionForFirebaseUser(firebaseUser)
            _sessionState.value = state
            state
        } else {
            val state = AuthSessionState.EmailVerificationRequired(firebaseUser.email ?: "")
            _sessionState.value = state
            state
        }
    }

    fun signOut() {
        firebaseAuthRepo.signOut()
        _sessionState.value = AuthSessionState.Unauthenticated
    }

    private suspend fun loadSessionForFirebaseUser(firebaseUser: FirebaseUser): AuthSessionState {
        // Check email verification status
        if (!firebaseUser.isEmailVerified) {
            return AuthSessionState.EmailVerificationRequired(firebaseUser.email ?: "")
        }

        // Fetch private account from /users/{uid}
        val privateAccountResult = userRepository.getPrivateAccount(firebaseUser.uid)
        if (privateAccountResult.isFailure) {
            val error = privateAccountResult.exceptionOrNull()
            return AuthSessionState.Error("Failed to read user account: ${error?.message}")
        }

        val privateAccount = privateAccountResult.getOrNull()
        if (privateAccount == null) {
            // Document successfully read, but does NOT exist in Firestore
            return AuthSessionState.ProfileMissing(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: ""
            )
        }

        // Check account activation state
        if (!privateAccount.isActive) {
            return AuthSessionState.AccountInactive(email = privateAccount.email)
        }

        // Fetch public profile from /public_profiles/{uid}
        val publicProfileResult = publicProfileRepository.getPublicProfile(firebaseUser.uid)
        if (publicProfileResult.isFailure) {
            val error = publicProfileResult.exceptionOrNull()
            return AuthSessionState.Error("Failed to read public profile: ${error?.message}")
        }

        val publicProfile = publicProfileResult.getOrNull() // May be null if optional/missing

        return AuthSessionState.Authenticated(
            user = privateAccount,
            publicProfile = publicProfile
        )
    }
}
