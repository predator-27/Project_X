package com.projectx.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.projectx.app.R
import com.projectx.app.components.*
import com.projectx.app.model.Appointment
import com.projectx.app.model.AppointmentStatus
import com.projectx.app.model.TimetableSlot
import com.projectx.app.model.UniversityAnnouncement
import com.projectx.app.model.lms.AnnouncementPriority
import com.projectx.app.theme.*
import com.projectx.app.ui.academics.AcademicViewModel
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.util.Resource
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. GREETING & STUDENT HEADER
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
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_logo),
                                    contentDescription = "Project X Logo",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            when (val session = sessionState) {
                                is AuthSessionState.Authenticated -> {
                                    val name = session.publicProfile?.displayName?.ifBlank { null }
                                        ?: session.user.email.substringBefore("@")
                                    val roll = session.user.rollNumber
                                    val schoolHeader = session.publicProfile?.schoolName?.ifBlank { null } ?: "Bennett University"

                                    Text(
                                        text = "Welcome back, $name",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HeadingNavy
                                    )
                                    Text(
                                        text = listOfNotNull(schoolHeader, roll?.let { "($it)" }).joinToString(" • "),
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Welcome to Project X",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HeadingNavy
                                    )
                                    Text(
                                        text = "Digital Campus Platform",
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. NEXT / TODAY'S CLASS
            item {
                SectionHeader(title = "Next Class", icon = Icons.Default.Schedule)
            }

            when (val resource = timetableState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
                    val nextClass = resource.data.sortedBy { it.timeSlot }.firstOrNull()
                    if (nextClass != null) {
                        item {
                            TimetableRowCard(
                                slot = nextClass,
                                onMapRouteClick = { onNavigateToTab("campus_map") }
                            )
                        }
                    } else {
                        item {
                            EmptyNoticeCard(message = "No classes scheduled for today")
                        }
                    }
                }
                else -> {
                    item {
                        EmptyNoticeCard(message = "No classes scheduled for today")
                    }
                }
            }

            // 3. ATTENDANCE OVERVIEW
            item {
                SectionHeader(title = "Attendance Overview", icon = Icons.Default.CheckCircle)
            }

            when (val resource = attendanceState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
                    val shortageCount = list.count { it.percentage < 75f }

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
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Overall Attendance",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HeadingNavy
                                    )
                                    Text(
                                        text = if (shortageCount > 0) "⚠️ $shortageCount subject(s) below 75%" else "$totalAttended / $totalClasses Classes Conducted",
                                        fontSize = 11.sp,
                                        color = if (shortageCount > 0) AccentCoral else MutedText,
                                        fontWeight = if (shortageCount > 0) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                Surface(
                                    shape = PillShape,
                                    color = if (overallPct >= 75f) SecondaryEmeraldBg else AccentCoralBg
                                ) {
                                    Text(
                                        text = "%.1f%%".format(overallPct),
                                        fontSize = 13.sp,
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

            // 4. ANNOUNCEMENTS
            item {
                SectionHeader(title = "Latest Announcement", icon = Icons.Default.Campaign)
            }

            when (val resource = announcementState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
                    val latest = resource.data.firstOrNull()
                    if (latest != null) {
                        item {
                            HomeAnnouncementCard(
                                announcement = latest,
                                onClick = { onNavigateToTab("messages") }
                            )
                        }
                    } else {
                        item {
                            EmptyNoticeCard(message = "No university announcements published")
                        }
                    }
                }
            }

            // 5. UPCOMING APPOINTMENT (CHRONOLOGICALLY NEXT ACTIVE CONFIRMED/PENDING APPOINTMENT)
            item {
                SectionHeader(title = "Next Appointment", icon = Icons.Default.Event)
            }

            when (val resource = studentAppointmentsState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
                    val nextAppointment = resource.data
                        .filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING }
                        .sortedWith(compareBy({ it.date }, { it.timeSlot }))
                        .firstOrNull()

                    if (nextAppointment != null) {
                        item {
                            HomeAppointmentCard(
                                appointment = nextAppointment,
                                onClick = { onNavigateToTab("teachers") }
                            )
                        }
                    } else {
                        item {
                            EmptyNoticeCard(message = "No upcoming faculty appointments")
                        }
                    }
                }
                else -> {
                    item {
                        EmptyNoticeCard(message = "No upcoming faculty appointments")
                    }
                }
            }

            // 6. QUICK ACTIONS GRID
            item {
                SectionHeader(title = "Campus Shortcuts", icon = Icons.Default.GridView)
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionTile(
                        title = "Faculty",
                        subtitle = "Directory",
                        icon = Icons.Default.Person,
                        isEnabled = true,
                        onClick = { onNavigateToTab("teachers") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        title = "Campus Map",
                        subtitle = "Block N1",
                        icon = Icons.Default.Map,
                        isEnabled = true,
                        onClick = { onNavigateToTab("campus_map") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        title = "Courses",
                        subtitle = "LMS Portal",
                        icon = Icons.Default.Book,
                        isEnabled = true,
                        onClick = { onNavigateToTab("courses") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        title = "Lost & Found",
                        subtitle = "Repository",
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

@Composable
fun HomeAnnouncementCard(
    announcement: UniversityAnnouncement,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
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
                    fontSize = 11.sp,
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
fun HomeAppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            .height(60.dp)
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
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isEnabled) PrimaryIndigo.copy(alpha = 0.1f) else MutedText.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled) PrimaryIndigo else MutedText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
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
                .padding(14.dp),
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
