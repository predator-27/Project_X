package com.example.projectx.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

@Composable
fun CampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val activePreset by CampusThemeState.currentTheme.collectAsState()

    val dynamicColorScheme = if (darkTheme || activePreset == CampusThemePreset.CYBER_MIDNIGHT) {
        darkColorScheme(
            primary = activePreset.primaryColor,
            onPrimary = Color.White,
            primaryContainer = activePreset.primaryColor.copy(alpha = 0.3f),
            onPrimaryContainer = Color.White,
            surface = activePreset.surfaceColor,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = activePreset.surfaceColor,
            onSurfaceVariant = Color(0xFF94A3B8),
            background = activePreset.pageBackground,
            onBackground = Color(0xFFF8FAFC),
            outline = Color(0xFF334155),
            error = Color(0xFFF43F5E),
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = activePreset.primaryColor,
            onPrimary = Color.White,
            primaryContainer = activePreset.primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = Color(0xFF0F172A),
            surface = activePreset.surfaceColor,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = activePreset.primaryColor.copy(alpha = 0.12f),
            onSurfaceVariant = Color(0xFF64748B),
            background = activePreset.pageBackground,
            onBackground = Color(0xFF0F172A),
            outline = Color(0xFFE2E8F0),
            error = Color(0xFFE11D48),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
