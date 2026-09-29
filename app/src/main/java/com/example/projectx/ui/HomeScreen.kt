package com.example.projectx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectx.R
import com.example.projectx.components.*
import com.example.projectx.model.Appointment
import com.example.projectx.model.TimetableSlot
import com.example.projectx.model.UniversityAnnouncement
import com.example.projectx.model.lms.AnnouncementPriority
import com.example.projectx.theme.*
import com.example.projectx.ui.academics.AcademicViewModel
import com.example.projectx.ui.auth.AuthSessionState
import com.example.projectx.ui.auth.AuthViewModel
import com.example.projectx.util.Resource
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    academicViewModel: AcademicViewModel = viewModel(),
    appointmentViewModel: AppointmentViewModel = viewModel(),
    announcementViewModel: AnnouncementViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onMenuClick: () -> Unit,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val timetableState by academicViewModel.timetableState.collectAsState()
    val attendanceState by academicViewModel.attendanceState.collectAsState()
    val studentAppointmentsState by appointmentViewModel.studentAppointmentsState.collectAsState()
    val announcementState by announcementViewModel.announcementsState.collectAsState()

    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val studentUid = currentUser?.uid ?: ""

    LaunchedEffect(studentUid) {
        if (studentUid.isNotBlank()) {
            appointmentViewModel.loadStudentAppointments(studentUid)
        }
    }

    AppScaffold(
        title = "Dashboard",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. INSTITUTION HEADER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_logo),
                                    contentDescription = "Project X Logo",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val schoolHeader = remember(sessionState) {
                                ((sessionState as? AuthSessionState.Authenticated)?.publicProfile?.schoolName?.ifBlank { null } ?: "Bennett University").uppercase()
                            }

                            Text(
                                text = schoolHeader,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryIndigo,
                                letterSpacing = 0.5.sp
                            )

                            when (val session = sessionState) {
                                is AuthSessionState.Authenticated -> {
                                    val name = session.publicProfile?.displayName?.ifBlank { null }
                                        ?: session.user.email.substringBefore("@")
                                    val roll = session.user.rollNumber
                                    val program = session.publicProfile?.program?.ifBlank { null }
                                    val dept = session.publicProfile?.department?.ifBlank { null }
                                    val sec = session.publicProfile?.section?.ifBlank { null }

                                    val line1Text = listOfNotNull(name, roll?.let { "($it)" }).joinToString(" ")
                                    val academicSub = listOfNotNull(program, dept, sec?.let { "Sec $it" }).joinToString(" • ")
                                        .ifBlank { "Student Portal Active" }

                                    Text(
                                        text = line1Text,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HeadingNavy
                                    )
                                    Text(
                                        text = academicSub,
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                                is AuthSessionState.Loading -> {
                                    Text(
                                        text = "Loading profile details...",
                                        fontSize = 14.sp,
                                        color = MutedText
                                    )
                                }
                                else -> {
                                    Text(
                                        text = currentUser?.email ?: "Authenticated User",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HeadingNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. UNIVERSITY ANNOUNCEMENTS & NOTICES
            item {
                SectionHeader(title = "University Announcements", icon = Icons.Default.Campaign)
            }

            when (val resource = announcementState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        ErrorNoticeCard(
                            message = resource.message,
                            onRetry = { announcementViewModel.loadAnnouncements() }
                        )
                    }
                }
                is Resource.Empty -> {
                    item {
                        EmptyNoticeCard(message = "No university announcements published")
                    }
                }
                is Resource.Success -> {
                    val recentList = resource.data.take(3)
                    items(recentList, key = { it.id }) { ann ->
                        HomeAnnouncementCard(announcement = ann)
                    }
                }
            }

            // 3. TODAY'S TIMETABLE
            item {
                SectionHeader(title = "Today's Schedule", icon = Icons.Default.Schedule)
            }

            when (val resource = timetableState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        ErrorNoticeCard(
                            message = resource.message,
                            onRetry = { academicViewModel.loadTimetable() }
                        )
                    }
                }
                is Resource.Success -> {
                    items(resource.data, key = { it.id }) { slot ->
                        TimetableRowCard(
                            slot = slot,
                            onMapRouteClick = { onNavigateToTab("campus_map") }
                        )
                    }
                }
                else -> {
                    item {
                        EmptyNoticeCard(message = "No classes scheduled for today")
                    }
                }
            }

            // 4. ATTENDANCE OVERVIEW
            item {
                SectionHeader(title = "Attendance Status", icon = Icons.Default.CheckCircle)
            }

            when (val resource = attendanceState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        ErrorNoticeCard(
                            message = resource.message,
                            onRetry = { academicViewModel.loadAttendance() }
                        )
                    }
                }
                is Resource.Success -> {
                    val list = resource.data
                    val totalAttended = list.sumOf { it.attendedClasses }
                    val totalClasses = list.sumOf { it.totalClasses }
                    val overallPct = if (totalClasses > 0) (totalAttended.toFloat() / totalClasses.toFloat()) * 100f else 0f

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTab("attendance") },
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Overall Attendance",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HeadingNavy
                                    )
                                    Text(
                                        text = "$totalAttended / $totalClasses Total Classes Conducted",
                                        fontSize = 12.sp,
                                        color = MutedText
                                    )
                                }

                                Surface(
                                    shape = PillShape,
                                    color = if (overallPct >= 75f) SecondaryEmeraldBg else AccentCoralBg
                                ) {
                                    Text(
                                        text = "%.1f%%".format(overallPct),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (overallPct >= 75f) SecondaryEmerald else AccentCoral,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    item {
                        EmptyNoticeCard(message = "No attendance records found.")
                    }
                }
            }

            // 5. APPOINTMENTS SHORTCUT
            item {
                SectionHeader(title = "Faculty Appointments", icon = Icons.Default.Event)
            }

            when (val resource = studentAppointmentsState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        ErrorNoticeCard(
                            message = resource.message,
                            onRetry = {
                                if (studentUid.isNotBlank()) {
                                    appointmentViewModel.loadStudentAppointments(studentUid)
                                }
                            }
                        )
                    }
                }
                is Resource.Success -> {
                    val activeAppointments = resource.data.filter { it.status.name != "CANCELLED" }
                    if (activeAppointments.isEmpty()) {
                        item {
                            EmptyNoticeCard(message = "No upcoming appointments")
                        }
                    } else {
                        items(activeAppointments.take(2), key = { it.id }) { appt ->
                            HomeAppointmentCard(appointment = appt)
                        }
                    }
                }
                else -> {
                    item {
                        EmptyNoticeCard(message = "No upcoming appointments")
                    }
                }
            }

            // 6. QUICK ACTIONS GRID
            item {
                SectionHeader(title = "Campus Shortcuts", icon = Icons.Default.GridView)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QuickActionTile(
                            title = "Faculty Directory",
                            subtitle = "Find Cabins & Hours",
                            icon = Icons.Default.Person,
                            isEnabled = true,
                            onClick = { onNavigateToTab("teachers") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            title = "Indoor Map",
                            subtitle = "Block N1 Vector Grid",
                            icon = Icons.Default.Map,
                            isEnabled = true,
                            onClick = { onNavigateToTab("campus_map") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QuickActionTile(
                            title = "Courses & LMS",
                            subtitle = "Syllabus & Work",
                            icon = Icons.Default.Book,
                            isEnabled = true,
                            onClick = { onNavigateToTab("courses") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            title = "Lost & Found",
                            subtitle = "Browse & Claim",
                            icon = Icons.Default.FindInPage,
                            isEnabled = true,
                            onClick = { onNavigateToTab("lost_found") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeAnnouncementCard(announcement: UniversityAnnouncement) {
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
                    text = announcement.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = announcement.priority.name,
                    tone = if (announcement.priority == AnnouncementPriority.URGENT) StatusTone.DANGER else StatusTone.NEUTRAL
                )
            }

            Text(
                text = announcement.content,
                fontSize = 12.sp,
                color = BodyText,
                maxLines = 2
            )

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By: ${announcement.authorName} • Dept: ${announcement.targetDepartment}",
                    fontSize = 11.sp,
                    color = MutedText
                )

                val dateStr = remember(announcement.createdAt) {
                    if (announcement.createdAt > 0) {
                        SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(announcement.createdAt))
                    } else {
                        "Recently"
                    }
                }
                Text(text = dateStr, fontSize = 11.sp, color = MutedText)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = HeadingNavy
        )
    }
}

@Composable
fun TimetableRowCard(
    slot: TimetableSlot,
    onMapRouteClick: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.courseName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy
                )
                Text(
                    text = "${slot.timeSlot} • ${slot.facultyName}",
                    fontSize = 12.sp,
                    color = MutedText
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = InfoBannerBg,
                modifier = Modifier.clickable { onMapRouteClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(14.dp))
                    Text(
                        text = slot.roomCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
            }
        }
    }
}

@Composable
fun HomeAppointmentCard(appointment: Appointment) {
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
                    text = "Appointment: ${appointment.teacherName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy
                )
                StatusPill(
                    text = appointment.status.name,
                    tone = when (appointment.status.name) {
                        "CONFIRMED" -> StatusTone.SUCCESS
                        "PENDING" -> StatusTone.WARNING
                        "REJECTED", "CANCELLED" -> StatusTone.DANGER
                        else -> StatusTone.NEUTRAL
                    }
                )
            }

            Text(
                text = "📅 ${appointment.date} @ ${appointment.timeSlot}",
                fontSize = 12.sp,
                color = BodyText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Topic: ${appointment.purpose}",
                fontSize = 11.sp,
                color = MutedText
            )
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(enabled = isEnabled) { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) SurfaceCard else SurfaceBorder.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isEnabled) PrimaryIndigo.copy(alpha = 0.1f) else MutedText.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled) PrimaryIndigo else MutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) HeadingNavy else MutedText
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MutedText
                )
            }
        }
    }
}

@Composable
fun EmptyNoticeCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                fontSize = 12.sp,
                color = MutedText
            )
        }
    }
}

@Composable
fun ErrorNoticeCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, AccentCoral)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
