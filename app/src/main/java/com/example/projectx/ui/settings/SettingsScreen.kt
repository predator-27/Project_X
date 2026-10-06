package com.projectx.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.BuildConfig
import com.projectx.app.components.AppScaffold
import com.projectx.app.theme.CampusTextSize
import com.projectx.app.theme.CampusThemeMode
import com.projectx.app.theme.CampusThemePreset
import com.projectx.app.theme.CampusThemeState
import com.projectx.app.theme.CampusTokens
import com.projectx.app.ui.TeacherManagementViewModel
import com.projectx.app.ui.auth.AuthViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    teacherViewModel: TeacherManagementViewModel = viewModel(),
) {
    val c = CampusTokens.colors
    val context = LocalContext.current
    val currentTheme by CampusThemeState.currentTheme.collectAsState()
    val currentMode by CampusThemeState.mode.collectAsState()
    val currentText by CampusThemeState.textSize.collectAsState()

    AppScaffold(title = "Settings & Appearance", onMenuClick = onMenuClick, modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { SectionHeader(icon = Icons.Default.Palette, title = "Theme preset") }
            item { ThemePresetGrid(current = currentTheme) }
            item { SectionHeader(icon = Icons.Default.WbSunny, title = "Appearance mode") }
            item { ModeRow(current = currentMode) }
            item { SectionHeader(icon = Icons.Default.TextFields, title = "Text size") }
            item { TextSizeRow(current = currentText) }
            item { SectionHeader(icon = Icons.Default.CloudDownload, title = "Updates") }
            item {
                SettingsRow(
                    title = "Check for updates",
                    subtitle = "Query GitHub for the latest release and show the banner.",
                    onClick = { teacherViewModel.checkForAppUpdates(context) },
                )
            }
            item {
                SettingsRow(
                    title = "App version",
                    subtitle = "${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                    enabled = false,
                )
            }
            item {
                SignOutRow(onClick = { authViewModel?.signOut() })
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    val c = CampusTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
        Text(title, color = c.heading, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ThemePresetGrid(current: CampusThemePreset) {
    val c = CampusTokens.colors
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp),
    ) {
        items(CampusThemePreset.ALL_THEMES, key = { it.name }) { preset ->
            val isSelected = preset == current
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = preset.pageBackground,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) preset.primaryColor else c.surfaceBorder,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { CampusThemeState.setTheme(preset) },
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = CircleShape, color = preset.primaryColor, modifier = Modifier.size(20.dp)) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        Surface(shape = CircleShape, color = preset.sidebarColor, modifier = Modifier.size(16.dp)) {}
                    }
                    Text(
                        text = preset.themeName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (preset) {
                            CampusThemePreset.CYBER_MIDNIGHT,
                            CampusThemePreset.FROSTED_MIDNIGHT -> Color.White
                            else -> Color(0xFF0F172A)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeRow(current: CampusThemeMode) {
    SegmentRow(
        options = CampusThemeMode.entries.map { it to it.name.lowercase().replaceFirstChar(Char::titlecase) },
        selected = current,
        onSelect = { CampusThemeState.setMode(it) },
    )
}

@Composable
private fun TextSizeRow(current: CampusTextSize) {
    SegmentRow(
        options = CampusTextSize.entries.map { it to it.label },
        selected = current,
        onSelect = { CampusThemeState.setTextSize(it) },
    )
}

@Composable
private fun <T> SegmentRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, c.surfaceBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) c.primary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(value) },
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = label,
                        color = if (isSelected) c.onPrimary else c.bodyText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, c.surfaceBorder, RoundedCornerShape(12.dp))
            .then(if (enabled && onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = c.heading, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = c.mutedText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SignOutRow(onClick: () -> Unit) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.dangerRedBg)
            .border(1.dp, c.dangerRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = c.dangerRed)
        Text("Sign out", color = c.dangerRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
