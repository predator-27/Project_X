package com.example.projectx.model.lms

import androidx.compose.runtime.Immutable

@Immutable
data class CourseMaterial(
    val materialId: String = "",
    val courseCode: String = "",
    val title: String = "",
    val description: String? = null,
    val fileUrl: String = "",
    val fileType: String = "PDF",
    val fileSizeBytes: Long = 0L,
    val uploadedByUid: String = "",
    val uploadedByName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
