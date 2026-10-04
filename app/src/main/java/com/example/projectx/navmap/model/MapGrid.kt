package com.projectx.app.navmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cell(val x: Int, val y: Int)

@Serializable
data class RoutingConfig(
    val algorithm: String? = null,
    val walkableChars: List<String>,
    val diagonal: Boolean,
    val cornerCutting: Boolean,
    val cellMeters: Double,
    val dotSpacingMeters: Double,
) {
    val walkableSet: Set<Char> = walkableChars.mapNotNull { it.firstOrNull() }.toSet()
    fun isWalkable(ch: Char): Boolean = ch in walkableSet
}

@Serializable
data class ReferenceWall(
    val unitLength: Double? = null,
    val measuredMeters: Double? = null,
)

@Serializable
data class Calibration(
    val metersPerUnit: Double,
    val source: String? = null,
    val calibratedOn: String? = null,
    val referenceWall: ReferenceWall? = null,
)

@Serializable
data class NorthConfig(
    val northOffsetDeg: Double? = null,
)

@Serializable
data class Anchor(
    val anchorId: String,
    val label: String,
    val floor: Int,
    val cell: Cell,
    val headingDeg: Int,
    val payload: String,
    val signed: Boolean = false,
    val note: String? = null,
)

@Serializable
data class Poi(
    val id: String,
    val type: String,
    val label: String,
    val floor: Int,
    val badge: String? = null,
    val zone: String? = null,
    val cell: Cell,
    val approachCell: Cell,
    val tags: List<String> = emptyList(),
    val distanceFromEntranceM: Double? = null,
)

@Serializable
data class VerifiedBlock(
    val allSeatsReachableFromEntrance: Boolean? = null,
    val walkableCells: Int? = null,
)

/** Grid file — the navigation source of truth. */
@Serializable
data class MapGrid(
    val schemaVersion: Int,
    val mapId: String,
    val building: String,
    val floor: Int,
    val rows: Int,
    val cols: Int,
    val cellSizeUnits: Int,
    val metersPerUnit: Double,
    val cellSizeMeters: Double,
    val legend: Map<String, String> = emptyMap(),
    val cells: List<String>,
    val routing: RoutingConfig,
    val calibration: Calibration,
    val north: NorthConfig,
    val anchors: List<Anchor> = emptyList(),
    val pois: List<Poi> = emptyList(),
    val verified: VerifiedBlock? = null,
    @SerialName("schema") val schemaTag: String? = null,
) {
    private val cellRows: List<CharArray> = cells.map { it.toCharArray() }

    fun inBounds(row: Int, col: Int): Boolean =
        row in 0 until rows && col in 0 until cols

    fun cellAt(row: Int, col: Int): Char =
        if (inBounds(row, col)) cellRows[row][col] else '#'

    fun isWalkable(row: Int, col: Int): Boolean =
        inBounds(row, col) && routing.isWalkable(cellRows[row][col])

    val poiById: Map<String, Poi> by lazy { pois.associateBy { it.id } }
    val anchorById: Map<String, Anchor> by lazy { anchors.associateBy { it.anchorId } }
}
