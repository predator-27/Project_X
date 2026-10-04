package com.projectx.app.feature.campusmap

/** The scanned user position in cell-space plus their current heading in map-degrees. */
data class NavUserState(
    val cellRow: Double,
    val cellCol: Double,
    val headingDeg: Double,
)
