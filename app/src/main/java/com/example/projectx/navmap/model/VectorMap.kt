package com.projectx.app.navmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VectorUnits(
    val width: Double,
    val height: Double,
    val metersPerUnit: Double,
    val note: String? = null,
)

@Serializable
data class Door(
    val kind: String,
    val label: String,
    val x: Double,
    val y: Double,
)

@Serializable
data class VectorLabel(
    val text: String,
    val x: Double,
    val y: Double,
    val vertical: Boolean = false,
)

@Serializable
data class Corridor(
    val id: String,
    val label: String,
    val orientation: String,
    val x: Double? = null,
    val y: Double? = null,
    val fromY: Double? = null,
    val toY: Double? = null,
    val fromX: Double? = null,
    val toX: Double? = null,
)

@Serializable
data class Aisle(
    val n: Int,
    val y: Double,
    val label: String,
)

@Serializable
data class SeatCounts(
    val total: Int,
    val cub: Int,
    val cab: Int,
    val ws: Int,
)

@Serializable
data class VectorSeat(
    val id: String,
    val type: String,
    val kind: String,
    val num: Int,
    val label: String,
    val badge: String,
    val zone: String,
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
    val cx: Double,
    val cy: Double,
    val fromCorridor: Int? = null,
)

/** Vector file — exact geometry for drawing. */
@Serializable
data class VectorMap(
    val schemaVersion: Int,
    val id: String,
    val name: String,
    val floor: Int,
    val units: VectorUnits,
    val widthMeters: Double,
    val heightMeters: Double,
    val origin: String,
    val doors: List<Door> = emptyList(),
    val labels: List<VectorLabel> = emptyList(),
    val corridors: List<Corridor> = emptyList(),
    val aisles: List<Aisle> = emptyList(),
    val seatCounts: SeatCounts,
    val seats: List<VectorSeat> = emptyList(),
    val calibration: Calibration,
    val north: NorthConfig,
    @SerialName("schema") val schemaTag: String? = null,
)
