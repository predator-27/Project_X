package com.projectx.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.projectx.app.model.UserRole
import com.projectx.app.theme.CampusTokens

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

// ─────────────────────────────────────────────────────────────────────
// Master menu — every drawer item lives here.
// Per-role menus are a filtered subset of this list.
// ─────────────────────────────────────────────────────────────────────
private object DrawerItems {
    val Home        = NavDrawerItem("home",            "Dashboard",                 Icons.Default.Dashboard)
    val Courses     = NavDrawerItem("courses",         "Courses & Syllabus",        Icons.Default.Book)
    val Attendance  = NavDrawerItem("attendance",      "Attendance Tracker",        Icons.Default.CheckCircle)
    val Timetable   = NavDrawerItem("timetable",       "Class Timetable",           Icons.Default.Schedule)
    val Assignments = NavDrawerItem("assignments",     "Assignments",               Icons.Default.Assignment)
    val Exams       = NavDrawerItem("exam_schedules",  "Exam Schedule",             Icons.AutoMirrored.Filled.EventNote)
    val Results     = NavDrawerItem("results",         "Results & Grades",          Icons.Default.Grade)
    val Reports     = NavDrawerItem("reports",         "Academic Reports",          Icons.AutoMirrored.Filled.TrendingUp)
    val Teachers    = NavDrawerItem("teachers",        "Faculty Directory",         Icons.Default.PersonSearch)
    val CampusMap   = NavDrawerItem("campus_map",      "Campus Map & Indoor Nav",   Icons.Default.Map)
    val Navigation  = NavDrawerItem("navigation",      "Navigation (N1 live)",      Icons.Default.Explore)
    val Appointments= NavDrawerItem("appointments",    "Faculty Appointments",      Icons.Default.CalendarMonth)
    val LostFound   = NavDrawerItem("lost_found",      "Lost & Found Portal",       Icons.Default.FindInPage)
    val Announce    = NavDrawerItem("announcements",   "Announcements",             Icons.Default.Campaign)
    val Messages    = NavDrawerItem("messages",        "Messages",                  Icons.Default.Email)
    val Community   = NavDrawerItem("community",       "Community & Mentorship",    Icons.Default.Groups)
    val Career      = NavDrawerItem("career_portfolio","Career & Portfolio",        Icons.Default.Work)
    val Cafeteria   = NavDrawerItem("cafeteria",       "Cafeteria & Dining",        Icons.Default.Restaurant)
    val Leave       = NavDrawerItem("leave",           "Leave Application",         Icons.Default.FlightTakeoff)
    val RoomPartner = NavDrawerItem("room_partner",    "Room Partner Selection",    Icons.Default.GroupAdd)
    val Holidays    = NavDrawerItem("holidays",        "Holidays & Calendar",       Icons.Default.DateRange)
    val Feedback    = NavDrawerItem("feedback",        "Institutional Feedback",    Icons.Default.Feedback)
    val Ai          = NavDrawerItem("ai_assistant",    "Ask AI Campus Tutor",       Icons.Default.AutoAwesome, badgeText = "Gemini", badgeTone = StatusTone.SUCCESS)
    val Settings    = NavDrawerItem("settings",        "Settings & Appearance",     Icons.Default.Settings)
}

/** The classic student / super-admin menu — everything the drawer can show. */
val CATEGORIZED_DRAWER_ITEMS: List<NavDrawerCategory> = drawerFor(UserRole.STUDENT)

/**
 * Build a role-specific menu. Every role gets Settings + Sign Out (Sign Out lives
 * in the drawer chrome, not the menu). Keep the categories familiar between roles so
 * users switching accounts see the same structure.
 */
