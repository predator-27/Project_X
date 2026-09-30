package com.projectx.app.map

/**
 * Kotlin mirrors of the three map JSON files shipped in `assets/maps/`.
 * Parsed with `org.json` (built into Android) — no serialization plugin needed.
 *
 * Field names match the JSON exactly so a future switch to kotlinx.serialization
 * is mechanical.
 */

// ─── Vector map — seat rectangles, doors, corridors, aisles ─────────
data class MapVector(
    val id: String,
    val name: String,
    val floor: Int,
    val widthUnits: Float,
    val heightUnits: Float,
    val metersPerUnit: Float,
    val widthMeters: Float,
    val heightMeters: Float,
    val doors: List<Door>,
    val labels: List<VectorLabel>,
    val corridors: List<Corridor>,
    val aisles: List<Aisle>,
    val seats: List<Seat>,
    val seatCounts: SeatCounts,
)

data class SeatCounts(val total: Int, val cub: Int, val cab: Int, val ws: Int)

data class Seat(
    val id: String,           // "CUB 001", "WS 12", "MC 002"
    val type: String,         // "seat"
    val kind: String,         // "cubicle" | "workstation" | "cabin"
    val num: Int,
    val label: String,        // human-readable — full id
    val badge: String,        // short badge — "C1", "W12", "M2"
    val zone: String,         // "left" | "right" | "center" | ...
    val x: Float, val y: Float,
    val w: Float, val h: Float,
    val cx: Float, val cy: Float,
    val fromCorridor: String? = null,
)

data class Door(val kind: String, val label: String, val x: Float, val y: Float)
data class VectorLabel(val text: String, val x: Float, val y: Float, val vertical: Boolean)
data class Corridor(val id: String, val label: String, val orientation: String, val x: Float, val fromY: Float, val toY: Float)
data class Aisle(val n: Int, val y: Float, val label: String)

// ─── Grid — 103 × 91 tile map for A* / wall-snapping ─────────────────
data class MapGrid(
    val mapId: String,
    val rows: Int,
    val cols: Int,
    val cellSizeMeters: Float,
    val cellSizeUnits: Float,
    val cells: List<String>,             // one string per row, one char per cell
    val anchors: List<Anchor>,
    val pois: List<Poi>,
    val walkableCells: Int,
) {
    fun cellAt(row: Int, col: Int): Char =
        if (row in 0 until rows && col in 0 until cols) cells[row][col] else '#'
}

data class Anchor(
    val anchorId: String,
    val label: String,
    val cellRow: Int, val cellCol: Int,
    val headingDeg: Int,
)

/** POI on the grid — same seat as [Seat] but expressed in cell coords, plus
 *  the walkable cell an A* route should target. */
data class Poi(
    val id: String,
    val type: String,
    val label: String,
    val badge: String,
    val zone: String,
    val cellRow: Int, val cellCol: Int,
    val approachRow: Int, val approachCol: Int,
    val tags: List<String>,
)

// ─── Faculty directory — 12 seat ownerships ─────────────────────────
data class Faculty(
    val seatId: String,
    val name: String,
    val designation: String,
    val department: String,
    val subjects: List<String>,
    val status: String,               // "In office" | "In class" | "Away" | …
    val hours: String,
    val email: String,
)

// ─── Aggregate loaded map ───────────────────────────────────────────
data class CampusMap(
    val vector: MapVector,
    val grid: MapGrid,
    val faculty: List<Faculty>,
) {
    val facultyBySeat: Map<String, Faculty> = faculty.associateBy { it.seatId }
}
