package com.projectx.app.ui.career

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.projectx.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerPortfolioScreen(
    onMenuClick: () -> Unit,
    careerViewModel: CareerViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionState by authViewModel.sessionState.collectAsState()
    val authSession = sessionState as? AuthSessionState.Authenticated
    val currentUid = authSession?.user?.uid ?: "demo_student_uid"

    var selectedTab by remember { mutableStateOf(0) } // 0 = Certs, 1 = Projects, 2 = Skills, 3 = Achievements, 4 = Vault, 5 = CV Builder

    val certsState by careerViewModel.certificationsState.collectAsState()
    val projectsState by careerViewModel.projectsState.collectAsState()
    val skillsState by careerViewModel.skillsState.collectAsState()
    val achievementsState by careerViewModel.achievementsState.collectAsState()
    val vaultState by careerViewModel.portfolioFilesState.collectAsState()
    val actionState by careerViewModel.actionState.collectAsState()

    var showAddCertDialog by remember { mutableStateOf(false) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddSkillDialog by remember { mutableStateOf(false) }
    var showAddAchieveDialog by remember { mutableStateOf(false) }

    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { careerViewModel.uploadVaultFile(context, it, "Portfolio Document") }
    }

    LaunchedEffect(currentUid) {
        careerViewModel.loadAllCareerData()
    }

    AppScaffold(
        title = "Career & Portfolio Hub",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Work, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Professional Career Vault", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                            Text("Verified Certifications, Projects, Skills & Deterministic CV Builder", fontSize = 11.sp, color = MutedText)
                        }
                    }

                    IconButton(onClick = { careerViewModel.loadAllCareerData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Tab Row
            UnderlineTabRow(
                tabs = listOf("Certs", "Projects", "Skills", "Achievements", "Vault", "CV Builder"),
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Action Error Alert
            if (actionState is Resource.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (actionState as Resource.Error).message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { careerViewModel.resetActionState() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: CERTIFICATIONS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { showAddCertDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Certification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        when (val state = certsState) {
                            is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) }
                            is Resource.Error -> EmptyStateCard(title = state.message)
                            is Resource.Success -> {
                                if (state.data.isEmpty()) {
                                    EmptyStateCard(title = "No certifications added yet.")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                        items(state.data, key = { it.certificationId }) { cert ->
                                            CertificationRowCard(cert = cert, onDelete = { careerViewModel.deleteCertification(cert.certificationId) })
                                        }
                                    }
                                }
                            }
                            else -> EmptyStateCard(title = "No certifications found.")
                        }
                    }
                }
                1 -> {
                    // TAB 1: PROJECTS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { showAddProjectDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Project", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        when (val state = projectsState) {
                            is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) }
                            is Resource.Error -> EmptyStateCard(title = state.message)
                            is Resource.Success -> {
                                if (state.data.isEmpty()) {
                                    EmptyStateCard(title = "No projects added yet.")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                        items(state.data, key = { it.projectId }) { proj ->
                                            ProjectRowCard(project = proj, onDelete = { careerViewModel.deleteProject(proj.projectId) })
                                        }
                                    }
                                }
                            }
                            else -> EmptyStateCard(title = "No projects found.")
                        }
                    }
                }
                2 -> {
                    // TAB 2: SKILLS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { showAddSkillDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Skill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        when (val state = skillsState) {
                            is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) }
                            is Resource.Error -> EmptyStateCard(title = state.message)
                            is Resource.Success -> {
                                if (state.data.isEmpty()) {
                                    EmptyStateCard(title = "No skills added yet.")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                        items(state.data, key = { it.skillId }) { sk ->
                                            SkillRowCard(skill = sk, onDelete = { careerViewModel.deleteSkill(sk.skillId) })
                                        }
                                    }
                                }
                            }
                            else -> EmptyStateCard(title = "No skills found.")
                        }
                    }
                }
                3 -> {
                    // TAB 3: ACHIEVEMENTS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { showAddAchieveDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Achievement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        when (val state = achievementsState) {
                            is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) }
                            is Resource.Error -> EmptyStateCard(title = state.message)
                            is Resource.Success -> {
                                if (state.data.isEmpty()) {
                                    EmptyStateCard(title = "No achievements added yet.")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                        items(state.data, key = { it.achievementId }) { ach ->
                                            AchievementRowCard(achievement = ach, onDelete = { careerViewModel.deleteAchievement(ach.achievementId) })
                                        }
                                    }
                                }
                            }
                            else -> EmptyStateCard(title = "No achievements found.")
                        }
                    }
                }
                4 -> {
                    // TAB 4: PORTFOLIO VAULT
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { documentPicker.launch("*/*") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Vault Document", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        when (val state = vaultState) {
                            is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) }
                            is Resource.Error -> EmptyStateCard(title = state.message)
                            is Resource.Success -> {
                                if (state.data.isEmpty()) {
                                    EmptyStateCard(title = "No documents uploaded to Portfolio Vault.")
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                        items(state.data, key = { it.fileId }) { file ->
                                            VaultFileRowCard(file = file, onDelete = { careerViewModel.deletePortfolioFile(file.fileId, file.storagePath) })
                                        }
                                    }
                                }
                            }
                            else -> EmptyStateCard(title = "No vault documents found.")
                        }
                    }
                }
                5 -> {
                    // TAB 5: CV BUILDER
                    CvBuilderScreen(careerViewModel = careerViewModel, authViewModel = authViewModel)
                }
            }
        }

        // Dialogs
        if (showAddCertDialog) {
            AddCertificationDialog(
                onDismiss = { showAddCertDialog = false },
                onConfirm = { name, issuer, date, cat, desc ->
                    careerViewModel.addCertification(
                        Certification(
                            ownerUid = currentUid,
                            name = name,
                            issuingOrganization = issuer,
                            issueDate = date,
                            category = cat,
                            description = desc
                        )
                    )
                    showAddCertDialog = false
                }
            )
        }

        if (showAddProjectDialog) {
            AddProjectDialog(
                onDismiss = { showAddProjectDialog = false },
                onConfirm = { name, desc, techs ->
                    careerViewModel.addProject(
                        ProjectItem(
                            ownerUid = currentUid,
                            projectName = name,
                            description = desc,
                            technologies = techs
                        )
                    )
                    showAddProjectDialog = false
                }
            )
        }

        if (showAddSkillDialog) {
            AddSkillDialog(
                onDismiss = { showAddSkillDialog = false },
                onConfirm = { name, cat, prof ->
                    careerViewModel.addSkill(
                        SkillItem(
                            ownerUid = currentUid,
                            name = name,
                            category = cat,
                            proficiency = prof
                        )
                    )
                    showAddSkillDialog = false
                }
            )
        }

        if (showAddAchieveDialog) {
            AddAchievementDialog(
                onDismiss = { showAddAchieveDialog = false },
                onConfirm = { title, desc, date, issuer ->
                    careerViewModel.addAchievement(
                        Achievement(
                            ownerUid = currentUid,
                            title = title,
                            description = desc,
                            date = date,
                            issuingOrganization = issuer
                        )
                    )
                    showAddAchieveDialog = false
                }
            )
        }
    }
}

