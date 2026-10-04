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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.navmap.model.CampusMap
import com.projectx.app.navmap.model.Corridor
import com.projectx.app.navmap.model.VectorSeat

/**
 * Compose canvas that draws Block N1 in the neon Pac-Man reference style:
 * dark background, bright blue outer frame, rounded seats with their numbers inside,
 * glow passes, Pac-Man anchor markers labelled ENTRANCE/EXIT, aisle labels along the
 * main corridor, status pips on occupied seats, dashed corridor guides. Routing and
 * user overlays are fed through [overlay].
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
    val density = LocalDensity.current

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
                drawFrame(campus, transform)
                drawCorridorGuides(campus, transform)
                drawWalls(campus, transform)
                drawSeats(campus, transform, textMeasurer, density)
                drawAisleLabels(campus, transform, textMeasurer, density)
                drawSeatStatusPipsAlwaysOn(campus, transform)
                drawDoors(campus, transform)
                drawVectorLabels(campus, transform, textMeasurer, density)
                drawAnchors(campus, transform, textMeasurer, density)
                overlay?.invoke(this, transform)
            }
        }
    }
}

/** Thick blue stroke frame around the vector area — the maze's outer boundary. */
private fun DrawScope.drawFrame(campus: CampusMap, t: MapTransform) {
    val margin = t.cellPixels * 0.5f
    val tl = t.unitToScreen(0.0, 0.0)
    val br = t.unitToScreen(campus.vector.units.width, campus.vector.units.height)
    val topLeft = Offset(tl.x - margin, tl.y - margin)
    val size = Size(br.x - tl.x + margin * 2f, br.y - tl.y + margin * 2f)
    drawRoundRect(
        color = MapTheme.Wall.copy(alpha = 0.15f),
        topLeft = Offset(topLeft.x - 4f, topLeft.y - 4f),
        size = Size(size.width + 8f, size.height + 8f),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 10f),
    )
    drawRoundRect(
        color = MapTheme.Wall,
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 3.5f),
    )
}

/** Walls are '#' cells in the grid. Draw as glowing rectangles. */
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

private fun DrawScope.drawSeats(
    campus: CampusMap,
    t: MapTransform,
    measurer: TextMeasurer,
    density: Density,
) {
    campus.vector.seats.forEach { seat ->
        val tl = t.unitToScreen(seat.x, seat.y)
        val size = t.unitSize(seat.w, seat.h)
        val color = seatColor(seat.kind)
        val cornerPx = (minOf(size.width, size.height) * 0.22f).coerceIn(2f, 14f)
        val radius = CornerRadius(cornerPx, cornerPx)

        // 3-pass glow halo
        drawRoundRect(
            color = color.copy(alpha = 0.08f),
            topLeft = Offset(tl.x - 6f, tl.y - 6f),
            size = Size(size.width + 12f, size.height + 12f),
            cornerRadius = CornerRadius(cornerPx + 4f, cornerPx + 4f),
        )
        drawRoundRect(
            color = color.copy(alpha = 0.14f),
            topLeft = Offset(tl.x - 3f, tl.y - 3f),
            size = Size(size.width + 6f, size.height + 6f),
            cornerRadius = CornerRadius(cornerPx + 2f, cornerPx + 2f),
        )
        // Soft fill
        drawRoundRect(color = color.copy(alpha = 0.08f), topLeft = tl, size = size, cornerRadius = radius)
        // Crisp stroke
        drawRoundRect(
            color = color,
            topLeft = tl,
            size = size,
            cornerRadius = radius,
            style = Stroke(width = 1.6f),
        )

        drawSeatNumber(seat, tl, size, color, measurer, density)
    }
}

private fun DrawScope.drawSeatNumber(
    seat: VectorSeat,
    tl: Offset,
    size: Size,
    color: Color,
    measurer: TextMeasurer,
    density: Density,
) {
    val display = seatDisplayNumber(seat)
    val fontSizePx = (size.height * 0.42f).coerceIn(6f, 24f)
    val fontSizeSp = with(density) { fontSizePx.toSp() }
    val style = TextStyle(
        color = color.copy(alpha = 0.95f),
        fontSize = fontSizeSp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )
    val result = measurer.measure(display, style)
    val textX = tl.x + (size.width - result.size.width) / 2f
    val textY = tl.y + (size.height - result.size.height) / 2f
    drawText(result, topLeft = Offset(textX, textY))
}

private fun seatColor(kind: String): Color = when (kind.uppercase()) {
    "CUB" -> MapTheme.Cubicle
    "WS" -> MapTheme.Workstation
    "MC" -> MapTheme.Cabin
    else -> MapTheme.Cubicle
}

private fun seatDisplayNumber(seat: VectorSeat): String = when (seat.kind.uppercase()) {
    "CUB", "MC" -> seat.num.toString().padStart(3, '0')
    "WS" -> seat.num.toString()
    else -> seat.badge
}

