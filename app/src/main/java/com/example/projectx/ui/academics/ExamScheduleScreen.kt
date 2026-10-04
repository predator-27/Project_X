package com.projectx.app.ui.academics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.*
import com.projectx.app.theme.*

data class ExamSlot(
    val examId: String,
    val courseCode: String,
    val courseName: String,
    val examDate: String,
    val timeSlot: String,
    val venue: String,
    val seatCode: String,
    val examType: String = "Mid-Term"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScheduleScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val publicProfile by academicViewModel.publicProfileState.collectAsState()

    val examSchedule = remember {
        listOf(
            ExamSlot("ex_1", "CS201", "Data Structures & Algorithms", "2026-10-05", "09:30 AM - 11:30 AM", "Block N1 — Room 204", "seat_c304", "Mid-Term"),
            ExamSlot("ex_2", "CS202", "Database Management Systems", "2026-10-07", "02:00 PM - 04:00 PM", "Block N1 — Lab 102", "seat_lab102", "Mid-Term Practical"),
            ExamSlot("ex_3", "CS203", "Web Technologies & Frameworks", "2026-10-09", "09:30 AM - 11:30 AM", "Block N1 — Room 205", "seat_c305", "Mid-Term"),
            ExamSlot("ex_4", "MA201", "Discrete Mathematics", "2026-10-12", "02:00 PM - 04:00 PM", "Block N1 — Room 301", "Desk #401", "Mid-Term")
        )
    }

    AppScaffold(
        title = "Exam Schedule & Venues",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Banner Card
            item {
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(22.dp))
                                Text("Mid-Term Examinations 2026", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                            }
                            StatusPill(text = "Hall Ticket Active", tone = StatusTone.SUCCESS)
                        }

                        val dept = publicProfile?.department?.ifBlank { null } ?: "Computer Science"
                        val sec = publicProfile?.section?.ifBlank { null } ?: "Sec-A"
                        Text(
                            text = "Official examination date sheet and seat allocation for $dept ($sec).",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
            }

            item {
                Text("Chronological Date Sheet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
            }

            items(examSchedule, key = { it.examId }) { exam ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
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
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = InfoBannerBg
                            ) {
                                Text(
                                    text = "${exam.courseCode} • ${exam.examType}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "📅 ${exam.examDate}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeadingNavy
                            )
                        }

                        Text(
                            text = exam.courseName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeadingNavy
                        )

                        HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(14.dp))
                                Text(
                                    text = exam.timeSlot,
                                    fontSize = 11.sp,
                                    color = BodyText,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${exam.venue} (${exam.seatCode})",
                                    fontSize = 11.sp,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
