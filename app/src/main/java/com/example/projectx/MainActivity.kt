package com.example.projectx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.projectx.ui.AdminTeacherScreen
import com.example.projectx.ui.AuthState
import com.example.projectx.ui.LoginScreen
import com.example.projectx.ui.StudentScreen
import com.example.projectx.ui.TeacherManagementViewModel
import com.example.projectx.ui.UpdateNotificationOverlay
import com.example.projectx.ui.WebViewScreen
import com.example.projectx.ui.theme.ProjectXTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TeacherManagementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: TeacherManagementViewModel) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkForAppUpdates(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (authState) {
            AuthState.UNAUTHENTICATED -> LoginScreen(viewModel = viewModel)
            AuthState.TEACHER_ADMIN -> AdminTeacherScreen(viewModel = viewModel)
            AuthState.STUDENT -> StudentScreen(viewModel = viewModel)
            AuthState.WEB_VIEW -> WebViewScreen(viewModel = viewModel)
        }

        // Floating In-App Update Notification Banner Overlay
        UpdateNotificationOverlay(
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
