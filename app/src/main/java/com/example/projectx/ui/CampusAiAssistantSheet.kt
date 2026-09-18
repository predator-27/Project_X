package com.example.projectx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.components.StatusPill
import com.example.projectx.components.StatusTone
import com.example.projectx.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusAiAssistantSheet(
    aiViewModel: CampusAiViewModel,
    onDismiss: () -> Unit
) {
    val messages by aiViewModel.messages.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val promptChips = listOf(
        "📚 Explain Trees vs Graphs in Data Structures",
        "📝 How to prepare for DBMS mid-terms?",
        "✉️ Draft a respectful leave request email",
        "🤝 How to form a Peer Study Group on campus?"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = InfoBannerBg,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(22.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Campus Gemini AI Tutor",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeadingNavy
                        )
                        Text(
                            text = "24/7 Educational & Academic Assistant",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = HeadingNavy)
                }
            }

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            // Suggested Prompt Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                items(promptChips) { chipText ->
                    Surface(
                        shape = PillShape,
                        color = InfoBannerBg,
                        modifier = Modifier.clickable {
                            aiViewModel.sendMessage(chipText)
                        }
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Chat Messages Thread
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryIndigo, strokeWidth = 2.dp)
                            Text("Gemini AI is thinking...", fontSize = 12.sp, color = MutedText)
                        }
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask AI Tutor about courses, exams...", fontSize = 13.sp, color = MutedText) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            aiViewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (inputText.isNotBlank() && !isLoading) PrimaryIndigo else SurfaceBorder,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) PrimaryIndigo else InfoBannerBg,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "You" else "✨ Gemini AI Tutor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color.White.copy(alpha = 0.8f) else PrimaryIndigo
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else BodyText,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
