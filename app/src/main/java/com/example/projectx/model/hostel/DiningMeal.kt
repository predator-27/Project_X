package com.projectx.app.model.hostel

import androidx.compose.runtime.Immutable

@Immutable
data class DiningMeal(
    val mealId: String = "",
    val date: String = "2026-10-04",
    val mealType: String = "",
    val timeRange: String = "",
    val menuItems: List<String> = emptyList(),
    val calories: String? = null,
    val location: String = "Central Mess - Ground Floor",
    val qrToken: String = "BENNETT-QR-DEMO-DEFAULT",
    val isConsumed: Boolean = false
)
