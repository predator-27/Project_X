package com.example.projectx.data.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AssignmentSubmissionStorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES: Long = 25L * 1024L * 1024L // 25 MiB
    }

    suspend fun uploadSubmissionFile(
        courseCode: String,
        assignmentId: String,
        studentUid: String,
        extension: String,
        contentType: String,
        fileBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanMime = contentType.trim().lowercase()

        // 1. Strict Canonical Identifier Validation (NO trimming or modifying identifiers!)
        if (isInvalidIdentifier(courseCode)) {
            return@withContext Result.failure(Exception("Invalid courseCode: must not be blank or contain whitespace or path characters ('$courseCode')."))
        }

        if (isInvalidIdentifier(assignmentId)) {
            return@withContext Result.failure(Exception("Invalid assignmentId: must not be blank or contain whitespace or path characters ('$assignmentId')."))
        }

        if (isInvalidIdentifier(studentUid)) {
            return@withContext Result.failure(Exception("Invalid studentUid: must not be blank or contain whitespace or path characters ('$studentUid')."))
        }

        // 2. Strict Extension Validation (Remove ONE optional leading '.', NO trimming of whitespace!)
        val rawExt = if (extension.startsWith(".")) extension.substring(1) else extension
        if (isInvalidExtension(rawExt)) {
            return@withContext Result.failure(Exception("Invalid file extension: '$extension'."))
        }
        val cleanExtension = rawExt.lowercase()

        // 3. MIME Allowlist & Consistency Check
        if (!isExtensionAndContentTypeValid(cleanExtension, cleanMime)) {
            return@withContext Result.failure(
                Exception("Inconsistent or unsupported file type: extension '$cleanExtension' with MIME '$contentType'.")
            )
        }

        // 4. File Size & Empty Check
        if (fileBytes.isEmpty()) {
            return@withContext Result.failure(Exception("Cannot upload empty submission file."))
        }

        if (fileBytes.size > MAX_FILE_SIZE_BYTES) {
            return@withContext Result.failure(
                Exception("File size (${fileBytes.size} bytes) exceeds maximum allowed limit of 25 MB.")
            )
        }

        // 5. Exact Canonical Storage Path (Uses exact supplied identifiers)
        val relativeStoragePath = "courses/$courseCode/submissions/$assignmentId/$studentUid/submission.$cleanExtension"

        try {
            val storageRef = storage.reference.child(relativeStoragePath)
            val metadata = StorageMetadata.Builder()
                .setContentType(cleanMime)
                .build()

            storageRef.putBytes(fileBytes, metadata).await()
            Result.success(relativeStoragePath)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload submission file to Firebase Storage: ${e.localizedMessage}"))
        }
    }

    private fun isInvalidIdentifier(value: String): Boolean {
        return value.isBlank() || value.contains("/") || value.contains("\\") || value.contains("..") || value.any { it.isWhitespace() }
    }

    private fun isInvalidExtension(ext: String): Boolean {
        return ext.isEmpty() || ext.contains("/") || ext.contains("\\") || ext.contains("..") || ext.any { it.isWhitespace() }
    }

    private fun isExtensionAndContentTypeValid(ext: String, mime: String): Boolean {
        return when (ext) {
            "pdf" -> mime == "application/pdf"
            "zip" -> mime == "application/zip" || mime == "application/x-zip-compressed"
            "doc" -> mime == "application/msword"
            "docx" -> mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "png" -> mime == "image/png"
            "jpg", "jpeg" -> mime == "image/jpeg"
            else -> false
        }
    }
}
