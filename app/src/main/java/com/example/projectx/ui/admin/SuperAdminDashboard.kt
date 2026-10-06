package com.projectx.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.*
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.model.User
import com.projectx.app.model.UserRole
import com.projectx.app.theme.CampusTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboard(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val colors = CampusTokens.colors

    var usersList by remember { mutableStateOf(DemoCampusData.demoStudents.toList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    var selectedUserForEdit by remember { mutableStateOf<User?>(null) }

    fun refreshUsers() {
        usersList = DemoCampusData.demoStudents.toList()
    }

    val filteredUsers = remember(usersList, searchQuery, selectedRoleFilter) {
        val q = searchQuery.trim().lowercase()
        usersList.filter { u ->
            val matchesQuery = q.isEmpty() || (u.email?.lowercase()?.contains(q) == true) || (u.rollNumber?.lowercase()?.contains(q) == true)
            val matchesRole = when (selectedRoleFilter) {
                "Student" -> u.role == UserRole.STUDENT
                "Faculty" -> u.role == UserRole.FACULTY
                "Staff" -> u.role == UserRole.LOST_FOUND_STAFF
                "Admin" -> u.role == UserRole.COLLEGE_ADMIN || u.role == UserRole.SUPER_ADMIN
                else -> true
            }
            matchesQuery && matchesRole
        }
    }

    AppScaffold(
        title = "Super Admin Console",
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
                                text = "System Administration — Bennett University",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.heading
                            )
                            StatusPill(text = "Healthy 99.98%", tone = StatusTone.SUCCESS)
                        }

                        Text(
                            text = "Platform-wide user management, role assignments, account status toggles, and system audit logs.",
                            fontSize = 12.sp,
                            color = colors.mutedText
                        )
                    }
                }
            }

            // System Metrics Grid
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
                                Icon(Icons.Default.People, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                Text("Accounts", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("${usersList.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
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
                                Icon(Icons.Default.Memory, contentDescription = null, tint = colors.successGreen, modifier = Modifier.size(14.dp))
                                Text("Active", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("${usersList.count { it.isActive }}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
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
                                Icon(Icons.Default.Security, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                Text("Security", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("14 Audit", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by email or roll number...", fontSize = 12.sp, color = colors.mutedText) },
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
                    listOf("All", "Student", "Faculty", "Staff", "Admin").forEach { roleName ->
                        FilterChip(
                            selected = selectedRoleFilter == roleName,
                            onClick = { selectedRoleFilter = roleName },
                            label = { Text(roleName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            item {
                Text("User Account Management (${filteredUsers.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.heading)
            }

            items(filteredUsers, key = { it.uid }) { user ->
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
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(user.email ?: "", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.heading)
                            Text("Roll: ${user.rollNumber ?: "N/A"} • Role: ${user.role.name}", fontSize = 11.sp, color = colors.mutedText)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusPill(
                                text = if (user.isActive) "Active" else "Inactive",
                                tone = if (user.isActive) StatusTone.SUCCESS else StatusTone.DANGER
                            )

                            IconButton(onClick = { selectedUserForEdit = user }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = colors.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Edit User Dialog
        selectedUserForEdit?.let { user ->
            var editRoll by remember(user) { mutableStateOf(user.rollNumber ?: "") }
            var editActive by remember(user) { mutableStateOf(user.isActive) }
            var editRole by remember(user) { mutableStateOf(user.role) }

            AlertDialog(
                onDismissRequest = { selectedUserForEdit = null },
                title = { Text("Edit User Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.heading) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Email: ${user.email}", fontSize = 12.sp, color = colors.mutedText)

                        OutlinedTextField(
                            value = editRoll,
                            onValueChange = { editRoll = it },
                            label = { Text("Roll Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Account Active Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.heading)
                            Switch(checked = editActive, onCheckedChange = { editActive = it })
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val idx = DemoCampusData.demoStudents.indexOfFirst { it.uid == user.uid }
                            if (idx != -1) {
                                DemoCampusData.demoStudents[idx] = DemoCampusData.demoStudents[idx].copy(
                                    rollNumber = editRoll.trim(),
                                    isActive = editActive,
                                    role = editRole
                                )
                            }
                            selectedUserForEdit = null
                            refreshUsers()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Save Account")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedUserForEdit = null }) { Text("Cancel") }
                }
            )
        }
    }
}
