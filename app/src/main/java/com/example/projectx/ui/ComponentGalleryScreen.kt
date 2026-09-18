package com.example.projectx.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.theme.*

@Composable
fun ComponentGalleryScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(1) }
    var selectedSemester by remember { mutableStateOf("2026-2027, Semester - 3, BCA") }
    var currentPage by remember { mutableStateOf(1) }

    val sampleHeaders = listOf("Course", "Code", "Credits", "Grade", "Status")
    val sampleRows = listOf(
        listOf("Data Structures", "CS201", "4", "A+", "Pass"),
        listOf("Database Systems", "CS202", "3", "A", "Pass"),
        listOf("Web Technologies", "CS203", "3", "B+", "Pass")
    )

    AppScaffold(
        title = "Component Gallery",
        onMenuClick = onMenuClick,
        isOffline = true,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "MyCamu Component System",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy
                )
            }

            // 1. UnderlineTabRow
            item {
                SectionFormCard(sectionTitle = "1. UnderlineTabRow") {
                    UnderlineTabRow(
                        tabs = listOf("Subject-wise", "Log", "Summary"),
                        selectedTabIndex = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }

            // 2. Buttons & StatusPills
            item {
                SectionFormCard(sectionTitle = "2. Buttons & StatusPills") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PrimaryButton(text = "Primary Button", onClick = {}, modifier = Modifier.weight(1f))
                        SecondaryButton(text = "Secondary Button", onClick = {}, modifier = Modifier.weight(1f))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        StatusPill(text = "Submitted", tone = StatusTone.SUCCESS)
                        StatusPill(text = "Pending", tone = StatusTone.WARNING)
                        StatusPill(text = "Fail", tone = StatusTone.DANGER)
                        StatusPill(text = "Active", tone = StatusTone.NEUTRAL)
                    }
                }
            }

            // 3. InfoStrip & BannerCard
            item {
                SectionFormCard(sectionTitle = "3. InfoStrip & BannerCard") {
                    InfoStrip(message = "No exam schedule found!")
                    Spacer(modifier = Modifier.height(8.dp))
                    BannerCard(
                        message = "Do you want to generate a reusable permanent QR code?",
                        actionLabel = "Generate",
                        onActionClick = {}
                    )
                }
            }

            // 4. FilterSearchBar & SemesterSelector
            item {
                SectionFormCard(sectionTitle = "4. Search, Filters & Dropdowns") {
                    FilterSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholderText = "Search modules or courses..."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SemesterSelector(
                        selectedOption = selectedSemester,
                        onOptionSelected = { selectedSemester = it }
                    )
                }
            }

            // 5. WizardStepper
            item {
                SectionFormCard(sectionTitle = "5. WizardStepper (Profile Form)") {
                    WizardStepper(
                        currentStepIndex = currentStep
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        SecondaryButton(
                            text = "Prev",
                            onClick = { if (currentStep > 0) currentStep-- },
                            enabled = currentStep > 0
                        )
                        PrimaryButton(
                            text = "Next",
                            onClick = { if (currentStep < 3) currentStep++ },
                            enabled = currentStep < 3
                        )
                    }
                }
            }

            // 6. SectionFormCard with FormFieldLabel
            item {
                SectionFormCard(sectionTitle = "6. Form Field Section Card") {
                    FormFieldLabel(text = "Full Name", isRequired = true)
                    OutlinedTextField(
                        value = "Alex Rivera",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormFieldLabel(text = "Roll Number", isRequired = false)
                    OutlinedTextField(
                        value = "26BCA102",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 7. DataTable & PaginationBar
            item {
                SectionFormCard(sectionTitle = "7. DataTable & PaginationBar") {
                    DataTable(
                        headers = sampleHeaders,
                        rows = sampleRows
                    )
                    PaginationBar(
                        currentPage = currentPage,
                        totalPages = 3,
                        showingCount = 3,
                        totalCount = 9,
                        onPageChange = { currentPage = it }
                    )
                }
            }

            // 8. EmptyStateCard
            item {
                SectionFormCard(sectionTitle = "8. EmptyStateCard") {
                    EmptyStateCard(
                        title = "No new announcements found!",
                        hint = "Announcements posted by faculty will appear here instantly.",
                        icon = Icons.Default.Collections,
                        ctaText = "Refresh Feed"
                    )
                }
            }
        }
    }
}
