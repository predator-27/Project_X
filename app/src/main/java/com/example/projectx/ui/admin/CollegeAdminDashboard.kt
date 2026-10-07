package com.projectx.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.*
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.model.lms.AnnouncementPriority
import com.projectx.app.theme.CampusTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeAdminDashboard(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val colors = CampusTokens.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Students, 1 = Faculty, 2 = Announcements

    val studentsList = remember { DemoCampusData.demoStudents }
    val facultyList = remember { DemoCampusData.demoFaculty }
    val announcementsList = remember { DemoCampusData.demoUniversityAnnouncements }

    val filteredStudents = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) studentsList else studentsList.filter {
            (it.email?.lowercase()?.contains(q) == true) || (it.rollNumber?.lowercase()?.contains(q) == true)
        }
    }

    AppScaffold(
        title = "College Admin Portal",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.surfaceBorder)
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
                                text = "College Administration — Bennett University",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.heading
                            )
                            StatusPill(text = "College Office", tone = StatusTone.SUCCESS)
                        }

                        Text(
                            text = "Manage student directory, faculty allocations, announcements, and academic operations.",
                            fontSize = 12.sp,
                            color = colors.mutedText
                        )
                    }
                }
            }

            // Summary Metrics Grid
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.School, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                Text("Students", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("${studentsList.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = colors.successGreen, modifier = Modifier.size(14.dp))
                                Text("Faculty", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("${facultyList.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = colors.warningAmber, modifier = Modifier.size(14.dp))
                                Text("Notices", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("${announcementsList.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Class, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                Text("Courses", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("64", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }
                }
            }

            // Tab Switcher
            item {
                TabRow(selectedTabIndex = selectedTab, containerColor = colors.surface) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Students", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp), color = if (selectedTab == 0) colors.primary else colors.mutedText)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Faculty", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp), color = if (selectedTab == 1) colors.primary else colors.mutedText)
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("Announcements", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp), color = if (selectedTab == 2) colors.primary else colors.mutedText)
                    }
                }
            }

            // Search Bar
            if (selectedTab == 0) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search student by email or roll number...", fontSize = 12.sp, color = colors.mutedText) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.mutedText) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedTextColor = colors.heading,
                            unfocusedTextColor = colors.heading,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.surfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(filteredStudents, key = { it.uid }) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(student.email ?: "", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.heading)
                                Text("Roll No: ${student.rollNumber ?: "N/A"} • Semester 5", fontSize = 11.sp, color = colors.mutedText)
                            }
                            StatusPill(
                                text = if (student.isActive) "Active" else "Inactive",
                                tone = if (student.isActive) StatusTone.SUCCESS else StatusTone.DANGER
                            )
                        }
                    }
                }
            } else if (selectedTab == 1) {
                items(facultyList, key = { it.id }) { teacher ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(teacher.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.heading)
                                StatusPill(text = teacher.status.label, tone = StatusTone.NEUTRAL)
                            }
                            Text("${teacher.title} • ${teacher.department}", fontSize = 12.sp, color = colors.primary, fontWeight = FontWeight.SemiBold)
                            Text("📍 Desk: ${teacher.deskNumber} • ${teacher.email}", fontSize = 11.sp, color = colors.mutedText)
                        }
                    }
                }
            } else {
                items(announcementsList, key = { it.id }) { notice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(notice.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.heading, modifier = Modifier.weight(1f))
                                StatusPill(
                                    text = notice.priority.name,
                                    tone = if (notice.priority == AnnouncementPriority.URGENT) StatusTone.DANGER else StatusTone.NEUTRAL
                                )
                            }
                            Text(notice.content, fontSize = 12.sp, color = colors.bodyText)
                            Text("Publisher: ${notice.authorName} • Target: ${notice.targetDepartment}", fontSize = 11.sp, color = colors.mutedText)
                        }
                    }
                }
            }
        }
    }
}
