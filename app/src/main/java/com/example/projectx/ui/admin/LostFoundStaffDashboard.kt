package com.projectx.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Info
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
fun LostFoundStaffDashboard(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val colors = CampusTokens.colors

    AppScaffold(
        title = "Lost & Found Staff Portal",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Demo Label Banner
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ DEMO LOST & FOUND STAFF PORTAL — FOR DEVELOPMENT PREVIEW ONLY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

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
                                text = "Lost & Found Operations",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.heading
                            )
                            StatusPill(text = "Lost & Found Desk", tone = StatusTone.SUCCESS)
                        }

                        Text(
                            text = "Item verification, student claim approvals, and physical handover logging.",
                            fontSize = 11.sp,
                            color = colors.mutedText
                        )
                    }
                }
            }

            // Metrics Summary Grid
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
                                Icon(Icons.Default.FindInPage, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Text("Reported", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("42 Items", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
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
                                Icon(Icons.Default.Info, contentDescription = null, tint = colors.warningAmber, modifier = Modifier.size(16.dp))
                                Text("Pending Claims", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("8 Claims", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
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
                                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = colors.successGreen, modifier = Modifier.size(16.dp))
                                Text("Handed Over", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("29 Items", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }
                }
            }

            // Operations List
            item {
                SectionFormCard(sectionTitle = "Lost & Found Operations") {
                    Text("• Verify Student ID & Match Property Claims", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Register Newly Found Campus Belongings", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Authorize Item Handover to Verified Student Owner", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Archive Handover Audit Log with Timestamp & Phone", fontSize = 12.sp, color = colors.bodyText)
                }
            }
        }
    }
}
