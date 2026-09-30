package com.projectx.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.R
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.util.AuthValidation

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onOpenWebView: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Student Login, 1 = Faculty Login, 2 = Register Account

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val isEmailValid = remember(email) { AuthValidation.isValidEmail(email) }
    val isPasswordValid = remember(password) { AuthValidation.isValidPassword(password) }
    val isPhoneValid = remember(phone) { AuthValidation.isValidPhoneNumber(phone) }

    val emailError = remember(email) { if (email.isNotBlank() && !isEmailValid) AuthValidation.getEmailError(email) else null }
    val passwordError = remember(password) { if (password.isNotBlank() && !isPasswordValid) AuthValidation.getPasswordError(password) else null }
    val phoneError = remember(phone) { if (phone.isNotBlank() && !isPhoneValid) AuthValidation.getPhoneNumberError(phone) else null }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Gradient Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.height(72.dp),
                        )

                        Text(
                            text = "Project-X Campus Portal",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "Bennett University Academic Portal",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3-Way Top Action Switcher (Student Login | Faculty Login | Register Account)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Student Tab
                    Button(
                        onClick = {
                            selectedTabIndex = 0
                            authViewModel.clearErrorMessage()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selectedTabIndex == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (selectedTabIndex == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Student", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Faculty Tab
                    Button(
                        onClick = {
                            selectedTabIndex = 1
                            authViewModel.clearErrorMessage()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selectedTabIndex == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (selectedTabIndex == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Faculty", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Register Tab
                    Button(
                        onClick = {
                            selectedTabIndex = 2
                            authViewModel.clearErrorMessage()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTabIndex == 2) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selectedTabIndex == 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (selectedTabIndex == 2) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Register", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Banner
            errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Animated Form Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { tab ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (tab) {
                            0 -> {
                                // STUDENT LOGIN FORM
                                Text(
                                    text = "Student Portal Login",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "Requires a valid Bennett University student email (@bennett.edu.in)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Student Email (@bennett.edu.in)") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    placeholder = { Text("student@bennett.edu.in") },
                                    singleLine = true,
                                    isError = emailError != null,
                                    supportingText = emailError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    placeholder = { Text("••••••••") },
                                    singleLine = true,
                                    isError = passwordError != null,
                                    supportingText = passwordError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        authViewModel.signIn(email, password)
                                    },
                                    enabled = isEmailValid && isPasswordValid && !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Sign In as Student", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = { selectedTabIndex = 2 },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("New Student? Register Account", fontSize = 13.sp)
                                }
                            }

                            1 -> {
                                // FACULTY LOGIN FORM
                                Text(
                                    text = "Faculty Portal Login",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "Enter your official Bennett University faculty email & credentials",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Faculty Email (@bennett.edu.in)") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    placeholder = { Text("faculty.name@bennett.edu.in") },
                                    singleLine = true,
                                    isError = emailError != null,
                                    supportingText = emailError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    placeholder = { Text("••••••••") },
                                    singleLine = true,
                                    isError = passwordError != null,
                                    supportingText = passwordError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        authViewModel.signIn(email, password)
                                    },
                                    enabled = isEmailValid && isPasswordValid && !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("Sign In as Faculty", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ℹ️ Faculty accounts are pre-issued by Campus IT Administration.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            2 -> {
                                // REGISTER ACCOUNT FORM
                                Text(
                                    text = "Self-Register Account",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "Self-registration requires a valid institutional @bennett.edu.in email address.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Institutional Email (@bennett.edu.in)") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    placeholder = { Text("student@bennett.edu.in") },
                                    singleLine = true,
                                    isError = emailError != null,
                                    supportingText = emailError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Mobile Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    placeholder = { Text("+91 98765 43210") },
                                    singleLine = true,
                                    isError = phoneError != null,
                                    supportingText = phoneError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Create Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    placeholder = { Text("••••••••") },
                                    singleLine = true,
                                    isError = passwordError != null,
                                    supportingText = passwordError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Password requirements checklist
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Password Requirements:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        PasswordRequirementRule("At least 8 characters", password.length >= 8)
                                        PasswordRequirementRule("At least 1 capital letter (A-Z)", password.any { it.isUpperCase() })
                                        PasswordRequirementRule("At least 1 number (0-9)", password.any { it.isDigit() })
                                        PasswordRequirementRule("At least 1 special character (!, @, #, $, etc.)", password.any { !it.isLetterOrDigit() })
                                    }
                                }

                                Button(
                                    onClick = {
                                        authViewModel.register(email, password, phone)
                                    },
                                    enabled = isEmailValid && isPhoneValid && isPasswordValid && !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("Register Account", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = { selectedTabIndex = 0 },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Already registered? Sign In here", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            onOpenWebView?.let { openWeb ->
                TextButton(onClick = openWeb) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Open Web Application View")
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirementRule(label: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = if (isMet) "✓" else "•",
            color = if (isMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = label,
            color = if (isMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
