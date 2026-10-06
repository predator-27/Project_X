package com.projectx.app.model

import androidx.compose.runtime.Immutable

enum class HolidayKind { HOLIDAY, EXAM, EVENT, DEADLINE }

@Immutable
data class HolidayEvent(
    val id: String,
    val title: String,
    val date: String,           // ISO yyyy-MM-dd — stored as a plain String so the model stays Kotlin-only.
    val kind: HolidayKind,
    val description: String = "",
)
