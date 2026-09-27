package com.example.projectx.ui.lms

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectx.data.firestore.AssignmentSubmissionRepository
import com.example.projectx.data.firestore.PublicProfileRepository
import com.example.projectx.data.firestore.UserRepository
import com.example.projectx.data.storage.AssignmentSubmissionStorageRepository
import com.example.projectx.model.lms.AssignmentSubmission
import com.example.projectx.model.lms.SubmissionStatus
import com.example.projectx.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssignmentSubmissionViewModel(
    private val submissionRepository: AssignmentSubmissionRepository = AssignmentSubmissionRepository(),
    private val storageRepository: AssignmentSubmissionStorageRepository = AssignmentSubmissionStorageRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val publicProfileRepository: PublicProfileRepository = PublicProfileRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _submissionState = MutableStateFlow<Resource<AssignmentSubmission?>>(Resource.Empty)
    val submissionState: StateFlow<Resource<AssignmentSubmission?>> = _submissionState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    fun loadStudentSubmission(assignmentId: String) {
        val studentUid = firebaseAuth.currentUser?.uid
        if (studentUid.isNullOrBlank()) {
            _submissionState.value = Resource.Error("User unauthenticated. Please log in.")
            return
        }

        viewModelScope.launch {
            _submissionState.value = Resource.Loading
            submissionRepository.getStudentSubmission(assignmentId, studentUid).fold(
                onSuccess = { submission ->
                    _submissionState.value = Resource.Success(submission)
                },
                onFailure = { error ->
                    _submissionState.value = Resource.Error(error.message ?: "Failed to load submission.")
                }
            )
        }
    }

    fun submitAssignment(
        context: Context,
        assignmentId: String,
        courseCode: String,
        dueDateTimestamp: Long?,
        responseText: String?,
        selectedFileUri: Uri?
    ) {
        // 1. Authenticate Student User
        val studentUid = firebaseAuth.currentUser?.uid
        if (studentUid.isNullOrBlank()) {
            _actionState.value = Resource.Error("Authentication error: Must be logged in to submit assignments.")
            return
        }

        // 2. Validate Assignment / Course Identifiers
        if (assignmentId.isBlank() || courseCode.isBlank()) {
            _actionState.value = Resource.Error("Invalid submission target: missing assignmentId or courseCode.")
            return
        }

        // 3. Validate Text / File Presence & Status
        val cleanText = responseText?.trim()?.ifBlank { null }
        if (cleanText == null && selectedFileUri == null) {
            _actionState.value = Resource.Error("Submission cannot be empty. Please enter a text response or attach a file.")
            return
        }

        val currentSub = (_submissionState.value as? Resource.Success)?.data
        if (currentSub?.status == SubmissionStatus.GRADED) {
            _actionState.value = Resource.Error("This assignment has already been graded and cannot be resubmitted.")
            return
        }

        viewModelScope.launch {
            _actionState.value = Resource.Loading

            // 4. Retrieve & Validate Student Identity Snapshot BEFORE File Upload
            val profileResult = publicProfileRepository.getPublicProfile(studentUid)
            val userResult = userRepository.getPrivateAccount(studentUid)

            if (profileResult.isFailure || userResult.isFailure) {
                _actionState.value = Resource.Error(
                    "Identity verification failed: Unable to fetch profile details. Please try again."
                )
                return@launch
            }

            val profile = profileResult.getOrNull()
            val user = userResult.getOrNull()

            val studentName = profile?.displayName?.trim()?.ifBlank { null }
            val studentRollNumber = user?.rollNumber?.trim()?.ifBlank { null }

            if (studentName == null || studentRollNumber == null) {
                _actionState.value = Resource.Error(
                    "Identity verification failed: Missing student name or roll number in profile data. Please complete profile setup before submitting."
                )
                return@launch
            }

            var uploadedStoragePath: String? = null
            var fileSizeBytes: Long? = null
            var fileType: String? = null

            // 5. Resolve & Validate File MIME, Extension, and Size (if File Present)
            if (selectedFileUri != null) {
                try {
                    val resolvedResult = resolveFileDetails(context, selectedFileUri)
                    if (resolvedResult.isFailure) {
                        _actionState.value = Resource.Error(
                            resolvedResult.exceptionOrNull()?.message ?: "Failed to validate file."
                        )
                        return@launch
                    }

                    val resolvedFile = resolvedResult.getOrThrow()

                    if (resolvedFile.sizeBytes <= 0) {
                        _actionState.value = Resource.Error("Selected file is empty (0 bytes).")
                        return@launch
                    }

                    if (resolvedFile.sizeBytes > AssignmentSubmissionStorageRepository.MAX_FILE_SIZE_BYTES) {
                        _actionState.value = Resource.Error("File size exceeds maximum allowed limit of 25 MB.")
                        return@launch
                    }

                    // 6. Read File Bytes
                    val fileBytes = context.contentResolver.openInputStream(selectedFileUri)?.use { it.readBytes() }
                    if (fileBytes == null || fileBytes.isEmpty()) {
                        _actionState.value = Resource.Error("Failed to read file contents from storage.")
                        return@launch
                    }

                    // 7. Upload File to Storage
                    val uploadResult = storageRepository.uploadSubmissionFile(
                        courseCode = courseCode,
                        assignmentId = assignmentId,
                        studentUid = studentUid,
                        extension = resolvedFile.extension,
                        contentType = resolvedFile.mimeType,
                        fileBytes = fileBytes
                    )

                    uploadResult.fold(
                        onSuccess = { path ->
                            uploadedStoragePath = path
                            fileSizeBytes = resolvedFile.sizeBytes
                            fileType = resolvedFile.extension.uppercase()
                        },
                        onFailure = { uploadError ->
                            _actionState.value = Resource.Error("File upload failed: ${uploadError.message}")
                            return@launch
                        }
                    )
                } catch (e: Exception) {
                    _actionState.value = Resource.Error("Failed to process file for upload: ${e.message}")
                    return@launch
                }
            }

            // 8. Derive Candidate isLate Value Locally
            val isLateCandidate = if (dueDateTimestamp != null) {
                System.currentTimeMillis() > dueDateTimestamp
            } else {
                false
            }

            // 9. Construct AssignmentSubmission Model
            val submission = AssignmentSubmission(
                submissionId = "sub_${assignmentId}_${studentUid}",
                assignmentId = assignmentId,
                courseCode = courseCode,
                studentUid = studentUid,
                studentName = studentName,
                studentRollNumber = studentRollNumber,
                responseText = cleanText,
                storagePath = uploadedStoragePath ?: currentSub?.storagePath,
                fileSizeBytes = fileSizeBytes ?: currentSub?.fileSizeBytes,
                fileType = fileType ?: currentSub?.fileType,
                status = SubmissionStatus.SUBMITTED,
                isLate = isLateCandidate
            )

            // 10. Firestore Write Metadata
            val writeResult = if (currentSub != null) {
                submissionRepository.updateStudentSubmission(submission)
            } else {
                submissionRepository.createSubmission(submission)
            }

            writeResult.fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadStudentSubmission(assignmentId)
                },
                onFailure = { writeError ->
                    if (uploadedStoragePath != null) {
                        _actionState.value = Resource.Error(
                            "Attachment file uploaded successfully, but submission metadata could not be saved to Firestore: ${writeError.message}"
                        )
                    } else {
                        _actionState.value = Resource.Error("Failed to save submission: ${writeError.message}")
                    }
                }
            )
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Empty
    }

    private data class FileDetails(
        val filename: String,
        val sizeBytes: Long,
        val mimeType: String,
        val extension: String
    )

    private fun resolveFileDetails(context: Context, uri: Uri): Result<FileDetails> {
        var filename = "attachment"
        var sizeBytes = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) filename = cursor.getString(nameIndex) ?: "attachment"
                if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
            }
        }

        val rawMime = (context.contentResolver.getType(uri) ?: "").trim().lowercase()

        val (canonicalMime, extension) = when {
            rawMime == "application/pdf" -> "application/pdf" to "pdf"
            rawMime == "application/zip" -> "application/zip" to "zip"
            rawMime == "application/x-zip-compressed" -> "application/x-zip-compressed" to "zip"
            rawMime == "application/msword" -> "application/msword" to "doc"
            rawMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "docx"
            rawMime == "image/png" -> "image/png" to "png"
            rawMime == "image/jpeg" -> "image/jpeg" to "jpg"
            else -> return Result.failure(
                Exception("Unsupported file MIME type: '$rawMime'. Allowed types: PDF, ZIP, DOC, DOCX, PNG, JPG.")
            )
        }

        return Result.success(FileDetails(filename, sizeBytes, canonicalMime, extension))
    }
}
