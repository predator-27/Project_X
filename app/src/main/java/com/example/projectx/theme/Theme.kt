package com.projectx.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

fun calculateOnColor(color: Color): Color {
    val luminance = (0.299f * color.red + 0.587f * color.green + 0.114f * color.blue)
    return if (luminance > 0.6f) Color(0xFF0F172A) else Color.White
}

/**
 * Applies the currently-selected [CampusThemePreset]. The M3 [ColorScheme]
 * is derived from the preset's [CampusColors] so switching preset instantly
 * retints every screen (buttons, text fields, chips, dialogs …).
 */
@Composable
fun CampusTheme(
    content: @Composable () -> Unit,
) {
    val preset by CampusThemeState.currentTheme.collectAsState()
    val mode by CampusThemeState.mode.collectAsState()
    val textSize by CampusThemeState.textSize.collectAsState()
    val systemDark = isSystemInDarkTheme()

    val wantDark = when {
        preset == CampusThemePreset.FROSTED_MIDNIGHT ||
            preset == CampusThemePreset.CYBER_MIDNIGHT -> true
        mode == CampusThemeMode.LIGHT -> false
        mode == CampusThemeMode.DARK -> true
        else -> systemDark
    }

    val tokens = campusColorsFor(preset).copy(isDark = wantDark)
    val colorScheme = tokens.toColorScheme()

    val baseDensity = LocalDensity.current
    val scaledDensity = Density(
        density = baseDensity.density,
        fontScale = baseDensity.fontScale * textSize.scale,
    )

    CompositionLocalProvider(
        LocalCampusColors provides tokens,
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

/** Build a Material-3 [ColorScheme] directly from the preset's tokens. */
internal fun CampusColors.toColorScheme() = if (isDark) {
    darkColorScheme(
        primary              = primary,
        onPrimary            = onPrimary,
        primaryContainer     = primaryContainer,
        onPrimaryContainer   = heading,
        secondary            = secondary,
        onSecondary          = onSecondary,
        secondaryContainer   = secondaryContainer,
        onSecondaryContainer = heading,
        tertiary             = secondary,
        onTertiary           = onSecondary,
        background           = pageBackground,
        onBackground         = heading,
        surface              = surface,
        onSurface            = bodyText,
        surfaceVariant       = surfaceElevated,
        onSurfaceVariant     = mutedText,
        surfaceTint          = primary,
        inverseSurface       = heading,
        inverseOnSurface     = pageBackground,
        outline              = surfaceBorder,
        outlineVariant       = divider,
        error                = dangerRed,
        onError              = Color.White,
        errorContainer       = dangerRedBg,
        onErrorContainer     = dangerRed,
        scrim                = Color(0xCC000000),
    )
} else {
    lightColorScheme(
        primary              = primary,
        onPrimary            = onPrimary,
        primaryContainer     = primaryContainer,
        onPrimaryContainer   = heading,
        secondary            = secondary,
        onSecondary          = onSecondary,
        secondaryContainer   = secondaryContainer,
        onSecondaryContainer = heading,
        tertiary             = secondary,
        onTertiary           = onSecondary,
        background           = pageBackground,
        onBackground         = heading,
        surface              = surface,
        onSurface            = bodyText,
        surfaceVariant       = surfaceElevated,
        onSurfaceVariant     = mutedText,
        surfaceTint          = primary,
        inverseSurface       = heading,
        inverseOnSurface     = pageBackground,
        outline              = surfaceBorder,
        outlineVariant       = divider,
        error                = dangerRed,
        onError              = Color.White,
        errorContainer       = dangerRedBg,
        onErrorContainer     = dangerRed,
        scrim                = Color(0x66000000),
    )
}
