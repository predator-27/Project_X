package com.projectx.app.navmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FacultyEntry(
    val seat: String,
    val name: String,
    val designation: String? = null,
    val department: String? = null,
    val subjects: List<String> = emptyList(),
    val status: String? = null,
    val hours: String? = null,
    val email: String? = null,
)

@Serializable
data class FacultyDirectory(
    val mapId: String,
    val faculty: List<FacultyEntry> = emptyList(),
    @SerialName("schema") val schemaTag: String? = null,
)
