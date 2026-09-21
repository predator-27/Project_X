package com.example.projectx.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.model.Institution

import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    viewModel: TeacherManagementViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val teachers by viewModel.teachers.collectAsState()
    val savedRememberMe by viewModel.rememberMe.collectAsState()
    val savedEmail by viewModel.loggedInUserEmail.collectAsState()
    val savedTeacherId by viewModel.selectedTeacherId.collectAsState()

    var selectedRoleIndex by remember { mutableStateOf(0) } // 0 = Teacher/Admin, 1 = Student

    var rememberMe by remember { mutableStateOf(savedRememberMe) }
    var email by remember(savedEmail) { mutableStateOf(savedEmail) }
    var password by remember { mutableStateOf("") }
    var selectedTeacherId by remember(savedTeacherId) { mutableStateOf(if (savedTeacherId.isBlank()) "t1" else savedTeacherId) }

    var showRegisterDialog by remember { mutableStateOf(false) }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Gradient Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = "Teacher Desk Portal",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "Real-time Presence & Appointment Manager",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Role Switcher Segmented Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = {
                            selectedRoleIndex = 0
                            if (email.isBlank()) email = "s.jenkins@university.edu"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRoleIndex == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selectedRoleIndex == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (selectedRoleIndex == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                    ) {
                        Text("👨‍🏫 Teacher Admin", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            selectedRoleIndex = 1
                            if (email.isBlank()) email = "alex.r@student.edu"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRoleIndex == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selectedRoleIndex == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (selectedRoleIndex == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                    ) {
                        Text("👨‍🎓 Student Portal", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Card Container for Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                AnimatedContent(
                    targetState = selectedRoleIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { role ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (role == 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Teacher Admin Sign In",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                TextButton(onClick = { showRegisterDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Teacher?", fontSize = 12.sp)
                                }
                            }

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
                                    label = { Text("Select Teacher Account") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    shape = RoundedCornerShape(12.dp),
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
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                placeholder = { Text("••••••••") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Remember Me Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "Remember Me (Auto sign in next time)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.loginAsTeacher(
                                        email = email.ifBlank { activeTeacher?.email ?: "s.jenkins@university.edu" },
                                        teacherId = selectedTeacherId,
                                        rememberMe = rememberMe,
                                        context = context
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sign In as Teacher", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.loginAsTeacher(
                                        email = "s.jenkins@university.edu",
                                        teacherId = "t1",
                                        rememberMe = rememberMe,
                                        context = context
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⚡ Quick Demo Sign In", fontSize = 14.sp)
                            }

                        } else {
                            Text(
                                text = "Student Portal Sign In",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = email.ifEmpty { "alex.r@student.edu" },
                                onValueChange = { email = it },
                                label = { Text("Student Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                placeholder = { Text("••••••••") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Remember Me Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "Remember Me (Auto sign in next time)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.loginAsStudent(
                                        email = email.ifBlank { "alex.r@student.edu" },
                                        rememberMe = rememberMe,
                                        context = context
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sign In as Student", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.loginAsStudent(
                                        email = "alex.r@student.edu",
                                        rememberMe = rememberMe,
                                        context = context
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⚡ Quick Demo Student Sign In", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = { viewModel.openWebView() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Open Web Application View")
                }
            }
        }

        // Register New Teacher Dialog
        if (showRegisterDialog) {
            RegisterTeacherDialog(
                onDismiss = { showRegisterDialog = false },
                onRegister = { name, title, dept, email, desk, timings, instName, instDomain ->
                    viewModel.registerAndLoginTeacher(
                        name = name,
                        title = title,
                        department = dept,
                        email = email,
                        deskNumber = desk,
                        timings = timings,
                        institution = instName,
                        institutionDomain = instDomain,
                        rememberMe = rememberMe,
                        context = context
                    )
                    showRegisterDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTeacherDialog(
    onDismiss: () -> Unit,
    onRegister: (name: String, title: String, dept: String, email: String, desk: String, timings: String, instName: String, instDomain: String) -> Unit
) {
    var regName by remember { mutableStateOf("") }
    var regTitle by remember { mutableStateOf("Professor") }
    var regDept by remember { mutableStateOf("Computer Science") }
    var regDesk by remember { mutableStateOf("Desk #101, Block A") }
    var regTimings by remember { mutableStateOf("Mon-Fri: 09:00 AM - 01:00 PM") }

    val institutions = remember { Institution.DEFAULT_LIST }
    var selectedInstitution by remember { mutableStateOf(institutions.first()) }
    var regEmailPrefix by remember { mutableStateOf("prof.new") }

    val fullEmail = "$regEmailPrefix@${selectedInstitution.domain}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Teacher Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = regName,
                    onValueChange = { regName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = regTitle,
                        onValueChange = { regTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = regDept,
                        onValueChange = { regDept = it },
                        label = { Text("Department") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Institution Selection
                var expandedInst by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedInst,
                    onExpandedChange = { expandedInst = !expandedInst }
                ) {
                    OutlinedTextField(
                        value = "${selectedInstitution.name} (@${selectedInstitution.domain})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Institution / College") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInst) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInst,
                        onDismissRequest = { expandedInst = false }
                    ) {
                        institutions.forEach { inst ->
                            DropdownMenuItem(
                                text = { Text("${inst.name} (@${inst.domain})") },
                                onClick = {
                                    selectedInstitution = inst
                                    expandedInst = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = regEmailPrefix,
                    onValueChange = { regEmailPrefix = it },
                    label = { Text("Email Username") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    trailingIcon = { Text("@${selectedInstitution.domain} ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Valid Institutional Email: $fullEmail",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = regDesk,
                    onValueChange = { regDesk = it },
                    label = { Text("Desk / Stall Location") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = regTimings,
                    onValueChange = { regTimings = it },
                    label = { Text("Office Hours / Timings") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (regName.isNotBlank() && regEmailPrefix.isNotBlank()) {
                        onRegister(
                            regName,
                            regTitle,
                            regDept,
                            fullEmail,
                            regDesk,
                            regTimings,
                            selectedInstitution.name,
                            selectedInstitution.domain
                        )
                    }
                },
                enabled = regName.isNotBlank() && regEmailPrefix.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Register & Enter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
