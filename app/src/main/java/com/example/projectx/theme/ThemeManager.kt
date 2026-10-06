package com.projectx.app.theme

import android.content.Context
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
        themeName = "Pixel Glass Green",
        primaryColor    = Color(0xFFA8B840),   // mosaic green from LO.png
        pageBackground  = Color(0xFF0F1812),   // deep glass olive
        surfaceColor    = Color(0xFF1B281E),   // elevated glass card surface
        sidebarColor    = Color(0xFF080D08),   // darkest anchor
        fieldLabelColor = Color(0xFFC8A840)    // warm gold pops on olive
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

enum class CampusThemeMode { SYSTEM, LIGHT, DARK }
enum class CampusTextSize(val scale: Float, val label: String) {
    NORMAL(1.0f, "Normal"),
    LARGE(1.18f, "Large"),
}

object CampusThemeState {
    private const val PREFS = "projectx_theme_prefs"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_MODE = "theme_mode"
    private const val KEY_TEXT = "text_size"
    private const val KEY_CUSTOM_PRIMARY = "custom_primary_color"
    private const val KEY_CUSTOM_SECONDARY = "custom_secondary_color"

    val DEFAULT_PRIMARY = Color(0xFF4F46E5)
    val DEFAULT_SECONDARY = Color(0xFF14B8A6)

    private val _currentTheme = MutableStateFlow(CampusThemePreset.AURA_INDIGO)
    val currentTheme: StateFlow<CampusThemePreset> = _currentTheme.asStateFlow()

    private val _mode = MutableStateFlow(CampusThemeMode.SYSTEM)
    val mode: StateFlow<CampusThemeMode> = _mode.asStateFlow()

    private val _textSize = MutableStateFlow(CampusTextSize.NORMAL)
    val textSize: StateFlow<CampusTextSize> = _textSize.asStateFlow()

    private val _customPrimary = MutableStateFlow<Color?>(null)
    val customPrimary: StateFlow<Color?> = _customPrimary.asStateFlow()

    private val _customSecondary = MutableStateFlow<Color?>(null)
    val customSecondary: StateFlow<Color?> = _customSecondary.asStateFlow()

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _currentTheme.value = CampusThemePreset.entries.find { it.name == prefs.getString(KEY_THEME, null) }
            ?: CampusThemePreset.AURA_INDIGO
        _mode.value = CampusThemeMode.entries.find { it.name == prefs.getString(KEY_MODE, null) }
            ?: CampusThemeMode.SYSTEM
        _textSize.value = CampusTextSize.entries.find { it.name == prefs.getString(KEY_TEXT, null) }
            ?: CampusTextSize.NORMAL

        val savedPrimaryHex = prefs.getString(KEY_CUSTOM_PRIMARY, null)
        _customPrimary.value = savedPrimaryHex?.let { parseHexColor(it) }

        val savedSecondaryHex = prefs.getString(KEY_CUSTOM_SECONDARY, null)
        _customSecondary.value = savedSecondaryHex?.let { parseHexColor(it) }
    }

    fun setTheme(theme: CampusThemePreset) {
        _currentTheme.value = theme
        appContext?.let { ctx ->
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_THEME, theme.name).apply()
        }
    }

    fun setMode(mode: CampusThemeMode) {
        _mode.value = mode
        appContext?.let { ctx ->
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_MODE, mode.name).apply()
        }
    }

    fun setTextSize(size: CampusTextSize) {
        _textSize.value = size
        appContext?.let { ctx ->
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_TEXT, size.name).apply()
        }
    }

    fun setCustomPrimary(color: Color) {
        _customPrimary.value = color
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()
            ?.putString(KEY_CUSTOM_PRIMARY, colorToHex(color))?.apply()
    }

    fun setCustomSecondary(color: Color) {
        _customSecondary.value = color
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()
            ?.putString(KEY_CUSTOM_SECONDARY, colorToHex(color))?.apply()
    }

    fun resetCustomColors() {
        _customPrimary.value = null
        _customSecondary.value = null
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()
            ?.remove(KEY_CUSTOM_PRIMARY)
            ?.remove(KEY_CUSTOM_SECONDARY)?.apply()
    }

    fun setTheme(context: Context, theme: CampusThemePreset) {
        appContext = context.applicationContext
        setTheme(theme)
    }
}

fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt().coerceIn(0, 255)
    val g = (color.green * 255).toInt().coerceIn(0, 255)
    val b = (color.blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = android.graphics.Color.parseColor("#$cleanHex")
        Color(colorInt)
    } catch (e: Exception) {
        CampusThemeState.DEFAULT_PRIMARY
    }
}
