package com.projectx.app.ui.hostel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.*
import com.projectx.app.theme.*
import com.projectx.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPartnerScreen(
    onMenuClick: () -> Unit,
    hostelViewModel: HostelViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val roomPartnerState by hostelViewModel.roomPartnerState.collectAsState()
    val currentRequest = (roomPartnerState as? Resource.Success)?.data

    var selectedRoomType by remember(currentRequest) { mutableStateOf(currentRequest?.roomType ?: "Triple Sharing") }
    var partnerRollNo by remember(currentRequest) { mutableStateOf(currentRequest?.partnerRollNo ?: "") }
    var partnerName by remember(currentRequest) { mutableStateOf(currentRequest?.partnerName ?: "") }

    AppScaffold(
        title = "Room Partner Selection",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Explanatory Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(22.dp))
                            Text("Room Partner & Preference Setup", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                        }

                        Text(
                            text = "Start sending and receiving room requests after selecting your preferred room type.",
                            fontSize = 12.sp,
                            color = BodyText
                        )
                    }
                }
            }

            // Current Status Overview
            item {
                if (currentRequest != null && currentRequest.status != "NO_REQUEST") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Active Request: ${currentRequest.roomType}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HeadingNavy
                                )
                                currentRequest.partnerName?.let { name ->
                                    Text(
                                        text = "Requested Partner: $name (${currentRequest.partnerRollNo ?: "N/A"})",
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                            }

                            StatusPill(
                                text = when (currentRequest.status) {
                                    "REQUEST_ACCEPTED" -> "Accepted"
                                    "REQUEST_SENT" -> "Request Sent"
                                    else -> "No Request"
                                },
                                tone = when (currentRequest.status) {
                                    "REQUEST_ACCEPTED" -> StatusTone.SUCCESS
                                    "REQUEST_SENT" -> StatusTone.WARNING
                                    else -> StatusTone.NEUTRAL
                                }
                            )
                        }
                    }
                }
            }

            // Select Room Type Options
            item {
                Text("Select Room Type Preference", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Triple Sharing Option
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRoomType == "Triple Sharing") InfoBannerBg else SurfaceCard
                        ),
                        border = BorderStroke(1.dp, if (selectedRoomType == "Triple Sharing") PrimaryIndigo else SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Bed, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                    Text("Triple Sharing Room", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                                }
                                Text("3 Students per room, equipped with individual study desks, wardrobes, and attached balcony.", fontSize = 11.sp, color = BodyText)
                            }

                            RadioButton(
                                selected = selectedRoomType == "Triple Sharing",
                                onClick = { selectedRoomType = "Triple Sharing" }
                            )
                        }
                    }

                    // Double Sharing Option
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRoomType == "Double Sharing") InfoBannerBg else SurfaceCard
                        ),
                        border = BorderStroke(1.dp, if (selectedRoomType == "Double Sharing") PrimaryIndigo else SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Bed, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                    Text("Double Sharing Room", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                                }
                                Text("2 Students per room, premium climate control, private balcony, and personal study pods.", fontSize = 11.sp, color = BodyText)
                            }

                            RadioButton(
                                selected = selectedRoomType == "Double Sharing",
                                onClick = { selectedRoomType = "Double Sharing" }
                            )
                        }
                    }
                }
            }

            // Room Partner Form
            item {
                SectionFormCard(sectionTitle = "Send Room Partner Request") {
                    OutlinedTextField(
                        value = partnerRollNo,
                        onValueChange = { partnerRollNo = it },
                        label = { Text("Requested Partner Roll Number") },
                        placeholder = { Text("e.g. E26CSEU0099") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = partnerName,
                        onValueChange = { partnerName = it },
                        label = { Text("Partner Full Name (Optional)") },
                        placeholder = { Text("e.g. Alex Rivera") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            hostelViewModel.selectRoomTypeAndPartner(
                                roomType = selectedRoomType,
                                partnerRollNo = partnerRollNo,
                                partnerName = partnerName
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Room Partner Request", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
