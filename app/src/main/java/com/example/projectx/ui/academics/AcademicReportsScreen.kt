package com.projectx.app.ui.academics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.components.*
import com.projectx.app.theme.*

data class AcademicReportDoc(
    val reportId: String,
    val title: String,
    val category: String,
    val issueDate: String,
    val fileSize: String,
    val isVerified: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicReportsScreen(
    academicViewModel: AcademicViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = CampusTokens.colors
    val reportsList = remember {
        listOf(
            AcademicReportDoc("rep_1", "Official Grade Transcript - Semester 5", "Official Transcript", "2026-09-01", "1.4 MB"),
            AcademicReportDoc("rep_2", "Semester Attendance Certificate", "Attendance Record", "2026-09-15", "850 KB"),
            AcademicReportDoc("rep_3", "Bonafide Student Certificate", "Administrative Pass", "2026-08-20", "520 KB"),
            AcademicReportDoc("rep_4", "Conduct & Character Certificate", "Student Affairs", "2026-08-10", "640 KB")
        )
    }

    AppScaffold(
        title = "Academic Reports & Transcripts",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                    border = BorderStroke(1.dp, c.surfaceBorder)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = c.primary, modifier = Modifier.size(22.dp))
                                Text("University Records & Transcripts", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.heading)
                            }
                            Spacer(Modifier.width(8.dp))
                            StatusPill(text = "Official Pass", tone = StatusTone.SUCCESS)
                        }

                        Text(
                            text = "Download signed transcripts, attendance summaries, and bonafide certificates issued by Academic Affairs.",
                            fontSize = 12.sp,
                            color = c.bodyText
                        )
                    }
                }
            }

            item {
                Text("Available Reports", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.heading)
            }

            items(reportsList, key = { it.reportId }) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                    border = BorderStroke(1.dp, c.surfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(doc.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.heading, modifier = Modifier.weight(1f))
                            StatusPill(text = doc.category, tone = StatusTone.NEUTRAL)
                        }

                        Text("Issued: ${doc.issueDate} • Size: ${doc.fileSize}", fontSize = 12.sp, color = c.mutedText)

                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, c.surfaceBorder),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = c.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Download Document", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.heading)
                        }
                    }
                }
            }
        }
    }
}
