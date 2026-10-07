package com.projectx.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
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
fun CollegeAdminDashboard(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val colors = CampusTokens.colors

    AppScaffold(
        title = "College Admin Portal",
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
                                text = "College Administration Console",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.heading
                            )
                            StatusPill(text = "Bennett University", tone = StatusTone.SUCCESS)
                        }

                        Text(
                            text = "Departmental metrics, faculty allocations, and academic course management.",
                            fontSize = 11.sp,
                            color = colors.mutedText
                        )
                    }
                }
            }

            // Metric Summary Cards Row
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
                                Icon(Icons.Default.School, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Text("Students", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("4,250", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
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
                                Icon(Icons.Default.Group, contentDescription = null, tint = colors.successGreen, modifier = Modifier.size(16.dp))
                                Text("Faculty", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("180", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
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
                                Icon(Icons.Default.Class, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Text("Courses", fontSize = 11.sp, color = colors.mutedText)
                            }
                            Text("64", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.heading)
                        }
                    }
                }
            }

            // Quick Actions Section
            item {
                SectionFormCard(sectionTitle = "Administrative Operations") {
                    Text("• Course Allocation & Department Mapping", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Faculty Desk & Office Hour Approvals", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Semester Academic Attendance Compliance", fontSize = 12.sp, color = colors.bodyText)
                    Text("• Official Transcript & Bonafide Issuance", fontSize = 12.sp, color = colors.bodyText)
                }
            }
        }
    }
}
