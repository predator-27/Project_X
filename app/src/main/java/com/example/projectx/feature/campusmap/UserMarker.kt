package com.projectx.app.feature.campusmap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the user marker: a frosted glass disc with a cyan rim, a yellow Pac-Man whose
 * mouth points along `headingDeg` (compass: 0 = north/screen-up, 90 = east/screen-right),
 * and a soft translucent facing cone extending forward.
 */
fun DrawScope.drawUserMarker(center: Offset, radius: Float, headingDeg: Double) {
    val forwardRad = Math.toRadians(headingDeg)
    val forwardScreenX = sin(forwardRad)
    val forwardScreenY = -cos(forwardRad)

    val cone = Path().apply {
        val coneHalfDeg = 18.0
        val coneLen = radius * 3.2f
        moveTo(center.x, center.y)
        val left = Math.toRadians(headingDeg - coneHalfDeg)
        val right = Math.toRadians(headingDeg + coneHalfDeg)
        lineTo(
            center.x + (coneLen * sin(left)).toFloat(),
            center.y + (coneLen * -cos(left)).toFloat(),
        )
        lineTo(
            center.x + (coneLen * sin(right)).toFloat(),
            center.y + (coneLen * -cos(right)).toFloat(),
        )
        close()
    }
    drawPath(cone, color = Color(0x55FFD400))

    drawCircle(color = Color(0x33FFFFFF), radius = radius * 1.25f, center = center)
    drawCircle(color = Color.White.copy(alpha = 0.08f), radius = radius * 1.1f, center = center)
    drawCircle(color = MapTheme.UserRim, radius = radius * 1.25f, center = center, style = Stroke(width = 2.4f))

    // Keep the forward vector around — some future features (e.g. motion blur) may use it.
    @Suppress("UNUSED_VARIABLE") val forward = forwardScreenX to forwardScreenY

    drawPacMan(center, radius, headingDeg)
}

private fun DrawScope.drawPacMan(center: Offset, radius: Float, headingDeg: Double) {
    val mouthDeg = 44.0
    // Canvas drawArc convention: 0° = +x (east), rotating clockwise.
    val canvasHeadingDeg = headingDeg - 90.0
    val startAngle = (canvasHeadingDeg + mouthDeg / 2).toFloat()
    val sweepAngle = (360.0 - mouthDeg).toFloat()
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(radius * 2, radius * 2)
    drawArc(
        color = Color(0xFFFFD400),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = topLeft,
        size = size,
    )
}
