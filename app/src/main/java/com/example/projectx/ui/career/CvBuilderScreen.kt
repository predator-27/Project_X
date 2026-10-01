package com.projectx.app.ui.career

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.*
import com.projectx.app.model.career.*
import com.projectx.app.theme.*
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.util.CvPdfGenerator
import com.projectx.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvBuilderScreen(
    careerViewModel: CareerViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionState by authViewModel.sessionState.collectAsState()
    val authSession = sessionState as? AuthSessionState.Authenticated

    val cvProfileState by careerViewModel.cvProfileState.collectAsState()
    val certsState by careerViewModel.certificationsState.collectAsState()
    val projectsState by careerViewModel.projectsState.collectAsState()
    val skillsState by careerViewModel.skillsState.collectAsState()
    val achievementsState by careerViewModel.achievementsState.collectAsState()

    val existingCv = (cvProfileState as? Resource.Success)?.data

    var summary by remember(existingCv) { mutableStateOf(existingCv?.personalSummary ?: "") }
    var targetRole by remember(existingCv) { mutableStateOf(existingCv?.targetRole ?: "Software Engineer") }
    var selectedTemplate by remember(existingCv) { mutableStateOf(existingCv?.selectedTemplate ?: "Classic") }

    val selectedCertIds = remember(existingCv) { mutableStateOf((existingCv?.selectedCertificationIds ?: emptyList()).toMutableSet()) }
    val selectedProjIds = remember(existingCv) { mutableStateOf((existingCv?.selectedProjectIds ?: emptyList()).toMutableSet()) }
    val selectedSkillIds = remember(existingCv) { mutableStateOf((existingCv?.selectedSkillIds ?: emptyList()).toMutableSet()) }
    val selectedAchieveIds = remember(existingCv) { mutableStateOf((existingCv?.selectedAchievementIds ?: emptyList()).toMutableSet()) }

    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfError by remember { mutableStateOf<String?>(null) }

    val templates = listOf("Classic", "Modern", "Minimal", "Technical", "Academic")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CV Builder Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Deterministic CV Builder & PDF Exporter", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                    Text("Select verified portfolio items to generate a clean, ATS-friendly PDF.", fontSize = 11.sp, color = MutedText)
                }

                Button(
                    onClick = {
                        val currentCv = CvProfile(
                            cvId = existingCv?.cvId ?: "",
                            ownerUid = authSession?.user?.uid ?: "demo_student_uid",
                            title = "Master CV",
                            selectedCertificationIds = selectedCertIds.value.toList(),
                            selectedProjectIds = selectedProjIds.value.toList(),
                            selectedSkillIds = selectedSkillIds.value.toList(),
                            selectedAchievementIds = selectedAchieveIds.value.toList(),
                            personalSummary = summary,
                            targetRole = targetRole,
                            selectedTemplate = selectedTemplate
                        )

                        careerViewModel.saveCvProfile(currentCv)

                        val certList = (certsState as? Resource.Success)?.data ?: emptyList()
                        val projList = (projectsState as? Resource.Success)?.data ?: emptyList()
                        val skillList = (skillsState as? Resource.Success)?.data ?: emptyList()
                        val achList = (achievementsState as? Resource.Success)?.data ?: emptyList()

                        isGeneratingPdf = true
                        pdfError = null

                        val pdfResult = CvPdfGenerator.generateCvPdf(
                            context = context,
                            user = authSession?.user,
                            profile = authSession?.publicProfile,
                            cvProfile = currentCv,
                            certifications = certList,
                            projects = projList,
                            skills = skillList,
                            achievements = achList
                        )

                        isGeneratingPdf = false

                        pdfResult.fold(
                            onSuccess = { file -> CvPdfGenerator.shareCvPdf(context, file) },
                            onFailure = { error -> pdfError = error.message ?: "Failed to generate PDF." }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (pdfError != null) {
            Text(text = pdfError!!, fontSize = 12.sp, color = AccentCoral)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Target Role & Summary
            item {
                SectionFormCard(sectionTitle = "Personal Profile & Target Role") {
                    OutlinedTextField(
                        value = targetRole,
                        onValueChange = { targetRole = it },
                        label = { Text("Target Professional Role") },
                        placeholder = { Text("e.g. Software Engineer / Android Architect") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Professional Summary") },
                        placeholder = { Text("Write a brief 2-3 sentence overview of your technical background...") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            }

            // Template Selector
            item {
                SectionFormCard(sectionTitle = "Choose CV Layout Template") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        templates.forEach { tmpl ->
                            FilterChip(
                                selected = selectedTemplate == tmpl,
                                onClick = { selectedTemplate = tmpl },
                                label = { Text(tmpl, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }

            // Select Certifications
            item {
                SectionFormCard(sectionTitle = "Include Certifications") {
                    val certList = (certsState as? Resource.Success)?.data ?: emptyList()
                    if (certList.isEmpty()) {
                        Text("No certifications added yet.", fontSize = 12.sp, color = MutedText)
                    } else {
                        certList.forEach { cert ->
                            val isChecked = selectedCertIds.value.contains(cert.certificationId)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        val newSet = selectedCertIds.value.toMutableSet()
                                        if (it) newSet.add(cert.certificationId) else newSet.remove(cert.certificationId)
                                        selectedCertIds.value = newSet
                                    }
                                )
                                Text(text = "${cert.name} (${cert.issuingOrganization})", fontSize = 13.sp, color = HeadingNavy)
                            }
                        }
                    }
                }
            }

            // Select Projects
            item {
                SectionFormCard(sectionTitle = "Include Projects") {
                    val projList = (projectsState as? Resource.Success)?.data ?: emptyList()
                    if (projList.isEmpty()) {
                        Text("No projects added yet.", fontSize = 12.sp, color = MutedText)
                    } else {
                        projList.forEach { proj ->
                            val isChecked = selectedProjIds.value.contains(proj.projectId)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        val newSet = selectedProjIds.value.toMutableSet()
                                        if (it) newSet.add(proj.projectId) else newSet.remove(proj.projectId)
                                        selectedProjIds.value = newSet
                                    }
                                )
                                Text(text = proj.projectName, fontSize = 13.sp, color = HeadingNavy)
                            }
                        }
                    }
                }
            }

            // Select Skills
            item {
                SectionFormCard(sectionTitle = "Include Technical Skills") {
                    val skillList = (skillsState as? Resource.Success)?.data ?: emptyList()
                    if (skillList.isEmpty()) {
                        Text("No skills added yet.", fontSize = 12.sp, color = MutedText)
                    } else {
                        skillList.forEach { sk ->
                            val isChecked = selectedSkillIds.value.contains(sk.skillId)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        val newSet = selectedSkillIds.value.toMutableSet()
                                        if (it) newSet.add(sk.skillId) else newSet.remove(sk.skillId)
                                        selectedSkillIds.value = newSet
                                    }
                                )
                                Text(text = "${sk.name} (${sk.category})", fontSize = 13.sp, color = HeadingNavy)
                            }
                        }
                    }
                }
            }

            // Select Achievements
            item {
                SectionFormCard(sectionTitle = "Include Achievements") {
                    val achList = (achievementsState as? Resource.Success)?.data ?: emptyList()
                    if (achList.isEmpty()) {
                        Text("No achievements added yet.", fontSize = 12.sp, color = MutedText)
                    } else {
                        achList.forEach { ach ->
                            val isChecked = selectedAchieveIds.value.contains(ach.achievementId)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        val newSet = selectedAchieveIds.value.toMutableSet()
                                        if (it) newSet.add(ach.achievementId) else newSet.remove(ach.achievementId)
                                        selectedAchieveIds.value = newSet
                                    }
                                )
                                Text(text = ach.title, fontSize = 13.sp, color = HeadingNavy)
                            }
                        }
                    }
                }
            }
        }
    }
}
