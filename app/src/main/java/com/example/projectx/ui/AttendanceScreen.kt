package com.example.projectx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.model.AttendanceSubject
import com.example.projectx.theme.*
import com.example.projectx.ui.academics.AcademicViewModel
import com.example.projectx.util.Resource

@Composable
fun AttendanceScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Subject-wise, 1 = Log, 2 = Summary
    var selectedSemester by remember { mutableStateOf("Current Semester") }
    val attendanceState by academicViewModel.attendanceState.collectAsState()

    AppScaffold(
        title = "Attendance",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Row: Tabs & Semester Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnderlineTabRow(
                        tabs = listOf("Subject-wise", "Log", "Summary"),
                        selectedTabIndex = selectedTab,
                        onTabSelected = { selectedTab = it },
                        modifier = Modifier.weight(1f)
                    )
                    SemesterSelector(
                        selectedOption = selectedSemester,
                        onOptionSelected = { selectedSemester = it }
                    )
                }
            }

            when (val state = attendanceState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryIndigo)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        SectionFormCard(sectionTitle = "Error Loading Attendance") {
                            Text(text = state.message, fontSize = 13.sp, color = AccentCoral)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { academicViewModel.loadAttendance() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is Resource.Empty -> {
                    item {
                        EmptyStateCard(title = "No attendance records found for this semester.")
                    }
                }
                is Resource.Success -> {
                    val subjects = state.data

                    if (selectedTab == 0) {
                        // Subject-wise Attendance Cards with 75% Calculator
                        items(subjects, key = { it.courseCode }) { subject ->
                            SubjectAttendanceCard(subject = subject)
                        }
                    } else if (selectedTab == 1) {
                        // Attendance Session Log
                        item {
                            SectionFormCard(sectionTitle = "Date-wise Session Log") {
                                EmptyStateCard(title = "No attendance session records available.")
                            }
                        }
                    } else {
                        // Overall Summary Stats
                        item {
                            SectionFormCard(sectionTitle = "Overall Attendance Summary") {
                                val totalAttended = remember(subjects) { subjects.sumOf { it.attendedClasses } }
                                val totalClasses = remember(subjects) { subjects.sumOf { it.totalClasses } }
                                val overallPercentage = remember(subjects) { if (totalClasses == 0) 0f else (totalAttended.toFloat() / totalClasses) * 100f }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Overall Attendance",
                                            fontSize = 13.sp,
                                            color = MutedText
                                        )
                                        Text(
                                            text = "%.1f%%".format(overallPercentage),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (overallPercentage >= 75f) SecondaryEmerald else AccentCoral
                                        )
                                    }
                                    StatusPill(
                                        text = if (overallPercentage >= 75f) "Good Standing" else "Shortage Warning",
                                        tone = if (overallPercentage >= 75f) StatusTone.SUCCESS else StatusTone.DANGER
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Classes", fontSize = 11.sp, color = MutedText)
                                        Text("$totalClasses", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Attended", fontSize = 11.sp, color = MutedText)
                                        Text("$totalAttended", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SecondaryEmerald)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Missed", fontSize = 11.sp, color = MutedText)
                                        Text("${totalClasses - totalAttended}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentCoral)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectAttendanceCard(subject: AttendanceSubject) {
    val toneColor = when {
        subject.percentage >= 75f -> SecondaryEmerald
        subject.percentage >= 65f -> WarningAmber
        else -> AccentCoral
    }

    val toneBg = when {
        subject.percentage >= 75f -> SecondaryEmeraldBg
        subject.percentage >= 65f -> WarningAmberBg
        else -> AccentCoralBg
    }

    SectionFormCard(sectionTitle = "${subject.courseName} (${subject.courseCode})") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${subject.attendedClasses} / ${subject.totalClasses} Classes Attended",
                fontSize = 13.sp,
                color = MutedText
            )
            Text(
                text = "%.1f%%".format(subject.percentage),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = toneColor
            )
        }

        // Custom Horizontal Bar Chart Progress Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(toneBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (subject.percentage / 100f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(toneColor)
            )
        }

        // 75% Target Calculator Smart Advice Strip
        if (subject.percentage < 75f) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentCoralBg)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(16.dp))
                Text(
                    text = "Shortage Alert: You must attend next ${subject.classesNeededFor75} classes continuously to reach 75%.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCoral
                )
            }
        } else {
            val bunkable = subject.classesBunkable
            Text(
                text = if (bunkable > 0) "✅ Safe: You can afford to skip next $bunkable class(es) & stay above 75%." else "✅ Safe: Attendance is at or above 75% target.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryEmerald
            )
        }
    }
}
