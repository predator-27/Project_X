package com.projectx.app.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.projectx.app.theme.CampusTokens
import kotlin.math.max
import kotlin.math.min

/**
 * Block N1 renderer. Consumes the vector map (not the grid) so seats look
 * like real rectangles, not fat pixels.
 *
 * Layering (bottom to top):
 *   1. Ambient tinted background
 *   2. Corridors — thick coloured strokes
 *   3. Seats — filled rectangles with badge label
 *   4. Doors — chevron markers
 *   5. Vertical labels ("MAIN CORRIDOR" etc.)
 *   6. Selected seat highlight ring (glowing primary)
 *
 * Pinch to zoom (0.5x–5x), drag to pan. Tap a seat to select.
 */
@Composable
fun MapCanvas(
    map: CampusMap,
    selectedSeatId: String?,
    onSeatTap: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vec = map.vector
    val colors = CampusTokens.colors
    val measurer = rememberTextMeasurer()

    // Transform state (unit space → screen space)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Fit-to-screen on first layout
    LaunchedEffect(canvasSize, vec.widthUnits, vec.heightUnits) {
        if (canvasSize.width > 0f && scale == 1f && offset == Offset.Zero) {
            val fitScale = min(
                canvasSize.width  / vec.widthUnits,
                canvasSize.height / vec.heightUnits,
            ) * 0.95f
            scale = fitScale
            offset = Offset(
                (canvasSize.width  - vec.widthUnits  * fitScale) / 2f,
                (canvasSize.height - vec.heightUnits * fitScale) / 2f,
            )
        }
    }

    // Snap camera onto the currently-selected seat.
    LaunchedEffect(selectedSeatId, canvasSize) {
        val seat = vec.seats.firstOrNull { it.id == selectedSeatId } ?: return@LaunchedEffect
        if (canvasSize.width <= 0f) return@LaunchedEffect
        offset = Offset(
            canvasSize.width  / 2f - seat.cx * scale,
            canvasSize.height / 2f - seat.cy * scale,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(vec.id) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(0.5f, 6f)
                        // Zoom keeps the pointer position fixed in unit space
                        val focus = (centroid - offset) / scale
                        scale = newScale
                        offset = centroid - focus * newScale + pan
                    }
                }
                .pointerInput(vec.id, selectedSeatId) {
                    detectTapGestures { tapped ->
                        // Convert screen tap → unit coords
                        val ux = (tapped.x - offset.x) / scale
                        val uy = (tapped.y - offset.y) / scale
                        // Hit test seat rects
                        val hit = vec.seats.firstOrNull { s ->
                            ux in s.x..(s.x + s.w) && uy in s.y..(s.y + s.h)
                        }
                        onSeatTap(hit?.id)
                    }
                },
        ) {
            canvasSize = size

            drawRect(color = colors.pageBackground, size = size)

            // Group draws inside the unit-space transform
            fun mapX(u: Float) = u * scale + offset.x
            fun mapY(u: Float) = u * scale + offset.y

            drawCorridors(vec, colors, ::mapX, ::mapY, scale)
            drawAisles(vec, colors, ::mapX, ::mapY, scale)
            drawSeats(vec, map.facultyBySeat, colors, ::mapX, ::mapY, scale, measurer)
            drawDoors(vec, colors, ::mapX, ::mapY, scale)
            drawVectorLabels(vec, colors, ::mapX, ::mapY, scale, measurer)

            selectedSeatId?.let { id ->
                vec.seats.firstOrNull { it.id == id }?.let { s ->
                    drawSelectionRing(s, colors, ::mapX, ::mapY, scale)
                }
            }
        }
    }
}

// ─── Drawing primitives ────────────────────────────────────

private fun DrawScope.drawCorridors(
    v: MapVector, c: com.projectx.app.theme.CampusColors,
    mx: (Float) -> Float, my: (Float) -> Float, s: Float,
) {
    val corridorFill = if (c.isFrosted) c.surfaceElevated else c.infoBanner
    val corridorStroke = c.glassBorderGlow.takeIf { c.isFrosted } ?: c.surfaceBorder
    v.corridors.forEach { corr ->
        // vertical corridor drawn as a thick rectangle
        val halfW = 6f
        val left = mx(corr.x - halfW)
        val top  = my(corr.fromY)
        val w = 2 * halfW * s
        val h = (corr.toY - corr.fromY) * s
        drawRect(color = corridorFill, topLeft = Offset(left, top), size = Size(w, h))
        drawRect(color = corridorStroke, topLeft = Offset(left, top), size = Size(w, h), style = Stroke(1f))
    }
}

