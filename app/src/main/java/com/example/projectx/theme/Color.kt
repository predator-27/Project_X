package com.projectx.app.theme

import androidx.compose.ui.graphics.Color

// Custom "Aura Campus" Theme Palette (Light)
val PrimaryIndigo = Color(0xFF4F46E5)      // Electric Indigo
val PrimaryIndigoLight = Color(0xFF6366F1) // Vibrant Lavender Indigo
val PrimaryIndigoDark = Color(0xFF3730A3)  // Deep Indigo
val SecondaryEmerald = Color(0xFF10B981)   // Vibrant Emerald Teal
val SecondaryEmeraldBg = Color(0xFFD1FAE5) // Light Emerald Tint
val AccentCoral = Color(0xFFF43F5E)        // Warm Coral Red
val AccentCoralBg = Color(0xFFFFE4E6)      // Soft Coral Tint
val WarningAmber = Color(0xFFF59E0B)       // Golden Amber
val WarningAmberBg = Color(0xFFFEF3C7)     // Light Amber Tint

val NavySidebar = Color(0xFF1E1B4B)        // Deep Midnight Indigo Drawer
val NavySidebarActive = Color(0xFFEEF2FF)  // Active Drawer Item Pill
val NavySidebarText = Color(0xFFF8FAFC)    // Drawer Item Label
val PageBackground = Color(0xFFF8FAFC)     // Light Slate Canvas
val SurfaceCard = Color(0xFFFFFFFF)        // Crisp Elevated Card Surface
val SurfaceBorder = Color(0xFFE2E8F0)      // Smooth 1dp Hairline Border
val HeadingNavy = Color(0xFF0F172A)        // Charcoal Navy Heading
val BodyText = Color(0xFF1E293B)           // Primary Body Text
val MutedText = Color(0xFF64748B)          // Cool Grey Muted Text
val FieldLabel = Color(0xFF4338CA)         // Royal Violet Form Field Label
val FieldRequired = Color(0xFFE11D48)      // Rose Red Required Marker
val FieldFill = Color(0xFFF1F5F9)          // Soft Grey Input Fill
val InfoBannerBg = Color(0xFFE0E7FF)       // Light Lavender Info Strip

// Compatibility Aliases for Design System
val PrimaryBlue = PrimaryIndigo
val DangerRed = AccentCoral
val DangerRedBg = AccentCoralBg

// Custom "Aura Campus" Theme Palette (Dark)
val PageBackgroundDark = Color(0xFF090D16)
val SurfaceCardDark = Color(0xFF111827)
val SurfaceBorderDark = Color(0xFF1F2937)
val HeadingNavyDark = Color(0xFFF9FAFB)
val BodyTextDark = Color(0xFFE5E7EB)
val MutedTextDark = Color(0xFF9CA3AF)

// ─────────────────────────────────────────────────────────────
// Pixel Glass Green — the definitive dark theme.
// Blends the LO.png logo's mosaic-green + chi-rho black with a
// frosted-glass surface treatment. Cold-launch flows navy → green
// with no jump because the splash bg matches pageBackground exactly.
//
// The Frost* names are retained for backward compatibility — callers
// don't need to change. Only the underlying palette has moved to green.
// ─────────────────────────────────────────────────────────────
val FrostBackground      = Color(0xFF0A100A)   // near-black, olive undertone
val FrostSurface         = Color(0xFF101812)   // 1st glass tier
val FrostSurfaceElevated = Color(0xFF182218)   // 2nd glass tier (nested)
val FrostOutline         = Color(0xFF2A3620)   // green-tinged hairline
val FrostDivider         = Color(0xFF1F2A18)
val FrostSidebar         = Color(0xFF050805)   // darkest — anchors the app

val FrostPrimary         = Color(0xFFA8B840)   // mosaic green from LO.png
val FrostOnPrimary       = Color(0xFF0A100A)   // near-black text on green
val FrostPrimaryContainer   = Color(0xFF2A3618)   // dark olive container
val FrostOnPrimaryContainer = Color(0xFFE8F0D8)   // warm light text

val FrostSecondary          = Color(0xFF6B8348)   // deeper matcha partner
val FrostSecondaryContainer = Color(0xFF1E2A10)

val FrostHeading   = Color(0xFFE8F0D8)           // warm off-white, green cast
val FrostBody      = Color(0xFFC8D4B8)
val FrostMuted     = Color(0xFF7A8870)
val FrostFieldLabel = Color(0xFFC8A840)          // warm gold — pops on olive
val FrostFieldRequired = Color(0xFFE85C4B)
val FrostFieldFill = Color(0xFF182218)
val FrostInfoBanner = Color(0x26A8B840)          // 15% primary tint

val FrostSuccess     = Color(0xFF7BAE45)         // matcha — same family
val FrostSuccessBg   = Color(0xFF182210)
val FrostWarning     = Color(0xFFE8A83D)
val FrostWarningBg   = Color(0xFF382608)
val FrostDanger      = Color(0xFFE85C4B)
val FrostDangerBg    = Color(0xFF3A1810)

// Frosted glass rendering — pixel-inspired hard border, green glow
val FrostHaze          = Color(0x0AFFFFFF)       // 4% white haze
val FrostSheenTop      = Color(0x18FFFFFF)       // 9% white top-edge highlight
val FrostBorderGlow    = Color(0x40A8B840)       // 25% primary — the "pixel outline"
val FrostAmbientPrimary   = Color(0x26A8B840)    // 15% green radial glow
val FrostAmbientSecondary = Color(0x18C8A840)    // 10% gold radial glow
