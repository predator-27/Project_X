package com.projectx.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.*
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.model.Appointment
import com.projectx.app.model.AppointmentStatus
import com.projectx.app.model.Course
import com.projectx.app.model.Teacher
import com.projectx.app.model.TeacherStatus
import com.projectx.app.model.TimetableSlot
import com.projectx.app.theme.*
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.util.Resource
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeacherScreen(
    onMenuClick: () -> Unit = {},
    facultyViewModel: FacultyViewModel = viewModel(),
    appointmentViewModel: AppointmentViewModel = viewModel(),
    authViewModel: AuthViewModel? = null,
    modifier: Modifier = Modifier
) {
    val currentFacultyUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: DemoCampusData.DEMO_STUDENT_UID }

    val activeFacultyResource by facultyViewModel.activeFacultyProfile.collectAsState()
    val facultyAppointmentsResource by appointmentViewModel.facultyAppointmentsState.collectAsState()

    var showSavedSnackbar by remember { mutableStateOf(false) }

    val todaySchedule = DemoCampusData.demoFacultySchedule
    val myClasses = DemoCampusData.demoFacultyClasses

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
                title = { Text("👨‍🏫 Faculty Portal & Admin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel?.signOut()
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
                    Text("Faculty profile and settings updated!")
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            val isWideScreen = maxWidth > 600.dp

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Demo Label Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ DEMO FACULTY PORTAL — FOR DEVELOPMENT PREVIEW ONLY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Faculty Header Card
                item {
                    val teacher = (activeFacultyResource as? Resource.Success)?.data
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder)
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
                                    text = "Welcome, ${teacher?.name ?: "Dr. Sarah Jenkins"}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HeadingNavy
                                )

                                StatusPill(
                                    text = (teacher?.status ?: TeacherStatus.AT_DESK).name.replace("_", " "),
                                    tone = when (teacher?.status) {
                                        TeacherStatus.AT_DESK -> StatusTone.SUCCESS
                                        TeacherStatus.IN_CLASS -> StatusTone.WARNING
                                        TeacherStatus.BUSY -> StatusTone.DANGER
                                        else -> StatusTone.NEUTRAL
                                    }
                                )
                            }

                            Text(
                                text = "${teacher?.title ?: "Professor"} • ${teacher?.department ?: "Computer Science"}",
                                fontSize = 12.sp,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Desk Location: ${teacher?.deskNumber ?: "seat_c304"} • Office Hours: ${teacher?.timings ?: "Mon, Wed, Fri: 11:00 AM - 01:00 PM"}",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }
                }

                // Today's Schedule Section
                item {
                    Text("Today's Teaching Schedule", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                }

                items(todaySchedule, key = { it.id }) { slot ->
                    val isCurrent = isCurrentTimeSlot(slot.timeSlot)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) InfoBannerBg else SurfaceCard
                        ),
                        border = BorderStroke(1.dp, if (isCurrent) PrimaryIndigo else SurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = slot.courseCode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (isCurrent) {
                                    StatusPill(text = "Current Class", tone = StatusTone.SUCCESS)
                                }
                            }

                            Text(
                                text = slot.courseName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeadingNavy
                            )

                            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(14.dp))
                                    Text(slot.timeSlot, fontSize = 11.sp, color = BodyText, fontWeight = FontWeight.Medium)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                                    Text("${slot.roomCode} • ${slot.section}", fontSize = 11.sp, color = MutedText)
                                }
                            }
                        }
                    }
                }

                // My Classes Section
                item {
                    Text("My Assigned Classes & Courses", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                }

                items(myClasses, key = { it.courseCode }) { course ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = InfoBannerBg
                                    ) {
                                        Text(
                                            text = course.courseCode,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryIndigo,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text("${course.credits} Credits • Sec ${course.section}", fontSize = 11.sp, color = MutedText)
                                }

                                Text(
                                    text = course.courseName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HeadingNavy
                                )
                            }

                            Icon(Icons.Default.Book, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Faculty Presence & Desk Control
                item {
                    Text("Presence & Desk Availability Control", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                }

                item {
                    val teacher = (activeFacultyResource as? Resource.Success)?.data ?: Teacher(
                        id = "fac_jenkins",
                        name = "Dr. Sarah Jenkins",
                        title = "Professor",
                        department = "Computer Science",
                        deskNumber = "seat_c304",
                        timings = "Mon, Wed, Fri: 11:00 AM - 01:00 PM",
                        email = "sarah.jenkins@projectx.demo",
                        bio = "Specializes in Data Structures, Algorithm Optimization, and Computational Complexity."
                    )

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
                }

                // Student Appointment Requests
                val apptCount = (facultyAppointmentsResource as? Resource.Success)?.data?.size ?: 0
                item {
                    Text("Student Appointment Requests ($apptCount)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                }

                when (val apptResource = facultyAppointmentsResource) {
                    is Resource.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryIndigo)
                            }
                        }
                    }
                    is Resource.Empty -> {
                        item {
                            EmptyStateCard(title = "No pending appointment requests for your faculty account.")
                        }
                    }
                    is Resource.Success -> {
                        val appts = apptResource.data
                        if (appts.isEmpty()) {
                            item { EmptyStateCard(title = "No pending appointment requests for your faculty account.") }
                        } else {
                            items(appts, key = { it.id }) { appointment ->
                                FacultyAppointmentCard(
                                    appointment = appointment,
                                    onConfirm = {
                                        appointmentViewModel.updateAppointmentStatus(
                                            appointmentId = appointment.id,
                                            newStatus = AppointmentStatus.CONFIRMED,
                                            facultyUid = currentFacultyUid
                                        )
                                        showSavedSnackbar = true
                                    },
                                    onReject = {
                                        appointmentViewModel.updateAppointmentStatus(
                                            appointmentId = appointment.id,
                                            newStatus = AppointmentStatus.REJECTED,
                                            facultyUid = currentFacultyUid
                                        )
                                        showSavedSnackbar = true
                                    }
                                )
                            }
                        }
                    }
                    else -> {
                        item { EmptyStateCard(title = "No pending appointment requests for your faculty account.") }
                    }
                }
            }
        }
    }
}

