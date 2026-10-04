package com.projectx.app.navmap.position

import com.projectx.app.navmap.model.Cell
import com.projectx.app.navmap.model.MapGrid
import com.projectx.app.navmap.model.PositionEstimate
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure-Kotlin pedestrian dead reckoning engine. State starts at a scanned anchor cell
 * and advances by one step length per step, in the device's current heading. The
 * [ParticleFilter] layered on top resolves drift against walkable cells.
 */
class PdrEngine(
    private val grid: MapGrid,
    private val stepLength: StepLength,
    private val headingSource: () -> Double,
    private val northOffsetDeg: Double = grid.north.northOffsetDeg ?: 0.0,
) {
    private var row: Double = 0.0
    private var col: Double = 0.0
    private var scanHeadingDeg: Double = 0.0
    private var deviceHeadingAtScanDeg: Double = 0.0

    fun resetToAnchor(anchorCell: Cell, scanHeadingDeg: Int, deviceHeadingAtScan: Double) {
        row = anchorCell.y.toDouble()
        col = anchorCell.x.toDouble()
        this.scanHeadingDeg = scanHeadingDeg.toDouble()
        this.deviceHeadingAtScanDeg = deviceHeadingAtScan
    }

    fun snapshot(): PositionEstimate = PositionEstimate(
        cellRow = row,
        cellCol = col,
        headingDeg = currentMapHeadingDeg(),
        accuracyMeters = 2.0,
    )

    /**
     * Advance one step. Uses the compass convention: heading 0° = north (screen-up = -y),
     * 90° = east (+x = +col), 180° = south (+y = +row), 270° = west.
     */
    fun onStep(): PositionEstimate {
        val headingRad = Math.toRadians(currentMapHeadingDeg())
        val stepCells = stepLength.current() / grid.routing.cellMeters
        col += stepCells * sin(headingRad)
        row -= stepCells * cos(headingRad)
        return snapshot()
    }

    /** map-heading = device-heading - northOffset, calibrated by the delta observed at scan time. */
    fun currentMapHeadingDeg(): Double {
        val device = headingSource()
        val delta = device - deviceHeadingAtScanDeg
        return normalize(scanHeadingDeg + delta - northOffsetDeg)
    }

    private fun normalize(deg: Double): Double {
        var d = deg
        while (d < 0) d += 360.0
        while (d >= 360.0) d -= 360.0
        return d
    }
}