private fun DrawScope.drawAisles(
    v: MapVector, c: com.projectx.app.theme.CampusColors,
    mx: (Float) -> Float, my: (Float) -> Float, s: Float,
) {
    val stroke = c.divider
    v.aisles.forEach { a ->
        val y = my(a.y)
        drawLine(
            color = stroke,
            start = Offset(mx(0f), y),
            end   = Offset(mx(v.widthUnits), y),
            strokeWidth = 1f,
        )
    }
}

private fun DrawScope.drawSeats(
    v: MapVector, byId: Map<String, Faculty>,
    c: com.projectx.app.theme.CampusColors,
    mx: (Float) -> Float, my: (Float) -> Float, s: Float,
    measurer: TextMeasurer,
) {
    v.seats.forEach { seat ->
        val hasFaculty = byId.containsKey(seat.id)
        val isCabin = seat.kind == "cabin"
        val fill = when {
            isCabin      -> c.warningAmberBg
            hasFaculty   -> c.primary.copy(alpha = 0.28f)
            else         -> c.surface
        }
        val border = when {
            isCabin      -> c.warningAmber
            hasFaculty   -> c.primary
            else         -> c.surfaceBorder
        }
        val left = mx(seat.x)
        val top  = my(seat.y)
        val w = seat.w * s
        val h = seat.h * s
        drawRect(color = fill, topLeft = Offset(left, top), size = Size(w, h))
        drawRect(color = border, topLeft = Offset(left, top), size = Size(w, h),
                 style = Stroke(width = if (isCabin) 1.5f else 1f))

        // Only render the badge when the seat is big enough on-screen to be readable
        if (w > 24f && h > 14f) {
            val style = TextStyle(
                color = if (hasFaculty || isCabin) c.heading else c.mutedText,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
            )
            val result = measurer.measure(seat.badge, style)
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    left + (w - result.size.width) / 2f,
                    top  + (h - result.size.height) / 2f,
                ),
            )
        }
    }
}

private fun DrawScope.drawDoors(
    v: MapVector, c: com.projectx.app.theme.CampusColors,
    mx: (Float) -> Float, my: (Float) -> Float, s: Float,
) {
    val color = c.successGreen
    v.doors.forEach { d ->
        // Simple chevron: two short lines forming a >
        val cx = mx(d.x); val cy = my(d.y)
        val armLen = 8f * s.coerceAtLeast(0.4f)
        drawLine(color, Offset(cx - armLen, cy - armLen), Offset(cx, cy), strokeWidth = 3f)
        drawLine(color, Offset(cx, cy), Offset(cx - armLen, cy + armLen), strokeWidth = 3f)
    }
}

private fun DrawScope.drawVectorLabels(
    v: MapVector, c: com.projectx.app.theme.CampusColors,
    mx: (Float) -> Float, my: (Float) -> Float, s: Float,
    measurer: TextMeasurer,
) {
    v.labels.forEach { lbl ->
        val style = TextStyle(color = c.mutedText, fontSize = 9.sp)
        val result = measurer.measure(lbl.text, style)
        val cx = mx(lbl.x); val cy = my(lbl.y)
        if (lbl.vertical) {
            rotate(-90f, pivot = Offset(cx, cy)) {
                drawText(result, topLeft = Offset(cx - result.size.width / 2f, cy - result.size.height / 2f))
            }
        } else {
            drawText(result, topLeft = Offset(cx - result.size.width / 2f, cy - result.size.height / 2f))
        }
    }
}

private fun DrawScope.drawSelectionRing(
    seat: Seat, c: com.projectx.app.theme.CampusColors,
    mx: (Float) -> Float, my: (Float) -> Float, s: Float,
) {
    val left = mx(seat.x) - 3f
    val top  = my(seat.y) - 3f
    val w = seat.w * s + 6f
    val h = seat.h * s + 6f
    drawRect(color = c.primary.copy(alpha = 0.15f), topLeft = Offset(left, top), size = Size(w, h))
    drawRect(color = c.primary, topLeft = Offset(left, top), size = Size(w, h), style = Stroke(2.5f))
}
