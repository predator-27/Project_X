package com.example.projectx.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/**
 * Applies the currently-selected [CampusThemePreset].
 *
 * FROSTED_MIDNIGHT gets its own hand-tuned dark scheme so the whole app
 * flows visually from the splash straight into the UI. Every other preset
 * keeps the classic light / cyber-dark behavior from the prior iteration.
 */
@Composable
fun CampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val preset by CampusThemeState.currentTheme.collectAsState()
    val tokens = campusColorsFor(preset)

    val colorScheme = when {
        preset == CampusThemePreset.FROSTED_MIDNIGHT -> frostedScheme(tokens)
        preset == CampusThemePreset.CYBER_MIDNIGHT || darkTheme -> darkColorScheme(
            primary = preset.primaryColor,
            onPrimary = Color.White,
            primaryContainer = preset.primaryColor.copy(alpha = 0.30f),
            onPrimaryContainer = Color.White,
            surface = preset.surfaceColor,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = preset.surfaceColor,
            onSurfaceVariant = Color(0xFF94A3B8),
            background = preset.pageBackground,
            onBackground = Color(0xFFF8FAFC),
            outline = Color(0xFF334155),
            error = Color(0xFFF43F5E),
            onError = Color.White,
        )
        else -> lightColorScheme(
            primary = preset.primaryColor,
            onPrimary = Color.White,
            primaryContainer = preset.primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = Color(0xFF0F172A),
            surface = preset.surfaceColor,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = preset.primaryColor.copy(alpha = 0.12f),
            onSurfaceVariant = Color(0xFF64748B),
            background = preset.pageBackground,
            onBackground = Color(0xFF0F172A),
            outline = Color(0xFFE2E8F0),
            error = Color(0xFFE11D48),
            onError = Color.White,
        )
    }

    CompositionLocalProvider(LocalCampusColors provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

// Hand-crafted Material3 scheme for Frosted Midnight — every slot maps to
// a token that reads correctly against the deep navy base.
private fun frostedScheme(c: CampusColors) = darkColorScheme(
    primary            = FrostPrimary,
    onPrimary          = FrostOnPrimary,
    primaryContainer   = FrostPrimaryContainer,
    onPrimaryContainer = FrostOnPrimaryContainer,
    secondary          = FrostSecondary,
    onSecondary        = FrostOnPrimary,
    secondaryContainer = FrostSecondaryContainer,
    onSecondaryContainer = FrostOnPrimaryContainer,
    tertiary           = FrostSecondary,
    background         = c.pageBackground,
    onBackground       = c.heading,
    surface            = c.surface,
    onSurface          = c.bodyText,
    surfaceVariant     = c.surfaceElevated,
    onSurfaceVariant   = c.mutedText,
    surfaceTint        = FrostPrimary,
    inverseSurface     = c.heading,
    inverseOnSurface   = c.pageBackground,
    outline            = c.surfaceBorder,
    outlineVariant     = c.divider,
    error              = c.dangerRed,
    onError            = FrostOnPrimary,
    errorContainer     = c.dangerRedBg,
    onErrorContainer   = c.dangerRed,
    scrim              = Color(0xCC000000),
)
