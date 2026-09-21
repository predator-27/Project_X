package com.example.projectx.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.theme.*

data class NavDrawerCategory(
    val categoryTitle: String,
    val items: List<NavDrawerItem>
)

data class NavDrawerItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badgeText: String? = null,
    val badgeTone: StatusTone = StatusTone.NEUTRAL
)

val CATEGORIZED_DRAWER_ITEMS = listOf(
    NavDrawerCategory(
        categoryTitle = "ACADEMICS",
        items = listOf(
            NavDrawerItem("home", "Dashboard", Icons.Default.Dashboard),
            NavDrawerItem("timetable", "Timetable", Icons.Default.Schedule),
            NavDrawerItem("attendance", "Attendance", Icons.Default.CheckCircle, badgeText = "64%", badgeTone = StatusTone.DANGER),
            NavDrawerItem("teachers", "Teacher Directory & Booking", Icons.Default.PersonSearch, badgeText = "Booking", badgeTone = StatusTone.SUCCESS),
            NavDrawerItem("exams", "Exam Schedules", Icons.AutoMirrored.Filled.EventNote),
            NavDrawerItem("reports", "Reports & Hall Ticket", Icons.AutoMirrored.Filled.Assignment),
            NavDrawerItem("progress", "Progress Report", Icons.AutoMirrored.Filled.TrendingUp),
            NavDrawerItem("result", "Final Result", Icons.Default.Grade)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "CAMPUS LIFE & SERVICES",
        items = listOf(
            NavDrawerItem("messages", "Messages", Icons.Default.Email, badgeText = "1 New", badgeTone = StatusTone.SUCCESS),
            NavDrawerItem("community", "Social Community", Icons.Default.Groups, badgeText = "Peer", badgeTone = StatusTone.SUCCESS),
            NavDrawerItem("campus_map", "Campus Map & Navigation", Icons.Default.Map),
            NavDrawerItem("cafeteria", "Cafeteria & QR Menu", Icons.Default.Restaurant),
            NavDrawerItem("holidays", "Holidays & Events", Icons.Default.CalendarMonth),
            NavDrawerItem("services", "Service Requests", Icons.Default.Build),
            NavDrawerItem("leave", "Apply Leave", Icons.Default.FlightTakeoff),
            NavDrawerItem("gallery", "Component Gallery", Icons.Default.Collections)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "ADMINISTRATION",
        items = listOf(
            NavDrawerItem("institution", "My Institution", Icons.Default.AccountBalance),
            NavDrawerItem("enrollment", "Enrollment (> Pre enlistment)", Icons.Default.HowToReg),
            NavDrawerItem("clearance", "Clearance", Icons.Default.Verified),
            NavDrawerItem("announcement", "Announcements", Icons.Default.Campaign),
            NavDrawerItem("room_partner", "Room Partner Selection", Icons.Default.MeetingRoom)
        )
    )
)

@Composable
fun SideNavDrawerContent(
    activeItemId: String,
    onItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavySidebar)
            .padding(18.dp)
    ) {
        // Logo Card Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NavySidebarActive,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryIndigo,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Project-X Portal",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeadingNavy
                    )
                    Text(
                        text = "Bennett University",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }
        }

        // Search Field inside Drawer
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search modules...", fontSize = 13.sp, color = MutedText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MutedText) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = PrimaryIndigo,
                unfocusedBorderColor = SurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        // Module Categories List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            CATEGORIZED_DRAWER_ITEMS.forEach { category ->
                val matchingItems = if (searchQuery.isBlank()) category.items
                else category.items.filter { it.label.contains(searchQuery, ignoreCase = true) }

                if (matchingItems.isNotEmpty()) {
                    item {
                        Text(
                            text = category.categoryTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigoLight,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }

                    items(matchingItems, key = { it.id }) { item ->
                        val isActive = item.id == activeItemId

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isActive) NavySidebarActive else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onItemClick(item.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isActive) PrimaryIndigo else NavySidebarText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = item.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isActive) HeadingNavy else NavySidebarText
                                    )
                                }

                                item.badgeText?.let { badge ->
                                    StatusPill(text = badge, tone = item.badgeTone)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

        // Logout Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
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
                    tint = AccentCoral,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Sign Out",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCoral
                )
            }
        }
    }
}
