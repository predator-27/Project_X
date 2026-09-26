package com.example.projectx.theme

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CampusThemePreset(
    val themeName: String,
    val primaryColor: Color,
    val pageBackground: Color,
    val surfaceColor: Color,
    val sidebarColor: Color,
    val fieldLabelColor: Color
) {
    FROSTED_MIDNIGHT(
        themeName = "Frosted Midnight",
        primaryColor    = Color(0xFF60A5FA),   // cornflower
        pageBackground  = Color(0xFF0B1220),   // deep splash navy
        surfaceColor    = Color(0xFF131B30),   // 1st glass tier
        sidebarColor    = Color(0xFF060B18),   // darkest anchor
        fieldLabelColor = Color(0xFFE0B872)    // warm gold on navy
    ),
    AURA_INDIGO(
        themeName = "Aura Indigo",
        primaryColor = Color(0xFF4F46E5),
        pageBackground = Color(0xFFF8FAFC),
        surfaceColor = Color(0xFFFFFFFF),
        sidebarColor = Color(0xFF1E1B4B),
        fieldLabelColor = Color(0xFF4338CA)
    ),
    MYCAMU_CLASSIC(
        themeName = "MyCamu Classic",
        primaryColor = Color(0xFF2B8CE3),
        pageBackground = Color(0xFFE3EFF9),
        surfaceColor = Color(0xFFFFFFFF),
        sidebarColor = Color(0xFF16233E),
        fieldLabelColor = Color(0xFFC77B2E)
    ),
    EMERALD_FOREST(
        themeName = "Emerald Forest",
        primaryColor = Color(0xFF10B981),
        pageBackground = Color(0xFFECFDF5),
        surfaceColor = Color(0xFFFFFFFF),
        sidebarColor = Color(0xFF064E3B),
        fieldLabelColor = Color(0xFF047857)
    ),
    SUNSET_CRIMSON(
        themeName = "Sunset Crimson",
        primaryColor = Color(0xFFE11D48),
        pageBackground = Color(0xFFFFF1F2),
        surfaceColor = Color(0xFFFFFFFF),
        sidebarColor = Color(0xFF881337),
        fieldLabelColor = Color(0xFFBE123C)
    ),
    CYBER_MIDNIGHT(
        themeName = "Cyber Midnight",
        primaryColor = Color(0xFF8B5CF6),
        pageBackground = Color(0xFF0F172A),
        surfaceColor = Color(0xFF1E293B),
        sidebarColor = Color(0xFF020617),
        fieldLabelColor = Color(0xFFA78BFA)
    ),
    NORDIC_SLATE(
        themeName = "Nordic Slate",
        primaryColor = Color(0xFF0284C7),
        pageBackground = Color(0xFFF0F9FF),
        surfaceColor = Color(0xFFFFFFFF),
        sidebarColor = Color(0xFF0C4A6E),
        fieldLabelColor = Color(0xFF0369A1)
    ),
    GOLDEN_SAND(
        themeName = "Golden Sand",
        primaryColor = Color(0xFFD97706),
        pageBackground = Color(0xFFFFFBEB),
        surfaceColor = Color(0xFFFFFFFF),
        sidebarColor = Color(0xFF78350F),
        fieldLabelColor = Color(0xFFB45309)
    ),
    ROYAL_PURPLE(
        themeName = "Royal Purple",
        primaryColor = Color(0xFF7C3AED),
        pageBackground = Color(0xFFF5F3FF),
        surfaceColor = Color(0xFFFFFFFF),
        sidebarColor = Color(0xFF4C1D95),
        fieldLabelColor = Color(0xFF6D28D9)
    );

    companion object {
        val ALL_THEMES = entries.toList()
    }
}

object CampusThemeState {
    // Default = Frosted Midnight. Splash lands straight into the same navy tone.
    private val _currentTheme = MutableStateFlow(CampusThemePreset.FROSTED_MIDNIGHT)
    val currentTheme: StateFlow<CampusThemePreset> = _currentTheme.asStateFlow()

    fun setTheme(theme: CampusThemePreset) {
        _currentTheme.value = theme
    }
}
