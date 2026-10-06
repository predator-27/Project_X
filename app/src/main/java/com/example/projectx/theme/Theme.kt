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

fun colorToHue(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
        hsv
    )
    return hsv[0]
}

/**
 * Derives a complete [CampusColors] palette where:
 *   - [primaryColor] controls background, surface, and overall UI atmosphere.
 *   - [secondaryColor] controls icons, buttons, active badges, and visual accents.
 */
fun deriveTonalPalette(
    primaryColor: Color,
    secondaryColor: Color,
    isDark: Boolean
): CampusColors {
    val pHue = colorToHue(primaryColor)

    val pageBg = if (isDark) {
        Color.hsv(pHue, 0.35f, 0.08f)
    } else {
        Color.hsv(pHue, 0.06f, 0.98f)
    }

    val surf = if (isDark) {
        Color.hsv(pHue, 0.30f, 0.14f)
    } else {
        Color(0xFFFFFFFF)
    }

    val surfElev = if (isDark) {
        Color.hsv(pHue, 0.28f, 0.18f)
    } else {
        Color(0xFFFFFFFF)
    }

    val border = if (isDark) {
        Color.hsv(pHue, 0.25f, 0.25f)
    } else {
        Color.hsv(pHue, 0.15f, 0.88f)
    }

    val side = if (isDark) {
        Color.hsv(pHue, 0.40f, 0.05f)
    } else {
        Color.hsv(pHue, 0.45f, 0.20f)
    }

    val head = if (isDark) {
        Color.hsv(pHue, 0.08f, 0.96f)
    } else {
        Color.hsv(pHue, 0.35f, 0.12f)
    }

    val body = if (isDark) {
        Color.hsv(pHue, 0.08f, 0.86f)
    } else {
        Color.hsv(pHue, 0.30f, 0.22f)
    }

    val muted = if (isDark) {
        Color.hsv(pHue, 0.10f, 0.72f)
    } else {
        Color.hsv(pHue, 0.25f, 0.42f)
    }

    val s = secondaryColor
    val onS = calculateOnColor(s)

    return CampusColors(
        pageBackground   = pageBg,
        surface          = surf,
        surfaceElevated  = surfElev,
        surfaceBorder    = border,
        divider          = border,
        sidebar          = side,
        sidebarActive    = s.copy(alpha = 0.22f),
        sidebarText      = if (isDark) head else Color.White,
        heading          = head,
        bodyText         = body,
        mutedText        = muted,
        onPrimary        = onS,
        onSecondary      = onS,
        disabledText     = if (isDark) Color(0xFF5A6450) else Neutral400,
        fieldLabel       = s,
        fieldRequired    = Danger500,
        fieldFill        = if (isDark) Color.hsv(pHue, 0.25f, 0.18f) else Neutral100,
        infoBanner       = s.copy(alpha = 0.15f),
        primary          = s,
        primaryContainer = s.copy(alpha = 0.18f),
        secondary        = s,
        secondaryContainer = s.copy(alpha = 0.18f),
        successGreen     = Success500,
        successGreenBg   = if (isDark) Color(0xFF064E3B) else Success100,
        warningAmber     = Warning500,
        warningAmberBg   = if (isDark) Color(0xFF78350F) else Warning100,
        dangerRed        = Danger500,
        dangerRedBg      = if (isDark) Color(0xFF7F1D1D) else Danger100,
        infoBlue         = Info500,
        infoBlueBg       = if (isDark) Info700 else Info100,
        glassHaze        = if (isDark) Color(0x14FFFFFF) else Color.Transparent,
        glassSheenTop    = if (isDark) Color(0x1AFFFFFF) else Color.Transparent,
        glassBorderGlow  = if (isDark) border else Color.Transparent,
        ambientGlowPrimary   = primaryColor.copy(alpha = 0.14f),
        ambientGlowSecondary = s.copy(alpha = 0.10f),
        isFrosted = isDark,
        isDark = isDark,
    )
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
    val customPrimary by CampusThemeState.customPrimary.collectAsState()
    val customSecondary by CampusThemeState.customSecondary.collectAsState()
    val systemDark = isSystemInDarkTheme()

    val wantDark = when {
        preset == CampusThemePreset.FROSTED_MIDNIGHT ||
            preset == CampusThemePreset.CYBER_MIDNIGHT -> true
        mode == CampusThemeMode.LIGHT -> false
        mode == CampusThemeMode.DARK -> true
        else -> systemDark
    }

    var tokens = campusColorsFor(preset).copy(isDark = wantDark)
    if (customPrimary != null || customSecondary != null) {
        val p = customPrimary ?: tokens.pageBackground
        val s = customSecondary ?: tokens.primary
        tokens = deriveTonalPalette(
            primaryColor = p,
            secondaryColor = s,
            isDark = wantDark
        )
    }

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
