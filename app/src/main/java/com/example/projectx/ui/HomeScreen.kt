package com.example.projectx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.theme.*

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppScaffold(
        title = "Dashboard",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Next Class Banner Card
            BannerCard(
                message = "Next Class: Data Structures @ 09:25 AM in Room 010-N-CC",
                actionLabel = "Map Route",
                onActionClick = { onNavigateToTab("campus_map") }
            )

            // Shortage Warning Strip
            InfoStrip(
                message = "Attendance Shortage: DBMS is at 64.2%. Attend next 3 classes."
            )

            Text(
                text = "Campus Modules & Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HeadingNavy
            )

            // Quick Module Tiles Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeModuleTile(
                        title = "Timetable",
                        subtitle = "Today: 3 Lectures",
                        icon = Icons.Default.Schedule,
                        onClick = { onNavigateToTab("timetable") },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModuleTile(
                        title = "Attendance",
                        subtitle = "Overall: 75.1%",
                        icon = Icons.Default.CheckCircle,
                        onClick = { onNavigateToTab("attendance") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeModuleTile(
                        title = "Campus Map",
                        subtitle = "Indoor Navigation",
                        icon = Icons.Default.Map,
                        onClick = { onNavigateToTab("campus_map") },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModuleTile(
                        title = "Messages",
                        subtitle = "1 Unread Notice",
                        icon = Icons.Default.Email,
                        onClick = { onNavigateToTab("messages") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = InfoBannerBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(22.dp))
                }
            }

            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                Text(text = subtitle, fontSize = 12.sp, color = MutedText)
            }
        }
    }
}
