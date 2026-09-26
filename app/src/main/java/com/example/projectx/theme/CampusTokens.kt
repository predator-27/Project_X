package com.example.projectx.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic tokens the app reads instead of raw Color.kt constants.
 * Every [CampusThemePreset] supplies its own set through [CampusTheme].
 * That way switching preset (e.g. Frosted Midnight) retints the whole app
 * without touching a single screen.
 */
data class CampusColors(
    val pageBackground: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceBorder: Color,
    val divider: Color,
    val sidebar: Color,
    val sidebarActive: Color,
    val sidebarText: Color,
    val heading: Color,
    val bodyText: Color,
    val mutedText: Color,
    val fieldLabel: Color,
    val fieldRequired: Color,
    val fieldFill: Color,
    val infoBanner: Color,
    val primary: Color,
    val successGreen: Color,
    val successGreenBg: Color,
    val warningAmber: Color,
    val warningAmberBg: Color,
    val dangerRed: Color,
    val dangerRedBg: Color,
    /** Frosted-glass rendering knobs — [FrostedSurface] and ambient layers read these. */
    val glassHaze: Color,
    val glassSheenTop: Color,
    val glassBorderGlow: Color,
    val ambientGlowPrimary: Color,
    val ambientGlowSecondary: Color,
    /** True when the theme is designed for frosted-glass surfaces (dark base + translucent lift). */
    val isFrosted: Boolean,
)

val LocalCampusColors = staticCompositionLocalOf<CampusColors> {
    error("CampusColors not provided — wrap content in CampusTheme.")
}

object CampusTokens {
    val colors: CampusColors
        @Composable @ReadOnlyComposable
        get() = LocalCampusColors.current
}

// ─── Preset → CampusColors ────────────────────────────────────
internal fun campusColorsFor(preset: CampusThemePreset): CampusColors = when (preset) {
    CampusThemePreset.FROSTED_MIDNIGHT -> CampusColors(
        pageBackground   = FrostBackground,
        surface          = FrostSurface,
        surfaceElevated  = FrostSurfaceElevated,
        surfaceBorder    = FrostOutline,
        divider          = FrostDivider,
        sidebar          = FrostSidebar,
        sidebarActive    = FrostPrimaryContainer,
        sidebarText      = FrostHeading,
        heading          = FrostHeading,
        bodyText         = FrostBody,
        mutedText        = FrostMuted,
        fieldLabel       = FrostFieldLabel,
        fieldRequired    = FrostFieldRequired,
        fieldFill        = FrostFieldFill,
        infoBanner       = FrostInfoBanner,
        primary          = FrostPrimary,
        successGreen     = FrostSuccess,
        successGreenBg   = FrostSuccessBg,
        warningAmber     = FrostWarning,
        warningAmberBg   = FrostWarningBg,
        dangerRed        = FrostDanger,
        dangerRedBg      = FrostDangerBg,
        glassHaze        = FrostHaze,
        glassSheenTop    = FrostSheenTop,
        glassBorderGlow  = FrostBorderGlow,
        ambientGlowPrimary   = FrostAmbientPrimary,
        ambientGlowSecondary = FrostAmbientSecondary,
        isFrosted = true,
    )
    // Every other preset uses the classic light palette so nothing breaks.
    else -> CampusColors(
        pageBackground   = preset.pageBackground,
        surface          = preset.surfaceColor,
        surfaceElevated  = preset.surfaceColor,
        surfaceBorder    = SurfaceBorder,
        divider          = SurfaceBorder,
        sidebar          = preset.sidebarColor,
        sidebarActive    = NavySidebarActive,
        sidebarText      = NavySidebarText,
        heading          = HeadingNavy,
        bodyText         = BodyText,
        mutedText        = MutedText,
        fieldLabel       = preset.fieldLabelColor,
        fieldRequired    = FieldRequired,
        fieldFill        = FieldFill,
        infoBanner       = InfoBannerBg,
        primary          = preset.primaryColor,
        successGreen     = SecondaryEmerald,
        successGreenBg   = SecondaryEmeraldBg,
        warningAmber     = WarningAmber,
        warningAmberBg   = WarningAmberBg,
        dangerRed        = AccentCoral,
        dangerRedBg      = AccentCoralBg,
        glassHaze        = Color.Transparent,
        glassSheenTop    = Color.Transparent,
        glassBorderGlow  = Color.Transparent,
        ambientGlowPrimary   = Color.Transparent,
        ambientGlowSecondary = Color.Transparent,
        isFrosted = false,
    )
}
