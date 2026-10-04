package com.projectx.app.navmap.routing

import com.projectx.app.navmap.model.CampusMap
import com.projectx.app.navmap.model.Cell
import com.projectx.app.navmap.model.MapGrid
import com.projectx.app.navmap.model.Poi
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

enum class TurnDirection { STRAIGHT, SLIGHT_LEFT, LEFT, SHARP_LEFT, UTURN, SHARP_RIGHT, RIGHT, SLIGHT_RIGHT }

data class RouteStep(
    val instruction: String,
    val distanceMeters: Double,
)

data class RouteDotCell(val r: Double, val c: Double)

data class Route(
    val rawPath: List<GridNode>,
    val smoothedPath: List<GridNode>,
    val dots: List<RouteDotCell>,
    val distanceMeters: Double,
    val steps: List<RouteStep>,
) {
    val found: Boolean get() = rawPath.isNotEmpty()
    companion object { val Empty = Route(emptyList(), emptyList(), emptyList(), 0.0, emptyList()) }
}

object RouteBuilder {

    /** Build a route from a user's current cell to a POI's `approachCell`. */
    fun toPoi(campus: CampusMap, from: Cell, poiId: String): Route {
        val poi = campus.grid.poiById[poiId] ?: return Route.Empty
        return buildBetween(campus, from, poi.approachCell, labelForPoi(poi, campus))
    }

    fun toAnchor(campus: CampusMap, from: Cell, anchorId: String): Route {
        val a = campus.grid.anchorById[anchorId] ?: return Route.Empty
        return buildBetween(campus, from, a.cell, destLabel = a.label)
    }

    private fun buildBetween(
        campus: CampusMap,
        from: Cell,
        to: Cell,
        destLabel: String,
    ): Route {
        val grid = campus.grid
        val start = GridNode(from.y, from.x)
        val goal = GridNode(to.y, to.x)
        val raw = AStar.find(grid, start, goal)
        if (!raw.found) return Route.Empty

        val smoothed = PathSmoother.smooth(grid, raw.path)
        val cellMeters = grid.routing.cellMeters
        val distanceMeters = raw.cost * cellMeters
        val dots = dotsAlong(smoothed, cellMeters, grid.routing.dotSpacingMeters)
        val steps = turnsFromSmoothed(smoothed, campus, cellMeters, destLabel)
        return Route(raw.path, smoothed, dots, distanceMeters, steps)
    }

    private fun labelForPoi(poi: Poi, campus: CampusMap): String {
        val person = campus.facultyBySeat[poi.id]?.name
        return person?.let { "$it (${poi.label})" } ?: poi.label
    }

    /** Place one dot every `dotSpacingMeters` along the smoothed path, in cell-space. */
    private fun dotsAlong(
        smoothed: List<GridNode>,
        cellMeters: Double,
        dotSpacingMeters: Double,
    ): List<RouteDotCell> {
        if (smoothed.size < 2) return emptyList()
        val dotSpacingCells = dotSpacingMeters / cellMeters
        val out = mutableListOf<RouteDotCell>()
        var carry = 0.0
        for (i in 0 until smoothed.size - 1) {
            val a = smoothed[i]
            val b = smoothed[i + 1]
            val dr = (b.r - a.r).toDouble()
            val dc = (b.c - a.c).toDouble()
            val segLenCells = hypot(dr, dc)
            if (segLenCells == 0.0) continue
            val ur = dr / segLenCells
            val uc = dc / segLenCells
            var remaining = segLenCells
            var cursor = carry
            while (cursor <= remaining) {
                out.add(RouteDotCell(a.r + ur * cursor, a.c + uc * cursor))
                cursor += dotSpacingCells
            }
            carry = cursor - remaining
        }
        return out
    }

    /** Group smoothed segments into "Walk straight N m" / "Turn left into Aisle 3" instructions. */
    private fun turnsFromSmoothed(
        smoothed: List<GridNode>,
        campus: CampusMap,
        cellMeters: Double,
        destLabel: String,
    ): List<RouteStep> {
        if (smoothed.size < 2) return emptyList()
        val steps = mutableListOf<RouteStep>()
        var prevBearing: Double? = null
        for (i in 0 until smoothed.size - 1) {
            val a = smoothed[i]
            val b = smoothed[i + 1]
            val segLenMeters = hypot((b.r - a.r).toDouble(), (b.c - a.c).toDouble()) * cellMeters
            val bearing = segmentBearing(a, b)
            val turn = if (prevBearing == null) TurnDirection.STRAIGHT else classifyTurn(prevBearing, bearing)
            val landmark = landmarkAlong(a, b, campus)
            val instruction = buildString {
                when (turn) {
                    TurnDirection.STRAIGHT -> append("Walk straight ")
                    TurnDirection.LEFT, TurnDirection.SLIGHT_LEFT -> append("Turn left and walk ")
                    TurnDirection.SHARP_LEFT -> append("Sharp left for ")
                    TurnDirection.RIGHT, TurnDirection.SLIGHT_RIGHT -> append("Turn right and walk ")
                    TurnDirection.SHARP_RIGHT -> append("Sharp right for ")
                    TurnDirection.UTURN -> append("Turn around and walk ")
                }
                append(segLenMeters.roundToInt()).append(" m")
                if (landmark != null) append(" along ").append(landmark)
            }
            steps.add(RouteStep(instruction, segLenMeters))
            prevBearing = bearing
        }
        steps.add(RouteStep("Arrive at $destLabel", 0.0))
        return steps
    }

    /** Bearing in radians, 0 = +col (east), measured counter-clockwise. */
    private fun segmentBearing(a: GridNode, b: GridNode): Double =
        atan2((b.r - a.r).toDouble(), (b.c - a.c).toDouble())

    private fun classifyTurn(prev: Double, next: Double): TurnDirection {
        var d = next - prev
        while (d > Math.PI) d -= 2 * Math.PI
        while (d < -Math.PI) d += 2 * Math.PI
        val deg = Math.toDegrees(d)
        return when {
            deg >= 160.0 || deg <= -160.0 -> TurnDirection.UTURN
            deg > 110.0 -> TurnDirection.SHARP_LEFT
            deg > 25.0 -> TurnDirection.LEFT
            deg > 10.0 -> TurnDirection.SLIGHT_LEFT
            deg < -110.0 -> TurnDirection.SHARP_RIGHT
            deg < -25.0 -> TurnDirection.RIGHT
            deg < -10.0 -> TurnDirection.SLIGHT_RIGHT
            else -> TurnDirection.STRAIGHT
        }
    }

    /** If this segment runs along an aisle or corridor from n1_map.json, return its label. */
    private fun landmarkAlong(a: GridNode, b: GridNode, campus: CampusMap): String? {
        val cellSize = campus.grid.cellSizeUnits
        val midY = (a.r + b.r) / 2.0 * cellSize
        val midX = (a.c + b.c) / 2.0 * cellSize
        val horizontal = a.r == b.r
        val vertical = a.c == b.c
        if (horizontal) {
            campus.vector.aisles.firstOrNull { kotlin.math.abs(it.y - midY) < cellSize * 1.5 }?.let { return it.label }
        }
        if (vertical) {
            campus.vector.corridors.firstOrNull { cor ->
                cor.orientation.equals("vertical", true) && cor.x != null &&
                    kotlin.math.abs(cor.x - midX) < cellSize * 1.5
            }?.let { return it.label }
        }
        return null
    }
}
