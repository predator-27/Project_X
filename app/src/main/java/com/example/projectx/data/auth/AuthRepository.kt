package com.projectx.app.data.auth

import android.app.Activity
import com.projectx.app.BuildConfig
import com.projectx.app.data.firestore.PublicProfileRepository
import com.projectx.app.data.firestore.UserRepository
import com.projectx.app.model.AcademicProfile
import com.projectx.app.model.PublicProfile
import com.projectx.app.model.User
import com.projectx.app.model.UserRole
import com.projectx.app.ui.auth.AuthSessionState
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

        val verifyResult = firebaseAuthRepo.verifyAndRefreshUser()
        if (verifyResult.isFailure) {
            val errMessage = verifyResult.exceptionOrNull()?.message
            val state = when (errMessage) {
                "NETWORK_TIMEOUT" -> {
                    AuthSessionState.Unauthenticated
                }
                "EMAIL_NOT_VERIFIED" -> {
                    if (isBypassVerificationUser(currentFirebaseUser)) {
                        loadSessionForFirebaseUser(currentFirebaseUser)
                    } else {
                        AuthSessionState.EmailVerificationRequired(currentFirebaseUser.email ?: "")
                    }
                }
                else -> {
                    if (currentFirebaseUser.isEmailVerified || isBypassVerificationUser(currentFirebaseUser)) {
                        loadSessionForFirebaseUser(currentFirebaseUser)
                    } else {
                        AuthSessionState.Unauthenticated
                    }
                }
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

    private var cachedPhoneNumber: String? = null

    suspend fun register(
        email: String,
        password: String,
        phoneNumber: String? = null
    ): AuthSessionState = withContext(Dispatchers.IO) {
        _sessionState.value = AuthSessionState.Loading
        cachedPhoneNumber = phoneNumber?.trim()

        val authResult = firebaseAuthRepo.registerWithEmailAndPassword(email, password)
        val firebaseUser = authResult.getOrElse { error ->
            val errorState = AuthSessionState.Error(error.message ?: "Registration failed.")
            _sessionState.value = errorState
            return@withContext errorState
        }

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
        val privateUser = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            phoneNumber = cachedPhoneNumber,
            rollNumber = academicProfile?.rollNumber,
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

    fun enterDemoSession(role: UserRole = UserRole.STUDENT) {
        enterRoleDemoSession(role)
    }

    fun enterFacultyDemoSession() {
        enterRoleDemoSession(UserRole.FACULTY)
    }

    fun enterRoleDemoSession(role: UserRole) {
        if (!BuildConfig.DEBUG) return

        val (user, profile) = buildDemoIdentity(role)
        _sessionState.value = AuthSessionState.Authenticated(
            user = user,
            publicProfile = profile
        )
    }

    private fun buildDemoIdentity(role: UserRole): Pair<User, PublicProfile> = when (role) {
        UserRole.STUDENT -> User(
            uid = "demo_student_uid",
            email = "demo.student@projectx.demo",
            rollNumber = "DEMO001",
            role = UserRole.STUDENT,
            isActive = true
        ) to PublicProfile(
            uid = "demo_student_uid",
            displayName = "Demo Student",
            schoolName = "Demo University",
            program = "Undergraduate Program",
            department = "Demo Department",
            specialization = "General",
            admissionYear = 2025,
            currentSemester = 3,
            section = "Demo-A",
            bio = "Development Demo Student Profile"
        )
        UserRole.FACULTY -> User(
            uid = "demo_faculty_uid",
            email = "demo.faculty@projectx.demo",
            rollNumber = "FAC001",
            role = UserRole.FACULTY,
            isActive = true
        ) to PublicProfile(
            uid = "demo_faculty_uid",
            displayName = "Dr. Demo Professor",
            schoolName = "Demo University",
            program = "Computer Science Department",
            department = "Computer Science",
            specialization = "Artificial Intelligence",
            admissionYear = 2020,
            currentSemester = 1,
            section = "Faculty-A",
            bio = "Development Demo Faculty Profile"
        )
        UserRole.LOST_FOUND_STAFF -> User(
            uid = "demo_lostfound_uid",
            email = "demo.lostfound@projectx.demo",
            rollNumber = "LF001",
            role = UserRole.LOST_FOUND_STAFF,
            isActive = true
        ) to PublicProfile(
            uid = "demo_lostfound_uid",
            displayName = "Demo Lost & Found Staff",
            schoolName = "Demo University",
            program = "Campus Services",
            department = "Lost & Found Desk",
            specialization = "Student Services",
            bio = "Development Demo Lost & Found Staff Profile"
        )
        UserRole.COLLEGE_ADMIN -> User(
            uid = "demo_collegeadmin_uid",
            email = "demo.collegeadmin@projectx.demo",
            rollNumber = "CA001",
            role = UserRole.COLLEGE_ADMIN,
            isActive = true
        ) to PublicProfile(
            uid = "demo_collegeadmin_uid",
            displayName = "Demo College Admin",
            schoolName = "Demo University",
            program = "Administration",
            department = "College Office",
            specialization = "Operations",
            bio = "Development Demo College Admin Profile"
        )
        UserRole.SUPER_ADMIN -> User(
            uid = "demo_superadmin_uid",
            email = "demo.superadmin@projectx.demo",
            rollNumber = "SA001",
            role = UserRole.SUPER_ADMIN,
            isActive = true
        ) to PublicProfile(
            uid = "demo_superadmin_uid",
            displayName = "Demo Super Admin",
            schoolName = "Demo University",
            program = "Platform Administration",
            department = "Super Admin Office",
            specialization = "Platform-wide",
            bio = "Development Demo Super Admin Profile"
        )
    }

    suspend fun signInWithMicrosoft(activity: Activity): AuthSessionState = withContext(Dispatchers.IO) {
        _sessionState.value = AuthSessionState.Loading

        val authResult = firebaseAuthRepo.signInWithMicrosoft(activity)
        val firebaseUser = authResult.getOrElse { error ->
            val errorState = AuthSessionState.Error(error.message ?: "Microsoft Authentication failed.")
            _sessionState.value = errorState
            return@withContext errorState
        }

        val state = loadSessionForFirebaseUser(firebaseUser)
        _sessionState.value = state
        state
    }

    private fun isMicrosoftOAuthUser(user: FirebaseUser): Boolean {
        return user.providerData.any { it.providerId == "microsoft.com" }
    }

    private fun isBypassVerificationUser(user: FirebaseUser): Boolean {
        if (isMicrosoftOAuthUser(user)) return true
        if (BuildConfig.DEBUG && user.email?.endsWith(".test@bennett.edu.in") == true) return true
        return false
    }

    private suspend fun loadSessionForFirebaseUser(firebaseUser: FirebaseUser): AuthSessionState {
        // Check email verification status for password accounts (Microsoft M365 accounts & DEBUG test accounts are pre-verified)
        if (!firebaseUser.isEmailVerified && !isBypassVerificationUser(firebaseUser)) {
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