/** Status pip top-right of occupied seats: green/orange/grey. Always-on, idle too. */
private fun DrawScope.drawSeatStatusPipsAlwaysOn(campus: CampusMap, t: MapTransform) {
    campus.vector.seats.forEach { seat ->
        val faculty = campus.facultyBySeat[seat.id] ?: return@forEach
        val color = when (faculty.status?.lowercase()) {
            "available" -> MapTheme.StatusAvailable
            "busy" -> MapTheme.StatusBusy
            "away" -> MapTheme.StatusAway
            else -> return@forEach
        }
        val tl = t.unitToScreen(seat.x + seat.w, seat.y)
        val radius = t.cellPixels * 0.32f
        drawCircle(color = color.copy(alpha = 0.25f), radius = radius * 1.6f, center = tl)
        drawCircle(color = color.copy(alpha = 0.70f), radius = radius, center = tl)
    }
}

/** Entrance/exit anchors: yellow Pac-Man disc + ENTRANCE / EXIT text label. */
private fun DrawScope.drawAnchors(
    campus: CampusMap,
    t: MapTransform,
    measurer: TextMeasurer,
    density: Density,
) {
    campus.grid.anchors.forEach { a ->
        val p = t.cellCenterScreen(a.cell.y, a.cell.x)
        val radius = t.cellPixels * 1.5f

        drawCircle(color = MapTheme.PowerBall.copy(alpha = 0.14f), radius = radius * 2.0f, center = p)
        drawCircle(color = MapTheme.PowerBall.copy(alpha = 0.28f), radius = radius * 1.5f, center = p)

        val mouthDeg = 46.0
        val facing = a.headingDeg - 90.0
        val startAngle = (facing + mouthDeg / 2).toFloat()
        val sweep = (360.0 - mouthDeg).toFloat()
        drawArc(
            color = MapTheme.PowerBall,
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = true,
            topLeft = Offset(p.x - radius, p.y - radius),
            size = Size(radius * 2f, radius * 2f),
        )

        val labelText = if (a.label.equals("entrance", true)) "ENTRANCE"
        else if (a.label.equals("exit", true)) "EXIT"
        else a.label.uppercase()
        val labelColor = if (labelText == "EXIT") MapTheme.ExitMark else MapTheme.PowerBall
        val labelPx = (t.cellPixels * 1.3f).coerceIn(10f, 28f)
        val labelStyle = TextStyle(
            color = labelColor,
            fontSize = with(density) { labelPx.toSp() },
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        val labelResult = measurer.measure(labelText, labelStyle)
        val labelY = if (a.headingDeg == 180) p.y - radius - labelResult.size.height - 4f
        else p.y + radius + 6f
        drawText(
            textLayoutResult = labelResult,
            topLeft = Offset(p.x - labelResult.size.width / 2f, labelY),
        )
    }
}

private fun DrawScope.drawDoors(campus: CampusMap, t: MapTransform) {
    campus.vector.doors.forEach { door ->
        val p = t.unitToScreen(door.x, door.y)
        val r = t.cellPixels * 0.6f
        drawCircle(
            color = MapTheme.ExitMark.copy(alpha = 0.18f),
            center = p,
            radius = r * 1.6f,
        )
        drawCircle(color = MapTheme.ExitMark, center = p, radius = r, style = Stroke(width = 1.6f))
    }
}

private fun DrawScope.drawVectorLabels(
    campus: CampusMap,
    t: MapTransform,
    measurer: TextMeasurer,
    density: Density,
) {
    campus.vector.labels.forEach { label ->
        val p = t.unitToScreen(label.x, label.y)
        val sizePx = (t.cellPixels * 0.9f).coerceIn(7f, 16f)
        val result = measurer.measure(
            text = label.text,
            style = TextStyle(
                color = MapTheme.LabelText,
                fontSize = with(density) { sizePx.toSp() },
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            ),
        )
        drawText(result, topLeft = Offset(p.x - result.size.width / 2f, p.y - result.size.height / 2f))
    }
}

/** Aisles 1-8 run horizontally in the vector. Draw their labels at the main corridor. */
private fun DrawScope.drawAisleLabels(
    campus: CampusMap,
    t: MapTransform,
    measurer: TextMeasurer,
    density: Density,
) {
    val mainCorridor = campus.vector.corridors.firstOrNull { it.id.equals("main", true) }
    val xUnits = mainCorridor?.x ?: (campus.vector.units.width * 0.68)
    val sizePx = (t.cellPixels * 0.9f).coerceIn(7f, 14f)
    val style = TextStyle(
        color = MapTheme.Workstation.copy(alpha = 0.80f),
        fontSize = with(density) { sizePx.toSp() },
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )
    campus.vector.aisles.forEach { aisle ->
        val p = t.unitToScreen(xUnits, aisle.y.toDouble())
        val text = "A${aisle.n}"
        val result = measurer.measure(text, style)
        drawText(result, topLeft = Offset(p.x + 8f, p.y - result.size.height / 2f))
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
