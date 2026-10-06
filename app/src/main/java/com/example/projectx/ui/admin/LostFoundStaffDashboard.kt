package com.projectx.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.*
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.model.LostItem
import com.projectx.app.model.LostItemStatus
import com.projectx.app.theme.*
import com.projectx.app.ui.LostFoundViewModel
import com.projectx.app.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostFoundStaffDashboard(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    lostFoundViewModel: LostFoundViewModel = viewModel()
) {
    val colors = CampusTokens.colors

    LaunchedEffect(Unit) {
        lostFoundViewModel.loadStaffQueue()
    }

    val staffQueueState by lostFoundViewModel.staffQueueState.collectAsState()
    val itemsList = (staffQueueState as? Resource.Success)?.data ?: DemoCampusData.demoLostItems.toList()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    // Dialog & Sheet States
    var selectedItemForClaimReview by remember { mutableStateOf<LostItem?>(null) }
    var selectedItemForStatusUpdate by remember { mutableStateOf<LostItem?>(null) }
    var selectedItemForEdit by remember { mutableStateOf<LostItem?>(null) }
    var selectedItemForNote by remember { mutableStateOf<LostItem?>(null) }
    var selectedItemForTimeline by remember { mutableStateOf<LostItem?>(null) }

    val pendingCount = itemsList.count { it.status == LostItemStatus.REPORTED }
    val claimCount = itemsList.count { it.status == LostItemStatus.CLAIM_SUBMITTED }
    val verifiedCount = itemsList.count { it.status == LostItemStatus.VERIFIED }
    val handoverCount = itemsList.count { it.status == LostItemStatus.HANDOVER_COMPLETE }

    val filteredItems = remember(itemsList, searchQuery, selectedStatusFilter) {
        val q = searchQuery.trim().lowercase()
        itemsList.filter { item ->
            val matchesQuery = q.isEmpty() ||
                    item.title.lowercase().contains(q) ||
                    item.description.lowercase().contains(q) ||
                    item.locationFound.lowercase().contains(q) ||
                    (item.claimantPhone?.contains(q) == true)

            val matchesStatus = when (selectedStatusFilter) {
                "Reported" -> item.status == LostItemStatus.REPORTED
                "Claims Pending" -> item.status == LostItemStatus.CLAIM_SUBMITTED
                "Verified" -> item.status == LostItemStatus.VERIFIED
                "Handed Over" -> item.status == LostItemStatus.HANDOVER_COMPLETE
                else -> true
            }

            matchesQuery && matchesStatus
        }
    }

    AppScaffold(
        title = "Lost & Found Staff Portal",
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
                                text = "Lost & Found Operations Control",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.heading
                            )
                            StatusPill(text = "Lost & Found Desk", tone = StatusTone.SUCCESS)
                        }

                        Text(
                            text = "Item verification, student claim approvals, listing updates, and physical handover logging.",
                            fontSize = 12.sp,
                            color = colors.mutedText
                        )
                    }
                }
            }

            // Metrics Summary Grid
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
                            Text("Reported", fontSize = 11.sp, color = colors.mutedText)
                            Text("$pendingCount Items", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Claims Pending", fontSize = 11.sp, color = colors.mutedText)
                            Text("$claimCount Claims", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.warningAmber)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Verified", fontSize = 11.sp, color = colors.mutedText)
                            Text("$verifiedCount Items", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Handed Over", fontSize = 11.sp, color = colors.mutedText)
                            Text("$handoverCount Items", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.successGreen)
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title, location, or phone...", fontSize = 12.sp, color = colors.mutedText) },
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

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("All", "Reported", "Claims Pending", "Verified", "Handed Over").forEach { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                            label = { Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Inventory & Claim Management (${filteredItems.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.heading
                )
            }

            if (filteredItems.isEmpty()) {
                item {
                    EmptyStateCard(title = "No lost & found items match your filter criteria.")
                }
            } else {
                items(filteredItems, key = { it.itemId }) { item ->
                    StaffLostItemCard(
                        item = item,
                        onReviewClaim = { selectedItemForClaimReview = item },
                        onUpdateStatus = { selectedItemForStatusUpdate = item },
                        onEdit = { selectedItemForEdit = item },
                        onAddNote = { selectedItemForNote = item },
                        onViewTimeline = { selectedItemForTimeline = item }
                    )
                }
            }
        }

        // --- DIALOGS & SHEETS ---

        // 1. Claim Review Dialog
        selectedItemForClaimReview?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedItemForClaimReview = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = colors.primary)
                        Text("Claim Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.heading)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Item: ${item.title}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.heading)
                        Text("Location Found: ${item.locationFound}", fontSize = 12.sp, color = colors.bodyText)

                        HorizontalDivider(color = colors.surfaceBorder)

                        Text("Student Claimant Information:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.heading)
                        Text("Phone: ${item.claimantPhone ?: "Not provided"}", fontSize = 12.sp, color = colors.bodyText)
                        Text("Claim Evidence/Notes: ${item.claimNotes ?: "None"}", fontSize = 12.sp, color = colors.mutedText)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.primary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Verification Checklist:\n✓ Match student ID card\n✓ Verify property evidence notes\n✓ Record handover timestamp",
                                fontSize = 11.sp,
                                color = colors.heading,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lostFoundViewModel.verifyClaim(
                                itemId = item.itemId,
                                staffNotes = "Claim approved at security desk after student ID match."
                            )
                            selectedItemForClaimReview = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.successGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Approve Claim")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            lostFoundViewModel.rejectClaim(
                                itemId = item.itemId,
                                staffNotes = "Claim rejected: property description mismatch."
                            )
                            selectedItemForClaimReview = null
                        }
                    ) {
                        Text("Reject Claim", color = colors.dangerRed)
                    }
                }
            )
        }

        // 2. Status Change Dialog (Sensible Transitions)
        selectedItemForStatusUpdate?.let { item ->
            val allowedStatuses = when (item.status) {
                LostItemStatus.REPORTED -> listOf(LostItemStatus.REPORTED, LostItemStatus.CANCELLED)
                LostItemStatus.CLAIM_SUBMITTED -> listOf(LostItemStatus.CLAIM_SUBMITTED, LostItemStatus.VERIFIED, LostItemStatus.CANCELLED)
                LostItemStatus.VERIFIED -> listOf(LostItemStatus.VERIFIED, LostItemStatus.HANDOVER_COMPLETE, LostItemStatus.CANCELLED)
                LostItemStatus.HANDOVER_COMPLETE -> listOf(LostItemStatus.HANDOVER_COMPLETE)
                LostItemStatus.CANCELLED -> listOf(LostItemStatus.CANCELLED)
            }
            var chosenStatus by remember(item) { mutableStateOf(item.status) }

            AlertDialog(
                onDismissRequest = { selectedItemForStatusUpdate = null },
                title = { Text("Update Item Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.heading) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        allowedStatuses.forEach { st ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { chosenStatus = st }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                RadioButton(selected = chosenStatus == st, onClick = { chosenStatus = st })
                                Spacer(Modifier.width(6.dp))
                                Text(st.label, fontSize = 13.sp, color = colors.bodyText)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lostFoundViewModel.updateItemStatus(item.itemId, chosenStatus)
                            selectedItemForStatusUpdate = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Save Status")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedItemForStatusUpdate = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 3. Edit Listing Dialog
        selectedItemForEdit?.let { item ->
            var editTitle by remember(item) { mutableStateOf(item.title) }
            var editLocation by remember(item) { mutableStateOf(item.locationFound) }
            var editDesc by remember(item) { mutableStateOf(item.description) }

            AlertDialog(
                onDismissRequest = { selectedItemForEdit = null },
                title = { Text("Edit Listing Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.heading) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Item Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editLocation,
                            onValueChange = { editLocation = it },
                            label = { Text("Location Found") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDesc,
                            onValueChange = { editDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lostFoundViewModel.editItemListing(item.itemId, editTitle, editLocation, editDesc)
                            selectedItemForEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedItemForEdit = null }) { Text("Cancel") }
                }
            )
        }

        // 4. Staff Internal Note Dialog
        selectedItemForNote?.let { item ->
            var noteText by remember(item) { mutableStateOf(item.staffNotes ?: "") }

            AlertDialog(
                onDismissRequest = { selectedItemForNote = null },
                title = { Text("Add Internal Staff Note", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.heading) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Staff notes are logged internally for desk auditing.", fontSize = 11.sp, color = colors.mutedText)
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("e.g. Stored at Lost & Found desk locker 3. Student notified.") },
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lostFoundViewModel.addStaffNote(item.itemId, noteText)
                            selectedItemForNote = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Save Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedItemForNote = null }) { Text("Cancel") }
                }
            )
        }

        // 5. Activity / Timeline Dialog
        selectedItemForTimeline?.let { item ->
            val sdf = remember { SimpleDateFormat("d MMM yyyy, h:mm a", Locale.ENGLISH) }
            AlertDialog(
                onDismissRequest = { selectedItemForTimeline = null },
                title = { Text("Item Activity Timeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.heading) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("• Report Created: ${if (item.createdAt > 0) sdf.format(Date(item.createdAt)) else "Unknown"}", fontSize = 12.sp, color = colors.bodyText)
                        if (item.claimTimestamp != null) {
                            Text("• Claim Submitted: ${sdf.format(Date(item.claimTimestamp))}", fontSize = 12.sp, color = colors.bodyText)
                        }
                        if (item.verificationTimestamp != null) {
                            Text("• Claim Approved/Verified: ${sdf.format(Date(item.verificationTimestamp))}", fontSize = 12.sp, color = colors.bodyText)
                        }
                        if (item.handoverTimestamp != null) {
                            Text("• Item Returned / Handover Complete: ${sdf.format(Date(item.handoverTimestamp))}", fontSize = 12.sp, color = colors.bodyText)
                        }
                        if (!item.staffNotes.isNullOrBlank()) {
                            HorizontalDivider(color = colors.surfaceBorder)
                            Text("Internal Staff Notes:\n${item.staffNotes}", fontSize = 11.sp, color = colors.mutedText)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedItemForTimeline = null }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
private fun StaffLostItemCard(
    item: LostItem,
    onReviewClaim: () -> Unit,
    onUpdateStatus: () -> Unit,
    onEdit: () -> Unit,
    onAddNote: () -> Unit,
    onViewTimeline: () -> Unit
) {
    val colors = CampusTokens.colors
    val sdf = remember { SimpleDateFormat("d MMM, h:mm a", Locale.ENGLISH) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.surfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.heading,
                    modifier = Modifier.weight(1f)
                )

                StatusPill(
                    text = item.status.label,
                    tone = when (item.status) {
                        LostItemStatus.REPORTED -> StatusTone.NEUTRAL
                        LostItemStatus.CLAIM_SUBMITTED -> StatusTone.WARNING
                        LostItemStatus.VERIFIED -> StatusTone.SUCCESS
                        LostItemStatus.HANDOVER_COMPLETE -> StatusTone.SUCCESS
                        LostItemStatus.CANCELLED -> StatusTone.DANGER
                    }
                )
            }

            Text(
                text = "📍 ${item.locationFound} • ${sdf.format(Date(item.createdAt))}",
                fontSize = 11.sp,
                color = colors.mutedText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = colors.bodyText
            )

            if (!item.claimantPhone.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.warningAmberBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = colors.warningAmber, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Claimant Phone: ${item.claimantPhone} | Notes: ${item.claimNotes ?: "None"}",
                            fontSize = 11.sp,
                            color = colors.heading
                        )
                    }
                }
            }

            if (!item.staffNotes.isNullOrBlank()) {
                Text(
                    text = "📝 Staff Note: ${item.staffNotes}",
                    fontSize = 11.sp,
                    color = colors.mutedText,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Listing", tint = colors.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onAddNote, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Add Note", tint = colors.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onViewTimeline, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.History, contentDescription = "Timeline", tint = colors.mutedText, modifier = Modifier.size(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.status == LostItemStatus.CLAIM_SUBMITTED) {
                        Button(
                            onClick = onReviewClaim,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.warningAmber),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Review Claim", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    OutlinedButton(
                        onClick = onUpdateStatus,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, colors.surfaceBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Status", fontSize = 11.sp, color = colors.heading, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
