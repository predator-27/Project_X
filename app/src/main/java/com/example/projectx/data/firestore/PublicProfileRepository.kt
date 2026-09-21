package com.example.projectx.data.firestore

import com.example.projectx.model.PublicProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PublicProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val publicProfilesCollection = firestore.collection("public_profiles")

    suspend fun createPublicProfile(publicProfile: PublicProfile): Result<Unit> = withContext(Dispatchers.IO) {
        if (publicProfile.uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("UID cannot be empty."))
        }

        try {
            val profileMap = hashMapOf<String, Any?>(
                "uid" to publicProfile.uid,
                "displayName" to publicProfile.displayName,
                "profileImageUrl" to publicProfile.profileImageUrl,
                "schoolName" to publicProfile.schoolName,
                "program" to publicProfile.program,
                "department" to publicProfile.department,
                "specialization" to publicProfile.specialization,
                "admissionYear" to publicProfile.admissionYear,
                "currentSemester" to publicProfile.currentSemester,
                "section" to publicProfile.section,
                "bio" to publicProfile.bio
            )

            publicProfilesCollection.document(publicProfile.uid).set(profileMap, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save public profile: ${e.localizedMessage}"))
        }
    }

    suspend fun getPublicProfile(uid: String): Result<PublicProfile?> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("UID cannot be empty."))
        }

        try {
            val snapshot = publicProfilesCollection.document(uid).get().await()
            if (!snapshot.exists()) {
                return@withContext Result.success(null)
            }

            val profile = PublicProfile(
                uid = snapshot.getString("uid") ?: uid,
                displayName = snapshot.getString("displayName") ?: "",
                profileImageUrl = snapshot.getString("profileImageUrl"),
                schoolName = snapshot.getString("schoolName"),
                program = snapshot.getString("program"),
                department = snapshot.getString("department"),
                specialization = snapshot.getString("specialization"),
                admissionYear = snapshot.getLong("admissionYear")?.toInt(),
                currentSemester = snapshot.getLong("currentSemester")?.toInt(),
                section = snapshot.getString("section"),
                bio = snapshot.getString("bio")
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read public profile: ${e.localizedMessage}"))
        }
    }

    suspend fun updatePublicProfile(publicProfile: PublicProfile): Result<Unit> = withContext(Dispatchers.IO) {
        if (publicProfile.uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("UID cannot be empty."))
        }

        try {
            val updates = hashMapOf<String, Any?>()
            updates["displayName"] = publicProfile.displayName
            publicProfile.profileImageUrl?.let { updates["profileImageUrl"] = it }
            publicProfile.schoolName?.let { updates["schoolName"] = it }
            publicProfile.program?.let { updates["program"] = it }
            publicProfile.department?.let { updates["department"] = it }
            publicProfile.specialization?.let { updates["specialization"] = it }
            publicProfile.admissionYear?.let { updates["admissionYear"] = it }
            publicProfile.currentSemester?.let { updates["currentSemester"] = it }
            publicProfile.section?.let { updates["section"] = it }
            publicProfile.bio?.let { updates["bio"] = it }

            publicProfilesCollection.document(publicProfile.uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update public profile: ${e.localizedMessage}"))
        }
    }
}
