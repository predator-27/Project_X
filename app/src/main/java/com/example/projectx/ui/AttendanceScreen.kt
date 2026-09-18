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
import com.example.projectx.model.SampleCampusData
import com.example.projectx.theme.*

@Composable
fun AttendanceScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Subject-wise, 1 = Log, 2 = Summary
    var selectedSemester by remember { mutableStateOf("2026-2027, Semester - 3, BCA") }
    val subjects = SampleCampusData.sampleAttendance

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

            if (selectedTab == 0) {
                // Subject-wise Attendance Cards with 75% Calculator
                items(subjects, key = { it.courseCode }) { subject ->
                    SubjectAttendanceCard(subject = subject)
                }
            } else if (selectedTab == 1) {
                // Attendance Log
                item {
                    SectionFormCard(sectionTitle = "Date-wise Session Log") {
                        InfoStrip(message = "Detailed attendance session logs synchronized offline.")
                        Spacer(modifier = Modifier.height(8.dp))
                        DataTable(
                            headers = listOf("Date", "Subject", "Slot", "Status"),
                            rows = listOf(
                                listOf("17-Sep-2026", "Data Structures", "09:25 AM", "Present"),
                                listOf("16-Sep-2026", "DBMS", "10:30 AM", "Absent"),
                                listOf("15-Sep-2026", "Web Tech", "02:00 PM", "Present"),
                                listOf("14-Sep-2026", "Discrete Math", "11:30 AM", "Present")
                            )
                        )
                    }
                }
            } else {
                // Overall Summary Stats
                item {
                    SectionFormCard(sectionTitle = "Overall Attendance Summary") {
                        val totalAttended = subjects.sumOf { it.attendedClasses }
                        val totalClasses = subjects.sumOf { it.totalClasses }
                        val overallPercentage = (totalAttended.toFloat() / totalClasses) * 100f

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
