package com.example.projectx.model

import androidx.compose.runtime.Immutable

enum class LostItemStatus(val label: String) {
    REPORTED("Reported"),
    CLAIM_SUBMITTED("Claim Submitted"),
    VERIFIED("Verified"),
    HANDOVER_COMPLETE("Handover Complete"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String?): LostItemStatus {
            if (value.isNullOrBlank()) {
                throw IllegalArgumentException("LostItemStatus cannot be null or blank")
            }
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid LostItemStatus: '$value'")
        }
    }
}

@Immutable
data class LostItem(
    val itemId: String = "",
    val title: String = "",
    val description: String = "",
    val locationFound: String = "",
    val imageUrl: String? = null,
    val reporterUid: String = "",
    val status: LostItemStatus = LostItemStatus.REPORTED,
    val createdAt: Long = 0L,

    // Claim fields
    val claimantUid: String? = null,
    val claimNotes: String? = null,
    val claimantPhone: String? = null,
    val claimTimestamp: Long? = null,

    // Staff review & verification fields
    val reviewedByUid: String? = null,
    val reviewTimestamp: Long? = null,
    val verifiedByUid: String? = null,
    val verificationTimestamp: Long? = null,
    val staffNotes: String? = null,

    // Handover field
    val handoverTimestamp: Long? = null
)
