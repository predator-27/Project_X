package com.example.projectx.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.model.Appointment
import com.example.projectx.model.Teacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    viewModel: TeacherManagementViewModel,
    modifier: Modifier = Modifier
) {
    val teachers by viewModel.filteredTeachers.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDept by viewModel.selectedDepartment.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Teachers Directory, 1 = My Appointments
    var bookingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val departments = listOf("All", "Computer Science", "Data Science", "Software Engineering", "Cybersecurity")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            if (showSuccessSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSuccessSnackbar = false }) {
                            Text("View", color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Appointment requested successfully!")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "Student Desk & Mentor Portal",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Teacher Directory") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Appointments (${appointments.size})") }
                )
            }

            if (selectedTab == 0) {
                // Search & Filter
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by teacher, desk #, or subject...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(departments) { dept ->
                        FilterChip(
                            selected = dept == selectedDept,
                            onClick = { viewModel.setDepartmentFilter(dept) },
                            label = { Text(dept) }
                        )
                    }
                }

                // Teachers List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(teachers) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            onBookClick = { bookingTeacher = teacher }
                        )
                    }
                }
            } else {
                // Appointments Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (appointments.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No appointments booked yet.")
                                }
                            }
                        }
                    } else {
                        items(appointments) { appointment ->
                            StudentAppointmentCard(appointment = appointment)
                        }
                    }
                }
            }
        }

        // Booking Modal Dialog
        bookingTeacher?.let { teacher ->
            BookAppointmentDialog(
                teacher = teacher,
                onDismiss = { bookingTeacher = null },
                onConfirm = { name, email, date, slot, purpose ->
                    viewModel.bookAppointment(
                        teacherId = teacher.id,
                        teacherName = teacher.name,
                        studentName = name,
                        studentEmail = email,
                        date = date,
                        timeSlot = slot,
                        purpose = purpose
                    )
                    bookingTeacher = null
                    showSuccessSnackbar = true
                    selectedTab = 1 // Switch to appointments tab
                }
            )
        }
    }
}

@Composable
fun TeacherCard(
    teacher: Teacher,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = teacher.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${teacher.title} • ${teacher.department}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = teacher.status)
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 Desk Location:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = teacher.deskNumber,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🕒 Timings:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = teacher.timings,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (teacher.bio.isNotEmpty()) {
                Text(
                    text = teacher.bio,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onBookClick,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Book Appointment")
            }
        }
    }
}

@Composable
fun StudentAppointmentCard(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.teacherName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                AppointmentStatusBadge(status = appointment.status)
            }

            Text(
                text = "📅 ${appointment.date} @ ${appointment.timeSlot}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Topic: ${appointment.purpose}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BookAppointmentDialog(
    teacher: Teacher,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, date: String, timeSlot: String, purpose: String) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("2026-09-20") }
    var selectedSlot by remember { mutableStateOf("11:00 AM - 11:30 AM") }
    var purpose by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book Appointment with ${teacher.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Desk: ${teacher.deskNumber}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Your Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = studentEmail,
                    onValueChange = { studentEmail = it },
                    label = { Text("Your Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = selectedSlot,
                    onValueChange = { selectedSlot = it },
                    label = { Text("Preferred Time Slot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose / Discussion Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studentName.isNotBlank() && purpose.isNotBlank()) {
                        onConfirm(studentName, studentEmail, selectedDate, selectedSlot, purpose)
                    }
                },
                enabled = studentName.isNotBlank() && purpose.isNotBlank()
            ) {
                Text("Confirm Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
