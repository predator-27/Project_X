package com.projectx.app.model.career

import androidx.compose.runtime.Immutable

@Immutable
data class PortfolioFile(
    val fileId: String = "",
    val ownerUid: String = "",
    val fileName: String = "",
    val fileType: String = "PDF",
    val category: String = "General",
    val storagePath: String = "",
    val fileSizeBytes: Long = 0L,
    val uploadedAt: Long = 0L
)
