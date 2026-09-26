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
import com.example.projectx.theme.CampusTheme
import com.example.projectx.theme.CampusTokens
import com.example.projectx.ui.*
import com.example.projectx.ui.academics.AcademicViewModel
import com.example.projectx.ui.academics.CoursesScreen
import kotlinx.coroutines.launch

enum class BottomTab(val id: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    TIMETABLE("timetable", "Timetable", Icons.Default.Schedule),
    MAP("campus_map", "Campus Map", Icons.Default.Map),
    MESSAGES("messages", "Messages", Icons.Default.Email),
    PROFILE("profile", "Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TeacherManagementViewModel by viewModels()
    private val aiViewModel: CampusAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusTheme {
                var splashDone by rememberSaveable { mutableStateOf(false) }
                if (!splashDone) {
                    // Splash draws its own background — keep it full-bleed.
                    SplashScreen(onFinished = { splashDone = true })
                } else {
                    // Ambient layer sits behind every screen so the whole app
                    // shares the splash's navy + soft glow palette.
                    AmbientBackground(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = androidx.compose.ui.graphics.Color.Transparent,
                        ) {
                            CampusAppShell(viewModel = viewModel, aiViewModel = aiViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CampusAppShell(
    viewModel: TeacherManagementViewModel,
    aiViewModel: CampusAiViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSavedAuth(context)
        viewModel.checkForAppUpdates(context)
    }

    when (authState) {
        AuthState.UNAUTHENTICATED -> {
            Box(modifier = Modifier.fillMaxSize()) {
                LoginScreen(viewModel = viewModel)

                // In-App Update Banner Overlay
                UpdateNotificationOverlay(
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
        AuthState.STUDENT -> {
            StudentCampusShell(viewModel = viewModel, aiViewModel = aiViewModel)
        }
        AuthState.TEACHER_ADMIN -> {
            AdminTeacherScreen(viewModel = viewModel)
        }
        AuthState.WEB_VIEW -> {
            WebViewScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun StudentCampusShell(
    viewModel: TeacherManagementViewModel,
    aiViewModel: CampusAiViewModel,
    academicViewModel: AcademicViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var activeDrawerModule by remember { mutableStateOf("home") }
    var selectedBottomTab by remember { mutableStateOf(BottomTab.HOME) }
    var showAiAssistantSheet by remember { mutableStateOf(false) }

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
                    onItemClick = { moduleId ->
                        activeDrawerModule = moduleId
                        when (moduleId) {
                            "home" -> selectedBottomTab = BottomTab.HOME
                            "timetable" -> selectedBottomTab = BottomTab.TIMETABLE
                            "messages" -> selectedBottomTab = BottomTab.MESSAGES
                            "attendance" -> selectedBottomTab = BottomTab.HOME
                            "courses" -> selectedBottomTab = BottomTab.HOME
                            "community" -> selectedBottomTab = BottomTab.HOME
                            "teachers" -> selectedBottomTab = BottomTab.HOME
                            "campus_map" -> selectedBottomTab = BottomTab.MAP
                            "gallery" -> selectedBottomTab = BottomTab.MAP
                        }
                        coroutineScope.launch { drawerState.close() }
                    },
                    onLogoutClick = {
                        viewModel.logout(context)
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
                                when (tab) {
                                    BottomTab.HOME -> activeDrawerModule = "home"
                                    BottomTab.TIMETABLE -> activeDrawerModule = "timetable"
                                    BottomTab.MESSAGES -> activeDrawerModule = "messages"
                                    BottomTab.MAP -> activeDrawerModule = "campus_map"
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
                        } else if (activeDrawerModule == "courses") {
                            CoursesScreen(
                                academicViewModel = academicViewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        } else if (activeDrawerModule == "community") {
                            SocialCommunityScreen(
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        } else if (activeDrawerModule == "teachers") {
                            StudentScreen(
                                viewModel = viewModel
                            )
                        } else {
                            HomeScreen(
                                academicViewModel = academicViewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                onNavigateToTab = { tabId ->
                                    when (tabId) {
                                        "timetable" -> {
                                            selectedBottomTab = BottomTab.TIMETABLE
                                            activeDrawerModule = "timetable"
                                        }
                                        "attendance" -> {
                                            activeDrawerModule = "attendance"
                                        }
                                        "courses" -> {
                                            activeDrawerModule = "courses"
                                        }
                                        "campus_map" -> {
                                            selectedBottomTab = BottomTab.MAP
                                            activeDrawerModule = "campus_map"
                                        }
                                        "messages" -> {
                                            selectedBottomTab = BottomTab.MESSAGES
                                            activeDrawerModule = "messages"
                                        }
                                        "community" -> {
                                            activeDrawerModule = "community"
                                        }
                                    }
                                }
                            )
                        }
                    }
                    BottomTab.TIMETABLE -> {
                        TimetableScreen(
                            academicViewModel = academicViewModel,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } },
                            onNavigateToRoom = {
                                selectedBottomTab = BottomTab.MAP
                                activeDrawerModule = "campus_map"
                            }
                        )
                    }
                    BottomTab.MAP -> {
                        ComponentGalleryScreen(
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    BottomTab.MESSAGES -> {
                        MessagesScreen(
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    BottomTab.PROFILE -> {
                        ProfileScreen(
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                }

                // In-App Update Banner Overlay
                UpdateNotificationOverlay(
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Gemini AI Assistant Bottom Sheet
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
