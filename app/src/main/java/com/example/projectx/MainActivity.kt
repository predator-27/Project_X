package com.example.projectx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.projectx.ui.AdminTeacherScreen
import com.example.projectx.ui.StudentScreen
import com.example.projectx.ui.TeacherManagementViewModel
import com.example.projectx.ui.UserRole
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
    val currentRole by viewModel.currentRole.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRole == UserRole.ADMIN_TEACHER,
                    onClick = { viewModel.selectRole(UserRole.ADMIN_TEACHER) },
                    icon = { Text("👨‍🏫") },
                    label = { Text("Teacher Admin") }
                )

                NavigationBarItem(
                    selected = currentRole == UserRole.STUDENT,
                    onClick = { viewModel.selectRole(UserRole.STUDENT) },
                    icon = { Text("👨‍🎓") },
                    label = { Text("Student View") }
                )

                NavigationBarItem(
                    selected = currentRole == UserRole.WEB_VIEW,
                    onClick = { viewModel.selectRole(UserRole.WEB_VIEW) },
                    icon = { Text("🌐") },
                    label = { Text("Web Portal") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentRole) {
                UserRole.ADMIN_TEACHER -> AdminTeacherScreen(viewModel = viewModel)
                UserRole.STUDENT -> StudentScreen(viewModel = viewModel)
                UserRole.WEB_VIEW -> WebViewScreen()
            }
        }
    }
}
