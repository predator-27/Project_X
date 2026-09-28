package com.example.projectx.data.firestore

import com.example.projectx.model.UniversityAnnouncement
import com.example.projectx.model.lms.AnnouncementPriority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class AnnouncementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun getAnnouncements(): Result<List<UniversityAnnouncement>> {
        return try {
            val querySnapshot = firestore.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = querySnapshot.documents.map { doc ->
                doc.toAnnouncementStrict()
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAnnouncement(
        title: String,
        content: String,
        targetDepartment: String,
        priority: AnnouncementPriority
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be logged in to publish an announcement."))

        if (title.isBlank() || content.isBlank() || targetDepartment.isBlank()) {
            return Result.failure(IllegalArgumentException("Title, content, and target department cannot be empty."))
        }

        return try {
            val profileSnapshot = firestore.collection("public_profiles").document(currentUid).get().await()
            val authorDisplayName = profileSnapshot.getString("displayName")
            if (authorDisplayName.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Authenticated user public profile display name is missing or blank."))
            }

            val docRef = firestore.collection("announcements").document()
            val data = mapOf(
                "id" to docRef.id,
                "title" to title.trim(),
                "content" to content.trim(),
                "authorName" to authorDisplayName,
                "authorUid" to currentUid,
                "targetDepartment" to targetDepartment.trim(),
                "priority" to priority.name,
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnnouncement(
        id: String,
        title: String,
        content: String,
        targetDepartment: String,
        priority: AnnouncementPriority
    ): Result<Unit> {
        if (id.isBlank() || title.isBlank() || content.isBlank() || targetDepartment.isBlank()) {
            return Result.failure(IllegalArgumentException("ID, title, content, and target department cannot be empty."))
        }

        return try {
            val updates = mapOf(
                "title" to title.trim(),
                "content" to content.trim(),
                "targetDepartment" to targetDepartment.trim(),
                "priority" to priority.name
            )

            firestore.collection("announcements").document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("Announcement ID cannot be blank."))
        }

        return try {
            firestore.collection("announcements").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toAnnouncementStrict(): UniversityAnnouncement {
        if (!exists()) {
            throw IllegalStateException("Document /announcements/$id does not exist")
        }

        val annId = getString("id")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing required field 'id'")
        val title = getString("title")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing required field 'title'")
        val content = getString("content")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing required field 'content'")
        val authorName = getString("authorName")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing required field 'authorName'")
        val authorUid = getString("authorUid")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing required field 'authorUid'")
        val targetDept = getString("targetDepartment")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing required field 'targetDepartment'")

        val priorityStr = getString("priority")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing required field 'priority'")
        val priorityVal = try {
            AnnouncementPriority.valueOf(priorityStr)
        } catch (e: Exception) {
            throw IllegalStateException("Malformed document in /announcements/$id: invalid priority value '$priorityStr'")
        }

        val createdAtTimestamp = getTimestamp("createdAt")
            ?: throw IllegalStateException("Malformed document in /announcements/$id: missing or invalid required timestamp 'createdAt'")
        val createdAtVal = createdAtTimestamp.toDate().time

        return UniversityAnnouncement(
            id = annId,
            title = title,
            content = content,
            authorName = authorName,
            authorUid = authorUid,
            targetDepartment = targetDept,
            priority = priorityVal,
            createdAt = createdAtVal
        )
    }
}