fun drawerFor(role: UserRole): List<NavDrawerCategory> = when (role) {
    UserRole.STUDENT -> listOf(
        NavDrawerCategory("HOME", listOf(DrawerItems.Home)),
        NavDrawerCategory("ACADEMICS", listOf(
            DrawerItems.Courses, DrawerItems.Attendance, DrawerItems.Timetable,
            DrawerItems.Assignments, DrawerItems.Exams, DrawerItems.Results, DrawerItems.Reports,
        )),
        NavDrawerCategory("CAMPUS", listOf(
            DrawerItems.Teachers, DrawerItems.Navigation,
            DrawerItems.Appointments, DrawerItems.LostFound,
        )),
        NavDrawerCategory("COMMUNICATION", listOf(
            DrawerItems.Announce, DrawerItems.Messages, DrawerItems.Community,
        )),
        NavDrawerCategory("SERVICES", listOf(
            DrawerItems.Career, DrawerItems.Cafeteria, DrawerItems.Leave,
            DrawerItems.RoomPartner, DrawerItems.Holidays, DrawerItems.Feedback,
        )),
        NavDrawerCategory("AI ASSISTANT", listOf(DrawerItems.Ai)),
        NavDrawerCategory("SETTINGS", listOf(DrawerItems.Settings)),
    )
    UserRole.FACULTY -> listOf(
        NavDrawerCategory("HOME", listOf(DrawerItems.Home)),
        NavDrawerCategory("CAMPUS", listOf(
            DrawerItems.Teachers, DrawerItems.Appointments,
            DrawerItems.Navigation,
        )),
        NavDrawerCategory("COMMUNICATION", listOf(
            DrawerItems.Announce, DrawerItems.Messages,
        )),
        NavDrawerCategory("SERVICES", listOf(DrawerItems.Holidays, DrawerItems.Feedback)),
        NavDrawerCategory("AI ASSISTANT", listOf(DrawerItems.Ai)),
        NavDrawerCategory("SETTINGS", listOf(DrawerItems.Settings)),
    )
    UserRole.LOST_FOUND_STAFF -> listOf(
        NavDrawerCategory("HOME", listOf(DrawerItems.Home)),
        NavDrawerCategory("DESK", listOf(DrawerItems.LostFound)),
        NavDrawerCategory("CAMPUS", listOf(DrawerItems.Navigation)),
        NavDrawerCategory("SERVICES", listOf(DrawerItems.Holidays, DrawerItems.Feedback)),
        NavDrawerCategory("SETTINGS", listOf(DrawerItems.Settings)),
    )
    UserRole.COLLEGE_ADMIN -> listOf(
        NavDrawerCategory("HOME", listOf(DrawerItems.Home)),
        NavDrawerCategory("CAMPUS", listOf(
            DrawerItems.Teachers, DrawerItems.Navigation,
        )),
        NavDrawerCategory("COMMUNICATION", listOf(DrawerItems.Announce, DrawerItems.Messages)),
        NavDrawerCategory("SERVICES", listOf(DrawerItems.Holidays, DrawerItems.Feedback)),
        NavDrawerCategory("SETTINGS", listOf(DrawerItems.Settings)),
    )
    UserRole.SUPER_ADMIN -> drawerFor(UserRole.STUDENT)
}

@Composable
fun SideNavDrawerContent(
    activeItemId: String,
    onItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    displayName: String? = null,
    rollNumber: String? = null,
    email: String? = null,
    role: UserRole = UserRole.STUDENT,
    modifier: Modifier = Modifier
) {
    val c = CampusTokens.colors
    var searchQuery by remember { mutableStateOf("") }
    val menu = remember(role) { drawerFor(role) }

    val resolvedName = remember(displayName, email) {
        displayName?.ifBlank { null }
            ?: email?.substringBefore("@")
            ?: "Project X"
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
            .background(c.sidebar)
            .padding(18.dp)
    ) {
        // Header card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = c.sidebarActive,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .border(1.dp, c.glassBorderGlow, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
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
                            color = c.heading
                        )
                        Text(
                            text = resolvedSubtext,
                            fontSize = 12.sp,
                            color = c.mutedText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = c.primary.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "Project X • ${roleLabel(role)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (c.isDark) c.primary else c.heading,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search modules...", fontSize = 13.sp, color = c.mutedText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = c.mutedText) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface,
                focusedTextColor = c.heading,
                unfocusedTextColor = c.heading,
                focusedBorderColor = c.primary,
                unfocusedBorderColor = c.surfaceBorder,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        HorizontalDivider(color = c.divider.copy(alpha = 0.4f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            menu.forEach { category ->
                val matching = if (searchQuery.isBlank()) category.items
                else category.items.filter { it.label.contains(searchQuery, ignoreCase = true) }

                if (matching.isNotEmpty()) {
                    item(key = "cat-${category.categoryTitle}") {
                        Text(
                            text = category.categoryTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.primary,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }

                    items(matching, key = { it.id }) { item ->
                        DrawerRow(
                            item = item,
                            isActive = item.id == activeItemId,
                            onClick = { onItemClick(item.id) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = c.divider.copy(alpha = 0.4f), thickness = 1.dp)

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLogoutClick() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = c.dangerRed,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Sign Out",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.dangerRed
                )
            }
        }
    }
}

@Composable
private fun DrawerRow(
    item: NavDrawerItem,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val c = CampusTokens.colors
    val bg = if (isActive) c.sidebarActive else Color.Transparent
    val iconTint = if (isActive) c.primary else c.sidebarText
    val labelColor = if (isActive) c.heading else c.sidebarText
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
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
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.label,
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = labelColor
                )
            }
            if (isActive) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(c.primary))
            }
            item.badgeText?.let { badge ->
                Spacer(modifier = Modifier.width(6.dp))
                StatusPill(text = badge, tone = item.badgeTone)
            }
        }
    }
}

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.STUDENT -> "Student Portal"
    UserRole.FACULTY -> "Faculty Portal"
    UserRole.LOST_FOUND_STAFF -> "Lost & Found Desk"
    UserRole.COLLEGE_ADMIN -> "College Admin"
    UserRole.SUPER_ADMIN -> "Super Admin"
}
