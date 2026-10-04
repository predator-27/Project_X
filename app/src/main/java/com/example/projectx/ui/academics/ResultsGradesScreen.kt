package com.projectx.app.ui.academics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.*
import com.projectx.app.theme.*

data class GradeEntry(
    val courseCode: String,
    val courseName: String,
    val credits: Int,
    val grade: String,
    val points: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsGradesScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val publicProfile by academicViewModel.publicProfileState.collectAsState()

    val gradesList = remember {
        listOf(
            GradeEntry("CS201", "Data Structures & Algorithms", 4, "A+", 10.0),
            GradeEntry("CS202", "Database Management Systems", 4, "A", 9.0),
            GradeEntry("CS203", "Web Technologies & Frameworks", 3, "A+", 10.0),
            GradeEntry("MA201", "Discrete Mathematics", 3, "B+", 8.0)
        )
    }

    AppScaffold(
        title = "Academic Results & Grades",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Summary CGPA / SGPA Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Icon(Icons.Default.Grade, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(22.dp))
                                Text("Cumulative Grade Point Average", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                            }
                            StatusPill(text = "Semester 5", tone = StatusTone.SUCCESS)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CGPA", fontSize = 11.sp, color = MutedText)
                                Text("8.72 / 10.0", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryIndigo)
                            }

                            Column {
                                Text("SGPA", fontSize = 11.sp, color = MutedText)
                                Text("8.85", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SecondaryEmerald)
                            }

                            Column {
                                Text("Total Credits", fontSize = 11.sp, color = MutedText)
                                Text("84 Credits", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                            }
                        }
                    }
                }
            }

            item {
                Text("Subject-Wise Letter Grades", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
            }

            items(gradesList, key = { it.courseCode }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = InfoBannerBg
                                ) {
                                    Text(
                                        text = entry.courseCode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("${entry.credits} Credits", fontSize = 11.sp, color = MutedText)
                            }

                            Text(
                                text = entry.courseName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeadingNavy
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryIndigo.copy(alpha = 0.1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = entry.grade,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryIndigo
                                )
                                Text(
                                    text = "${entry.points} pts",
                                    fontSize = 10.sp,
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
