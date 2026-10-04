package com.projectx.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.R
import com.projectx.app.theme.*

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
        categoryTitle = "HOME",
        items = listOf(
            NavDrawerItem("home", "Dashboard", Icons.Default.Dashboard)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "ACADEMICS",
        items = listOf(
            NavDrawerItem("courses", "Courses & Syllabus", Icons.Default.Book),
            NavDrawerItem("attendance", "Attendance Tracker", Icons.Default.CheckCircle),
            NavDrawerItem("timetable", "Class Timetable", Icons.Default.Schedule),
            NavDrawerItem("assignments", "Assignments & Submissions", Icons.Default.Assignment),
            NavDrawerItem("exam_schedules", "Exam Schedule", Icons.AutoMirrored.Filled.EventNote),
            NavDrawerItem("results", "Results & Grades", Icons.Default.Grade),
            NavDrawerItem("reports", "Academic Reports", Icons.AutoMirrored.Filled.TrendingUp)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "CAMPUS",
        items = listOf(
            NavDrawerItem("teachers", "Faculty Directory", Icons.Default.PersonSearch),
            NavDrawerItem("campus_map", "Campus Map & Indoor Nav", Icons.Default.Map),
            NavDrawerItem("navigation", "Navigation (N1 live)", Icons.Default.Explore),
            NavDrawerItem("appointments", "Faculty Appointments", Icons.Default.CalendarMonth),
            NavDrawerItem("lost_found", "Lost & Found Portal", Icons.Default.FindInPage)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "COMMUNICATION",
        items = listOf(
            NavDrawerItem("announcements", "Announcements", Icons.Default.Campaign),
            NavDrawerItem("messages", "Messages", Icons.Default.Email),
            NavDrawerItem("community", "Community & Mentorship", Icons.Default.Groups)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "SERVICES",
        items = listOf(
            NavDrawerItem("career_portfolio", "Career & Portfolio", Icons.Default.Work),
            NavDrawerItem("cafeteria", "Cafeteria & Dining", Icons.Default.Restaurant),
            NavDrawerItem("leave", "Leave Application", Icons.Default.FlightTakeoff),
            NavDrawerItem("room_partner", "Room Partner Selection", Icons.Default.GroupAdd),
            NavDrawerItem("holidays", "Holidays & Calendar", Icons.Default.DateRange),
            NavDrawerItem("feedback", "Institutional Feedback", Icons.Default.Feedback)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "AI ASSISTANT",
        items = listOf(
            NavDrawerItem("ai_assistant", "Ask AI Campus Tutor", Icons.Default.AutoAwesome, badgeText = "Gemini", badgeTone = StatusTone.SUCCESS)
        )
    ),
    NavDrawerCategory(
        categoryTitle = "SETTINGS",
        items = listOf(
            NavDrawerItem("settings", "Settings & Appearance", Icons.Default.Settings)
        )
    )
)

@Composable
fun SideNavDrawerContent(
    activeItemId: String,
    onItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    displayName: String? = null,
    rollNumber: String? = null,
    email: String? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val resolvedName = remember(displayName, email) {
        displayName?.ifBlank { null }
            ?: email?.substringBefore("@")
            ?: "Project X Student"
    }

    val resolvedSubtext = remember(rollNumber, email) {
        listOfNotNull(
            rollNumber?.ifBlank { null },
            email?.ifBlank { null }
        ).joinToString(" • ").ifBlank { "Bennett University" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavySidebar)
            .padding(18.dp)
    ) {
        // Project X Student Header Card (NO ROLE SWITCHING)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NavySidebarActive,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = resolvedName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeadingNavy
                        )
                        Text(
                            text = resolvedSubtext,
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryIndigo.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Project X • Student Portal",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
