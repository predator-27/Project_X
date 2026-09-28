package com.example.projectx.data.firestore

import com.example.projectx.model.Teacher
import com.example.projectx.model.TeacherStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FacultyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val facultyCollection = firestore.collection("faculty_profiles")

    suspend fun getFacultyDirectory(): Result<List<Teacher>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = facultyCollection.get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val facultyList = querySnapshot.documents.mapNotNull { doc ->
                doc.toTeacher()
            }

            Result.success(facultyList)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load faculty directory from Firestore: ${e.localizedMessage}"))
        }
    }

    suspend fun getFacultyByUid(facultyUid: String): Result<Teacher?> = withContext(Dispatchers.IO) {
        if (facultyUid.isBlank()) {
            return@withContext Result.success(null)
        }

        try {
            val snapshot = facultyCollection.document(facultyUid).get().await()
            if (!snapshot.exists()) {
                return@withContext Result.success(null)
            }

            Result.success(snapshot.toTeacher())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read faculty profile: ${e.localizedMessage}"))
        }
    }

    suspend fun updateOwnFacultyStatus(facultyUid: String, status: TeacherStatus): Result<Unit> = withContext(Dispatchers.IO) {
        if (facultyUid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Faculty UID cannot be empty."))
        }

        try {
            facultyCollection.document(facultyUid).update("status", status.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update faculty status: ${e.localizedMessage}"))
        }
    }

    suspend fun updateOwnFacultyTimings(facultyUid: String, timings: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (facultyUid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Faculty UID cannot be empty."))
        }

        try {
            facultyCollection.document(facultyUid).update("timings", timings.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update office hours / timings: ${e.localizedMessage}"))
        }
    }

    suspend fun updateOwnFacultyBio(facultyUid: String, bio: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (facultyUid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Faculty UID cannot be empty."))
        }

        try {
            facultyCollection.document(facultyUid).update("bio", bio.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update faculty bio: ${e.localizedMessage}"))
        }
    }

    private fun DocumentSnapshot.toTeacher(): Teacher? {
        if (!exists()) return null
        return try {
            val statusStr = getString("status") ?: TeacherStatus.AT_DESK.name
            val statusVal = try {
                TeacherStatus.valueOf(statusStr)
            } catch (e: Exception) {
                TeacherStatus.AT_DESK
            }

            Teacher(
                id = getString("uid") ?: id,
                name = getString("name") ?: "",
                title = getString("title") ?: "Faculty Member",
                department = getString("department") ?: "",
                deskNumber = getString("deskNumber") ?: "",
                timings = getString("timings") ?: "",
                email = getString("email") ?: "",
                institution = getString("institution") ?: "Bennett University",
                institutionDomain = getString("institutionDomain") ?: "bennett.edu.in",
                status = statusVal,
                isAvailableForAppointments = getBoolean("isAvailableForAppointments") ?: true,
                bio = getString("bio") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
