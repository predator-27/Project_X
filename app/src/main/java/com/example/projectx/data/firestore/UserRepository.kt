package com.example.projectx.data.firestore

import com.example.projectx.model.User
import com.example.projectx.model.UserRole
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection("users")

    suspend fun createPrivateAccount(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        if (user.uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("User UID cannot be empty."))
        }

        try {
            val userMap = hashMapOf<String, Any?>(
                "uid" to user.uid,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "rollNumber" to user.rollNumber,
                "role" to UserRole.STUDENT.name, // Force role = STUDENT for self-registration
                "isActive" to true,               // Force isActive = true for self-registration
                "createdAt" to FieldValue.serverTimestamp(), // Matches Firestore Rule: request.resource.data.createdAt == request.time
                "updatedAt" to FieldValue.serverTimestamp()
            )

            usersCollection.document(user.uid).set(userMap, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save user account data: ${e.localizedMessage}"))
        }
    }

    suspend fun getPrivateAccount(uid: String): Result<User?> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("UID cannot be empty."))
        }

        try {
            val snapshot = usersCollection.document(uid).get().await()
            if (!snapshot.exists()) {
                return@withContext Result.success(null)
            }

            val roleStr = snapshot.getString("role")
            val role = UserRole.fromString(roleStr)

            val createdAtMillis = snapshot.getTimestamp("createdAt")?.toDate()?.time
                ?: snapshot.getLong("createdAt")
                ?: System.currentTimeMillis()

            val updatedAtMillis = snapshot.getTimestamp("updatedAt")?.toDate()?.time
                ?: snapshot.getLong("updatedAt")
                ?: System.currentTimeMillis()

            val user = User(
                uid = snapshot.getString("uid") ?: uid,
                email = snapshot.getString("email") ?: "",
                phoneNumber = snapshot.getString("phoneNumber"),
                rollNumber = snapshot.getString("rollNumber"),
                role = role,
                isActive = snapshot.getBoolean("isActive") ?: true,
                createdAt = createdAtMillis,
                updatedAt = updatedAtMillis
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read user account: ${e.localizedMessage}"))
        }
    }

    suspend fun updatePhoneNumber(uid: String, phoneNumber: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("UID cannot be empty."))
        }

        try {
            val updates = hashMapOf<String, Any?>(
                "phoneNumber" to phoneNumber,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update phone number: ${e.localizedMessage}"))
        }
    }
}
