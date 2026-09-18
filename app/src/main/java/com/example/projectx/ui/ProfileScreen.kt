package com.example.projectx.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.theme.*

@Composable
fun ProfileScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) } // 0..3
    var isSubmitted by remember { mutableStateOf(false) }

    // Step 1: Student Details
    var studentName by remember { mutableStateOf("Alex Rivera") }
    var rollNo by remember { mutableStateOf("26BCA102") }
    var email by remember { mutableStateOf("alex.r@student.edu") }
    var mobile by remember { mutableStateOf("+91 98765 43210") }

    // Step 2: Parent & Guardian Details
    var fatherName by remember { mutableStateOf("Carlos Rivera") }
    var fatherMobile by remember { mutableStateOf("+91 98765 00000") }
    var motherName by remember { mutableStateOf("Maria Rivera") }

    // Step 3: Educational Details
    var highSchool by remember { mutableStateOf("Global International School") }
    var highSchoolPercent by remember { mutableStateOf("92.4%") }

    // Step 4: Other Info
    var bloodGroup by remember { mutableStateOf("O+") }
    var hostelStatus by remember { mutableStateOf("Hosteller (Block B - 204)") }

    AppScaffold(
        title = "Student Profile",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Student Summary Card
            item {
                SectionFormCard(sectionTitle = "Summary Profile") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = studentName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                            Text(text = "Roll No: $rollNo • BCA Dept", fontSize = 13.sp, color = MutedText)
                        }
                        StatusPill(
                            text = if (isSubmitted) "Pending Approval" else "Active Student",
                            tone = if (isSubmitted) StatusTone.WARNING else StatusTone.SUCCESS
                        )
                    }
                }
            }

            // 4-Step Wizard Stepper
            item {
                SectionFormCard(sectionTitle = "Profile Verification Wizard") {
                    WizardStepper(
                        steps = listOf(
                            "Student Details",
                            "Parent & Guardian",
                            "Educational Details",
                            "Other Info"
                        ),
                        currentStepIndex = currentStep
                    )
                }
            }

            // Active Step Content Form
            item {
                when (currentStep) {
                    0 -> {
                        SectionFormCard(sectionTitle = "Step 1: Student Personal Details") {
                            FormFieldLabel(text = "Full Name", isRequired = true)
                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { studentName = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            FormFieldLabel(text = "Roll Number", isRequired = true)
                            OutlinedTextField(
                                value = rollNo,
                                onValueChange = { rollNo = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            FormFieldLabel(text = "Email Address", isRequired = true)
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            FormFieldLabel(text = "Mobile Number", isRequired = true)
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    1 -> {
                        SectionFormCard(sectionTitle = "Step 2: Parent & Guardian Details") {
                            FormFieldLabel(text = "Father's Full Name", isRequired = true)
                            OutlinedTextField(
                                value = fatherName,
                                onValueChange = { fatherName = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            FormFieldLabel(text = "Father's Contact", isRequired = true)
                            OutlinedTextField(
                                value = fatherMobile,
                                onValueChange = { fatherMobile = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            FormFieldLabel(text = "Mother's Full Name", isRequired = true)
                            OutlinedTextField(
                                value = motherName,
                                onValueChange = { motherName = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    2 -> {
                        SectionFormCard(sectionTitle = "Step 3: Educational Details") {
                            FormFieldLabel(text = "High School / Institution", isRequired = true)
                            OutlinedTextField(
                                value = highSchool,
                                onValueChange = { highSchool = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            FormFieldLabel(text = "Percentage / GPA", isRequired = true)
                            OutlinedTextField(
                                value = highSchoolPercent,
                                onValueChange = { highSchoolPercent = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    else -> {
                        SectionFormCard(sectionTitle = "Step 4: Other Info & Health") {
                            FormFieldLabel(text = "Blood Group", isRequired = true)
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = { bloodGroup = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            FormFieldLabel(text = "Hostel / Residential Status", isRequired = true)
                            OutlinedTextField(
                                value = hostelStatus,
                                onValueChange = { hostelStatus = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Wizard Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SecondaryButton(
                        text = "Previous",
                        onClick = { if (currentStep > 0) currentStep-- },
                        enabled = currentStep > 0
                    )

                    if (currentStep < 3) {
                        PrimaryButton(
                            text = "Next Step",
                            onClick = { currentStep++ }
                        )
                    } else {
                        PrimaryButton(
                            text = "Submit for Approval",
                            onClick = {
                                isSubmitted = true
                            }
                        )
                    }
                }
            }
        }
    }
}
