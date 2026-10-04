package com.projectx.app.navmap.model

/** User position in cell coordinates with a map-frame heading and an accuracy spread. */
data class PositionEstimate(
    val cellRow: Double,
    val cellCol: Double,
    val headingDeg: Double,
    val accuracyMeters: Double,
)
