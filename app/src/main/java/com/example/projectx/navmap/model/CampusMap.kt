package com.projectx.app.navmap.model

/** Aggregate of every file the navigation code needs for a single building/floor. */
data class CampusMap(
    val grid: MapGrid,
    val vector: VectorMap,
    val searchIndex: SearchIndex,
    val faculty: FacultyDirectory,
) {
    val facultyBySeat: Map<String, FacultyEntry> by lazy { faculty.faculty.associateBy { it.seat } }
}
