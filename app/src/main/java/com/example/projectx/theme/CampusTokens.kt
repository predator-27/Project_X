package com.projectx.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic tokens every screen reads instead of raw [Color] constants.
 * One [CampusColors] instance is supplied per preset via [CampusTheme].
 */
data class CampusColors(
    // Backdrops
    val pageBackground: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceBorder: Color,
    val divider: Color,
    // Sidebar / drawer
    val sidebar: Color,
    val sidebarActive: Color,
    val sidebarText: Color,
    // Text (WCAG AA guaranteed against pageBackground + surface)
    val heading: Color,
    val bodyText: Color,
    val mutedText: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val disabledText: Color,
    // Form fields
    val fieldLabel: Color,
    val fieldRequired: Color,
    val fieldFill: Color,
    val infoBanner: Color,
    // Brand
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    // Semantic
    val successGreen: Color,
    val successGreenBg: Color,
    val warningAmber: Color,
    val warningAmberBg: Color,
    val dangerRed: Color,
    val dangerRedBg: Color,
    val infoBlue: Color,
    val infoBlueBg: Color,
    // Frosted glass rendering knobs
    val glassHaze: Color,
    val glassSheenTop: Color,
    val glassBorderGlow: Color,
    val ambientGlowPrimary: Color,
    val ambientGlowSecondary: Color,
    /** True when the preset expects frosted-glass surfaces (dark base + translucent lift). */
    val isFrosted: Boolean,
    /** True for dark presets. Drives Material 3 light/dark selection. */
    val isDark: Boolean,
)

val LocalCampusColors = staticCompositionLocalOf<CampusColors> {
    error("CampusColors not provided — wrap content in CampusTheme.")
}

object CampusTokens {
    val colors: CampusColors
        @Composable @ReadOnlyComposable
        get() = LocalCampusColors.current
}

// ─────────────────────────────────────────────────────────────
// Light preset builder — derives everything from the tonal ramps so
// a palette swap only needs new primary/secondary 500 values.
// ─────────────────────────────────────────────────────────────
internal fun lightPreset(
    primary: Color = Indigo500,
    primaryContainer: Color = Indigo100,
    secondary: Color = Teal500,
    secondaryContainer: Color = Teal100,
    pageBackground: Color = Neutral50,
    sidebar: Color = Indigo900,
    sidebarActive: Color = Indigo50,
    fieldLabel: Color = Indigo600,
): CampusColors = CampusColors(
    pageBackground   = pageBackground,
    surface          = Color(0xFFFFFFFF),
    surfaceElevated  = Color(0xFFFFFFFF),
    surfaceBorder    = Neutral200,
    divider          = Neutral200,
    sidebar          = sidebar,
    sidebarActive    = sidebarActive,
    sidebarText      = Neutral50,
    heading          = Neutral900,
    bodyText         = Neutral800,
    mutedText        = Neutral600,  // AA-safe on white (contrast ~6.4)
    onPrimary        = Color.White,
    onSecondary      = Color.White,
    disabledText     = Neutral400,
    fieldLabel       = fieldLabel,
    fieldRequired    = Danger500,
    fieldFill        = Neutral100,
    infoBanner       = Indigo100,
    primary          = primary,
    primaryContainer = primaryContainer,
    secondary        = secondary,
    secondaryContainer = secondaryContainer,
    successGreen     = Success500,
    successGreenBg   = Success100,
    warningAmber     = Warning500,
    warningAmberBg   = Warning100,
    dangerRed        = Danger500,
    dangerRedBg      = Danger100,
    infoBlue         = Info500,
    infoBlueBg       = Info100,
    glassHaze        = Color.Transparent,
    glassSheenTop    = Color.Transparent,
    glassBorderGlow  = Color.Transparent,
    ambientGlowPrimary   = Color.Transparent,
    ambientGlowSecondary = Color.Transparent,
    isFrosted = false,
    isDark = false,
)

// ─────────────────────────────────────────────────────────────
// Dark preset builder — ramp-derived dark surfaces with WCAG-AA text.
// ─────────────────────────────────────────────────────────────
internal fun darkPreset(
    primary: Color = Indigo400,
    primaryContainer: Color = Indigo700,
    secondary: Color = Teal400,
    secondaryContainer: Color = Teal700,
    pageBackground: Color = Neutral950,
    surface: Color = Neutral900,
    sidebar: Color = Color(0xFF020617),
): CampusColors = CampusColors(
    pageBackground   = pageBackground,
    surface          = surface,
    surfaceElevated  = Neutral800,
    surfaceBorder    = Neutral700,
    divider          = Neutral800,
    sidebar          = sidebar,
    sidebarActive    = Indigo800,
    sidebarText      = Neutral50,
    heading          = Neutral50,
    bodyText         = Neutral100,
    mutedText        = Neutral300,  // AA-safe on Neutral900 (contrast ~9.0)
    onPrimary        = Color.Black,
    onSecondary      = Color.Black,
    disabledText     = Neutral500,
    fieldLabel       = primary,
    fieldRequired    = Danger500,
    fieldFill        = Neutral800,
    infoBanner       = Indigo900,
    primary          = primary,
    primaryContainer = primaryContainer,
    secondary        = secondary,
    secondaryContainer = secondaryContainer,
    successGreen     = Success500,
    successGreenBg   = Color(0xFF064E3B),
    warningAmber     = Warning500,
    warningAmberBg   = Color(0xFF78350F),
    dangerRed        = Danger500,
    dangerRedBg      = Color(0xFF7F1D1D),
    infoBlue         = Info500,
    infoBlueBg       = Info700,
    glassHaze        = Color(0x14FFFFFF),
    glassSheenTop    = Color(0x1AFFFFFF),
    glassBorderGlow  = Color(0x33FFFFFF),
    ambientGlowPrimary   = primary.copy(alpha = 0.14f),
    ambientGlowSecondary = secondary.copy(alpha = 0.10f),
    isFrosted = true,
    isDark = true,
)

