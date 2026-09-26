package com.example.projectx.data.firestore

import com.example.projectx.model.TimetableSlot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TimetableRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val timetablesCollection = firestore.collection("timetables")

    suspend fun getTimetableSlots(sectionFilter: String? = null): Result<List<TimetableSlot>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = timetablesCollection.get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val slots = querySnapshot.documents.mapNotNull { doc ->
                try {
                    TimetableSlot(
                        id = doc.id,
                        courseName = doc.getString("courseName") ?: "",
                        courseCode = doc.getString("courseCode") ?: "",
                        section = doc.getString("section") ?: "",
                        timeSlot = doc.getString("timeSlot") ?: "",
                        durationMins = doc.getLong("durationMins")?.toInt() ?: 60,
                        facultyName = doc.getString("facultyName") ?: "",
                        roomCode = doc.getString("roomCode") ?: "",
                        type = doc.getString("type") ?: "Lecture"
                    )
                } catch (e: Exception) {
                    null
                }
            }

            val filteredSlots = if (!sectionFilter.isNullOrBlank()) {
                slots.filter { it.section.equals(sectionFilter, ignoreCase = true) }
            } else {
                slots
            }

            Result.success(filteredSlots)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load timetable from Firestore: ${e.localizedMessage}"))
        }
    }
}
