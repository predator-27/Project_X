package com.projectx.app.data.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CareerStorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES: Long = 25L * 1024L * 1024L // 25 MiB
    }

    suspend fun uploadCareerFile(
        uid: String,
        subPath: String,
        fileName: String,
        extension: String,
        contentType: String,
        fileBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        if (isInvalidIdentifier(uid)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid uid: must not be blank, contain whitespace, or path characters ('$uid')."))
        }

        if (isInvalidIdentifier(subPath)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid subPath: must not be blank, contain whitespace, or path characters ('$subPath')."))
        }

        if (isInvalidFilename(fileName)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid fileName: must not be blank or contain path traversal characters ('$fileName')."))
        }

        val rawExt = extension.removePrefix(".").trim().lowercase()
        if (rawExt.isEmpty() || rawExt.contains("/") || rawExt.contains("\\") || rawExt.contains("..")) {
            return@withContext Result.failure(IllegalArgumentException("Invalid extension: '$extension'."))
        }

        if (fileBytes.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Cannot upload empty file."))
        }

        if (fileBytes.size > MAX_FILE_SIZE_BYTES) {
            return@withContext Result.failure(IllegalArgumentException("File size (${fileBytes.size} bytes) exceeds 25 MB limit."))
        }

        val cleanFileName = fileName.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val canonicalStoragePath = "career/$uid/$subPath/$cleanFileName.$rawExt"

        try {
            val storageRef = storage.reference.child(canonicalStoragePath)
            val metadata = StorageMetadata.Builder()
                .setContentType(contentType.trim().ifBlank { "application/octet-stream" })
                .build()

            storageRef.putBytes(fileBytes, metadata).await()
            Result.success(canonicalStoragePath)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload career document to Firebase Storage: ${e.localizedMessage}"))
        }
    }

    suspend fun deleteCareerFile(storagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (storagePath.isBlank() || storagePath.contains("..")) {
            return@withContext Result.failure(IllegalArgumentException("Invalid storage path."))
        }
        try {
            storage.reference.child(storagePath).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete file from Firebase Storage: ${e.localizedMessage}"))
        }
    }

    private fun isInvalidIdentifier(value: String): Boolean {
        return value.isBlank() || value.contains("/") || value.contains("\\") || value.contains("..") || value.any { it.isWhitespace() }
    }

    private fun isInvalidFilename(name: String): Boolean {
        return name.isBlank() || name.contains("/") || name.contains("\\") || name.contains("..")
    }
}
