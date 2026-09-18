package com.example.projectx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: String = "Just now"
)

enum class MessageSender {
    USER,
    AI
}

class CampusAiViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Hello Alex! I am your Campus Gemini AI Tutor & Study Buddy 🎓. Ask me anything about your courses, exam preparation, study schedules, or campus life!"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return

        val userMessage = ChatMessage(sender = MessageSender.USER, text = userPrompt)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val generativeModel = Firebase.ai.generativeModel(modelName = "gemini-flash-latest")
                val response = generativeModel.generateContent(userPrompt)
                val responseText = response.text

                if (!responseText.isNullOrBlank()) {
                    _messages.value = _messages.value + ChatMessage(
                        sender = MessageSender.AI,
                        text = responseText
                    )
                } else {
                    _messages.value = _messages.value + ChatMessage(
                        sender = MessageSender.AI,
                        text = getSmartFallbackResponse(userPrompt)
                    )
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    sender = MessageSender.AI,
                    text = getSmartFallbackResponse(userPrompt)
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
