package com.projectx.app.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────
// Primary ramp — Indigo (base 500 = #4F46E5)
// ─────────────────────────────────────────────────────────────────────
val Indigo50  = Color(0xFFEEF2FF)
val Indigo100 = Color(0xFFE0E7FF)
val Indigo200 = Color(0xFFC7D2FE)
val Indigo300 = Color(0xFFA5B4FC)
val Indigo400 = Color(0xFF818CF8)
val Indigo500 = Color(0xFF4F46E5)
val Indigo600 = Color(0xFF4338CA)
val Indigo700 = Color(0xFF3730A3)
val Indigo800 = Color(0xFF312E81)
val Indigo900 = Color(0xFF1E1B4B)

// ─────────────────────────────────────────────────────────────────────
// Secondary ramp — Teal (base 500 = #14B8A6)
// ─────────────────────────────────────────────────────────────────────
val Teal50  = Color(0xFFF0FDFA)
val Teal100 = Color(0xFFCCFBF1)
val Teal200 = Color(0xFF99F6E4)
val Teal300 = Color(0xFF5EEAD4)
val Teal400 = Color(0xFF2DD4BF)
val Teal500 = Color(0xFF14B8A6)
val Teal600 = Color(0xFF0D9488)
val Teal700 = Color(0xFF0F766E)
val Teal800 = Color(0xFF115E59)
val Teal900 = Color(0xFF134E4A)

// ─────────────────────────────────────────────────────────────────────
// Neutral ramp — Slate (50-950) for surfaces and text
// ─────────────────────────────────────────────────────────────────────
val Neutral50  = Color(0xFFF8FAFC)
val Neutral100 = Color(0xFFF1F5F9)
val Neutral200 = Color(0xFFE2E8F0)
val Neutral300 = Color(0xFFCBD5E1)
val Neutral400 = Color(0xFF94A3B8)
val Neutral500 = Color(0xFF64748B)
val Neutral600 = Color(0xFF475569)
val Neutral700 = Color(0xFF334155)
val Neutral800 = Color(0xFF1E293B)
val Neutral900 = Color(0xFF0F172A)
val Neutral950 = Color(0xFF020617)

// ─────────────────────────────────────────────────────────────────────
// Semantic ramps (success / warning / danger / info)
// ─────────────────────────────────────────────────────────────────────
val Success500 = Color(0xFF10B981)   // Emerald
val Success100 = Color(0xFFD1FAE5)
val Success700 = Color(0xFF047857)

val Warning500 = Color(0xFFF59E0B)   // Amber
val Warning100 = Color(0xFFFEF3C7)
val Warning700 = Color(0xFFB45309)

val Danger500  = Color(0xFFEF4444)   // Red
val Danger100  = Color(0xFFFEE2E2)
val Danger700  = Color(0xFFB91C1C)

val Info500    = Color(0xFF0EA5E9)   // Sky
val Info100    = Color(0xFFE0F2FE)
val Info700    = Color(0xFF0369A1)

// ─────────────────────────────────────────────────────────────────────
// Backward-compatible aliases — old screen imports keep working.
// Nothing outside theme/ should reach for these directly; new code reads
// CampusTokens.colors.*
// ─────────────────────────────────────────────────────────────────────
val PrimaryIndigo      = Indigo500
val PrimaryIndigoLight = Indigo400
val PrimaryIndigoDark  = Indigo700
val PrimaryBlue        = Indigo500

val SecondaryEmerald   = Success500
val SecondaryEmeraldBg = Success100

val AccentCoral        = Danger500
val AccentCoralBg      = Danger100
val DangerRed          = Danger500
val DangerRedBg        = Danger100

val WarningAmber       = Warning500
val WarningAmberBg     = Warning100

val NavySidebar        = Indigo900
val NavySidebarActive  = Indigo50
val NavySidebarText    = Neutral50
val PageBackground     = Neutral50
val SurfaceCard        = Color(0xFFFFFFFF)
val SurfaceBorder      = Neutral200
val HeadingNavy        = Neutral900
val BodyText           = Neutral800
val MutedText          = Neutral500
val FieldLabel         = Indigo600
val FieldRequired      = Danger500
val FieldFill          = Neutral100
val InfoBannerBg       = Indigo100

val PageBackgroundDark = Color(0xFF090D16)
val SurfaceCardDark    = Color(0xFF111827)
val SurfaceBorderDark  = Color(0xFF1F2937)
val HeadingNavyDark    = Color(0xFFF9FAFB)
val BodyTextDark       = Color(0xFFE5E7EB)
val MutedTextDark      = Color(0xFF9CA3AF)

// ─────────────────────────────────────────────────────────────────────
// Frosted / Pixel-Glass palette — the dark preset is bespoke;
// kept intact so nothing on that preset shifts visually.
// ─────────────────────────────────────────────────────────────────────
val FrostBackground      = Color(0xFF0A100A)
val FrostSurface         = Color(0xFF101812)
val FrostSurfaceElevated = Color(0xFF182218)
val FrostOutline         = Color(0xFF2A3620)
val FrostDivider         = Color(0xFF1F2A18)
val FrostSidebar         = Color(0xFF050805)

val FrostPrimary            = Color(0xFFA8B840)
val FrostOnPrimary          = Color(0xFF0A100A)
val FrostPrimaryContainer   = Color(0xFF2A3618)
val FrostOnPrimaryContainer = Color(0xFFE8F0D8)

val FrostSecondary          = Color(0xFF6B8348)
val FrostSecondaryContainer = Color(0xFF1E2A10)

val FrostHeading   = Color(0xFFE8F0D8)
val FrostBody      = Color(0xFFC8D4B8)
val FrostMuted     = Color(0xFF8EA082)   // tightened for WCAG AA against FrostBackground
val FrostFieldLabel = Color(0xFFD4B850)
val FrostFieldRequired = Color(0xFFE85C4B)
val FrostFieldFill = Color(0xFF182218)
val FrostInfoBanner = Color(0x26A8B840)

val FrostSuccess     = Color(0xFF7BAE45)
val FrostSuccessBg   = Color(0xFF182210)
val FrostWarning     = Color(0xFFE8A83D)
val FrostWarningBg   = Color(0xFF382608)
val FrostDanger      = Color(0xFFE85C4B)
val FrostDangerBg    = Color(0xFF3A1810)

val FrostHaze          = Color(0x0AFFFFFF)
val FrostSheenTop      = Color(0x18FFFFFF)
val FrostBorderGlow    = Color(0x40A8B840)
val FrostAmbientPrimary   = Color(0x26A8B840)
val FrostAmbientSecondary = Color(0x18C8A840)

// ─────────────────────────────────────────────────────────────────────
// Fixed Splash & Brand Constants
// ─────────────────────────────────────────────────────────────────────
val SplashBackground     = Color(0xFF0A100A)
val SplashBrandNameColor = Color(0xFFE8F0D8)
val SplashTaglineColor   = Color(0xFF7A8870)

val MicrosoftBrandBlue   = Color(0xFF00A4EF)
