package com.example.projectx.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectx.model.Appointment
import com.example.projectx.model.AppointmentStatus
import com.example.projectx.model.Teacher
import com.example.projectx.model.TeacherStatus
import com.example.projectx.ui.auth.AuthViewModel
import com.example.projectx.util.Resource
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeacherScreen(
    facultyViewModel: FacultyViewModel = viewModel(),
    appointmentViewModel: AppointmentViewModel = viewModel(),
    authViewModel: AuthViewModel? = null,
    modifier: Modifier = Modifier
) {
    val currentFacultyUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val activeFacultyResource by facultyViewModel.activeFacultyProfile.collectAsState()
    val facultyAppointmentsResource by appointmentViewModel.facultyAppointmentsState.collectAsState()

    var showSavedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(currentFacultyUid) {
        if (currentFacultyUid.isNotBlank()) {
            facultyViewModel.loadActiveFacultyProfile(currentFacultyUid)
            appointmentViewModel.loadFacultyAppointments(currentFacultyUid)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("👨‍🏫 Teacher Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        if (authViewModel != null) {
                            authViewModel.signOut()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        snackbarHost = {
            if (showSavedSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSavedSnackbar = false }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Faculty settings updated in Firestore!")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                when (val teacherResource = activeFacultyResource) {
                    is Resource.Success -> {
                        val teacher = teacherResource.data
                        Text(
                            text = "Welcome, ${teacher?.name ?: "Faculty Member"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${teacher?.title ?: ""} • ${teacher?.department ?: ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Text(
                            text = "Faculty Desk Portal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Status Control Card
            item {
                when (val teacherResource = activeFacultyResource) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is Resource.Success -> {
                        val teacher = teacherResource.data
                        if (teacher != null) {
                            FacultyPresenceCard(
                                teacher = teacher,
                                onStatusChange = { newStatus ->
                                    facultyViewModel.updateOwnStatus(currentFacultyUid, newStatus)
                                },
                                onTimingsSave = { newTimings ->
                                    facultyViewModel.updateOwnTimings(currentFacultyUid, newTimings)
                                    showSavedSnackbar = true
                                },
                                onBioSave = { newBio ->
                                    facultyViewModel.updateOwnBio(currentFacultyUid, newBio)
                                    showSavedSnackbar = true
                                }
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Faculty profile document missing in Firestore (/faculty_profiles/$currentFacultyUid). Please contact College Admin.",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = teacherResource.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }

            // Student Appointments Section
            val apptCount = (facultyAppointmentsResource as? Resource.Success)?.data?.size ?: 0
            item {
                Text(
                    text = "Student Appointment Requests ($apptCount)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when (val apptResource = facultyAppointmentsResource) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                is Resource.Empty -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No appointment requests for your faculty account.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = apptResource.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
                is Resource.Success -> {
                    items(apptResource.data, key = { it.id }) { appointment ->
                        AppointmentRequestCard(
                            appointment = appointment,
                            onAccept = {
                                appointmentViewModel.updateAppointmentStatus(
                                    appointmentId = appointment.id,
                                    newStatus = AppointmentStatus.CONFIRMED,
                                    facultyUid = currentFacultyUid
                                )
                            },
                            onReject = {
                                appointmentViewModel.updateAppointmentStatus(
                                    appointmentId = appointment.id,
                                    newStatus = AppointmentStatus.REJECTED,
                                    facultyUid = currentFacultyUid
                                )
                            },
                            onComplete = {
                                appointmentViewModel.updateAppointmentStatus(
                                    appointmentId = appointment.id,
                                    newStatus = AppointmentStatus.COMPLETED,
                                    facultyUid = currentFacultyUid
                                )
                            },
                            onCancel = {
                                appointmentViewModel.updateAppointmentStatus(
                                    appointmentId = appointment.id,
                                    newStatus = AppointmentStatus.CANCELLED,
                                    facultyUid = currentFacultyUid
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FacultyPresenceCard(
    teacher: Teacher,
    onStatusChange: (TeacherStatus) -> Unit,
    onTimingsSave: (String) -> Unit,
    onBioSave: (String) -> Unit
) {
    var editableTimings by remember(teacher.timings) { mutableStateOf(teacher.timings) }
    var editableBio by remember(teacher.bio) { mutableStateOf(teacher.bio) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Real-time Presence Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = teacher.status)
            }

            HorizontalDivider()

            // Status Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TeacherStatus.entries.forEach { status ->
                    val isSelected = teacher.status == status
                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) getStatusColor(status) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        label = "bgAnimation"
                    )

                    Button(
                        onClick = { onStatusChange(status) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedBg,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = status.label.split(" ").first(),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider()

            // Desk Location (Read-Only for Faculty; Admin controlled)
            OutlinedTextField(
                value = teacher.deskNumber.ifBlank { "Unassigned Desk" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Desk Location (College Admin Managed)") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Timings Input
            OutlinedTextField(
                value = editableTimings,
                onValueChange = { editableTimings = it },
                label = { Text("Available Office Hours / Timings") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                placeholder = { Text("e.g. Mon-Fri: 10:00 AM - 01:00 PM") },
                trailingIcon = {
                    IconButton(onClick = { onTimingsSave(editableTimings) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Office Hours")
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Bio Input
            OutlinedTextField(
                value = editableBio,
                onValueChange = { editableBio = it },
                label = { Text("Faculty Bio / Discussion Focus") },
                placeholder = { Text("Specializes in AI and Distributed Systems mentorship.") },
                trailingIcon = {
                    IconButton(onClick = { onBioSave(editableBio) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Bio")
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AppointmentRequestCard(
    appointment: Appointment,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = appointment.studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = appointment.studentEmail,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AppointmentStatusBadge(status = appointment.status)
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📅 ${appointment.date} @ ${appointment.timeSlot}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Purpose: ${appointment.purpose}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Actions for PENDING
            AnimatedVisibility(visible = appointment.status == AppointmentStatus.PENDING) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept", fontSize = 11.sp)
                    }
                }
            }

            // Actions for CONFIRMED
            AnimatedVisibility(visible = appointment.status == AppointmentStatus.CONFIRMED) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Completed", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: TeacherStatus) {
    Surface(
        color = getStatusColor(status).copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(status))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = getStatusColor(status)
            )
        }
    }
}

@Composable
fun AppointmentStatusBadge(status: AppointmentStatus) {
    val color = when (status) {
        AppointmentStatus.CONFIRMED -> Color(0xFF2E7D32)
        AppointmentStatus.PENDING -> Color(0xFFE65100)
        AppointmentStatus.REJECTED -> Color(0xFFC62828)
        AppointmentStatus.COMPLETED -> Color(0xFF1565C0)
        AppointmentStatus.CANCELLED -> Color(0xFF616161)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = status.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

fun getStatusColor(status: TeacherStatus): Color {
    return when (status) {
        TeacherStatus.AT_DESK -> Color(0xFF2E7D32) // Green
        TeacherStatus.BUSY -> Color(0xFFE65100)    // Orange
        TeacherStatus.IN_CLASS -> Color(0xFFC62828)  // Red
        TeacherStatus.AWAY -> Color(0xFF616161)      // Gray
    }
}
