package com.projectx.app.data.firestore

import com.projectx.app.model.lms.AnnouncementPriority
import com.projectx.app.model.lms.CourseAnnouncement
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CourseAnnouncementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val announcementsCollection = firestore.collection("course_announcements")

    suspend fun getAnnouncementsForCourse(courseCode: String): Result<List<CourseAnnouncement>> = withContext(Dispatchers.IO) {
        if (courseCode.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val querySnapshot = announcementsCollection.whereEqualTo("courseCode", courseCode).get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val announcements = querySnapshot.documents.mapNotNull { doc ->
                try {
                    val priorityStr = doc.getString("priority") ?: AnnouncementPriority.NORMAL.name
                    val priorityVal = try {
                        AnnouncementPriority.valueOf(priorityStr)
                    } catch (e: Exception) {
                        AnnouncementPriority.NORMAL
                    }

                    val createdAtVal = when (val raw = doc.get("createdAt")) {
                        is Long -> raw
                        is Number -> raw.toLong()
                        is Timestamp -> raw.seconds * 1000L
                        else -> return@mapNotNull null
                    }

                    CourseAnnouncement(
                        announcementId = doc.getString("announcementId") ?: doc.id,
                        courseCode = doc.getString("courseCode") ?: "",
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        authorUid = doc.getString("authorUid") ?: "",
                        priority = priorityVal,
                        createdAt = createdAtVal
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(announcements)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load course announcements from Firestore: ${e.localizedMessage}"))
        }
    }
}
