package com.projectx.app.ui.hostel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.*
import com.projectx.app.model.hostel.HostelLeavePass
import com.projectx.app.theme.*
import com.projectx.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveGatePassScreen(
    onMenuClick: () -> Unit,
    hostelViewModel: HostelViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val passesState by hostelViewModel.leavePassesState.collectAsState()
    val actionState by hostelViewModel.actionState.collectAsState()

    var leaveType by remember { mutableStateOf("Home Leave") }
    var startDate by remember { mutableStateOf("2026-09-28") }
    var endDate by remember { mutableStateOf("2026-09-30") }
    var reason by remember { mutableStateOf("Family function at hometown") }

    var selectedPassForDetails by remember { mutableStateOf<HostelLeavePass?>(null) }

    val leaveTypes = listOf("Home Leave", "Local Gate Pass", "Emergency Leave")

    AppScaffold(
        title = "Apply Leave & Gate Pass",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Application Form
            item {
                SectionFormCard(sectionTitle = "Apply Leave and Gate Pass") {
                    Text("Select Leave / Pass Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        leaveTypes.forEach { type ->
                            FilterChip(
                                selected = leaveType == type,
                                onClick = { leaveType = type },
                                label = { Text(type, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave") },
                        placeholder = { Text("State purpose for leaving campus...") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            hostelViewModel.applyLeavePass(
                                leaveType = leaveType,
                                startDate = startDate,
                                endDate = endDate,
                                reason = reason
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Leave Application", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Error Alert
            item {
                if (actionState is Resource.Error) {
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
                            IconButton(onClick = { hostelViewModel.resetActionState() }) {
                                Icon(Icons.Default.Info, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Application History Section
            item {
                Text("Leave & Gate Pass History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
            }

            when (val state = passesState) {
                is Resource.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryIndigo)
                        }
                    }
                }
                is Resource.Error -> {
                    item { EmptyStateCard(title = state.message) }
                }
                is Resource.Success -> {
                    if (state.data.isEmpty()) {
                        item { EmptyStateCard(title = "No previous leave applications found.") }
                    } else {
                        items(state.data, key = { it.leaveId }) { pass ->
                            LeavePassRowCard(
                                pass = pass,
                                onMoreClick = { selectedPassForDetails = pass }
                            )
                        }
                    }
                }
                else -> {
                    item { EmptyStateCard(title = "No previous leave applications found.") }
                }
            }
        }

        // Details / More Dialog
        selectedPassForDetails?.let { pass ->
            AlertDialog(
                onDismissRequest = { selectedPassForDetails = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = PrimaryIndigo)
                        Text(pass.leaveType, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Application ID: ${pass.leaveId}", fontSize = 11.sp, color = MutedText, fontWeight = FontWeight.Bold)
                        Text("Dates: ${pass.startDate} to ${pass.endDate}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HeadingNavy)
                        Text("Reason: ${pass.reason}", fontSize = 12.sp, color = BodyText)
                        Text("Status: ${pass.status}", fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                        pass.approvedBy?.let { warden ->
                            Text("Reviewed By: $warden", fontSize = 11.sp, color = MutedText)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedPassForDetails = null }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun LeavePassRowCard(
    pass: HostelLeavePass,
    onMoreClick: () -> Unit
) {
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
                    text = pass.leaveType,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy
                )

                StatusPill(
                    text = pass.status,
                    tone = when (pass.status) {
                        "APPROVED" -> StatusTone.SUCCESS
                        "REJECTED" -> StatusTone.DANGER
                        else -> StatusTone.WARNING
                    }
                )
            }

            Text(
                text = "📅 ${pass.startDate} ➔ ${pass.endDate}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BodyText
            )

            Text(
                text = "Reason: ${pass.reason}",
                fontSize = 11.sp,
                color = MutedText,
                maxLines = 2
            )

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pass.approvedBy?.let { warden ->
                    Text(text = "Warden: $warden", fontSize = 10.sp, color = MutedText)
                }

                TextButton(
                    onClick = onMoreClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Details / More ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                }
            }
        }
    }
}
