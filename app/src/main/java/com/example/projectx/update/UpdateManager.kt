package com.example.projectx.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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

    private fun getInstalledVersion(context: Context): String {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(context.packageName, 0)
            info.versionName ?: "0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDismissedVersion(context: Context): String? =
        prefs(context).getString(KEY_DISMISSED_VERSION, null)

    fun markVersionDismissed(context: Context, version: String) {
        prefs(context).edit().putString(KEY_DISMISSED_VERSION, version).apply()
    }

    suspend fun checkForUpdates(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        _isChecking.value = true
        _downloadError.value = null
        val installedVersion = getInstalledVersion(context)
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

                val tagName = json.optString("tag_name", "")
                if (tagName.isBlank()) {
                    _updateInfo.value = null
                    return@withContext null
                }
                val releaseNotes = json.optString("body", "New version available.")

                var downloadUrl = ""
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
                if (downloadUrl.isBlank()) {
                    downloadUrl = "https://raw.githubusercontent.com/predator-27/Project_X/main/releases/app-debug.apk"
                }

                val isAvailable = isVersionHigher(normalizeVersion(tagName), normalizeVersion(installedVersion))

                val info = UpdateInfo(
                    latestVersion = tagName,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    isUpdateAvailable = isAvailable
                )

                _updateInfo.value = info
                return@withContext info
            } else {
                // API unreachable / rate-limited — stay silent, don't nag.
                _updateInfo.value = null
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _updateInfo.value = null
            return@withContext null
        } finally {
            _isChecking.value = false
        }
    }

    private fun normalizeVersion(raw: String): String {
        return raw.trim()
            .removePrefix("v")
            .removePrefix("V")
            .removePrefix("release-")
            .removePrefix("release/")
            .trim()
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

            if (remoteParts.isEmpty() || currentParts.isEmpty()) return false

            val length = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until length) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    companion object {
        private const val PREFS_NAME = "project_x_update_prefs"
        private const val KEY_DISMISSED_VERSION = "dismissed_version"

        @Volatile
        private var instance: UpdateManager? = null

        fun getInstance(): UpdateManager {
            return instance ?: synchronized(this) {
                instance ?: UpdateManager().also { instance = it }
            }
        }
    }
}