private fun isCurrentTimeSlot(timeSlot: String): Boolean {
    return try {
        val parts = timeSlot.split(" - ")
        if (parts.size != 2) return false
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        val startTime = sdf.parse(parts[0].trim()) ?: return false
        val endTime = sdf.parse(parts[1].trim()) ?: return false

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startCal = Calendar.getInstance().apply { time = startTime }
        val endCal = Calendar.getInstance().apply { time = endTime }

        val startMinutes = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
        val endMinutes = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)

        currentMinutes in startMinutes..endMinutes
    } catch (e: Exception) {
        false
    }
}

@Composable
fun FacultyPresenceCard(
    teacher: Teacher,
    onStatusChange: (TeacherStatus) -> Unit,
    onTimingsSave: (String) -> Unit,
    onBioSave: (String) -> Unit
) {
    var editableTimings by remember(teacher) { mutableStateOf(teacher.timings) }
    var editableBio by remember(teacher) { mutableStateOf(teacher.bio) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Update Live Availability", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TeacherStatus.entries.forEach { status ->
                    val isSelected = teacher.status == status
                    val statusColor by animateColorAsState(
                        if (isSelected) PrimaryIndigo else SurfaceBorder,
                        label = "statusColor"
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryIndigo.copy(alpha = 0.15f) else Color.Transparent,
                        border = BorderStroke(1.dp, statusColor),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        TextButton(
                            onClick = { onStatusChange(status) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = status.name.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryIndigo else MutedText
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = editableTimings,
                onValueChange = { editableTimings = it },
                label = { Text("Office Hours / Consultation Timings") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editableBio,
                onValueChange = { editableBio = it },
                label = { Text("Faculty Bio & Research Focus") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = {
                        onTimingsSave(editableTimings)
                        onBioSave(editableBio)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Profile & Timings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FacultyAppointmentCard(
    appointment: Appointment,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.studentName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy
                )

                StatusPill(
                    text = appointment.status.name,
                    tone = when (appointment.status.name) {
                        "CONFIRMED" -> StatusTone.SUCCESS
                        "PENDING" -> StatusTone.WARNING
                        "REJECTED" -> StatusTone.DANGER
                        else -> StatusTone.NEUTRAL
                    }
                )
            }

            Text(
                text = "📅 ${appointment.date} @ ${appointment.timeSlot}",
                fontSize = 11.sp,
                color = BodyText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Topic: ${appointment.purpose}",
                fontSize = 11.sp,
                color = MutedText
            )

            if (appointment.status == AppointmentStatus.PENDING) {
                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onReject) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
