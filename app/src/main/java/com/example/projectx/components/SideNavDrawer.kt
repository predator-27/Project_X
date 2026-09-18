package com.example.projectx.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.theme.*

data class NavDrawerItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

val MYCAMU_DRAWER_ITEMS = listOf(
    NavDrawerItem("institution", "My Institution", Icons.Default.AccountBalance),
    NavDrawerItem("messages", "Messages", Icons.Default.Email),
    NavDrawerItem("attendance", "Attendance", Icons.Default.CheckCircle),
    NavDrawerItem("exams", "Exam schedules", Icons.AutoMirrored.Filled.EventNote),
    NavDrawerItem("reports", "Reports", Icons.AutoMirrored.Filled.Assignment),
    NavDrawerItem("progress", "Progress Report", Icons.AutoMirrored.Filled.TrendingUp),
    NavDrawerItem("holidays", "Holidays", Icons.Default.CalendarMonth),
    NavDrawerItem("cafeteria", "Cafeteria", Icons.Default.Restaurant),
    NavDrawerItem("timetable", "Timetable", Icons.Default.Schedule),
    NavDrawerItem("leave", "Leave", Icons.Default.FlightTakeoff),
    NavDrawerItem("services", "Services", Icons.Default.Build),
    NavDrawerItem("enrollment", "Enrollment (> Pre enlistment)", Icons.Default.HowToReg),
    NavDrawerItem("activity", "Activity", Icons.Default.SportsBasketball),
    NavDrawerItem("clearance", "Clearance", Icons.Default.Verified),
    NavDrawerItem("announcement", "Announcement", Icons.Default.Campaign),
    NavDrawerItem("feedback", "Feedback", Icons.Default.Feedback),
    NavDrawerItem("gallery", "Gallery", Icons.Default.Collections),
    NavDrawerItem("result", "Final result", Icons.Default.Grade),
    NavDrawerItem("room_partner", "Room partner selection", Icons.Default.MeetingRoom)
)

@Composable
fun SideNavDrawerContent(
    activeItemId: String,
    onItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) MYCAMU_DRAWER_ITEMS
        else MYCAMU_DRAWER_ITEMS.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(NavySidebar)
            .padding(16.dp)
    ) {
        // Logo Card Header
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = NavySidebarActive),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Campus Portal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeadingNavy
                    )
                    Text(
                        text = "Bennett University",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }
        }

        // Search Field inside Drawer
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search modules...", fontSize = 12.sp, color = MutedText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MutedText) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = SurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(8.dp))

        // Module Items List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val isActive = item.id == activeItemId

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) NavySidebarActive else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isActive) HeadingNavy else NavySidebarText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) HeadingNavy else NavySidebarText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

        // Logout Button
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { onLogoutClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Sign Out",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed
                )
            }
        }
    }
}
