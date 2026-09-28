package com.example.projectx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectx.components.*
import com.example.projectx.model.UniversityAnnouncement
import com.example.projectx.model.UserRole
import com.example.projectx.model.lms.AnnouncementPriority
import com.example.projectx.theme.*
import com.example.projectx.ui.auth.AuthSessionState
import com.example.projectx.ui.auth.AuthViewModel
import com.example.projectx.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onMenuClick: () -> Unit,
    announcementViewModel: AnnouncementViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val isCollegeAdminOrSuperAdmin = remember(sessionState) {
        val authSession = sessionState as? AuthSessionState.Authenticated
        authSession?.role == UserRole.COLLEGE_ADMIN || authSession?.role == UserRole.SUPER_ADMIN
    }

    val authorDisplayName = remember(sessionState) {
        val authSession = sessionState as? AuthSessionState.Authenticated
        authSession?.publicProfile?.displayName?.ifBlank { null }
            ?: authSession?.user?.email?.substringBefore("@")
            ?: "University Administration"
    }

    val announcementsState by announcementViewModel.announcementsState.collectAsState()
    val actionState by announcementViewModel.actionState.collectAsState()
    val searchQuery by announcementViewModel.searchQuery.collectAsState()

    var selectedAnnouncementForDetail by remember { mutableStateOf<UniversityAnnouncement?>(null) }
    var editingAnnouncement by remember { mutableStateOf<UniversityAnnouncement?>(null) }
    var deletingAnnouncementId by remember { mutableStateOf<String?>(null) }
    var showPublishDialog by remember { mutableStateOf(false) }

    AppScaffold(
        title = "Communication Inbox",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "University Notices & Circulars",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HeadingNavy
                                )
                                Text(
                                    text = "Official Bennett University Communications",
                                    fontSize = 11.sp,
                                    color = MutedText
                                )
                            }
                        }

                        IconButton(onClick = { announcementViewModel.loadAnnouncements() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Search Bar & Admin Publish Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { announcementViewModel.setSearchQuery(it) },
                        placeholder = { Text("Search title, content, or department...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryIndigo) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { announcementViewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedText)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    if (isCollegeAdminOrSuperAdmin) {
                        Button(
                            onClick = { showPublishDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Action State Error Alert
            if (actionState is Resource.Error) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (actionState as Resource.Error).message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { announcementViewModel.resetActionState() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Announcement Feed List
            when (val state = announcementsState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryIndigo, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = state.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                                TextButton(onClick = { announcementViewModel.loadAnnouncements() }) {
                                    Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is Resource.Empty -> {
                    item {
                        EmptyStateCard(title = "No university announcements published.")
                    }
                }
                is Resource.Success -> {
                    val filteredList = if (searchQuery.isBlank()) {
                        state.data
                    } else {
                        val q = searchQuery.lowercase()
                        state.data.filter {
                            it.title.lowercase().contains(q) ||
                                    it.content.lowercase().contains(q) ||
                                    it.authorName.lowercase().contains(q) ||
                                    it.targetDepartment.lowercase().contains(q)
                        }
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            EmptyStateCard(title = "No announcements match \"$searchQuery\".")
                        }
                    } else {
                        items(filteredList, key = { it.id }) { ann ->
                            UniversityAnnouncementRowCard(
                                announcement = ann,
                                isAdmin = isCollegeAdminOrSuperAdmin,
                                onClick = { selectedAnnouncementForDetail = ann },
                                onEditClick = { editingAnnouncement = ann },
                                onDeleteClick = { deletingAnnouncementId = ann.id }
                            )
                        }
                    }
                }
            }
        }

        // Announcement Detail Dialog
        selectedAnnouncementForDetail?.let { ann ->
            AnnouncementDetailDialog(
                announcement = ann,
                onDismiss = { selectedAnnouncementForDetail = null }
            )
        }

        // Admin Publish Dialog
        if (showPublishDialog) {
            PublishAnnouncementDialog(
                isSubmitting = actionState is Resource.Loading,
                authorName = authorDisplayName,
                onDismiss = { showPublishDialog = false },
                onConfirm = { title, content, targetDept, priority ->
                    announcementViewModel.createAnnouncement(
                        title = title,
                        content = content,
                        targetDepartment = targetDept,
                        priority = priority
                    )
                    showPublishDialog = false
                }
            )
        }

        // Admin Edit Dialog
        editingAnnouncement?.let { ann ->
            PublishAnnouncementDialog(
                existingAnnouncement = ann,
                isSubmitting = actionState is Resource.Loading,
                authorName = ann.authorName,
                onDismiss = { editingAnnouncement = null },
                onConfirm = { title, content, targetDept, priority ->
                    announcementViewModel.updateAnnouncement(
                        id = ann.id,
                        title = title,
                        content = content,
                        targetDepartment = targetDept,
                        priority = priority
                    )
                    editingAnnouncement = null
                }
            )
        }

        // Admin Delete Confirmation Dialog
        deletingAnnouncementId?.let { id ->
            AlertDialog(
                onDismissRequest = { deletingAnnouncementId = null },
                title = { Text("Delete Announcement?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this official notice? This action cannot be undone.", fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            announcementViewModel.deleteAnnouncement(id)
                            deletingAnnouncementId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingAnnouncementId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun UniversityAnnouncementRowCard(
    announcement: UniversityAnnouncement,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = dateStr, fontSize = 11.sp, color = MutedText)

                    if (isAdmin) {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryIndigo, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementDetailDialog(
    announcement: UniversityAnnouncement,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = announcement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = HeadingNavy,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = announcement.priority.name,
                    tone = if (announcement.priority == AnnouncementPriority.URGENT) StatusTone.DANGER else StatusTone.NEUTRAL
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val dateStr = remember(announcement.createdAt) {
                    if (announcement.createdAt > 0) {
                        SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault()).format(Date(announcement.createdAt))
                    } else {
                        "Recently"
                    }
                }

                Text(
                    text = "Author: ${announcement.authorName} | Dept: ${announcement.targetDepartment}",
                    fontSize = 12.sp,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Date: $dateStr",
                    fontSize = 11.sp,
                    color = MutedText
                )

                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                Text(
                    text = announcement.content,
                    fontSize = 13.sp,
                    color = BodyText
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PublishAnnouncementDialog(
    existingAnnouncement: UniversityAnnouncement? = null,
    isSubmitting: Boolean,
    authorName: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, targetDept: String, priority: AnnouncementPriority) -> Unit
) {
    var title by remember(existingAnnouncement) { mutableStateOf(existingAnnouncement?.title ?: "") }
    var content by remember(existingAnnouncement) { mutableStateOf(existingAnnouncement?.content ?: "") }
    var targetDept by remember(existingAnnouncement) { mutableStateOf(existingAnnouncement?.targetDepartment ?: "ALL") }
    var priority by remember(existingAnnouncement) { mutableStateOf(existingAnnouncement?.priority ?: AnnouncementPriority.NORMAL) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isEdit = existingAnnouncement != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Announcement" else "Publish University Announcement", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (validationError != null) {
                    Text(text = validationError!!, fontSize = 12.sp, color = AccentCoral)
                }

                Text(
                    text = "Publisher: $authorName",
                    fontSize = 12.sp,
                    color = MutedText
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; validationError = null },
                    label = { Text("Announcement Title") },
                    placeholder = { Text("e.g. Mid-Term Examination Guidelines") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )

                OutlinedTextField(
                    value = targetDept,
                    onValueChange = { targetDept = it; validationError = null },
                    label = { Text("Target Department / Audience") },
                    placeholder = { Text("e.g. ALL, Computer Science, BCA") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Priority Level:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = priority == AnnouncementPriority.NORMAL,
                            onClick = { priority = AnnouncementPriority.NORMAL },
                            label = { Text("NORMAL", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = priority == AnnouncementPriority.URGENT,
                            onClick = { priority = AnnouncementPriority.URGENT },
                            label = { Text("URGENT", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it; validationError = null },
                    label = { Text("Announcement Content") },
                    placeholder = { Text("Enter full notice text here...") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    enabled = !isSubmitting
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank() || targetDept.isBlank()) {
                        validationError = "Please fill in title, content, and target department."
                    } else {
                        onConfirm(title, content, targetDept, priority)
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isEdit) "Update Announcement" else "Publish Announcement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        }
    )
}
