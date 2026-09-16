package com.example.projectx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: TeacherManagementViewModel,
    modifier: Modifier = Modifier
) {
    val teachers by viewModel.teachers.collectAsState()
    var selectedRoleIndex by remember { mutableStateOf(0) } // 0 = Teacher/Admin, 1 = Student

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf("t1") }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Icon & Title
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🏫", fontSize = 36.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Teacher Desk & Schedule Portal",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Select your account type to sign in",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selector Tabs
            TabRow(
                selectedTabIndex = selectedRoleIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedRoleIndex == 0,
                    onClick = {
                        selectedRoleIndex = 0
                        email = "s.jenkins@university.edu"
                    },
                    text = { Text("👨‍🏫 Teacher / Admin") }
                )
                Tab(
                    selected = selectedRoleIndex == 1,
                    onClick = {
                        selectedRoleIndex = 1
                        email = "alex.r@student.edu"
                    },
                    text = { Text("👨‍🎓 Student / User") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedRoleIndex == 0) {
                        // Teacher Admin Login Fields
                        Text(
                            text = "Teacher / Admin Portal Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Select Teacher Profile:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        var expanded by remember { mutableStateOf(false) }
                        val activeTeacher = teachers.find { it.id == selectedTeacherId } ?: teachers.firstOrNull()

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = activeTeacher?.let { "${it.name} (${it.department})" } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Teacher Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                teachers.forEach { teacher ->
                                    DropdownMenuItem(
                                        text = { Text("${teacher.name} - ${teacher.department}") },
                                        onClick = {
                                            selectedTeacherId = teacher.id
                                            email = teacher.email
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = email.ifEmpty { activeTeacher?.email ?: "s.jenkins@university.edu" },
                            onValueChange = { email = it },
                            label = { Text("Work Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            placeholder = { Text("••••••••") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.loginAsTeacher(
                                    email = email.ifBlank { activeTeacher?.email ?: "s.jenkins@university.edu" },
                                    teacherId = selectedTeacherId
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sign In as Teacher")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.loginAsTeacher(
                                    email = "s.jenkins@university.edu",
                                    teacherId = "t1"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Quick Demo Teacher Sign In")
                        }

                    } else {
                        // Student Login Fields
                        Text(
                            text = "Student Portal Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = email.ifEmpty { "alex.r@student.edu" },
                            onValueChange = { email = it },
                            label = { Text("Student Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            placeholder = { Text("••••••••") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.loginAsStudent(
                                    email = email.ifBlank { "alex.r@student.edu" }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sign In as Student")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.loginAsStudent("alex.r@student.edu")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Quick Demo Student Sign In")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.openWebView() }
            ) {
                Text("🌐 Open Web App Portal View")
            }
        }
    }
}
