package com.example.projectx.data.firestore

import com.example.projectx.model.LostItem
import com.example.projectx.model.LostItemStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LostFoundRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getBrowseableItems(): List<LostItem> {
        val querySnapshot = firestore.collection("lost_found")
            .get()
            .await()

        return querySnapshot.documents.map { it.toLostItemStrict() }
            .filter { it.status == LostItemStatus.REPORTED || it.status == LostItemStatus.CLAIM_SUBMITTED }
            .sortedByDescending { it.createdAt }
    }

    suspend fun getMyClaims(studentUid: String): List<LostItem> {
        if (studentUid.isBlank()) return emptyList()

        val querySnapshot = firestore.collection("lost_found")
            .whereEqualTo("claimantUid", studentUid)
            .get()
            .await()

        return querySnapshot.documents.map { it.toLostItemStrict() }
            .sortedByDescending { it.claimTimestamp ?: it.createdAt }
    }

    suspend fun getMyReports(reporterUid: String): List<LostItem> {
        if (reporterUid.isBlank()) return emptyList()

        val querySnapshot = firestore.collection("lost_found")
            .whereEqualTo("reporterUid", reporterUid)
            .get()
            .await()

        return querySnapshot.documents.map { it.toLostItemStrict() }
            .sortedByDescending { it.createdAt }
    }

    suspend fun getStaffQueue(): List<LostItem> {
        val querySnapshot = firestore.collection("lost_found")
            .whereEqualTo("status", LostItemStatus.CLAIM_SUBMITTED.name)
            .get()
            .await()

        return querySnapshot.documents.map { it.toLostItemStrict() }
            .sortedByDescending { it.claimTimestamp ?: it.createdAt }
    }

    suspend fun reportFoundItem(
        title: String,
        description: String,
        locationFound: String,
        imageUrl: String?,
        reporterUid: String
    ) {
        val docRef = firestore.collection("lost_found").document()
        val data = mutableMapOf<String, Any?>(
            "itemId" to docRef.id,
            "title" to title,
            "description" to description,
            "locationFound" to locationFound,
            "imageUrl" to imageUrl,
            "reporterUid" to reporterUid,
            "status" to LostItemStatus.REPORTED.name,
            "createdAt" to FieldValue.serverTimestamp()
        )

        docRef.set(data).await()
    }

    suspend fun submitClaim(
        itemId: String,
        claimantUid: String,
        claimNotes: String,
        claimantPhone: String
    ) {
        val updates = mapOf(
            "claimantUid" to claimantUid,
            "claimNotes" to claimNotes,
            "claimantPhone" to claimantPhone,
            "claimTimestamp" to FieldValue.serverTimestamp(),
            "status" to LostItemStatus.CLAIM_SUBMITTED.name
        )

        firestore.collection("lost_found").document(itemId).update(updates).await()
    }

    suspend fun rejectClaim(
        itemId: String,
        staffUid: String,
        staffNotes: String?
    ) {
        val updates = mutableMapOf<String, Any?>(
            "status" to LostItemStatus.REPORTED.name,
            "reviewedByUid" to staffUid,
            "reviewTimestamp" to FieldValue.serverTimestamp()
        )
        if (!staffNotes.isNullOrBlank()) {
            updates["staffNotes"] = staffNotes
        }

        firestore.collection("lost_found").document(itemId).update(updates).await()
    }

    suspend fun verifyClaim(
        itemId: String,
        staffUid: String,
        staffNotes: String?
    ) {
        val updates = mutableMapOf<String, Any?>(
            "status" to LostItemStatus.VERIFIED.name,
            "verifiedByUid" to staffUid,
            "verificationTimestamp" to FieldValue.serverTimestamp(),
            "reviewedByUid" to staffUid,
            "reviewTimestamp" to FieldValue.serverTimestamp()
        )
        if (!staffNotes.isNullOrBlank()) {
            updates["staffNotes"] = staffNotes
        }

        firestore.collection("lost_found").document(itemId).update(updates).await()
    }

    suspend fun completeHandover(
        itemId: String,
        staffNotes: String?
    ) {
        val updates = mutableMapOf<String, Any?>(
            "status" to LostItemStatus.HANDOVER_COMPLETE.name,
            "handoverTimestamp" to FieldValue.serverTimestamp()
        )
        if (!staffNotes.isNullOrBlank()) {
            updates["staffNotes"] = staffNotes
        }

        firestore.collection("lost_found").document(itemId).update(updates).await()
    }

    suspend fun cancelOwnReport(itemId: String) {
        val updates = mapOf(
            "status" to LostItemStatus.CANCELLED.name
        )

        firestore.collection("lost_found").document(itemId).update(updates).await()
    }

    private fun DocumentSnapshot.toLostItemStrict(): LostItem {
        if (!exists()) {
            throw IllegalStateException("Document /lost_found/$id does not exist")
        }

        val itemIdStr = getString("itemId") ?: id
        val title = getString("title")
            ?: throw IllegalStateException("Malformed document in /lost_found/$id: missing required field 'title'")
        val description = getString("description")
            ?: throw IllegalStateException("Malformed document in /lost_found/$id: missing required field 'description'")
        val locationFound = getString("locationFound")
            ?: throw IllegalStateException("Malformed document in /lost_found/$id: missing required field 'locationFound'")
        val reporterUid = getString("reporterUid")
            ?: throw IllegalStateException("Malformed document in /lost_found/$id: missing required field 'reporterUid'")
        val statusStr = getString("status")
            ?: throw IllegalStateException("Malformed document in /lost_found/$id: missing required field 'status'")

        val status = LostItemStatus.fromString(statusStr)

        val createdAtTimestamp = getTimestamp("createdAt")
            ?: throw IllegalStateException("Malformed document in /lost_found/$id: missing or invalid required timestamp 'createdAt'")
        val createdAt = createdAtTimestamp.toDate().time

        val imageUrl = getString("imageUrl")
        val claimantUid = getString("claimantUid")
        val claimNotes = getString("claimNotes")
        val claimantPhone = getString("claimantPhone")
        val claimTimestamp = getTimestamp("claimTimestamp")?.toDate()?.time

        val reviewedByUid = getString("reviewedByUid")
        val reviewTimestamp = getTimestamp("reviewTimestamp")?.toDate()?.time
        val verifiedByUid = getString("verifiedByUid")
        val verificationTimestamp = getTimestamp("verificationTimestamp")?.toDate()?.time
        val staffNotes = getString("staffNotes")

        val handoverTimestamp = getTimestamp("handoverTimestamp")?.toDate()?.time

        return LostItem(
            itemId = itemIdStr,
            title = title,
            description = description,
            locationFound = locationFound,
            imageUrl = imageUrl,
            reporterUid = reporterUid,
            status = status,
            createdAt = createdAt,
            claimantUid = claimantUid,
            claimNotes = claimNotes,
            claimantPhone = claimantPhone,
            claimTimestamp = claimTimestamp,
            reviewedByUid = reviewedByUid,
            reviewTimestamp = reviewTimestamp,
            verifiedByUid = verifiedByUid,
            verificationTimestamp = verificationTimestamp,
            staffNotes = staffNotes,
            handoverTimestamp = handoverTimestamp
        )
    }
}
