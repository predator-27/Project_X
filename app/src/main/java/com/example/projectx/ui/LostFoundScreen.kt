package com.example.projectx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
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
import com.example.projectx.model.LostItem
import com.example.projectx.model.LostItemStatus
import com.example.projectx.model.UserRole
import com.example.projectx.theme.*
import com.example.projectx.ui.auth.AuthSessionState
import com.example.projectx.ui.auth.AuthViewModel
import com.example.projectx.util.Resource
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostFoundScreen(
    onMenuClick: () -> Unit,
    viewModel: LostFoundViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUid = currentUser?.uid ?: ""

    val sessionState by authViewModel.sessionState.collectAsState()
    val isStaffOrAdmin = remember(sessionState) {
        val authSession = sessionState as? AuthSessionState.Authenticated
        authSession?.role == UserRole.LOST_FOUND_STAFF || authSession?.role == UserRole.SUPER_ADMIN
    }

    val itemsState by viewModel.itemsState.collectAsState()
    val myClaimsState by viewModel.myClaimsState.collectAsState()
    val staffQueueState by viewModel.staffQueueState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Browse, 1 = Report, 2 = My Claims, 3 = Staff Queue
    var claimingItem by remember { mutableStateOf<LostItem?>(null) }
    var staffActionItem by remember { mutableStateOf<Pair<LostItem, String>?>(null) } // Item + Action Type ("VERIFY", "REJECT", "HANDOVER")

    LaunchedEffect(selectedTab, currentUid) {
        when (selectedTab) {
            0 -> viewModel.loadBrowseableItems()
            2 -> if (currentUid.isNotBlank()) viewModel.loadMyClaims(currentUid)
            3 -> if (isStaffOrAdmin) viewModel.loadStaffQueue()
        }
    }

    AppScaffold(
        title = "Lost & Found Portal",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Card
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
                            imageVector = Icons.Default.FindInPage,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Campus Lost & Found Desk",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeadingNavy
                            )
                            Text(
                                text = "Project X Digital Repository",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }

                    IconButton(onClick = {
                        when (selectedTab) {
                            0 -> viewModel.loadBrowseableItems()
                            2 -> viewModel.loadMyClaims(currentUid)
                            3 -> viewModel.loadStaffQueue()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Tab Row
            val tabs = remember(isStaffOrAdmin) {
                if (isStaffOrAdmin) {
                    listOf("Browse Items", "Report Found Item", "My Claims", "Staff Review Queue")
                } else {
                    listOf("Browse Items", "Report Found Item", "My Claims")
                }
            }

            UnderlineTabRow(
                tabs = tabs,
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Action State Error Alert
            if (actionState is Resource.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (actionState as Resource.Error).message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.resetActionState() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: Browse Items
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search by title, location, description...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryIndigo) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedText)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        when (val state = itemsState) {
                            is Resource.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryIndigo, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                            is Resource.Error -> {
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
                                        TextButton(onClick = { viewModel.loadBrowseableItems() }) {
                                            Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            is Resource.Empty -> {
                                EmptyStateCard(title = "No active lost & found items reported.")
                            }
                            is Resource.Success -> {
                                val filteredList = remember(state.data, searchQuery) {
                                    if (searchQuery.isBlank()) {
                                        state.data
                                    } else {
                                        val q = searchQuery.lowercase()
                                        state.data.filter {
                                            it.title.lowercase().contains(q) ||
                                                    it.locationFound.lowercase().contains(q) ||
                                                    it.description.lowercase().contains(q)
                                        }
                                    }
                                }

                                if (filteredList.isEmpty()) {
                                    EmptyStateCard(title = "No items match \"$searchQuery\".")
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredList, key = { it.itemId }) { item ->
                                            LostItemCard(
                                                item = item,
                                                currentUid = currentUid,
                                                onClaimClick = { claimingItem = item },
                                                onCancelReportClick = {
                                                    viewModel.cancelOwnReport(item.itemId, currentUid)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: Report Found Item
                    ReportFoundItemForm(
                        isSubmitting = actionState is Resource.Loading,
                        onSubmit = { title, desc, loc, img ->
                            viewModel.reportFoundItem(
                                title = title,
                                description = desc,
                                locationFound = loc,
                                imageUrl = img,
                                reporterUid = currentUid
                            )
                        }
                    )
                }
                2 -> {
                    // TAB 2: My Claims
                    when (val state = myClaimsState) {
                        is Resource.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryIndigo, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                        is Resource.Error -> {
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
                                    TextButton(onClick = { viewModel.loadMyClaims(currentUid) }) {
                                        Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        is Resource.Empty -> {
                            EmptyStateCard(title = "You have not submitted any claims yet.")
                        }
                        is Resource.Success -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.data, key = { it.itemId }) { item ->
                                    MyClaimCard(item = item)
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TAB 3: Staff Review Queue
                    if (isStaffOrAdmin) {
                        when (val state = staffQueueState) {
                            is Resource.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryIndigo, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                            is Resource.Error -> {
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
                                        TextButton(onClick = { viewModel.loadStaffQueue() }) {
                                            Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            is Resource.Empty -> {
                                EmptyStateCard(title = "No pending claims in staff review queue.")
                            }
                            is Resource.Success -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(state.data, key = { it.itemId }) { item ->
                                        StaffQueueItemCard(
                                            item = item,
                                            onVerifyClick = { staffActionItem = item to "VERIFY" },
                                            onRejectClick = { staffActionItem = item to "REJECT" },
                                            onHandoverClick = { staffActionItem = item to "HANDOVER" }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Claim Modal Dialog
        claimingItem?.let { item ->
            SubmitClaimDialog(
                itemTitle = item.title,
                onDismiss = { claimingItem = null },
                onConfirm = { notes, phone ->
                    viewModel.submitClaim(
                        itemId = item.itemId,
                        claimantUid = currentUid,
                        claimNotes = notes,
                        claimantPhone = phone
                    )
                    claimingItem = null
                    selectedTab = 2
                }
            )
        }

        // Staff Action Modal Dialog
        staffActionItem?.let { (item, action) ->
            StaffActionDialog(
                itemTitle = item.title,
                actionType = action,
                onDismiss = { staffActionItem = null },
                onConfirm = { notes ->
                    when (action) {
                        "VERIFY" -> viewModel.verifyClaim(item.itemId, currentUid, notes)
                        "REJECT" -> viewModel.rejectClaim(item.itemId, currentUid, notes)
                        "HANDOVER" -> viewModel.completeHandover(item.itemId, notes)
                    }
                    staffActionItem = null
                }
            )
        }
    }
}

@Composable
fun LostItemCard(
    item: LostItem,
    currentUid: String,
    onClaimClick: () -> Unit,
    onCancelReportClick: () -> Unit
) {
    val isReporter = item.reporterUid == currentUid
    val isAlreadyClaimed = !item.claimantUid.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                    color = HeadingNavy,
                    modifier = Modifier.weight(1f)
                )

                val (pillText, tone) = when (item.status) {
                    LostItemStatus.REPORTED -> "REPORTED" to StatusTone.NEUTRAL
                    LostItemStatus.CLAIM_SUBMITTED -> "CLAIM SUBMITTED" to StatusTone.WARNING
                    LostItemStatus.VERIFIED -> "VERIFIED" to StatusTone.SUCCESS
                    LostItemStatus.HANDOVER_COMPLETE -> "HANDOVER COMPLETE" to StatusTone.SUCCESS
                    LostItemStatus.CANCELLED -> "CANCELLED" to StatusTone.NEUTRAL
                }
                StatusPill(text = pillText, tone = tone)
            }

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = BodyText
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
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(14.dp))
                    Text(
                        text = item.locationFound,
                        fontSize = 11.sp,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Medium
                    )
                }

                val dateStr = remember(item.createdAt) {
                    if (item.createdAt != null && item.createdAt > 0) {
                        SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(item.createdAt))
                    } else {
                        "Recently"
                    }
                }
                Text(text = "Reported: $dateStr", fontSize = 11.sp, color = MutedText)
            }

            // Actions
            if (item.status == LostItemStatus.REPORTED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isReporter) {
                        OutlinedButton(
                            onClick = onCancelReportClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Cancel Report", fontSize = 11.sp)
                        }
                    } else if (!isAlreadyClaimed) {
                        Button(
                            onClick = onClaimClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Claim Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportFoundItemForm(
    isSubmitting: Boolean,
    onSubmit: (title: String, desc: String, loc: String, img: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationFound by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    SectionFormCard(sectionTitle = "Report a Found Campus Item") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (validationError != null) {
                Text(text = validationError!!, fontSize = 12.sp, color = AccentCoral)
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; validationError = null },
                label = { Text("Item Title") },
                placeholder = { Text("e.g. Blue Titan Water Bottle") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = locationFound,
                onValueChange = { locationFound = it; validationError = null },
                label = { Text("Location Found") },
                placeholder = { Text("e.g. N1 Block, Room 102") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryIndigo) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; validationError = null },
                label = { Text("Detailed Description") },
                placeholder = { Text("Describe distinct marks, color, brand, or contents...") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(90.dp),
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (Optional)") },
                placeholder = { Text("e.g. https://.../photo.jpg") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || locationFound.isBlank()) {
                        validationError = "Please fill in title, location, and description."
                    } else {
                        onSubmit(title, description, locationFound, imageUrl.ifBlank { null })
                        title = ""
                        description = ""
                        locationFound = ""
                        imageUrl = ""
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting Report...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Found Item Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MyClaimCard(item: LostItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
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
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy
                )

                val (pillText, tone) = when (item.status) {
                    LostItemStatus.CLAIM_SUBMITTED -> "PENDING REVIEW" to StatusTone.WARNING
                    LostItemStatus.VERIFIED -> "VERIFIED" to StatusTone.SUCCESS
                    LostItemStatus.HANDOVER_COMPLETE -> "HANDOVER COMPLETE" to StatusTone.SUCCESS
                    else -> item.status.name to StatusTone.NEUTRAL
                }
                StatusPill(text = pillText, tone = tone)
            }

            Text(text = "Location: ${item.locationFound}", fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium)

            if (!item.claimNotes.isNullOrBlank()) {
                Text(text = "My Notes: ${item.claimNotes}", fontSize = 12.sp, color = BodyText)
            }

            if (!item.staffNotes.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Staff Note: ${item.staffNotes}",
                        fontSize = 11.sp,
                        color = HeadingNavy,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            val claimDate = remember(item.claimTimestamp) {
                if (item.claimTimestamp != null && item.claimTimestamp > 0) {
                    SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault()).format(Date(item.claimTimestamp))
                } else {
                    "Submitted"
                }
            }
            Text(text = "Submitted: $claimDate", fontSize = 11.sp, color = MutedText)
        }
    }
}

@Composable
fun StaffQueueItemCard(
    item: LostItem,
    onVerifyClick: () -> Unit,
    onRejectClick: () -> Unit,
    onHandoverClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                    color = HeadingNavy
                )
                StatusPill(text = item.status.name, tone = StatusTone.WARNING)
            }

            Text(text = "Location: ${item.locationFound}", fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium)
            Text(text = "Description: ${item.description}", fontSize = 12.sp, color = BodyText)

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = HeadingNavy, modifier = Modifier.size(14.dp))
                Text(text = "Claimant UID: ${item.claimantUid ?: "N/A"}", fontSize = 11.sp, color = HeadingNavy, fontWeight = FontWeight.Bold)
            }

            if (!item.claimantPhone.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(14.dp))
                    Text(text = "Phone: ${item.claimantPhone}", fontSize = 11.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium)
                }
            }

            if (!item.claimNotes.isNullOrBlank()) {
                Text(text = "Claim Proof/Notes: ${item.claimNotes}", fontSize = 12.sp, color = BodyText)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRejectClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text("Reject Claim", fontSize = 11.sp)
                }

                if (item.status == LostItemStatus.CLAIM_SUBMITTED) {
                    Button(
                        onClick = onVerifyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify Claim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (item.status == LostItemStatus.VERIFIED) {
                    Button(
                        onClick = onHandoverClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete Handover", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SubmitClaimDialog(
    itemTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (notes: String, phone: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Claim for \"$itemTitle\"", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Describe identifying features, contents, or proof of ownership.",
                    fontSize = 12.sp,
                    color = MutedText
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Proof of Ownership / Notes") },
                    placeholder = { Text("e.g. Scratched logo on back, silver keychain") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone Number") },
                    placeholder = { Text("e.g. +91 9876543210") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (notes.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(notes, phone)
                    }
                },
                enabled = notes.isNotBlank() && phone.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm Claim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StaffActionDialog(
    itemTitle: String,
    actionType: String,
    onDismiss: () -> Unit,
    onConfirm: (staffNotes: String?) -> Unit
) {
    var staffNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$actionType Claim for \"$itemTitle\"",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add optional staff notes for record keeping.",
                    fontSize = 12.sp,
                    color = MutedText
                )

                OutlinedTextField(
                    value = staffNotes,
                    onValueChange = { staffNotes = it },
                    label = { Text("Staff Review Notes (Optional)") },
                    placeholder = { Text("e.g. Verified student ID card match.") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(staffNotes.ifBlank { null }) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm $actionType")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
