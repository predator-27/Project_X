package com.example.projectx.ui.lms

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import com.example.projectx.components.*
import com.example.projectx.model.lms.SubmissionStatus
import com.example.projectx.theme.*
import com.example.projectx.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailScreen(
    assignmentId: String,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: AssignmentDetailViewModel = viewModel(),
    submissionViewModel: AssignmentSubmissionViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assignmentState by viewModel.assignmentState.collectAsState()
    val submissionState by submissionViewModel.submissionState.collectAsState()
    val actionState by submissionViewModel.actionState.collectAsState()

    var responseText by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var showResubmitForm by remember { mutableStateOf(false) }

    val allowedMimeTypes = remember {
        arrayOf(
            "application/pdf",
            "application/zip",
            "application/x-zip-compressed",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg"
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "selected_file"
        }
    }

    LaunchedEffect(assignmentId) {
        viewModel.loadAssignment(assignmentId)
        submissionViewModel.loadStudentSubmission(assignmentId)
    }

    AppScaffold(
        title = "Assignment Details",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Back Action
            item {
                SectionFormCard(sectionTitle = "Assignment Overview") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PrimaryIndigo
                                )
                            }
                            Text(
                                text = "Assignment Portal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeadingNavy
                            )
                        }

                        IconButton(onClick = {
                            viewModel.loadAssignment(assignmentId)
                            submissionViewModel.loadStudentSubmission(assignmentId)
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryIndigo)
                        }
                    }
                }
            }

            // Assignment Details Section
            when (val state = assignmentState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryIndigo)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        SectionFormCard(sectionTitle = "Error Loading Assignment") {
                            Text(text = state.message, fontSize = 13.sp, color = AccentCoral)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.refreshAssignment() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is Resource.Empty -> {
                    item {
                        EmptyStateCard(title = "Assignment not found or unavailable.")
                    }
                }
                is Resource.Success -> {
                    val assignment = state.data

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = assignment.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HeadingNavy,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatusPill(text = "${assignment.maxPoints} Points", tone = StatusTone.NEUTRAL)
                                }

                                if (assignment.courseCode.isNotBlank()) {
                                    Text(
                                        text = "Course Code: ${assignment.courseCode}",
                                        fontSize = 12.sp,
                                        color = PrimaryIndigo,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                                Text(
                                    text = assignment.description.ifBlank { "No additional details provided." },
                                    fontSize = 13.sp,
                                    color = BodyText
                                )

                                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                        val dueText = remember(assignment.dueDateTimestamp) {
                                            if (assignment.dueDateTimestamp != null) {
                                                "Due: " + SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault()).format(Date(assignment.dueDateTimestamp))
                                            } else {
                                                "Due date not specified"
                                            }
                                        }
                                        Text(text = dueText, fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                                    }

                                    if (!assignment.attachmentUrl.isNullOrBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                                            Text(text = "Reference Material", fontSize = 11.sp, color = MutedText)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Student Submission Portal Section
                    item {
                        SectionFormCard(sectionTitle = "Student Submission Portal") {
                            when (val subState = submissionState) {
                                is Resource.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = PrimaryIndigo)
                                    }
                                }
                                is Resource.Error -> {
                                    Text(text = subState.message, fontSize = 13.sp, color = AccentCoral)
                                }
                                else -> {
                                    val currentSubmission = (subState as? Resource.Success)?.data

                                    if (currentSubmission == null) {
                                        // No Submission Yet -> Render Submission Form
                                        SubmissionFormView(
                                            responseText = responseText,
                                            onResponseTextChange = { responseText = it },
                                            selectedFileName = selectedFileName,
                                            onSelectFileClick = { filePickerLauncher.launch(allowedMimeTypes) },
                                            onClearFileClick = {
                                                selectedFileUri = null
                                                selectedFileName = null
                                            },
                                            isSubmitting = actionState is Resource.Loading,
                                            onSubmitClick = {
                                                submissionViewModel.submitAssignment(
                                                    context = context,
                                                    assignmentId = assignment.assignmentId,
                                                    courseCode = assignment.courseCode,
                                                    dueDateTimestamp = assignment.dueDateTimestamp,
                                                    responseText = responseText,
                                                    selectedFileUri = selectedFileUri
                                                )
                                            }
                                        )
                                    } else {
                                        // Existing Submission Display
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryEmerald, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = "Status: ${currentSubmission.status.name}",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HeadingNavy
                                                    )
                                                }

                                                if (currentSubmission.isLate) {
                                                    StatusPill(text = "LATE SUBMISSION", tone = StatusTone.DANGER)
                                                } else {
                                                    StatusPill(text = "ON TIME", tone = StatusTone.SUCCESS)
                                                }
                                            }

                                            val submittedDateStr = remember(currentSubmission.submittedAtTimestamp) {
                                                if (currentSubmission.submittedAtTimestamp > 0) {
                                                    SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault()).format(Date(currentSubmission.submittedAtTimestamp))
                                                } else {
                                                    "Submitted"
                                                }
                                            }
                                            Text(text = "Submitted at: $submittedDateStr", fontSize = 12.sp, color = MutedText)

                                            if (!currentSubmission.responseText.isNullOrBlank()) {
                                                Text(text = "Text Response:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HeadingNavy)
                                                Text(text = currentSubmission.responseText, fontSize = 13.sp, color = BodyText)
                                            }

                                            if (!currentSubmission.storagePath.isNullOrBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                                    Text(
                                                        text = "Attachment: ${currentSubmission.storagePath.substringAfterLast('/')}",
                                                        fontSize = 12.sp,
                                                        color = PrimaryIndigo,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            // If Graded -> Display Feedback & Score (NO resubmit form!)
                                            if (currentSubmission.status == SubmissionStatus.GRADED) {
                                                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.Grade, contentDescription = null, tint = SecondaryEmerald, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = "Grade: ${currentSubmission.pointsEarned ?: 0} / ${assignment.maxPoints} Points",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SecondaryEmerald
                                                    )
                                                }

                                                if (!currentSubmission.facultyFeedback.isNullOrBlank()) {
                                                    Text(text = "Faculty Feedback:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HeadingNavy)
                                                    Text(text = currentSubmission.facultyFeedback, fontSize = 13.sp, color = BodyText)
                                                }

                                                Text(
                                                    text = "🔒 Submission Graded & Locked",
                                                    fontSize = 12.sp,
                                                    color = MutedText,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            } else {
                                                // Ungraded SUBMITTED -> Allow Resubmission Toggle
                                                HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

                                                if (!showResubmitForm) {
                                                    OutlinedButton(
                                                        onClick = { showResubmitForm = true },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        border = BorderStroke(1.dp, PrimaryIndigo)
                                                    ) {
                                                        Text("Resubmit Assignment", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    SubmissionFormView(
                                                        responseText = responseText,
                                                        onResponseTextChange = { responseText = it },
                                                        selectedFileName = selectedFileName,
                                                        onSelectFileClick = { filePickerLauncher.launch(allowedMimeTypes) },
                                                        onClearFileClick = {
                                                            selectedFileUri = null
                                                            selectedFileName = null
                                                        },
                                                        isSubmitting = actionState is Resource.Loading,
                                                        isResubmit = true,
                                                        onSubmitClick = {
                                                            submissionViewModel.submitAssignment(
                                                                context = context,
                                                                assignmentId = assignment.assignmentId,
                                                                courseCode = assignment.courseCode,
                                                                dueDateTimestamp = assignment.dueDateTimestamp,
                                                                responseText = responseText,
                                                                selectedFileUri = selectedFileUri
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Action State Error Alert Box
                            if (actionState is Resource.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = AccentCoralBg),
                                    border = BorderStroke(1.dp, AccentCoral)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = (actionState as Resource.Error).message,
                                            fontSize = 12.sp,
                                            color = AccentCoral,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { submissionViewModel.resetActionState() }) {
                                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = AccentCoral)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionFormView(
    responseText: String,
    onResponseTextChange: (String) -> Unit,
    selectedFileName: String?,
    onSelectFileClick: () -> Unit,
    onClearFileClick: () -> Unit,
    isSubmitting: Boolean,
    isResubmit: Boolean = false,
    onSubmitClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = responseText,
            onValueChange = onResponseTextChange,
            label = { Text("Text Response (Optional)") },
            placeholder = { Text("Enter your text response here...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            enabled = !isSubmitting
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSelectFileClick,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedFileName != null) "Change Attachment" else "Attach File (PDF, ZIP, DOCX, Image)",
                    fontSize = 12.sp,
                    color = HeadingNavy
                )
            }

            if (selectedFileName != null) {
                IconButton(onClick = onClearFileClick, enabled = !isSubmitting) {
                    Icon(Icons.Default.Close, contentDescription = "Remove File", tint = AccentCoral)
                }
            }
        }

        if (selectedFileName != null) {
            Text(
                text = "Attached: $selectedFileName",
                fontSize = 12.sp,
                color = PrimaryIndigo,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onSubmitClick,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uploading & Submitting...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isResubmit) "Confirm Resubmission" else "Submit Assignment", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
