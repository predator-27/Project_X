package com.projectx.app.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.BuildConfig
import com.projectx.app.R
import com.projectx.app.model.UserRole
import com.projectx.app.theme.CampusTokens
import com.projectx.app.theme.MicrosoftBrandBlue
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.util.AuthValidation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onOpenWebView: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Student Login, 1 = Faculty Login, 2 = Register Account (Hidden)
    var showDemoRoleDialog by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val isRegistering = selectedTabIndex == 2
    val isDemoCredentials = remember(email, password) {
        val trimmed = email.trim()
        (trimmed.equals("S25CSEU1823", ignoreCase = true) || trimmed.equals("s25cseu1823@bennett.edu.in", ignoreCase = true)) && password == "12345678"
    }
    val isEmailValid = remember(email, isDemoCredentials) {
        isDemoCredentials || AuthValidation.isValidEmail(email)
    }
    val isPasswordValid = remember(password, isRegistering) {
        if (isRegistering) AuthValidation.isValidPassword(password) else password.isNotBlank()
    }
    val isPhoneValid = remember(phone) { AuthValidation.isValidPhoneNumber(phone) }

    val emailError = remember(email, isDemoCredentials) {
        if (email.isNotBlank() && !isEmailValid && !isDemoCredentials) AuthValidation.getEmailError(email) else null
    }
    val passwordError = remember(password, isRegistering) {
        if (isRegistering && password.isNotBlank() && !AuthValidation.isValidPassword(password)) {
            AuthValidation.getPasswordError(password)
        } else null
    }
    val phoneError = remember(phone) { if (phone.isNotBlank() && !isPhoneValid) AuthValidation.getPhoneNumberError(phone) else null }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
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

            // 2-Way Top Action Switcher (Student Login | Faculty Login)
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
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (selectedTabIndex == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Student Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (selectedTabIndex == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Faculty Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

                                // Microsoft Single Tenant OAuth Login Button
                                Button(
                                    onClick = {
                                        activity?.let { authViewModel.signInWithMicrosoft(it) }
                                    },
                                    enabled = !isLoading && activity != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = MicrosoftBrandBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
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
                                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("Continue with Microsoft (@bennett.edu.in)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                                    Text("or sign in with password", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                                }

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Student ID / Email (@bennett.edu.in)") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    placeholder = { Text("e.g. S25CSEU1823 or student@bennett.edu.in") },
                                    singleLine = true,
                                    isError = emailError != null,
                                    supportingText = emailError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                            }
                                        }
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                            }
                                        }
                                )

                                Button(
                                    onClick = {
                                        if (isDemoCredentials) {
                                            authViewModel.enterRoleDemoSession(UserRole.STUDENT)
                                        } else {
                                            authViewModel.signIn(email, password)
                                        }
                                    },
                                    enabled = (isEmailValid || isDemoCredentials) && isPasswordValid && !isLoading,
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

                                Text(
                                    text = "⚡ Demo credentials: S25CSEU1823 / 12345678",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }

                            1 -> {
                                // FACULTY LOGIN FORM
                                Text(
                                    text = "Faculty Portal Login",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Microsoft Single Tenant OAuth Login Button for Faculty
                                Button(
                                    onClick = {
                                        activity?.let { authViewModel.signInWithMicrosoft(it) }
                                    },
                                    enabled = !isLoading && activity != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = MicrosoftBrandBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
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
                                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("Continue with Microsoft (@bennett.edu.in)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                                    Text("or sign in with password", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                                }

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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                            }
                                        }
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                            }
                                        }
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
                                // REGISTER ACCOUNT FORM (Kept in codebase, hidden from starting login switcher)
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                            }
                                        }
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                            }
                                        }
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                            }
                                        }
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

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showDemoRoleDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Explore Demo Profiles",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom clearance spacer for soft keyboard scrolling
            Spacer(modifier = Modifier.height(120.dp))
        }

        // 5-Role Demo Profile Selection Dialog
        if (showDemoRoleDialog) {
            AlertDialog(
                onDismissRequest = { showDemoRoleDialog = false },
                title = {
                    Text(
                        text = "Select Demo Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Preview role-specific dashboards with test data. No real data or authentication is modified.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val demoRoles = listOf(
                            Triple(UserRole.STUDENT, "🎓 Student Demo", "Student Campus Shell, LMS & Attendance"),
                            Triple(UserRole.FACULTY, "👨‍🏫 Faculty Demo", "Teacher Admin Portal & Desk Availability"),
                            Triple(UserRole.COLLEGE_ADMIN, "🏢 College Admin Demo", "College Administration Shell"),
                            Triple(UserRole.SUPER_ADMIN, "🛡️ Super Admin Demo", "System Super Admin Shell"),
                            Triple(UserRole.LOST_FOUND_STAFF, "🔍 Lost & Found Staff Demo", "Lost & Found Operations Shell")
                        )

                        demoRoles.forEach { (role, title, desc) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDemoRoleDialog = false
                                        authViewModel.enterRoleDemoSession(role)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDemoRoleDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
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
            color = if (isMet) CampusTokens.colors.successGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = label,
            color = if (isMet) CampusTokens.colors.successGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
