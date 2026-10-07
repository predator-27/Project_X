package com.projectx.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import com.projectx.app.components.StatusPill
import com.projectx.app.components.StatusTone
import com.projectx.app.theme.CampusTokens
import com.projectx.app.map.MapViewModel
import com.projectx.app.model.Appointment
import com.projectx.app.model.AppointmentStatus
import com.projectx.app.model.Teacher
import com.projectx.app.model.TeacherStatus
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.util.Resource
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    onMenuClick: () -> Unit = {},
    facultyViewModel: FacultyViewModel = viewModel(),
    appointmentViewModel: AppointmentViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onLocateOnMap: ((seatId: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val studentUid = currentUser?.uid ?: ""

    val sessionState by authViewModel.sessionState.collectAsState()
    val filteredTeachersResource by facultyViewModel.filteredTeachersState.collectAsState()
    val studentAppointmentsResource by appointmentViewModel.studentAppointmentsState.collectAsState()
    val actionState by appointmentViewModel.actionState.collectAsState()
    val mapState by mapViewModel.state.collectAsState()

    val searchQuery by facultyViewModel.searchQuery.collectAsState()
    val selectedDept by facultyViewModel.selectedDepartment.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Teachers Directory, 1 = My Appointments
    var bookingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val availableSeatIds = remember(mapState.map) {
        mapState.map?.vector?.seats?.map { it.id }?.toSet() ?: emptySet()
    }

    val studentDisplayName = remember(sessionState, currentUser) {
        val authSession = sessionState as? AuthSessionState.Authenticated
        authSession?.publicProfile?.displayName?.ifBlank { null }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Student"
    }

    val studentEmailAddress = remember(currentUser) {
        currentUser?.email ?: ""
    }

    val departments = remember { listOf("All", "Computer Science", "Data Science", "Software Engineering", "Cybersecurity") }

    LaunchedEffect(studentUid) {
        if (studentUid.isNotBlank()) {
            appointmentViewModel.loadStudentAppointments(studentUid)
        }
    }

    LaunchedEffect(actionState) {
        if (actionState is Resource.Success) {
            showSuccessSnackbar = true
            appointmentViewModel.resetActionState()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("👨‍🎓 Student Desk Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        snackbarHost = {
            if (showSuccessSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSuccessSnackbar = false }) {
                            Text("View", color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Appointment requested successfully!")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tab Row
            val apptCount = (studentAppointmentsResource as? Resource.Success)?.data?.size ?: 0
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🔍 Faculty Directory", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📅 My Appointments ($apptCount)", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { facultyViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by faculty name, desk #, or title...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { facultyViewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Department Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(departments, key = { it }) { dept ->
                        FilterChip(
                            selected = dept == selectedDept,
                            onClick = { facultyViewModel.setDepartmentFilter(dept) },
                            label = { Text(dept, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Teachers Directory List from Firestore
                when (val resource = filteredTeachersResource) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is Resource.Empty -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No faculty profiles available in campus directory.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = resource.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                                TextButton(onClick = { facultyViewModel.loadFacultyDirectory() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is Resource.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(resource.data, key = { it.id }) { teacher ->
                                TeacherCard(
                                    teacher = teacher,
                                    availableSeatIds = availableSeatIds,
                                    onBookClick = { bookingTeacher = teacher },
                                    onLocateOnMap = onLocateOnMap
                                )
                            }
                        }
                    }
                }
            } else {
                // My Appointments List from Firestore
                when (val resource = studentAppointmentsResource) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is Resource.Empty -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No appointments booked yet.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = resource.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                                TextButton(onClick = { appointmentViewModel.loadStudentAppointments(studentUid) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is Resource.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(resource.data, key = { it.id }) { appointment ->
                                StudentAppointmentCard(
                                    appointment = appointment,
                                    onCancelClick = {
                                        appointmentViewModel.updateAppointmentStatus(
                                            appointmentId = appointment.id,
                                            newStatus = AppointmentStatus.CANCELLED,
                                            studentUid = studentUid
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Booking Modal Dialog with REAL user inputs
        bookingTeacher?.let { teacher ->
            BookAppointmentDialog(
                teacher = teacher,
                studentDisplayName = studentDisplayName,
                studentEmailAddress = studentEmailAddress,
                onDismiss = { bookingTeacher = null },
                onConfirm = { date, slot, purpose ->
                    val newAppointment = Appointment(
                        id = "",
                        teacherId = teacher.id,
                        teacherName = teacher.name,
                        studentName = studentDisplayName,
                        studentEmail = studentEmailAddress,
                        date = date,
                        timeSlot = slot,
                        purpose = purpose,
                        status = AppointmentStatus.PENDING
                    )

                    appointmentViewModel.bookAppointment(
                        appointment = newAppointment,
                        studentUid = studentUid
                    )
                    bookingTeacher = null
                    selectedTab = 1
                }
            )
        }
    }
}

@Composable
fun TeacherCard(
    teacher: Teacher,
    availableSeatIds: Set<String>,
    onBookClick: () -> Unit,
    onLocateOnMap: ((seatId: String) -> Unit)? = null
) {
    val c = CampusTokens.colors
    val resolvedSeatId = remember(teacher.deskNumber, availableSeatIds) {
        val trimmed = teacher.deskNumber.trim()
        if (trimmed.isNotBlank() && availableSeatIds.contains(trimmed)) {
            trimmed
        } else {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.surfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    val initials = remember(teacher.name) {
                        teacher.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                    }
                    Surface(
                        shape = CircleShape,
                        color = c.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = c.heading
                            )
                        }
                    }

                    Column {
                        Text(
                            text = teacher.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.heading
                        )
                        Text(
                            text = "${teacher.title} • ${teacher.department}",
                            fontSize = 12.sp,
                            color = c.mutedText
                        )
                    }
                }

                StatusPill(
                    text = teacher.status.name.replace("_", " "),
                    tone = when (teacher.status) {
                        TeacherStatus.AT_DESK -> StatusTone.SUCCESS
                        TeacherStatus.IN_CLASS -> StatusTone.WARNING
                        TeacherStatus.BUSY -> StatusTone.DANGER
                        else -> StatusTone.NEUTRAL
                    }
                )
            }

            HorizontalDivider(color = c.divider, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Desk:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.heading
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = teacher.deskNumber.ifBlank { "Unassigned" },
                    fontSize = 12.sp,
                    color = c.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = c.mutedText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Office Hours:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.heading
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = teacher.timings.ifBlank { "Not specified" },
                    fontSize = 12.sp,
                    color = c.mutedText
                )
            }

            if (teacher.bio.isNotBlank()) {
                Text(
                    text = teacher.bio,
                    fontSize = 12.sp,
                    color = c.bodyText
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resolvedSeatId != null && onLocateOnMap != null) {
                    OutlinedButton(
                        onClick = { onLocateOnMap(resolvedSeatId) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, c.surfaceBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = c.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Locate on Map", fontSize = 11.sp, color = c.heading, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = c.surfaceElevated
                    ) {
                        Text(
                            text = "Map location unavailable",
                            fontSize = 10.sp,
                            color = c.mutedText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Button(
                    onClick = onBookClick,
                    enabled = teacher.isAvailableForAppointments,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Book Appointment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StudentAppointmentCard(
    appointment: Appointment,
    onCancelClick: () -> Unit
) {
    val c = CampusTokens.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.surfaceBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.teacherName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.heading
                )
                StatusPill(
                    text = appointment.status.name,
                    tone = when (appointment.status) {
                        AppointmentStatus.CONFIRMED -> StatusTone.SUCCESS
                        AppointmentStatus.PENDING -> StatusTone.WARNING
                        AppointmentStatus.REJECTED, AppointmentStatus.CANCELLED -> StatusTone.DANGER
                        else -> StatusTone.NEUTRAL
                    }
                )
            }

            Text(
                text = "📅 ${appointment.date} @ ${appointment.timeSlot}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = c.heading
            )

            Text(
                text = "Topic: ${appointment.purpose}",
                fontSize = 12.sp,
                color = c.bodyText
            )

            if (appointment.status == AppointmentStatus.PENDING || appointment.status == AppointmentStatus.CONFIRMED) {
                OutlinedButton(
                    onClick = onCancelClick,
                    border = BorderStroke(1.dp, c.dangerRed),
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Cancel Request", fontSize = 11.sp, color = c.dangerRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BookAppointmentDialog(
    teacher: Teacher,
    studentDisplayName: String,
    studentEmailAddress: String,
    onDismiss: () -> Unit,
    onConfirm: (date: String, timeSlot: String, purpose: String) -> Unit
) {
    val c = CampusTokens.colors
    var selectedDate by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = c.surface,
        unfocusedContainerColor = c.surface,
        focusedTextColor = c.heading,
        unfocusedTextColor = c.heading,
        focusedLabelColor = c.primary,
        unfocusedLabelColor = c.mutedText,
        focusedBorderColor = c.primary,
        unfocusedBorderColor = c.surfaceBorder,
        cursorColor = c.primary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text("Book Appointment with ${teacher.name}", fontWeight = FontWeight.Bold, color = c.heading) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "📍 Location: ${teacher.deskNumber.ifBlank { "Faculty Desk" }}",
                    fontSize = 12.sp,
                    color = c.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = studentDisplayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Student Name", color = c.mutedText) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = c.primary) },
                    singleLine = true,
                    colors = tfColors,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = studentEmailAddress,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Student Email", color = c.mutedText) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = c.primary) },
                    singleLine = true,
                    colors = tfColors,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Date (YYYY-MM-DD)", color = c.mutedText) },
                    placeholder = { Text("e.g. 2026-09-20", color = c.mutedText) },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = c.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = selectedSlot,
                    onValueChange = { selectedSlot = it },
                    label = { Text("Preferred Time Slot", color = c.mutedText) },
                    placeholder = { Text("e.g. 11:00 AM - 11:30 AM", color = c.mutedText) },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = c.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    colors = tfColors,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Discussion Topic / Purpose", color = c.mutedText) },
                    placeholder = { Text("e.g. Guidance on Internship or AI Project", color = c.mutedText) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = tfColors,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDate.isNotBlank() && selectedSlot.isNotBlank() && purpose.isNotBlank()) {
                        onConfirm(selectedDate, selectedSlot, purpose)
                    }
                },
                enabled = selectedDate.isNotBlank() && selectedSlot.isNotBlank() && purpose.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary)
            ) {
                Text("Confirm Request", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = c.heading, fontWeight = FontWeight.Bold)
            }
        }
    )
}
