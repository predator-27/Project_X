package com.example.projectx.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean
)

class UpdateManager private constructor() {

    private val currentVersion = "1.0.0"

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        _isChecking.value = true
        _downloadError.value = null
        try {
            val githubApiUrl = "https://api.github.com/repos/predator-27/Project_X/releases/latest"
            val url = URL(githubApiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)

                val tagName = json.optString("tag_name", "v1.4.0")
                val releaseNotes = json.optString("body", "Bug fixes, performance improvements, and in-app auto-installer.")
                
                // Find asset download url or fallback to raw GitHub link
                var downloadUrl = "https://raw.githubusercontent.com/predator-27/Project_X/main/releases/app-debug.apk"
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.optString("name").endsWith(".apk")) {
                            downloadUrl = asset.optString("browser_download_url")
                            break
                        }
                    }
                }

                val cleanRemoteVersion = tagName.replace("v", "").trim()
                val isAvailable = isVersionHigher(cleanRemoteVersion, currentVersion)

                val info = UpdateInfo(
                    latestVersion = tagName,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    isUpdateAvailable = isAvailable
                )

                _updateInfo.value = info
                return@withContext info
            } else {
                // Fallback direct check against raw repository release
                val fallbackInfo = UpdateInfo(
                    latestVersion = "v1.4.0",
                    releaseNotes = "New Update Available: In-App Auto-Installer & GitHub Releases Sync.",
                    downloadUrl = "https://raw.githubusercontent.com/predator-27/Project_X/main/releases/app-debug.apk",
                    isUpdateAvailable = true
                )
                _updateInfo.value = fallbackInfo
                return@withContext fallbackInfo
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Direct update fallback info if GitHub API rate-limited
            val directInfo = UpdateInfo(
                latestVersion = "v1.4.0",
                releaseNotes = "New Flawless In-App Update available with automatic installer.",
                downloadUrl = "https://raw.githubusercontent.com/predator-27/Project_X/main/releases/app-debug.apk",
                isUpdateAvailable = true
            )
            _updateInfo.value = directInfo
            return@withContext directInfo
        } finally {
            _isChecking.value = false
        }
    }

    suspend fun downloadAndInstallUpdate(context: Context, downloadUrl: String) = withContext(Dispatchers.IO) {
        _isDownloading.value = true
        _downloadProgress.value = 0f
        _downloadError.value = null

        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.connect()

            val fileLength = connection.contentLength
            val apkFile = File(context.externalCacheDir ?: context.cacheDir, "update_app.apk")
            if (apkFile.exists()) apkFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            _downloadProgress.value = total.toFloat() / fileLength.toFloat()
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }

            _downloadProgress.value = 1.0f
            _isDownloading.value = false

            // Launch Package Installer
            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            _isDownloading.value = false
            _downloadError.value = "Failed to download update: ${e.message}"
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        val authority = "${context.packageName}.fileprovider"
        val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }

    private fun isVersionHigher(remote: String, current: String): Boolean {
        try {
            val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

            val length = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until length) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        } catch (e: Exception) {
            return true
        }
        return false
    }

    companion object {
        @Volatile
        private var instance: UpdateManager? = null

        fun getInstance(): UpdateManager {
            return instance ?: synchronized(this) {
                instance ?: UpdateManager().also { instance = it }
            }
        }
    }
}
