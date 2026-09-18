package com.example.projectx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.*
import com.example.projectx.model.CampusMessage
import com.example.projectx.model.SampleCampusData
import com.example.projectx.theme.*

@Composable
fun MessagesScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val messages = SampleCampusData.sampleMessages

    val filteredMessages = remember(searchQuery) {
        if (searchQuery.isBlank()) messages
        else messages.filter {
            it.senderName.contains(searchQuery, ignoreCase = true) ||
                    it.subject.contains(searchQuery, ignoreCase = true)
        }
    }

    AppScaffold(
        title = "Messages",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                FilterSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholderText = "Search messages or sender..."
                )
            }

            items(filteredMessages, key = { it.id }) { message ->
                MessageCard(message = message)
            }
        }
    }
}

@Composable
fun MessageCard(message: CampusMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (message.isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PrimaryIndigo, shape = CircleShape)
                        )
                    }
                    Text(
                        text = message.senderName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
                Text(
                    text = message.timestamp,
                    fontSize = 11.sp,
                    color = MutedText
                )
            }

            Text(
                text = message.subject,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingNavy
            )

            Text(
                text = message.preview,
                fontSize = 13.sp,
                color = BodyText,
                maxLines = 2
            )
        }
    }
}