// ─────────────────────────────────────────────────────────────
// Preset → CampusColors
// ─────────────────────────────────────────────────────────────
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
        onPrimary        = FrostOnPrimary,
        onSecondary      = FrostOnPrimary,
        disabledText     = Color(0xFF5A6450),
        fieldLabel       = FrostFieldLabel,
        fieldRequired    = FrostFieldRequired,
        fieldFill        = FrostFieldFill,
        infoBanner       = FrostInfoBanner,
        primary          = FrostPrimary,
        primaryContainer = FrostPrimaryContainer,
        secondary        = FrostSecondary,
        secondaryContainer = FrostSecondaryContainer,
        successGreen     = FrostSuccess,
        successGreenBg   = FrostSuccessBg,
        warningAmber     = FrostWarning,
        warningAmberBg   = FrostWarningBg,
        dangerRed        = FrostDanger,
        dangerRedBg      = FrostDangerBg,
        infoBlue         = Info500,
        infoBlueBg       = FrostSurfaceElevated,
        glassHaze        = FrostHaze,
        glassSheenTop    = FrostSheenTop,
        glassBorderGlow  = FrostBorderGlow,
        ambientGlowPrimary   = FrostAmbientPrimary,
        ambientGlowSecondary = FrostAmbientSecondary,
        isFrosted = true,
        isDark = true,
    )
    CampusThemePreset.AURA_INDIGO   -> lightPreset(primary = Indigo500,  secondary = Teal500,   sidebar = Indigo900)
    CampusThemePreset.MYCAMU_CLASSIC -> lightPreset(primary = Color(0xFF2B8CE3), primaryContainer = Color(0xFFDBEAFE), secondary = Teal500, pageBackground = Color(0xFFE3EFF9), sidebar = Color(0xFF16233E), sidebarActive = Color(0xFFD1E3F6), fieldLabel = Color(0xFFC77B2E))
    CampusThemePreset.EMERALD_FOREST -> lightPreset(primary = Success500, primaryContainer = Success100, secondary = Teal500, pageBackground = Color(0xFFECFDF5), sidebar = Color(0xFF064E3B), sidebarActive = Success100, fieldLabel = Color(0xFF047857))
    CampusThemePreset.SUNSET_CRIMSON -> lightPreset(primary = Color(0xFFE11D48), primaryContainer = Color(0xFFFCE7F3), secondary = Warning500, pageBackground = Color(0xFFFFF1F2), sidebar = Color(0xFF881337), sidebarActive = Color(0xFFFCE7F3), fieldLabel = Color(0xFFBE123C))
    CampusThemePreset.CYBER_MIDNIGHT -> darkPreset(primary = Color(0xFF8B5CF6), primaryContainer = Color(0xFF5B21B6), secondary = Teal400, pageBackground = Color(0xFF0F172A), surface = Color(0xFF1E293B), sidebar = Color(0xFF020617))
    CampusThemePreset.NORDIC_SLATE   -> lightPreset(primary = Color(0xFF0284C7), primaryContainer = Color(0xFFBAE6FD), secondary = Teal500, pageBackground = Color(0xFFF0F9FF), sidebar = Color(0xFF0C4A6E), sidebarActive = Color(0xFFBAE6FD), fieldLabel = Color(0xFF0369A1))
    CampusThemePreset.GOLDEN_SAND    -> lightPreset(primary = Color(0xFFD97706), primaryContainer = Warning100, secondary = Teal500, pageBackground = Color(0xFFFFFBEB), sidebar = Color(0xFF78350F), sidebarActive = Warning100, fieldLabel = Color(0xFFB45309))
    CampusThemePreset.ROYAL_PURPLE   -> lightPreset(primary = Color(0xFF7C3AED), primaryContainer = Color(0xFFEDE9FE), secondary = Teal500, pageBackground = Color(0xFFF5F3FF), sidebar = Color(0xFF4C1D95), sidebarActive = Color(0xFFEDE9FE), fieldLabel = Color(0xFF6D28D9))
}
