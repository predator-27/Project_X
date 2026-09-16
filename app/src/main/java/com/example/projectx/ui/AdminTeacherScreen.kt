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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.model.Appointment
import com.example.projectx.model.AppointmentStatus
import com.example.projectx.model.Teacher
import com.example.projectx.model.TeacherStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeacherScreen(
    viewModel: TeacherManagementViewModel,
    modifier: Modifier = Modifier
) {
    val activeTeacher by viewModel.activeTeacher.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    var editableDesk by remember(activeTeacher) { mutableStateOf(activeTeacher?.deskNumber ?: "") }
    var editableTimings by remember(activeTeacher) { mutableStateOf(activeTeacher?.timings ?: "") }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    val teacherAppointments = remember(appointments, activeTeacher) {
        appointments.filter { it.teacherId == activeTeacher?.id }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("👨‍🏫 Teacher Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
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
                    Text("Desk location & timings updated successfully!")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Welcome, ${activeTeacher?.name ?: "Teacher"}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Manage your real-time desk presence, office hours, and student requests",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Control Card
            activeTeacher?.let { teacher ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Real-time Presence Status",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusBadge(status = teacher.status)
                            }

                            Divider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TeacherStatus.values().forEach { status ->
                                    val isSelected = teacher.status == status
                                    val animatedBg by animateColorAsState(
                                        targetValue = if (isSelected) getStatusColor(status) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        label = "bgAnimation"
                                    )

                                    Button(
                                        onClick = { viewModel.updateStatus(teacher.id, status) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = animatedBg,
                                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = status.label.split(" ").first(),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Desk & Schedule Editor Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Desk & Schedule Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = editableDesk,
                                onValueChange = { editableDesk = it },
                                label = { Text("Desk / Stall Location") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                placeholder = { Text("e.g. Desk #304, Block B") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editableTimings,
                                onValueChange = { editableTimings = it },
                                label = { Text("Available Timings / Office Hours") },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                                placeholder = { Text("e.g. Mon-Fri: 10:00 AM - 01:00 PM") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    viewModel.updateDeskAndTimings(teacher.id, editableDesk, editableTimings)
                                    showSavedSnackbar = true
                                },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Info")
                            }
                        }
                    }
                }

                // Student Appointments List
                item {
                    Text(
                        text = "Student Appointment Requests (${teacherAppointments.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (teacherAppointments.isEmpty()) {
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
                                Text("No appointment requests yet.")
                            }
                        }
                    }
                } else {
                    items(teacherAppointments) { appointment ->
                        AppointmentRequestCard(
                            appointment = appointment,
                            onUpdateStatus = { newStatus ->
                                viewModel.updateAppointmentStatus(appointment.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentRequestCard(
    appointment: Appointment,
    onUpdateStatus: (AppointmentStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        fontSize = 15.sp
                    )
                    Text(
                        text = appointment.studentEmail,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AppointmentStatusBadge(status = appointment.status)
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📅 ${appointment.date} @ ${appointment.timeSlot}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Purpose: ${appointment.purpose}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedVisibility(visible = appointment.status == AppointmentStatus.PENDING) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(AppointmentStatus.CANCELLED) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline")
                    }

                    Button(
                        onClick = { onUpdateStatus(AppointmentStatus.CONFIRMED) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept")
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(status))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.label,
                fontSize = 12.sp,
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
        AppointmentStatus.COMPLETED -> Color(0xFF1565C0)
        AppointmentStatus.CANCELLED -> Color(0xFFC62828)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
