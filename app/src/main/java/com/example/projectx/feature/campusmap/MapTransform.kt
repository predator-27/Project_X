package com.projectx.app.feature.campusmap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.projectx.app.navmap.model.CampusMap

/**
 * Converts between map-unit space (`VectorMap.units.width` x `.height`) and screen pixels,
 * and between grid cell indices (row, col) and map-unit space.
 *
 * The map itself is never rotated — only the compass rose is. The transform keeps
 * aspect ratio and centres the map inside the viewport.
 */
data class MapTransform(
    val unitsWidth: Double,
    val unitsHeight: Double,
    val gridRows: Int,
    val gridCols: Int,
    val cellSizeUnits: Int,
    val canvas: Size,
) {
    val scale: Float = run {
        val sx = canvas.width / unitsWidth.toFloat()
        val sy = canvas.height / unitsHeight.toFloat()
        minOf(sx, sy)
    }
    val offsetX: Float = (canvas.width - unitsWidth.toFloat() * scale) / 2f
    val offsetY: Float = (canvas.height - unitsHeight.toFloat() * scale) / 2f

    fun unitToScreen(x: Double, y: Double): Offset =
        Offset(offsetX + x.toFloat() * scale, offsetY + y.toFloat() * scale)

    fun unitSize(w: Double, h: Double): Size =
        Size(w.toFloat() * scale, h.toFloat() * scale)

    /** Centre of a grid cell in screen pixels. */
    fun cellCenterScreen(row: Int, col: Int): Offset {
        val x = (col + 0.5) * cellSizeUnits
        val y = (row + 0.5) * cellSizeUnits
        return unitToScreen(x, y)
    }

    /** Width/height of one grid cell in pixels — equal in x and y. */
    val cellPixels: Float = cellSizeUnits * scale

    companion object {
        fun from(campus: CampusMap, canvas: Size): MapTransform = MapTransform(
            unitsWidth = campus.vector.units.width,
            unitsHeight = campus.vector.units.height,
            gridRows = campus.grid.rows,
            gridCols = campus.grid.cols,
            cellSizeUnits = campus.grid.cellSizeUnits,
            canvas = canvas,
        )
    }
}
