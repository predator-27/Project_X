package com.projectx.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.*
import com.projectx.app.theme.CampusTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboard(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val colors = CampusTokens.colors

    AppScaffold(
        title = "Super Admin Console",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                                text = "System Platform Infrastructure",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.heading
                            )
                            StatusPill(text = "Healthy 99.98%", tone = StatusTone.SUCCESS)
                        }

                        Text(
                            text = "Platform-wide user role assignments, security policy audits, and database health.",
                            fontSize = 11.sp,
                            color = colors.mutedText
                        )
                    }
                }
            }

            // System Metrics Grid
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.People, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Text("Total Accounts", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("4,520", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = colors.successGreen, modifier = Modifier.size(16.dp))
                                Text("Active Sessions", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("1,280", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Text("Security Logs", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("14 Audit", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }
                }
            }

            // System Control Operations
            item {
                SectionFormCard(sectionTitle = "Platform Management") {
                    Text("• Role-Based Access Control (RBAC) Policy Audit", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Microsoft Single-Tenant Entra ID Synchronization", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Firestore Security Rules Compliance Inspector", fontSize = 12.sp, color = colors.bodyText)
                    Text("• System Backup & Automated Storage Rotation", fontSize = 12.sp, color = colors.bodyText)
                }
            }
        }
    }
}
