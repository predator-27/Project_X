package com.example.projectx.ui.academics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.model.Course
import com.example.projectx.theme.*
import com.example.projectx.util.Resource

@Composable
fun CoursesScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coursesState by academicViewModel.coursesState.collectAsState()
    val publicProfile by academicViewModel.publicProfileState.collectAsState()

    AppScaffold(
        title = "My Courses & Subjects",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Academic Profile Summary Card
            item {
                SectionFormCard(sectionTitle = "Academic Profile Info") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                            Text(
                                text = publicProfile?.schoolName?.ifBlank { null } ?: "Bennett University",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeadingNavy
                            )
                        }

                        val programText = listOfNotNull(
                            publicProfile?.program?.ifBlank { null },
                            publicProfile?.department?.ifBlank { null },
                            publicProfile?.specialization?.ifBlank { null }
                        ).joinToString(" • ").ifBlank { "Academic Info Not Set" }

                        Text(text = programText, fontSize = 13.sp, color = MutedText)

                        val yearSemText = listOfNotNull(
                            publicProfile?.admissionYear?.let { "Admission: $it" },
                            publicProfile?.currentSemester?.let { "Semester: $it" },
                            publicProfile?.section?.ifBlank { null }?.let { "Section: $it" }
                        ).joinToString(" | ")

                        if (yearSemText.isNotBlank()) {
                            Text(text = yearSemText, fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Courses Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enrolled Subjects",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeadingNavy
                    )
                    IconButton(onClick = { academicViewModel.loadCourses() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryIndigo)
                    }
                }
            }

            // State Handling
            when (val state = coursesState) {
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
                        SectionFormCard(sectionTitle = "Error Loading Courses") {
                            Text(text = state.message, fontSize = 13.sp, color = AccentCoral)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { academicViewModel.loadCourses() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is Resource.Empty -> {
                    item {
                        EmptyStateCard(
                            title = "No active courses enrolled for the current semester."
                        )
                    }
                }
                is Resource.Success -> {
                    items(state.data, key = { it.courseCode }) { course ->
                        CourseCard(course = course)
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
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
                        text = course.courseName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeadingNavy
                    )
                    Text(
                        text = "Code: ${course.courseCode} • Credits: ${course.credits}",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
                StatusPill(text = "${course.credits} Credits", tone = StatusTone.NEUTRAL)
            }

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                Text(
                    text = "Faculty: ${course.facultyName.ifBlank { "TBD" }}",
                    fontSize = 13.sp,
                    color = BodyText,
                    fontWeight = FontWeight.Medium
                )
            }

            if (course.schoolName.isNotBlank() || course.department.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                    Text(
                        text = "${course.department} (${course.schoolName})",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }
        }
    }
}
