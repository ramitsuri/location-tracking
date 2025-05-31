package com.ramitsuri.locationtracking.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.network.GithubApi
import java.io.File
import kotlin.math.max

class AppUpdateManager(
    private val api: GithubApi,
    context: Context,
) {
    private val context = context.applicationContext
    private var downloadUrl: String? = null

    suspend fun isNewerVersionAvailable(): Boolean {
        val currentVersionName = runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
        }.getOrNull()
        if (currentVersionName == null) {
            logE(TAG) { "Failed to get current version name" }
            return false
        }

        val latestRelease = api.getLatestRelease().getOrNull()
        if (latestRelease == null) {
            logI(TAG) { "Failed to get latest release" }
            return false
        }

        fun String.toComponents(): List<Int> = removePrefix("v")
            .split(".")
            .map { it.toIntOrNull() ?: 0 }

        val latestVersionComponents = latestRelease.name.toComponents()
        val currentVersionComponents = currentVersionName.toComponents()

        val maxComponents = max(currentVersionComponents.size, latestVersionComponents.size)

        // Iterate through the components from left to right (major, minor, patch, etc.)
        for (i in 0 until maxComponents) {
            val currentComponent = currentVersionComponents.getOrElse(i) { 0 }
            val latestComponent = latestVersionComponents.getOrElse(i) { 0 }

            if (latestComponent > currentComponent) {
                downloadUrl = latestRelease.assets.find { it.name == PHONE_APP_ASSET }?.downloadUrl
                logI(TAG) { "New version available: ${latestRelease.name}" }
                return true
            } else if (latestComponent < currentComponent) {
                logI(TAG) { "New version not available: ${latestRelease.name}" }
                return false
            }
        }

        logI(TAG) { "New version not available: ${latestRelease.name}" }
        return false
    }

    suspend fun downloadAndInstall() {
        val url = downloadUrl ?: run {
            logI(TAG) { "Download url not available" }
            return
        }
        File(context.cacheDir, DOWNLOAD_DIR).mkdirs()
        val file = File(context.cacheDir, "$DOWNLOAD_DIR/$APK_FILE_NAME")
        api.downloadAndSave(url = url, toFile = file)
        try {
            val uri: Uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            logI(TAG) { "Failed to start activity: $e" }
        }
    }

    companion object {
        private const val TAG = "GithubRepository"
        private const val DOWNLOAD_DIR = "app-download"
        private const val APK_FILE_NAME = "app.apk"
        private const val PHONE_APP_ASSET = "phone-release-unsigned-signed.apk"
    }
}
