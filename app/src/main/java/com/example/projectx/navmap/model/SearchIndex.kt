package com.projectx.app.navmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchPerson(
    val name: String,
    val designation: String? = null,
    val department: String? = null,
    val subjects: List<String> = emptyList(),
    val status: String? = null,
)

@Serializable
data class SearchEntry(
    val id: String,
    val type: String,
    val label: String,
    val cell: Cell,
    val approachCell: Cell,
    val distanceFromEntranceM: Double? = null,
    val person: SearchPerson? = null,
    val terms: List<String> = emptyList(),
)

@Serializable
data class SearchIndex(
    val mapId: String,
    val entries: List<SearchEntry> = emptyList(),
    @SerialName("schema") val schemaTag: String? = null,
)
