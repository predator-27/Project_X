package com.example.projectx.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = NavySidebarText,
    primaryContainer = PrimaryIndigoLight,
    onPrimaryContainer = HeadingNavy,
    secondary = SecondaryEmerald,
    onSecondary = NavySidebarText,
    secondaryContainer = SecondaryEmeraldBg,
    onSecondaryContainer = HeadingNavy,
    tertiary = AccentCoral,
    onTertiary = NavySidebarText,
    tertiaryContainer = AccentCoralBg,
    surface = SurfaceCard,
    onSurface = BodyText,
    surfaceVariant = InfoBannerBg,
    onSurfaceVariant = MutedText,
    background = PageBackground,
    onBackground = BodyText,
    outline = SurfaceBorder,
    error = AccentCoral,
    onError = SurfaceCard
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = NavySidebarText,
    primaryContainer = SurfaceBorderDark,
    onPrimaryContainer = HeadingNavyDark,
    secondary = SecondaryEmerald,
    onSecondary = NavySidebarText,
    secondaryContainer = SurfaceBorderDark,
    surface = SurfaceCardDark,
    onSurface = BodyTextDark,
    surfaceVariant = SurfaceBorderDark,
    onSurfaceVariant = MutedTextDark,
    background = PageBackgroundDark,
    onBackground = BodyTextDark,
    outline = SurfaceBorderDark,
    error = AccentCoral,
    onError = SurfaceCardDark
)

@Composable
fun CampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
