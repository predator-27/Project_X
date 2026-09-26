package com.example.projectx.theme

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
// Frosted Midnight — the definitive dark theme.
// Matches the SP.png splash tone so cold-launch flows straight
// into the app with no visual jump.
// ─────────────────────────────────────────────────────────────
val FrostBackground      = Color(0xFF0B1220)   // deep splash navy
val FrostSurface         = Color(0xFF131B30)   // 1st glass tier
val FrostSurfaceElevated = Color(0xFF1A2440)   // 2nd glass tier (nested)
val FrostOutline         = Color(0xFF2A3550)
val FrostDivider         = Color(0xFF1F2A44)
val FrostSidebar         = Color(0xFF060B18)   // darkest — anchors the app

val FrostPrimary         = Color(0xFF60A5FA)   // cornflower — catches light
val FrostOnPrimary       = Color(0xFF051021)
val FrostPrimaryContainer   = Color(0xFF1E3A8A)
val FrostOnPrimaryContainer = Color(0xFFDBEAFE)

val FrostSecondary          = Color(0xFFC4B5FD)   // soft lavender partner
val FrostSecondaryContainer = Color(0xFF4C1D95)

val FrostHeading   = Color(0xFFF1F5F9)
val FrostBody      = Color(0xFFE2E8F0)
val FrostMuted     = Color(0xFF94A3B8)
val FrostFieldLabel = Color(0xFFE0B872)          // warm gold pops on navy
val FrostFieldRequired = Color(0xFFFB7185)
val FrostFieldFill = Color(0xFF1A2440)
val FrostInfoBanner = Color(0x2660A5FA)          // 15% primary tint

val FrostSuccess     = Color(0xFF34D399)
val FrostSuccessBg   = Color(0xFF052E21)
val FrostWarning     = Color(0xFFFBBF24)
val FrostWarningBg   = Color(0xFF3B2A05)
val FrostDanger      = Color(0xFFFB7185)
val FrostDangerBg    = Color(0xFF431418)

// Frosted glass rendering constants
val FrostHaze          = Color(0x0AFFFFFF)       // 4% white overlay
val FrostSheenTop      = Color(0x1AFFFFFF)       // 10% white top-edge highlight
val FrostBorderGlow    = Color(0x3360A5FA)       // 20% primary glow border
val FrostAmbientPrimary   = Color(0x2460A5FA)    // 14% for radial glow
val FrostAmbientSecondary = Color(0x17C4B5FD)    // 9%  for radial glow
