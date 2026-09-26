package com.example.projectx.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.projectx.theme.CampusTokens

/**
 * The ambient layer painted BEHIND everything.
 *
 *   - base vertical gradient (splash-navy → slightly-lighter navy → splash-navy)
 *   - top-right icy blue radial glow  (14% alpha)
 *   - bottom-left lavender radial glow (9% alpha)
 *
 * Static — no animation — so it costs nothing per frame after the first paint.
 * Falls back to a plain [CampusColors.pageBackground] fill on non-frosted presets.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = CampusTokens.colors
    Box(modifier = modifier.fillMaxSize()) {
        if (!c.isFrosted) {
            Box(Modifier.fillMaxSize().background(c.pageBackground))
        } else {
            val base = remember(c.pageBackground, c.surface) {
                Brush.verticalGradient(
                    0f    to c.pageBackground,
                    0.5f  to c.surface,
                    1f    to c.pageBackground,
                )
            }
            Box(Modifier.fillMaxSize().background(base))

            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Top-right glow — sits over the top bar / hero area
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(c.ambientGlowPrimary, Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.15f),
                        radius = w * 0.60f,
                    ),
                )
                // Bottom-left glow — softly lights the bottom nav area
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(c.ambientGlowSecondary, Color.Transparent),
                        center = Offset(w * 0.15f, h * 0.85f),
                        radius = w * 0.55f,
                    ),
                )
            }
        }
        content()
    }
}
