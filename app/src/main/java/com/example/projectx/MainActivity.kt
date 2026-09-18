package com.example.projectx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.SideNavDrawerContent
import com.example.projectx.theme.CampusTheme
import com.example.projectx.theme.HeadingNavy
import com.example.projectx.theme.PrimaryBlue
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CampusAppShell(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CampusAppShell(viewModel: TeacherManagementViewModel) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var activeDrawerModule by remember { mutableStateOf("gallery") }
    var selectedBottomTab by remember { mutableStateOf(BottomTab.HOME) }

    LaunchedEffect(Unit) {
        viewModel.checkForAppUpdates(context)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SideNavDrawerContent(
                    activeItemId = activeDrawerModule,
                    onItemClick = { moduleId ->
                        activeDrawerModule = moduleId
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
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    BottomTab.entries.forEach { tab ->
                        val isSelected = selectedBottomTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedBottomTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
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
                // Show Component Gallery as default showcase for Step 1
                when (authState) {
                    AuthState.UNAUTHENTICATED -> {
                        ComponentGalleryScreen(
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }
                    AuthState.TEACHER_ADMIN -> AdminTeacherScreen(viewModel = viewModel)
                    AuthState.STUDENT -> StudentScreen(viewModel = viewModel)
                    AuthState.WEB_VIEW -> WebViewScreen(viewModel = viewModel)
                }

                // In-App Update Banner Overlay
                UpdateNotificationOverlay(
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
