package com.projectx.app.feature.campusmap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.projectx.app.navmap.model.CampusMap
import com.projectx.app.navmap.model.Poi
import com.projectx.app.navmap.routing.Route
import kotlin.math.hypot

/** Reserved by the route to let the renderer pick the right colour: Person vs Exit. */
enum class RouteKind { ToPerson, ToExit }

/**
 * Draws the route layer: pac-dots along the smoothed path, status pips on occupied
 * seats, and the power ball at the destination. Dots inside `eatRadiusPx` of
 * [userScreen] are suppressed with a fade pop so remaining dots == metres remaining.
 */
fun DrawScope.drawRouteOverlay(
    campus: CampusMap,
    transform: MapTransform,
    route: Route,
    kind: RouteKind,
    destinationPoiId: String?,
    userScreen: Offset?,
    eatRadiusPx: Float = 18f,
) {
    val routeColor = if (kind == RouteKind.ToExit) MapTheme.RouteExit else MapTheme.RoutePerson
    drawRoutePath(route, transform, routeColor)

    route.dots.forEach { dot ->
        val p = transform.cellCenterScreen(dot.r.toInt(), dot.c.toInt())
        val eaten = userScreen != null && hypot((p.x - userScreen.x).toDouble(), (p.y - userScreen.y).toDouble()) < eatRadiusPx
        val radius = transform.cellPixels * 0.26f
        if (eaten) {
            drawCircle(color = MapTheme.Dot.copy(alpha = 0.0f), radius = radius * 0.4f, center = p)
        } else {
            drawCircle(color = MapTheme.Dot.copy(alpha = 0.35f), radius = radius * 1.4f, center = p)
            drawCircle(color = MapTheme.Dot, radius = radius, center = p)
        }
    }

    destinationPoiId?.let { id ->
        val poi = campus.grid.poiById[id] ?: return@let
        val person = campus.facultyBySeat[poi.id]
        val available = person?.status?.equals("available", ignoreCase = true) == true
        drawPowerBall(transform.cellCenterScreen(poi.cell.y, poi.cell.x), transform.cellPixels, solid = available)
    }
}

private fun DrawScope.drawRoutePath(route: Route, t: MapTransform, color: Color) {
    val pts = route.smoothedPath
    if (pts.size < 2) return
    for (i in 0 until pts.size - 1) {
        val a = t.cellCenterScreen(pts[i].r, pts[i].c)
        val b = t.cellCenterScreen(pts[i + 1].r, pts[i + 1].c)
        drawLine(color.copy(alpha = 0.25f), a, b, strokeWidth = 10f)
        drawLine(color, a, b, strokeWidth = 2.4f)
    }
}

/** Yellow power ball: solid when the person is available, hollow ring otherwise. */
private fun DrawScope.drawPowerBall(center: Offset, cellPx: Float, solid: Boolean) {
    val radius = cellPx * 1.1f
    drawCircle(color = MapTheme.PowerBall.copy(alpha = 0.25f), radius = radius * 1.5f, center = center)
    if (solid) {
        drawCircle(color = MapTheme.PowerBall, radius = radius, center = center)
    } else {
        drawCircle(color = MapTheme.PowerBall, radius = radius, center = center, style = Stroke(width = 3f))
    }
}

/** Rect shortcut for drawing the destination cell border — exposed for future use. */
@Suppress("unused")
internal fun DrawScope.drawDestCellOutline(poi: Poi, t: MapTransform) {
    val c = t.cellCenterScreen(poi.cell.y, poi.cell.x)
    val half = t.cellPixels / 2f
    drawRect(
        color = MapTheme.PowerBall,
        topLeft = Offset(c.x - half, c.y - half),
        size = Size(t.cellPixels, t.cellPixels),
        style = Stroke(width = 2f),
    )
}