@Composable
fun CertificationRowCard(cert: Certification, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = cert.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                }
            }
            Text(text = "${cert.issuingOrganization} • ${cert.category}", fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium)
            if (cert.description.isNotBlank()) Text(text = cert.description, fontSize = 11.sp, color = BodyText)
        }
    }
}

@Composable
fun ProjectRowCard(project: ProjectItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = project.projectName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                }
            }
            if (project.technologies.isNotEmpty()) {
                Text(text = "Tech: ${project.technologies.joinToString(", ")}", fontSize = 11.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold)
            }
            Text(text = project.description, fontSize = 11.sp, color = BodyText)
        }
    }
}

@Composable
fun SkillRowCard(skill: SkillItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = skill.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                Text(text = "${skill.category} • ${skill.proficiency ?: "Intermediate"}", fontSize = 11.sp, color = MutedText)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun AchievementRowCard(achievement: Achievement, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = achievement.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                }
            }
            Text(text = "${achievement.issuingOrganization} (${achievement.date})", fontSize = 11.sp, color = PrimaryIndigo, fontWeight = FontWeight.Medium)
            Text(text = achievement.description, fontSize = 11.sp, color = BodyText)
        }
    }
}

@Composable
fun VaultFileRowCard(file: PortfolioFile, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = file.fileName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                Text(text = "Category: ${file.category} • Type: ${file.fileType}", fontSize = 11.sp, color = MutedText)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun AddCertificationDialog(onDismiss: () -> Unit, onConfirm: (name: String, issuer: String, date: String, cat: String, desc: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Cloud Computing") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Certification", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Certification Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = issuer, onValueChange = { issuer = it }, label = { Text("Issuing Organization") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Issue Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && issuer.isNotBlank()) onConfirm(name, issuer, date, cat, desc) }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddProjectDialog(onDismiss: () -> Unit, onConfirm: (name: String, desc: String, techs: List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var techs by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Project", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Project Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = techs, onValueChange = { techs = it }, label = { Text("Technologies (comma separated)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && desc.isNotBlank()) {
                    val techList = techs.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onConfirm(name, desc, techList)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddSkillDialog(onDismiss: () -> Unit, onConfirm: (name: String, cat: String, prof: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Programming") }
    var prof by remember { mutableStateOf("Advanced") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Technical Skill", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Skill Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = prof, onValueChange = { prof = it }, label = { Text("Proficiency Level") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, cat, prof) }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddAchievementDialog(onDismiss: () -> Unit, onConfirm: (title: String, desc: String, date: String, issuer: String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Achievement", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = issuer, onValueChange = { issuer = it }, label = { Text("Issuing Organization") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank() && desc.isNotBlank()) onConfirm(title, desc, date, issuer) }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
