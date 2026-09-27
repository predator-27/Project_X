package com.example.projectx.data.firestore

import com.example.projectx.model.lms.Assignment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AssignmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val assignmentsCollection = firestore.collection("assignments")

    suspend fun getAssignmentsForCourse(courseCode: String): Result<List<Assignment>> = withContext(Dispatchers.IO) {
        if (courseCode.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val querySnapshot = assignmentsCollection.whereEqualTo("courseCode", courseCode).get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val assignments = querySnapshot.documents.mapNotNull { doc ->
                try {
                    val dueVal = when (val raw = doc.get("dueDateTimestamp")) {
                        is Long -> raw
                        is Number -> raw.toLong()
                        is Timestamp -> raw.seconds * 1000L
                        else -> null
                    }

                    val createdVal = when (val raw = doc.get("createdAt")) {
                        is Long -> raw
                        is Number -> raw.toLong()
                        is Timestamp -> raw.seconds * 1000L
                        else -> return@mapNotNull null
                    }

                    Assignment(
                        assignmentId = doc.getString("assignmentId") ?: doc.id,
                        courseCode = doc.getString("courseCode") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        maxPoints = doc.getLong("maxPoints")?.toInt() ?: 100,
                        dueDateTimestamp = dueVal,
                        attachmentUrl = doc.getString("attachmentUrl"),
                        publishedByUid = doc.getString("publishedByUid") ?: "",
                        createdAt = createdVal
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(assignments)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load assignments from Firestore: ${e.localizedMessage}"))
        }
    }

    suspend fun getAssignment(assignmentId: String): Result<Assignment?> = withContext(Dispatchers.IO) {
        if (assignmentId.isBlank()) {
            return@withContext Result.success(null)
        }

        try {
            val doc = assignmentsCollection.document(assignmentId).get().await()
            if (!doc.exists()) {
                return@withContext Result.success(null)
            }

            val dueVal = when (val raw = doc.get("dueDateTimestamp")) {
                is Long -> raw
                is Number -> raw.toLong()
                is Timestamp -> raw.seconds * 1000L
                else -> null
            }

            val createdVal = when (val raw = doc.get("createdAt")) {
                is Long -> raw
                is Number -> raw.toLong()
                is Timestamp -> raw.seconds * 1000L
                else -> return@withContext Result.failure(Exception("Assignment document missing required createdAt timestamp."))
            }

            val assignment = Assignment(
                assignmentId = doc.getString("assignmentId") ?: doc.id,
                courseCode = doc.getString("courseCode") ?: "",
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                maxPoints = doc.getLong("maxPoints")?.toInt() ?: 100,
                dueDateTimestamp = dueVal,
                attachmentUrl = doc.getString("attachmentUrl"),
                publishedByUid = doc.getString("publishedByUid") ?: "",
                createdAt = createdVal
            )

            Result.success(assignment)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load assignment from Firestore: ${e.localizedMessage}"))
        }
    }
}
