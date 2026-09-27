package com.example.projectx.ui.lms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectx.components.*
import com.example.projectx.model.lms.AnnouncementPriority
import com.example.projectx.model.lms.Assignment
import com.example.projectx.model.lms.CourseAnnouncement
import com.example.projectx.model.lms.CourseMaterial
import com.example.projectx.theme.*
import com.example.projectx.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseCode: String,
    courseName: String? = null,
    onBackClick: () -> Unit,
    onAssignmentClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    viewModel: CourseDetailViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Materials, 1 = Assignments, 2 = Announcements

    val materialsState by viewModel.materialsState.collectAsState()
    val assignmentsState by viewModel.assignmentsState.collectAsState()
    val announcementsState by viewModel.announcementsState.collectAsState()

    LaunchedEffect(courseCode) {
        viewModel.loadCourseLmsData(courseCode)
    }

    AppScaffold(
        title = "Course: $courseCode",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Card with Back Button and Course Info
            item {
                SectionFormCard(sectionTitle = "Course LMS Portal") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PrimaryIndigo
                                )
                            }
                            Column {
                                Text(
                                    text = courseName?.ifBlank { null } ?: "Course $courseCode",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HeadingNavy
                                )
                                Text(
                                    text = "Code: $courseCode",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.loadCourseLmsData(courseCode) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryIndigo)
                        }
                    }
                }
            }

            // Sub-Section Tab Row
            item {
                UnderlineTabRow(
                    tabs = listOf("Materials", "Assignments", "Announcements"),
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tab Content Rendering
            when (selectedTab) {
                0 -> {
                    // Materials Section
                    when (val state = materialsState) {
                        is Resource.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryIndigo)
                                }
                            }
                        }
                        is Resource.Error -> {
                            item {
                                SectionFormCard(sectionTitle = "Materials Error") {
                                    Text(text = state.message, fontSize = 13.sp, color = AccentCoral)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.refreshMaterials() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        is Resource.Empty -> {
                            item {
                                EmptyStateCard(title = "No materials available.")
                            }
                        }
                        is Resource.Success -> {
                            items(state.data, key = { it.materialId }) { material ->
                                MaterialItemCard(material = material)
                            }
                        }
                    }
                }
                1 -> {
                    // Assignments Section
                    when (val state = assignmentsState) {
                        is Resource.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryIndigo)
                                }
                            }
                        }
                        is Resource.Error -> {
                            item {
                                SectionFormCard(sectionTitle = "Assignments Error") {
                                    Text(text = state.message, fontSize = 13.sp, color = AccentCoral)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.refreshAssignments() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        is Resource.Empty -> {
                            item {
                                EmptyStateCard(title = "No assignments available.")
                            }
                        }
                        is Resource.Success -> {
                            items(state.data, key = { it.assignmentId }) { assignment ->
                                AssignmentItemCard(
                                    assignment = assignment,
                                    onClick = { onAssignmentClick(assignment.assignmentId) }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Announcements Section
                    when (val state = announcementsState) {
                        is Resource.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryIndigo)
                                }
                            }
                        }
                        is Resource.Error -> {
                            item {
                                SectionFormCard(sectionTitle = "Announcements Error") {
                                    Text(text = state.message, fontSize = 13.sp, color = AccentCoral)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.refreshAnnouncements() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        is Resource.Empty -> {
                            item {
                                EmptyStateCard(title = "No announcements available.")
                            }
                        }
                        is Resource.Success -> {
                            items(state.data, key = { it.announcementId }) { announcement ->
                                AnnouncementItemCard(announcement = announcement)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialItemCard(material: CourseMaterial) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
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
                Text(
                    text = material.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(text = material.fileType, tone = StatusTone.NEUTRAL)
            }

            if (!material.description.isNullOrBlank()) {
                Text(text = material.description, fontSize = 12.sp, color = MutedText)
            }

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedDate = remember(material.createdAt) {
                    SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(material.createdAt))
                }
                val formattedSize = remember(material.fileSizeBytes) {
                    if (material.fileSizeBytes > 0) "%.1f MB".format(material.fileSizeBytes / (1024f * 1024f)) else ""
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                    Text(
                        text = material.uploadedByName.ifBlank { "Faculty" },
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                Text(
                    text = listOfNotNull(formattedSize.ifBlank { null }, formattedDate).joinToString(" • "),
                    fontSize = 11.sp,
                    color = MutedText
                )
            }
        }
    }
}

@Composable
fun AssignmentItemCard(
    assignment: Assignment,
    onClick: () -> Unit
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(text = "${assignment.maxPoints} Pts", tone = StatusTone.NEUTRAL)
            }

            if (assignment.description.isNotBlank()) {
                Text(
                    text = assignment.description,
                    fontSize = 12.sp,
                    color = MutedText,
                    maxLines = 2
                )
            }

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
                    val dueText = remember(assignment.dueDateTimestamp) {
                        if (assignment.dueDateTimestamp != null) {
                            "Due: " + SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault()).format(Date(assignment.dueDateTimestamp))
                        } else {
                            "Due date not specified"
                        }
                    }
                    Text(text = dueText, fontSize = 11.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium)
                }

                if (!assignment.attachmentUrl.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = MutedText, modifier = Modifier.size(12.dp))
                        Text(text = "Attachment", fontSize = 11.sp, color = MutedText)
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementItemCard(announcement: CourseAnnouncement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
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
                Text(
                    text = announcement.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = announcement.priority.name,
                    tone = if (announcement.priority == AnnouncementPriority.URGENT) StatusTone.DANGER else StatusTone.NEUTRAL
                )
            }

            Text(text = announcement.content, fontSize = 13.sp, color = BodyText)

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedDate = remember(announcement.createdAt) {
                    SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault()).format(Date(announcement.createdAt))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                    Text(
                        text = announcement.authorName.ifBlank { "Faculty" },
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                Text(text = formattedDate, fontSize = 11.sp, color = MutedText)
            }
        }
    }
}
