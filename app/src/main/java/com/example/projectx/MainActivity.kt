package com.example.projectx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectx.components.AmbientBackground
import com.example.projectx.components.SideNavDrawerContent
import com.example.projectx.map.MapScreen
import com.example.projectx.model.UserRole
import com.example.projectx.theme.CampusTheme
import com.example.projectx.theme.CampusTokens
import com.example.projectx.ui.*
import com.example.projectx.ui.academics.AcademicViewModel
import com.example.projectx.ui.academics.CoursesScreen
import com.example.projectx.ui.auth.*
import com.example.projectx.ui.lms.AssignmentDetailScreen
import com.example.projectx.ui.lms.CourseDetailScreen
import kotlinx.coroutines.launch

enum class BottomTab(val id: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    ACADEMICS("academics", "Academics", Icons.Default.School),
    CAMPUS("campus", "Campus", Icons.Default.LocationOn),
    MESSAGES("messages", "Messages", Icons.Default.Email),
    PROFILE("profile", "Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val facultyViewModel: FacultyViewModel by viewModels()
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val teacherViewModel: TeacherManagementViewModel by viewModels()
    private val aiViewModel: CampusAiViewModel by viewModels()
    private val announcementViewModel: AnnouncementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusTheme {
                AmbientBackground {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        CampusAppShell(
                            authViewModel = authViewModel,
                            facultyViewModel = facultyViewModel,
                            appointmentViewModel = appointmentViewModel,
                            teacherViewModel = teacherViewModel,
                            aiViewModel = aiViewModel,
                            announcementViewModel = announcementViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CampusAppShell(
    authViewModel: AuthViewModel,
    facultyViewModel: FacultyViewModel,
    appointmentViewModel: AppointmentViewModel,
    teacherViewModel: TeacherManagementViewModel,
    aiViewModel: CampusAiViewModel,
    announcementViewModel: AnnouncementViewModel
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val context = LocalContext.current
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var isWebViewActive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        teacherViewModel.checkForAppUpdates(context)
    }

    if (showSplash) {
        SplashScreen(
            onFinished = { showSplash = false }
        )
    } else if (isWebViewActive) {
        WebViewScreen(viewModel = teacherViewModel)
    } else {
        when (val session = sessionState) {
            is AuthSessionState.Loading -> {
                AuthLoadingScreen(message = "Restoring university session...")
            }
            is AuthSessionState.Unauthenticated -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onOpenWebView = { isWebViewActive = true }
                    )

                    UpdateNotificationOverlay(
                        viewModel = teacherViewModel,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
            is AuthSessionState.EmailVerificationRequired -> {
                EmailVerificationScreen(
                    email = session.email,
                    authViewModel = authViewModel
                )
            }
            is AuthSessionState.ProfileMissing -> {
                CompleteProfileScreen(
                    authViewModel = authViewModel
                )
            }
            is AuthSessionState.AccountInactive -> {
                AccountInactiveScreen(
                    email = session.email,
                    authViewModel = authViewModel
                )
            }
            is AuthSessionState.Error -> {
                AuthErrorScreen(
                    message = session.message,
                    authViewModel = authViewModel
                )
            }
            is AuthSessionState.Authenticated -> {
                when (session.role) {
                    UserRole.STUDENT -> {
                        StudentCampusShell(
                            authViewModel = authViewModel,
                            facultyViewModel = facultyViewModel,
                            appointmentViewModel = appointmentViewModel,
                            teacherViewModel = teacherViewModel,
                            aiViewModel = aiViewModel,
                            announcementViewModel = announcementViewModel
                        )
                    }
                    UserRole.FACULTY -> {
                        AdminTeacherScreen(
                            facultyViewModel = facultyViewModel,
                            appointmentViewModel = appointmentViewModel,
                            authViewModel = authViewModel
                        )
                    }
                    UserRole.LOST_FOUND_STAFF,
                    UserRole.COLLEGE_ADMIN,
                    UserRole.SUPER_ADMIN -> {
                        RolePlaceholderScreen(
                            role = session.role,
                            userEmail = session.user.email,
                            displayName = session.publicProfile?.displayName,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCampusShell(
    authViewModel: AuthViewModel,
    facultyViewModel: FacultyViewModel,
    appointmentViewModel: AppointmentViewModel,
    teacherViewModel: TeacherManagementViewModel,
    aiViewModel: CampusAiViewModel,
    announcementViewModel: AnnouncementViewModel,
    academicViewModel: AcademicViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val session = authViewModel.sessionState.collectAsState().value as? AuthSessionState.Authenticated
    val studentDisplayName = session?.publicProfile?.displayName
    val studentRollNumber = session?.user?.rollNumber
    val studentEmail = session?.user?.email

    var activeDrawerModule by remember { mutableStateOf("home") }
    var selectedBottomTab by remember { mutableStateOf(BottomTab.HOME) }
    var showAiAssistantSheet by remember { mutableStateOf(false) }

    // Map Target Seat Handoff State
    var targetMapSeatId by remember { mutableStateOf<String?>(null) }

    // LMS Sub-Navigation State
    var selectedCourseCode by remember { mutableStateOf<String?>(null) }
    var selectedCourseName by remember { mutableStateOf<String?>(null) }
    var selectedAssignmentId by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CampusTokens.colors.sidebar,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(320.dp)
            ) {
                SideNavDrawerContent(
                    activeItemId = activeDrawerModule,
                    displayName = studentDisplayName,
                    rollNumber = studentRollNumber,
                    email = studentEmail,
                    onItemClick = { moduleId ->
                        activeDrawerModule = moduleId
                        // Reset sub-navigation when switching main modules
                        selectedCourseCode = null
                        selectedCourseName = null
                        selectedAssignmentId = null

                        when (moduleId) {
                            "home" -> selectedBottomTab = BottomTab.HOME
                            "courses", "attendance", "timetable", "assignments", "exam_schedules", "results", "reports" -> selectedBottomTab = BottomTab.ACADEMICS
                            "teachers", "campus_map", "appointments", "lost_found" -> selectedBottomTab = BottomTab.CAMPUS
                            "messages", "announcements", "community" -> selectedBottomTab = BottomTab.MESSAGES
                            "profile", "settings" -> selectedBottomTab = BottomTab.PROFILE
                            "ai_assistant" -> {
                                showAiAssistantSheet = true
                            }
                        }
                        coroutineScope.launch { drawerState.close() }
                    },
                    onLogoutClick = {
                        authViewModel.signOut()
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("✨ Ask AI Tutor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Tutor", tint = Color.White) },
                    onClick = { showAiAssistantSheet = true },
                    containerColor = CampusTokens.colors.primary,
                    shape = RoundedCornerShape(999.dp)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    BottomTab.entries.forEach { tab ->
                        val isSelected = selectedBottomTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedBottomTab = tab
                                selectedCourseCode = null
                                selectedCourseName = null
                                selectedAssignmentId = null
                                when (tab) {
                                    BottomTab.HOME -> activeDrawerModule = "home"
                                    BottomTab.ACADEMICS -> activeDrawerModule = "courses"
                                    BottomTab.CAMPUS -> activeDrawerModule = "teachers"
                                    BottomTab.MESSAGES -> activeDrawerModule = "messages"
                                    BottomTab.PROFILE -> activeDrawerModule = "profile"
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) CampusTokens.colors.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) CampusTokens.colors.heading else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedBottomTab) {
                    BottomTab.HOME -> {
                        if (activeDrawerModule == "attendance") {
                            AttendanceScreen(
                                academicViewModel = academicViewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        } else if (activeDrawerModule == "timetable") {
                            TimetableScreen(
                                academicViewModel = academicViewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onNavigateToRoom = {
                                    selectedBottomTab = BottomTab.CAMPUS
                                    activeDrawerModule = "campus_map"
                                }
                            )
                        } else if (activeDrawerModule == "courses" || activeDrawerModule == "assignments") {
                            if (selectedAssignmentId != null) {
                                AssignmentDetailScreen(
                                    assignmentId = selectedAssignmentId!!,
                                    onBackClick = { selectedAssignmentId = null },
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                            } else if (selectedCourseCode != null) {
                                CourseDetailScreen(
                                    courseCode = selectedCourseCode!!,
                                    courseName = selectedCourseName,
                                    onBackClick = { selectedCourseCode = null; selectedCourseName = null },
                                    onAssignmentClick = { assignmentId -> selectedAssignmentId = assignmentId },
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                            } else {
                                CoursesScreen(
                                    academicViewModel = academicViewModel,
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                    onCourseClick = { course ->
                                        selectedCourseCode = course.courseCode
                                        selectedCourseName = course.courseName
                                    }
                                )
                            }
                        } else if (activeDrawerModule == "community") {
                            SocialCommunityScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        } else if (activeDrawerModule == "teachers" || activeDrawerModule == "appointments") {
                            StudentScreen(
                                facultyViewModel = facultyViewModel,
                                appointmentViewModel = appointmentViewModel,
                                authViewModel = authViewModel,
                                onLocateOnMap = { seatId ->
                                    targetMapSeatId = seatId
                                    selectedBottomTab = BottomTab.CAMPUS
                                    activeDrawerModule = "campus_map"
                                }
                            )
                        } else if (activeDrawerModule == "campus_map") {
                            MapScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                targetSeatId = targetMapSeatId
                            )
                        } else if (activeDrawerModule == "lost_found") {
                            LostFoundScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                authViewModel = authViewModel
                            )
                        } else if (activeDrawerModule == "messages" || activeDrawerModule == "announcements") {
                            MessagesScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                announcementViewModel = announcementViewModel,
                                authViewModel = authViewModel
                            )
                        } else if (activeDrawerModule == "profile") {
                            ProfileScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        } else {
                            HomeScreen(
                                academicViewModel = academicViewModel,
                                appointmentViewModel = appointmentViewModel,
                                announcementViewModel = announcementViewModel,
                                authViewModel = authViewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onNavigateToTab = { tabId ->
                                    when (tabId) {
                                        "timetable" -> {
                                            selectedBottomTab = BottomTab.ACADEMICS
                                            activeDrawerModule = "timetable"
                                        }
                                        "attendance" -> {
                                            selectedBottomTab = BottomTab.ACADEMICS
                                            activeDrawerModule = "attendance"
                                        }
                                        "courses" -> {
                                            selectedBottomTab = BottomTab.ACADEMICS
                                            activeDrawerModule = "courses"
                                            selectedCourseCode = null
                                            selectedCourseName = null
                                            selectedAssignmentId = null
                                        }
                                        "teachers" -> {
                                            selectedBottomTab = BottomTab.CAMPUS
                                            activeDrawerModule = "teachers"
                                        }
                                        "campus_map" -> {
                                            selectedBottomTab = BottomTab.CAMPUS
                                            activeDrawerModule = "campus_map"
                                        }
                                        "lost_found" -> {
                                            selectedBottomTab = BottomTab.CAMPUS
                                            activeDrawerModule = "lost_found"
                                        }
                                        "messages" -> {
                                            selectedBottomTab = BottomTab.MESSAGES
                                            activeDrawerModule = "messages"
                                        }
                                        "community" -> {
                                            selectedBottomTab = BottomTab.MESSAGES
                                            activeDrawerModule = "community"
                                        }
                                    }
                                }
                            )
                        }
                    }
                    BottomTab.ACADEMICS -> {
                        if (activeDrawerModule == "attendance") {
                            AttendanceScreen(
                                academicViewModel = academicViewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        } else if (activeDrawerModule == "timetable") {
                            TimetableScreen(
                                academicViewModel = academicViewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onNavigateToRoom = {
                                    selectedBottomTab = BottomTab.CAMPUS
                                    activeDrawerModule = "campus_map"
                                }
                            )
                        } else {
                            if (selectedAssignmentId != null) {
                                AssignmentDetailScreen(
                                    assignmentId = selectedAssignmentId!!,
                                    onBackClick = { selectedAssignmentId = null },
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                            } else if (selectedCourseCode != null) {
                                CourseDetailScreen(
                                    courseCode = selectedCourseCode!!,
                                    courseName = selectedCourseName,
                                    onBackClick = { selectedCourseCode = null; selectedCourseName = null },
                                    onAssignmentClick = { assignmentId -> selectedAssignmentId = assignmentId },
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                            } else {
                                CoursesScreen(
                                    academicViewModel = academicViewModel,
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                    onCourseClick = { course ->
                                        selectedCourseCode = course.courseCode
                                        selectedCourseName = course.courseName
                                    }
                                )
                            }
                        }
                    }
                    BottomTab.CAMPUS -> {
                        if (activeDrawerModule == "campus_map") {
                            MapScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                targetSeatId = targetMapSeatId
                            )
                        } else if (activeDrawerModule == "lost_found") {
                            LostFoundScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                authViewModel = authViewModel
                            )
                        } else {
                            StudentScreen(
                                facultyViewModel = facultyViewModel,
                                appointmentViewModel = appointmentViewModel,
                                authViewModel = authViewModel,
                                onLocateOnMap = { seatId ->
                                    targetMapSeatId = seatId
                                    selectedBottomTab = BottomTab.CAMPUS
                                    activeDrawerModule = "campus_map"
                                }
                            )
                        }
                    }
                    BottomTab.MESSAGES -> {
                        if (activeDrawerModule == "community") {
                            SocialCommunityScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        } else {
                            MessagesScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                announcementViewModel = announcementViewModel,
                                authViewModel = authViewModel
                            )
                        }
                    }
                    BottomTab.PROFILE -> {
                        ProfileScreen(
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                }

                UpdateNotificationOverlay(
                    viewModel = teacherViewModel,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                if (showAiAssistantSheet) {
                    CampusAiAssistantSheet(
                        aiViewModel = aiViewModel,
                        onDismiss = { showAiAssistantSheet = false }
                    )
                }
            }
        }
    }
}
