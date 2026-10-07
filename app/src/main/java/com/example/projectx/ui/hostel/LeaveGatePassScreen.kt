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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveGatePassScreen(
    onMenuClick: () -> Unit,
    hostelViewModel: HostelViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val c = CampusTokens.colors
    val passesState by hostelViewModel.leavePassesState.collectAsState()
    val actionState by hostelViewModel.actionState.collectAsState()

    var leaveType by remember { mutableStateOf("Home Leave") }
    var startDate by remember { mutableStateOf("2026-10-05") }
    var endDate by remember { mutableStateOf("2026-10-07") }
    var reason by remember { mutableStateOf("") }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = c.surface,
        unfocusedContainerColor = c.surface,
        focusedTextColor = c.heading,
        unfocusedTextColor = c.heading,
        focusedLabelColor = c.primary,
        unfocusedLabelColor = c.mutedText,
        focusedBorderColor = c.primary,
        unfocusedBorderColor = c.surfaceBorder,
        cursorColor = c.primary
    )

    AppScaffold(
        title = "Leave & Gate Pass Application",
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Home Leave", "Local Outing", "Emergency").forEach { type ->
                            FilterChip(
                                selected = leaveType == type,
                                onClick = { leaveType = type },
                                label = { Text(type, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
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
                            label = { Text("Start Date", color = c.mutedText) },
                            placeholder = { Text("YYYY-MM-DD", color = c.mutedText) },
                            singleLine = true,
                            colors = tfColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date", color = c.mutedText) },
                            placeholder = { Text("YYYY-MM-DD", color = c.mutedText) },
                            singleLine = true,
                            colors = tfColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave", color = c.mutedText) },
                        placeholder = { Text("State purpose for leaving campus...", color = c.mutedText) },
                        colors = tfColors,
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
                        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
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
                Text("Leave & Gate Pass History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.heading)
            }

            when (val state = passesState) {
                is Resource.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = c.primary)
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
                            LeavePassCard(pass = pass)
                        }
                    }
                }
                else -> {
                    item { EmptyStateCard(title = "No leave history available.") }
                }
            }
        }
    }
}

@Composable
fun LeavePassCard(pass: HostelLeavePass) {
    val c = CampusTokens.colors
    val sdf = remember { SimpleDateFormat("d MMM, h:mm a", Locale.ENGLISH) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.surfaceBorder)
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
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
                    Text(pass.leaveType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.heading)
                }

                StatusPill(
                    text = pass.status,
                    tone = when (pass.status) {
                        "APPROVED" -> StatusTone.SUCCESS
                        "PENDING", "SUBMITTED" -> StatusTone.WARNING
                        "REJECTED" -> StatusTone.DANGER
                        else -> StatusTone.NEUTRAL
                    }
                )
            }

            Text(
                text = "📅 ${pass.startDate} to ${pass.endDate}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = c.heading
            )

            Text(
                text = "Reason: ${pass.reason}",
                fontSize = 12.sp,
                color = c.bodyText
            )

            HorizontalDivider(color = c.divider, thickness = 1.dp)

            Text(
                text = "Applied: ${sdf.format(Date(pass.appliedAt))}",
                fontSize = 11.sp,
                color = c.mutedText
            )
        }
    }
}
