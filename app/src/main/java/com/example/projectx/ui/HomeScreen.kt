package com.example.projectx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.theme.*
import com.example.projectx.ui.academics.AcademicViewModel
import com.example.projectx.util.Resource

@Composable
fun HomeScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val overallAttendance by academicViewModel.overallAttendancePercentage.collectAsState()
    val shortageCount by academicViewModel.shortageSubjectCount.collectAsState()
    val timetableState by academicViewModel.timetableState.collectAsState()
    val profileState by academicViewModel.publicProfileState.collectAsState()

    val nextClassText = remember(timetableState) {
        val slots = (timetableState as? Resource.Success)?.data
        val firstSlot = slots?.firstOrNull()
        if (firstSlot != null) {
            "Next Class: ${firstSlot.courseName} @ ${firstSlot.timeSlot.substringBefore(" -")} in Room ${firstSlot.roomCode}"
        } else {
            "No upcoming classes scheduled for today."
        }
    }

    AppScaffold(
        title = "Dashboard",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Next Class Banner Card
            BannerCard(
                message = nextClassText,
                actionLabel = "Map Route",
                onActionClick = { onNavigateToTab("campus_map") }
            )

            // Shortage Warning Strip or Good Standing Info
            if (shortageCount > 0) {
                InfoStrip(
                    message = "Attendance Warning: $shortageCount subject(s) below 75% threshold. Check Attendance portal."
                )
            } else {
                InfoStrip(
                    message = "Academic Status: Overall Attendance is %.1f%%.".format(overallAttendance)
                )
            }

            // Student Academic Overview Summary Card
            SectionFormCard(sectionTitle = "Student Academic Overview") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = profileState?.schoolName?.ifBlank { null } ?: "Bennett University",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeadingNavy
                    )
                    val programDeptText = listOfNotNull(
                        profileState?.program?.ifBlank { null },
                        profileState?.department?.ifBlank { null },
                        profileState?.section?.ifBlank { null }
                    ).joinToString(" • ").ifBlank { "Academic Profile Setup Pending" }

                    Text(
                        text = programDeptText,
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }

            Text(
                text = "Campus Modules & Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HeadingNavy
            )

            // Quick Module Tiles Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeModuleTile(
                        title = "Timetable",
                        subtitle = "Daily Schedule",
                        icon = Icons.Default.Schedule,
                        onClick = { onNavigateToTab("timetable") },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModuleTile(
                        title = "Attendance",
                        subtitle = "Overall: %.1f%%".format(overallAttendance),
                        icon = Icons.Default.CheckCircle,
                        onClick = { onNavigateToTab("attendance") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeModuleTile(
                        title = "Courses & Subjects",
                        subtitle = "Enrolled Curriculum",
                        icon = Icons.Default.Book,
                        onClick = { onNavigateToTab("courses") },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModuleTile(
                        title = "Campus Map",
                        subtitle = "Indoor Navigation",
                        icon = Icons.Default.Map,
                        onClick = { onNavigateToTab("campus_map") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = InfoBannerBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(22.dp))
                }
            }

            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                Text(text = subtitle, fontSize = 12.sp, color = MutedText)
            }
        }
    }
}
