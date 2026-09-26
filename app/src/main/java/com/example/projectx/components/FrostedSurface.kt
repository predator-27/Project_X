package com.example.projectx.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.projectx.theme.CampusTokens

/**
 * The definitive glass card for Frosted Midnight.
 *
 * Stack (bottom to top):
 *   1. Elevation shadow (depth against the dark bg)
 *   2. Base fill  (surface / surfaceElevated)
 *   3. Haze overlay (~4% white — the "frost")
 *   4. Top-edge sheen (10% white → transparent over first 35 dp — light catch)
 *   5. Border stroke (20% primary — soft neon rim)
 *
 * Automatically degrades to a plain outlined card for non-frosted presets.
 */
@Composable
fun FrostedSurface(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    cornerRadius: Dp = 14.dp,
    elevation: Dp = 10.dp,
    content: @Composable () -> Unit,
) {
    val c = CampusTokens.colors
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val fill = if (elevated) c.surfaceElevated else c.surface

    if (!c.isFrosted) {
        // Non-frosted presets — fall back to a normal outlined card.
        Box(
            modifier = modifier
                .shadow(elevation = if (elevated) 2.dp else 0.dp, shape = shape, clip = false)
                .clip(shape)
                .background(fill, shape)
                .border(BorderStroke(1.dp, c.surfaceBorder), shape),
        ) { content() }
        return
    }

    val sheen = Brush.verticalGradient(
        0f    to c.glassSheenTop,
        0.25f to Color.Transparent,
    )

    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(fill, shape)
            .background(c.glassHaze, shape)
            .background(sheen, shape)
            .border(BorderStroke(1.dp, c.glassBorderGlow), shape),
    ) { content() }
}

