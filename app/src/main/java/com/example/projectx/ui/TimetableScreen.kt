package com.projectx.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.*
import com.projectx.app.model.TimetableSlot
import com.projectx.app.theme.*
import com.projectx.app.ui.academics.AcademicViewModel
import com.projectx.app.util.Resource

@Composable
fun TimetableScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    onNavigateToRoom: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val c = CampusTokens.colors
    var currentDateText by remember { mutableStateOf("Today") }
    var selectedSemester by remember { mutableStateOf("Semester - 5") }
    val timetableState by academicViewModel.timetableState.collectAsState()

    AppScaffold(
        title = "Timetable",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Semester Selector Header Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Semester Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.heading
                    )
                    SemesterSelector(
                        selectedOption = selectedSemester,
                        onOptionSelected = { selectedSemester = it }
                    )
                }
            }

            // Date Pager Header Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                    border = BorderStroke(1.dp, c.surfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Day", tint = c.primary)
                        }
                        Text(
                            text = currentDateText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.heading
                        )
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day", tint = c.primary)
                        }
                    }
                }
            }

            // State Handling
            when (val state = timetableState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = c.primary)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        SectionFormCard(sectionTitle = "Error Loading Timetable") {
                            Text(text = state.message, fontSize = 13.sp, color = c.dangerRed)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { academicViewModel.loadTimetable() },
                                colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is Resource.Empty -> {
                    item {
                        EmptyStateCard(title = "No classes scheduled for today.")
                    }
                }
                is Resource.Success -> {
                    items(state.data, key = { it.id }) { slot ->
                        TimetableClassCard(slot = slot)
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableClassCard(slot: TimetableSlot) {
    val c = CampusTokens.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.surfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${slot.courseName} (${slot.courseCode})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.heading
                    )
                    Text(
                        text = "Section: ${slot.section}",
                        fontSize = 12.sp,
                        color = c.mutedText
                    )
                }
                StatusPill(
                    text = slot.type,
                    tone = if (slot.type == "Lab") StatusTone.WARNING else StatusTone.NEUTRAL
                )
            }

            HorizontalDivider(color = c.divider, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = c.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "${slot.timeSlot} (${slot.durationMins} min)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.bodyText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = c.mutedText, modifier = Modifier.size(16.dp))
                    Text(
                        text = slot.facultyName,
                        fontSize = 12.sp,
                        color = c.mutedText
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = c.successGreen, modifier = Modifier.size(16.dp))
                Text(
                    text = "Location: ${slot.roomCode}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.successGreen
                )
            }
        }
    }
}
