package com.example.projectx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.theme.*

@Composable
fun SocialCommunityScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Peer Mentorship, 1 = Book Exchange, 2 = Social Drives

    AppScaffold(
        title = "Social & Peer Community",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                BannerCard(
                    message = "🤝 Campus Peer Tutorship: Volunteer to help junior students or find a free peer mentor!",
                    actionLabel = "Volunteer",
                    onActionClick = {}
                )
            }

            item {
                UnderlineTabRow(
                    tabs = listOf("Peer Mentorship", "Book Exchange", "Social Drives"),
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            if (selectedTab == 0) {
                // Peer Mentorship
                item {
                    SectionFormCard(sectionTitle = "Free Peer Mentorship Circles") {
                        Text(
                            text = "Senior BCA & Tech students volunteering free 1-on-1 tutoring sessions.",
                            fontSize = 13.sp,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MentorCard(
                            mentorName = "Rohan Sharma (Final Year)",
                            subject = "Data Structures & Algorithms",
                            timing = "Sat: 04:00 PM - 06:00 PM",
                            rating = "4.9 ⭐ (18 Students Mentored)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MentorCard(
                            mentorName = "Priya Patel (3rd Year)",
                            subject = "Database Management Systems (DBMS)",
                            timing = "Sun: 11:00 AM - 01:00 PM",
                            rating = "4.8 ⭐ (12 Students Mentored)"
                        )
                    }
                }
            } else if (selectedTab == 1) {
                // Book Exchange
                item {
                    SectionFormCard(sectionTitle = "Free Textbook & Notes Exchange") {
                        InfoStrip(message = "Donate your semester textbooks or borrow required course notes for free.")
                        Spacer(modifier = Modifier.height(8.dp))
                        BookItemCard(
                            title = "Core Java & OOPS Concepts (8th Ed)",
                            owner = "Shared by Alex Rivera",
                            condition = "Available for 14 days",
                            category = "Free Textbook"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BookItemCard(
                            title = "Discrete Mathematics Handwritten Exam Notes",
                            owner = "Shared by Computer Science Club",
                            condition = "PDF & Hardcopy",
                            category = "Exam Notes"
                        )
                    }
                }
            } else {
                // Social Drives
                item {
                    SectionFormCard(sectionTitle = "Campus Social Cause Initiatives") {
                        SocialDriveCard(
                            title = "Campus Voluntary Blood Donation Camp",
                            organizer = "NSS & Rotary Club Bennett",
                            date = "22-Sep-2026 @ Student Center",
                            icon = Icons.Default.Favorite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SocialDriveCard(
                            title = "Green Campus E-Waste Collection Drive",
                            organizer = "Environmental Club",
                            date = "25-Sep-2026 @ Tech Tower Gate",
                            icon = Icons.Default.Handshake
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MentorCard(mentorName: String, subject: String, timing: String, rating: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = mentorName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                StatusPill(text = "Free", tone = StatusTone.SUCCESS)
            }
            Text(text = "Subject: $subject", fontSize = 13.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold)
            Text(text = "Timing: $timing", fontSize = 12.sp, color = MutedText)
            Text(text = rating, fontSize = 12.sp, color = SecondaryEmerald, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookItemCard(title: String, owner: String, condition: String, category: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                StatusPill(text = category, tone = StatusTone.NEUTRAL)
            }
            Text(text = owner, fontSize = 12.sp, color = MutedText)
            Text(text = condition, fontSize = 12.sp, color = SecondaryEmerald, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SocialDriveCard(title: String, organizer: String, date: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = PillShape, color = AccentCoralBg, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                Text(text = organizer, fontSize = 12.sp, color = MutedText)
                Text(text = date, fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
