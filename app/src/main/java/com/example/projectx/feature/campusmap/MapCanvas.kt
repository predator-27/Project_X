package com.projectx.app.feature.campusmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.projectx.app.navmap.model.CampusMap
import com.projectx.app.navmap.model.Corridor

/**
 * Composable canvas that draws Block N1 in the neon Pac-Man style:
 * background, walls (with 3-pass glow), seat rectangles coloured by kind, labels,
 * dashed corridor guides. Supports pinch-zoom and pan. Routing/user overlays
 * come from higher-level screens.
 */
@Composable
fun MapCanvas(
    campus: CampusMap,
    modifier: Modifier = Modifier,
    overlay: (DrawScope.(MapTransform) -> Unit)? = null,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(MapTheme.Background)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 6f)
                    offset += pan
                }
            }
    ) {
        val transform = MapTransform.from(campus, size)
        translate(offset.x, offset.y) {
            scale(scale, scale, pivot = Offset(size.width / 2f, size.height / 2f)) {
                drawCorridorGuides(campus, transform)
                drawSeats(campus, transform, textMeasurer)
                drawWalls(campus, transform)
                drawDoors(campus, transform)
                drawVectorLabels(campus, transform, textMeasurer)
                drawAnchors(campus, transform)
                overlay?.invoke(this, transform)
            }
        }
    }
}

/** Walls are the '#' characters in the grid, drawn as glowing rectangles. */
private fun DrawScope.drawWalls(campus: CampusMap, t: MapTransform) {
    val cellPx = t.cellPixels
    for (r in 0 until campus.grid.rows) {
        val row = campus.grid.cells[r]
        var c = 0
        while (c < row.length) {
            if (row[c] == '#') {
                var end = c
                while (end < row.length && row[end] == '#') end++
                val tl = t.cellCenterScreen(r, c)
                val x = tl.x - cellPx / 2f
                val y = tl.y - cellPx / 2f
                val w = cellPx * (end - c)
                val h = cellPx
                drawGlowingRect(MapTheme.Wall, Offset(x, y), Size(w, h))
                c = end
            } else {
                c++
            }
        }
    }
}

private fun DrawScope.drawSeats(campus: CampusMap, t: MapTransform, measurer: TextMeasurer) {
    campus.vector.seats.forEach { seat ->
        val tl = t.unitToScreen(seat.x, seat.y)
        val size = t.unitSize(seat.w, seat.h)
        val color = when (seat.kind) {
            "CUB" -> MapTheme.Cubicle
            "WS" -> MapTheme.Workstation
            "MC" -> MapTheme.Cabin
            else -> MapTheme.Cubicle
        }
        drawRect(color = color.copy(alpha = 0.14f), topLeft = tl, size = size)
        drawRect(color = color, topLeft = tl, size = size, style = Stroke(width = 1.4f))
    }
}

private fun DrawScope.drawDoors(campus: CampusMap, t: MapTransform) {
    campus.vector.doors.forEach { door ->
        val p = t.unitToScreen(door.x, door.y)
        val r = t.cellPixels * 0.9f
        drawCircle(
            color = MapTheme.ExitMark.copy(alpha = 0.25f),
            center = p,
            radius = r * 1.6f,
        )
        drawCircle(color = MapTheme.ExitMark, center = p, radius = r, style = Stroke(width = 2.2f))
    }
}

private fun DrawScope.drawVectorLabels(
    campus: CampusMap,
    t: MapTransform,
    measurer: TextMeasurer,
) {
    campus.vector.labels.forEach { label ->
        val p = t.unitToScreen(label.x, label.y)
        val result = measurer.measure(
            text = label.text,
            style = TextStyle(
                color = MapTheme.LabelText,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        drawText(result, topLeft = Offset(p.x - result.size.width / 2f, p.y - result.size.height / 2f))
    }
}

private fun DrawScope.drawCorridorGuides(campus: CampusMap, t: MapTransform) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 10f))
    campus.vector.corridors.forEach { cor: Corridor ->
        if (cor.orientation.equals("vertical", true)) {
            val x = cor.x ?: return@forEach
            val y1 = cor.fromY ?: return@forEach
            val y2 = cor.toY ?: return@forEach
            val a = t.unitToScreen(x, y1)
            val b = t.unitToScreen(x, y2)
            drawLine(MapTheme.CorridorGuide, a, b, strokeWidth = 1.4f, pathEffect = dash)
        } else {
            val y = cor.y ?: return@forEach
            val x1 = cor.fromX ?: return@forEach
            val x2 = cor.toX ?: return@forEach
            val a = t.unitToScreen(x1, y)
            val b = t.unitToScreen(x2, y)
            drawLine(MapTheme.CorridorGuide, a, b, strokeWidth = 1.4f, pathEffect = dash)
        }
    }
}

private fun DrawScope.drawAnchors(campus: CampusMap, t: MapTransform) {
    campus.grid.anchors.forEach { a ->
        val p = t.cellCenterScreen(a.cell.y, a.cell.x)
        val r = t.cellPixels * 1.4f
        drawRect(
            color = MapTheme.PowerBall.copy(alpha = 0.12f),
            topLeft = Offset(p.x - r, p.y - r),
            size = Size(r * 2f, r * 2f),
        )
        drawRect(
            color = MapTheme.PowerBall,
            topLeft = Offset(p.x - r, p.y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = 2f),
        )
    }
}

/** Three-pass glow: wide @10%, mid @18%, thin @30%, then the crisp shape. */
internal fun DrawScope.drawGlowingRect(color: Color, topLeft: Offset, size: Size) {
    drawRect(color.copy(alpha = 0.10f), inflate(topLeft, size, 2.0f), inflatedSize(size, 2.0f))
    drawRect(color.copy(alpha = 0.18f), inflate(topLeft, size, 1.0f), inflatedSize(size, 1.0f))
    drawRect(color.copy(alpha = 0.30f), inflate(topLeft, size, 0.3f), inflatedSize(size, 0.3f))
    drawRect(color, topLeft, size)
}

private fun inflate(tl: Offset, size: Size, by: Float): Offset =
    Offset(tl.x - size.width * by / 2f, tl.y - size.height * by / 2f)

private fun inflatedSize(size: Size, by: Float): Size =
    Size(size.width * (1f + by), size.height * (1f + by))
