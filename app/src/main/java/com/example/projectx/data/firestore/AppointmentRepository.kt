package com.projectx.app.data.firestore

import com.projectx.app.model.Appointment
import com.projectx.app.model.AppointmentStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AppointmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val appointmentsCollection = firestore.collection("appointments")

    suspend fun createAppointment(
        appointment: Appointment,
        studentUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (studentUid.isBlank() || appointment.teacherId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Student UID and Faculty UID must not be empty."))
        }

        val canonicalDocId = "app_${studentUid}_${appointment.teacherId}_${System.currentTimeMillis()}"

        val createData = hashMapOf(
            "appointmentId" to canonicalDocId,
            "studentUid" to studentUid,
            "facultyUid" to appointment.teacherId,
            "studentName" to appointment.studentName,
            "studentEmail" to appointment.studentEmail,
            "facultyName" to appointment.teacherName,
            "purpose" to appointment.purpose,
            "proposedTime" to "${appointment.date} @ ${appointment.timeSlot}",
            "date" to appointment.date,
            "timeSlot" to appointment.timeSlot,
            "status" to AppointmentStatus.PENDING.name,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            appointmentsCollection.document(canonicalDocId).set(createData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save appointment request to Firestore: ${e.localizedMessage}"))
        }
    }

    suspend fun getStudentAppointments(studentUid: String): Result<List<Appointment>> = withContext(Dispatchers.IO) {
        if (studentUid.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val querySnapshot = appointmentsCollection
                .whereEqualTo("studentUid", studentUid)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val appointments = querySnapshot.documents.mapNotNull { doc ->
                doc.toAppointment()
            }

            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load student appointments from Firestore: ${e.localizedMessage}"))
        }
    }

    suspend fun getFacultyAppointments(facultyUid: String): Result<List<Appointment>> = withContext(Dispatchers.IO) {
        if (facultyUid.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val querySnapshot = appointmentsCollection
                .whereEqualTo("facultyUid", facultyUid)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val appointments = querySnapshot.documents.mapNotNull { doc ->
                doc.toAppointment()
            }

            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load faculty appointments from Firestore: ${e.localizedMessage}"))
        }
    }

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: AppointmentStatus,
        facultyNotes: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (appointmentId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Appointment ID must not be empty."))
        }

        try {
            val updates = hashMapOf<String, Any?>(
                "status" to newStatus.name
            )
            if (!facultyNotes.isNullOrBlank()) {
                updates["facultyNotes"] = facultyNotes.trim()
            }

            appointmentsCollection.document(appointmentId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update appointment status: ${e.localizedMessage}"))
        }
    }

    private fun DocumentSnapshot.toAppointment(): Appointment? {
        if (!exists()) return null
        return try {
            val statusStr = getString("status") ?: AppointmentStatus.PENDING.name
            val statusVal = try {
                AppointmentStatus.valueOf(statusStr)
            } catch (e: Exception) {
                AppointmentStatus.PENDING
            }

            val createdMillis = when (val raw = get("createdAt")) {
                is Long -> raw
                is Number -> raw.toLong()
                is Timestamp -> raw.seconds * 1000L
                else -> return null
            }

            val rawDate = getString("date") ?: ""
            val rawSlot = getString("timeSlot") ?: ""
            val proposed = getString("proposedTime") ?: ""

            val dateVal = rawDate.ifBlank { proposed.substringBefore(" @").ifBlank { "TBD" } }
            val slotVal = rawSlot.ifBlank { proposed.substringAfter("@ ").ifBlank { "TBD" } }

            Appointment(
                id = getString("appointmentId") ?: id,
                teacherId = getString("facultyUid") ?: "",
                teacherName = getString("facultyName") ?: "Faculty Member",
                studentName = getString("studentName") ?: "Student",
                studentEmail = getString("studentEmail") ?: "",
                date = dateVal,
                timeSlot = slotVal,
                purpose = getString("purpose") ?: "",
                status = statusVal,
                timestamp = createdMillis
            )
        } catch (e: Exception) {
            null
        }
    }
}
