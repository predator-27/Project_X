package com.example.projectx.data.firestore

import com.example.projectx.model.AttendanceSubject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AttendanceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val attendanceCollection = firestore.collection("attendance")

    suspend fun getStudentAttendance(studentUid: String): Result<List<AttendanceSubject>> = withContext(Dispatchers.IO) {
        if (studentUid.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val querySnapshot = attendanceCollection.whereEqualTo("studentUid", studentUid).get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val subjects = querySnapshot.documents.mapNotNull { doc ->
                try {
                    AttendanceSubject(
                        courseCode = doc.getString("courseCode") ?: "",
                        courseName = doc.getString("courseName") ?: "",
                        attendedClasses = doc.getLong("attendedClasses")?.toInt() ?: 0,
                        totalClasses = doc.getLong("totalClasses")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(subjects)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load attendance from Firestore: ${e.localizedMessage}"))
        }
    }
}
