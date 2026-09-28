package com.example.projectx.ui.academics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.R
import com.example.projectx.components.*
import com.example.projectx.model.Course
import com.example.projectx.theme.*
import com.example.projectx.util.Resource

@Composable
fun CoursesScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    onCourseClick: (Course) -> Unit = {},
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Academic Profile Summary Banner Card
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
                                    contentDescription = "Bennett Logo",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = publicProfile?.schoolName?.ifBlank { null } ?: "Bennett University",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo
                            )

                            val displayName = publicProfile?.displayName?.ifBlank { null } ?: "Academic Profile"
                            Text(
                                text = displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = HeadingNavy
                            )

                            val programText = listOfNotNull(
                                publicProfile?.program?.ifBlank { null },
                                publicProfile?.department?.ifBlank { null },
                                publicProfile?.section?.ifBlank { null }?.let { "Sec: $it" }
                            ).joinToString(" • ").ifBlank { "Academic Profile Setup Pending" }

                            Text(
                                text = programText,
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }
                }
            }

            // Courses Header with Refresh Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Enrolled Curriculum",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeadingNavy
                        )
                    }

                    IconButton(onClick = { academicViewModel.loadCourses() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
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
                            CircularProgressIndicator(color = PrimaryIndigo, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { academicViewModel.loadCourses() }) {
                                    Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
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
                        CourseCard(
                            course = course,
                            onClick = { onCourseClick(course) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    shape = RoundedCornerShape(8.dp),
                    color = InfoBannerBg
                ) {
                    Text(
                        text = course.courseCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                StatusPill(text = "${course.credits} Credits", tone = StatusTone.NEUTRAL)
            }

            Text(
                text = course.courseName,
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
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Faculty: ${course.facultyName.ifBlank { "TBD" }}",
                        fontSize = 12.sp,
                        color = BodyText,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (course.department.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                        Text(
                            text = course.department,
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
            }
        }
    }
}
