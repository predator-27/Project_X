package com.projectx.app.model.hostel

import androidx.compose.runtime.Immutable

@Immutable
data class DiningMeal(
    val mealId: String = "",
    val mealType: String = "",
    val timeRange: String = "",
    val menuItems: List<String> = emptyList(),
    val calories: String? = null,
    val location: String = "Central Mess - Ground Floor",
    val qrToken: String = "DEMO-QR-BENNETT-88219",
    val isConsumed: Boolean = false
)
