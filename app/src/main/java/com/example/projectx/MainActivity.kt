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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.SideNavDrawerContent
import com.example.projectx.theme.CampusTheme
import com.example.projectx.theme.HeadingNavy
import com.example.projectx.theme.NavySidebar
import com.example.projectx.theme.PrimaryIndigo
import com.example.projectx.ui.*
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CampusAppShell(viewModel = viewModel, aiViewModel = aiViewModel)
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var activeDrawerModule by remember { mutableStateOf("home") }
    var selectedBottomTab by remember { mutableStateOf(BottomTab.HOME) }
    var showAiAssistantSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkForAppUpdates(context)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NavySidebar,
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
                            "campus_map" -> selectedBottomTab = BottomTab.MAP
                            "gallery" -> selectedBottomTab = BottomTab.MAP
                        }
                        coroutineScope.launch { drawerState.close() }
                    },
                    onLogoutClick = {
                        viewModel.logout()
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
                    containerColor = PrimaryIndigo,
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
                                    tint = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) HeadingNavy else MaterialTheme.colorScheme.onSurfaceVariant
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
                if (authState == AuthState.UNAUTHENTICATED) {
                    when (selectedBottomTab) {
                        BottomTab.HOME -> {
                            if (activeDrawerModule == "attendance") {
                                AttendanceScreen(
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                            } else if (activeDrawerModule == "community") {
                                SocialCommunityScreen(
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                            } else {
                                HomeScreen(
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
                } else {
                    when (authState) {
                        AuthState.TEACHER_ADMIN -> AdminTeacherScreen(viewModel = viewModel)
                        AuthState.STUDENT -> StudentScreen(viewModel = viewModel)
                        AuthState.WEB_VIEW -> WebViewScreen(viewModel = viewModel)
                        else -> {}
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
