package com.projectx.app.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Immutable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
)

enum class MessageSender {
    USER,
    AI
}

class CampusAiViewModel : ViewModel() {

    private fun welcomeMessage(): String {
        val displayName = FirebaseAuth.getInstance().currentUser?.displayName?.trim()
        val name = displayName.takeIf { !it.isNullOrBlank() }
        val opening = if (name != null) "Hello $name!" else "Hello!"
        return "$opening I am your Campus Gemini AI Tutor & Study Buddy 🎓. Ask me anything about your courses, exam preparation, study schedules, or campus life!"
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = welcomeMessage()
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setUserNameGreeting(displayName: String?) {
        val greeting = if (!displayName.isNullOrBlank()) "Hello, $displayName!" else "Hello!"
        val updatedMessage = ChatMessage(
            sender = MessageSender.AI,
            text = "$greeting I am your Campus Gemini AI Tutor & Study Buddy 🎓. Ask me anything about your courses, exam preparation, study schedules, or campus life!"
        )
        if (_messages.value.size == 1 && _messages.value.first().sender == MessageSender.AI) {
            _messages.value = listOf(updatedMessage)
        }
    }

    fun sendMessage(userPrompt: String) {
        val cleanPrompt = userPrompt.trim()
        if (cleanPrompt.isBlank() || _isLoading.value) return

        val userMessage = ChatMessage(sender = MessageSender.USER, text = cleanPrompt)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val generativeModel = Firebase.ai.generativeModel(modelName = "gemini-flash-latest")
                val systemContextPrompt = """
                    You are Project X Campus AI Tutor for Bennett University.
                    You assist students with campus information, academics, attendance, timetable, faculty directory, appointments, indoor navigation, lost & found, announcements, and student services.
                    Be helpful, clear, concise, and polite. If live data is unavailable, state what is known without hallucinating.

                    Student Question: $cleanPrompt
                """.trimIndent()

                val response = generativeModel.generateContent(systemContextPrompt)
                val responseText = response.text

                if (!responseText.isNullOrBlank()) {
                    _messages.value = _messages.value + ChatMessage(
                        sender = MessageSender.AI,
                        text = responseText.trim()
                    )
                } else {
                    _messages.value = _messages.value + ChatMessage(
                        sender = MessageSender.AI,
                        text = getSmartFallbackResponse(cleanPrompt)
                    )
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    sender = MessageSender.AI,
                    text = getSmartFallbackResponse(cleanPrompt)
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getSmartFallbackResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("data structure") || p.contains("tree") || p.contains("graph") ->
                "📚 **Data Structures Concept**: Trees are hierarchical non-linear structures with a root node and child nodes (e.g. Binary Search Trees). Graphs consist of vertices connected by edges and can be directed or undirected. Tip: Focus on Time Complexity (O(log N) for BST search vs O(V+E) for BFS/DFS graph traversals) for your upcoming exam!"
            p.contains("dbms") || p.contains("sql") || p.contains("database") ->
                "💡 **DBMS Exam Strategy**: Prioritize ER Diagrams, Normalization (1NF, 2NF, 3NF, BCNF), and ACID Properties (Atomicity, Consistency, Isolation, Durability). Practice SQL JOINs and indexing queries to boost your score!"
            p.contains("leave") || p.contains("email") ->
                "✉️ **Sample Leave Email**: 'Respected Faculty, I am writing to request a 2-day leave from [Date] to [Date] due to [Reason]. I will ensure all class notes and assignments are caught up upon my return. Thank you.'"
            p.contains("group") || p.contains("peer") || p.contains("mentor") ->
                "🤝 **Peer Mentorship & Study Groups**: You can join campus peer study circles in the Community tab or volunteer to mentor junior students in subjects you excel at!"
            else ->
                "✨ Here is a quick educational tip: Break your study sessions into 25-minute Pomodoro intervals with 5-minute breaks. Reviewing your DBMS and Data Structures notes 20 minutes before sleeping significantly improves long-term memory retention!"
        }
    }
}
