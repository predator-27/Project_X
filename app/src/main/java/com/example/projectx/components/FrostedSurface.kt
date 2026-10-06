package com.projectx.app.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projectx.app.theme.CampusTokens

/**
 * Frosted-glass surface used by cards, drawers, top bars, bottom bars and dialogs.
 *
 * On Android 12+ (API 31) a real [RenderEffect] blur backs the fill. Below that the
 * fill is a semi-translucent tint with a soft sheen — same visual language, no runtime
 * blur cost on older devices. Non-frosted presets degrade to a plain outlined surface.
 */
@Composable
fun FrostedSurface(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 10.dp,
    intensity: Float = 0.70f,
    content: @Composable () -> Unit,
) {
    val c = CampusTokens.colors
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val baseFill = if (elevated) c.surfaceElevated else c.surface

    if (!c.isFrosted) {
        Box(
            modifier = modifier
                .shadow(elevation = if (elevated) 3.dp else 1.dp, shape = shape, clip = false)
                .clip(shape)
                .background(baseFill, shape)
                .border(BorderStroke(1.dp, c.surfaceBorder), shape),
        ) { content() }
        return
    }

    val translucentFill = baseFill.copy(alpha = intensity.coerceIn(0.4f, 0.95f))
    val sheen = Brush.verticalGradient(
        0f to c.glassSheenTop,
        0.3f to Color.Transparent,
    )

    val blurMod: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            renderEffect = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .then(blurMod)
            .background(translucentFill, shape)
            .background(c.glassHaze, shape)
            .background(sheen, shape)
            .border(BorderStroke(1.dp, c.glassBorderGlow), shape),
    ) { content() }
}

/**
 * Opaque scrim variant — use behind body text that would otherwise fail WCAG AA
 * against a busy translucent fill.
 */
@Composable
fun FrostedTextSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    val c = CampusTokens.colors
    val shape = RoundedCornerShape(cornerRadius)
    val fill = if (c.isDark) c.surface.copy(alpha = 0.92f) else c.surface.copy(alpha = 0.95f)
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill, shape)
            .border(BorderStroke(1.dp, c.surfaceBorder), shape),
    ) { content() }
}
