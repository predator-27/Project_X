package com.projectx.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.BuildConfig
import com.projectx.app.components.AppScaffold
import com.projectx.app.theme.*
import com.projectx.app.ui.TeacherManagementViewModel
import com.projectx.app.ui.auth.AuthViewModel
import com.projectx.app.update.UpdateInfo

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    teacherViewModel: TeacherManagementViewModel = viewModel(),
) {
    val context = LocalContext.current
    val currentMode by CampusThemeState.mode.collectAsState()
    val currentText by CampusThemeState.textSize.collectAsState()

    val updateInfo by teacherViewModel.updateInfo.collectAsState()
    val isCheckingUpdate by teacherViewModel.isCheckingUpdate.collectAsState()
    val isDownloadingUpdate by teacherViewModel.isDownloadingUpdate.collectAsState()
    val downloadProgress by teacherViewModel.downloadProgress.collectAsState()
    val downloadError by teacherViewModel.downloadError.collectAsState()

    AppScaffold(title = "Settings & Appearance", onMenuClick = onMenuClick, modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { SectionHeader(icon = Icons.Default.WbSunny, title = "Appearance Mode") }
            item { ModeRow(current = currentMode) }
            item { SectionHeader(icon = Icons.Default.TextFields, title = "Text Size") }
            item { TextSizeRow(current = currentText) }
            item { SectionHeader(icon = Icons.Default.CloudDownload, title = "Updates") }
            item {
                UpdateCheckSection(
                    isChecking = isCheckingUpdate,
                    updateInfo = updateInfo,
                    isDownloading = isDownloadingUpdate,
                    downloadProgress = downloadProgress,
                    downloadError = downloadError,
                    onCheck = { teacherViewModel.checkForAppUpdates(context) },
                    onInstall = { teacherViewModel.downloadAndInstallAppUpdate(context) }
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
private fun SectionHeader(icon: ImageVector, title: String) {
    val c = CampusTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
        Text(title, color = c.heading, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UpdateCheckSection(
    isChecking: Boolean,
    updateInfo: UpdateInfo?,
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadError: String?,
    onCheck: () -> Unit,
    onInstall: () -> Unit
) {
    val c = CampusTokens.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.surfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Check for updates", color = c.heading, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = when {
                            isChecking -> "Querying GitHub Releases API..."
                            updateInfo != null && updateInfo.isNewerVersion && !updateInfo.hasApkAsset ->
                                "A newer release (${updateInfo.latestVersion}) is available, but it does not contain an APK for in-app installation."
                            updateInfo != null && updateInfo.isUpdateAvailable ->
                                "🎉 Update available: ${updateInfo.latestVersion}"
                            updateInfo != null && !updateInfo.isNewerVersion ->
                                "You're up to date. (${updateInfo.latestVersion})"
                            else -> "Query GitHub repository for the latest app release."
                        },
                        color = c.mutedText,
                        fontSize = 12.sp
                    )
                }

                if (isChecking) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = c.primary, strokeWidth = 2.dp)
                } else {
                    OutlinedButton(
                        onClick = onCheck,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, c.surfaceBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Check Now", fontSize = 12.sp, color = c.heading, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (updateInfo?.isUpdateAvailable == true) {
                HorizontalDivider(color = c.surfaceBorder)

                Text(
                    text = "Release Notes (${updateInfo.latestVersion}):\n${updateInfo.releaseNotes}",
                    fontSize = 12.sp,
                    color = c.bodyText
                )

                if (isDownloading) {
                    val percent = (downloadProgress * 100).toInt()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Downloading update from GitHub...", fontSize = 11.sp, color = c.mutedText)
                            Text("$percent%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = c.heading)
                        }
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = c.primary
                        )
                    }
                } else {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Install Update Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                downloadError?.let { err ->
                    Text(err, fontSize = 11.sp, color = c.dangerRed, fontWeight = FontWeight.Medium)
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
