package com.projectx.app.data.firestore

import com.projectx.app.model.lms.CourseMaterial
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CourseMaterialRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val materialsCollection = firestore.collection("course_materials")

    suspend fun getMaterialsForCourse(courseCode: String): Result<List<CourseMaterial>> = withContext(Dispatchers.IO) {
        if (courseCode.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val querySnapshot = materialsCollection.whereEqualTo("courseCode", courseCode).get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val materials = querySnapshot.documents.mapNotNull { doc ->
                try {
                    val createdAtVal = when (val raw = doc.get("createdAt")) {
                        is Long -> raw
                        is Number -> raw.toLong()
                        is Timestamp -> raw.seconds * 1000L
                        else -> return@mapNotNull null
                    }

                    CourseMaterial(
                        materialId = doc.getString("materialId") ?: doc.id,
                        courseCode = doc.getString("courseCode") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description"),
                        fileUrl = doc.getString("fileUrl") ?: "",
                        fileType = doc.getString("fileType") ?: "PDF",
                        fileSizeBytes = doc.getLong("fileSizeBytes") ?: 0L,
                        uploadedByUid = doc.getString("uploadedByUid") ?: "",
                        uploadedByName = doc.getString("uploadedByName") ?: "",
                        createdAt = createdAtVal
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(materials)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load course materials from Firestore: ${e.localizedMessage}"))
        }
    }
}
