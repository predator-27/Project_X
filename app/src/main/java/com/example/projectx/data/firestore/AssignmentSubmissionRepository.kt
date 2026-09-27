package com.example.projectx.data.firestore

import com.example.projectx.model.lms.AssignmentSubmission
import com.example.projectx.model.lms.SubmissionStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AssignmentSubmissionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val submissionsCollection = firestore.collection("assignment_submissions")

    suspend fun getStudentSubmission(
        assignmentId: String,
        studentUid: String
    ): Result<AssignmentSubmission?> = withContext(Dispatchers.IO) {
        if (assignmentId.isBlank() || studentUid.isBlank()) {
            return@withContext Result.success(null)
        }

        val canonicalDocId = "sub_${assignmentId}_${studentUid}"
        getSubmission(canonicalDocId)
    }

    suspend fun getSubmission(
        submissionId: String
    ): Result<AssignmentSubmission?> = withContext(Dispatchers.IO) {
        if (submissionId.isBlank()) {
            return@withContext Result.success(null)
        }

        try {
            val doc = submissionsCollection.document(submissionId).get().await()
            if (!doc.exists()) {
                return@withContext Result.success(null)
            }

            val submission = doc.toAssignmentSubmission()
            Result.success(submission)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read assignment submission: ${e.localizedMessage}"))
        }
    }

    suspend fun getSubmissionsForAssignment(
        assignmentId: String
    ): Result<List<AssignmentSubmission>> = withContext(Dispatchers.IO) {
        if (assignmentId.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val querySnapshot = submissionsCollection.whereEqualTo("assignmentId", assignmentId).get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val submissions = querySnapshot.documents.map { doc ->
                doc.toAssignmentSubmission()
            }

            Result.success(submissions)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load submissions for assignment: ${e.localizedMessage}"))
        }
    }

    suspend fun createSubmission(
        submission: AssignmentSubmission
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (submission.assignmentId.isBlank() || submission.studentUid.isBlank()) {
            return@withContext Result.failure(Exception("Cannot create submission: assignmentId or studentUid is blank."))
        }

        val canonicalDocId = "sub_${submission.assignmentId}_${submission.studentUid}"

        val createData = hashMapOf(
            "submissionId" to canonicalDocId,
            "assignmentId" to submission.assignmentId,
            "courseCode" to submission.courseCode,
            "studentUid" to submission.studentUid,
            "studentName" to submission.studentName,
            "studentRollNumber" to submission.studentRollNumber,
            "responseText" to submission.responseText,
            "storagePath" to submission.storagePath,
            "fileSizeBytes" to submission.fileSizeBytes,
            "fileType" to submission.fileType,
            "submittedAtTimestamp" to FieldValue.serverTimestamp(),
            "status" to SubmissionStatus.SUBMITTED.name,
            "isLate" to submission.isLate
        )

        try {
            submissionsCollection.document(canonicalDocId).set(createData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create assignment submission in Firestore: ${e.localizedMessage}"))
        }
    }

    suspend fun updateStudentSubmission(
        submission: AssignmentSubmission
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (submission.assignmentId.isBlank() || submission.studentUid.isBlank()) {
            return@withContext Result.failure(Exception("Cannot update submission: assignmentId or studentUid is blank."))
        }

        val canonicalDocId = "sub_${submission.assignmentId}_${submission.studentUid}"

        val updateData = hashMapOf<String, Any?>(
            "responseText" to submission.responseText,
            "storagePath" to submission.storagePath,
            "fileSizeBytes" to submission.fileSizeBytes,
            "fileType" to submission.fileType,
            "submittedAtTimestamp" to FieldValue.serverTimestamp(),
            "isLate" to submission.isLate
        )

        try {
            submissionsCollection.document(canonicalDocId).update(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update assignment submission in Firestore: ${e.localizedMessage}"))
        }
    }

    private fun DocumentSnapshot.toAssignmentSubmission(): AssignmentSubmission {
        if (!exists()) throw Exception("Document does not exist")

        val subId = getString("submissionId") ?: id
        val asgId = getString("assignmentId") ?: throw Exception("Missing assignmentId")
        val cCode = getString("courseCode") ?: throw Exception("Missing courseCode")
        val sUid = getString("studentUid") ?: throw Exception("Missing studentUid")
        val sName = getString("studentName") ?: ""
        val sRoll = getString("studentRollNumber") ?: ""

        val statusStr = getString("status") ?: throw Exception("Missing status")
        val statusVal = try {
            SubmissionStatus.valueOf(statusStr)
        } catch (e: Exception) {
            throw Exception("Malformed submission status: $statusStr")
        }

        val submittedAtVal = when (val raw = get("submittedAtTimestamp")) {
            is Long -> raw
            is Number -> raw.toLong()
            is Timestamp -> raw.toDate().time
            else -> throw Exception("Missing or malformed submittedAtTimestamp")
        }

        val gradedAtVal = when (val raw = get("gradedAtTimestamp")) {
            is Long -> raw
            is Number -> raw.toLong()
            is Timestamp -> raw.toDate().time
            else -> null
        }

        return AssignmentSubmission(
            submissionId = subId,
            assignmentId = asgId,
            courseCode = cCode,
            studentUid = sUid,
            studentName = sName,
            studentRollNumber = sRoll,
            responseText = getString("responseText"),
            storagePath = getString("storagePath"),
            fileSizeBytes = getLong("fileSizeBytes"),
            fileType = getString("fileType"),
            submittedAtTimestamp = submittedAtVal,
            status = statusVal,
            isLate = getBoolean("isLate") ?: false,
            pointsEarned = getLong("pointsEarned")?.toInt(),
            facultyFeedback = getString("facultyFeedback"),
            gradedByUid = getString("gradedByUid"),
            gradedAtTimestamp = gradedAtVal
        )
    }
}
