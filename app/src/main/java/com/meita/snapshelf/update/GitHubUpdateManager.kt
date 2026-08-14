package com.meita.snapshelf.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class AvailableUpdate(val version: String, val apkUrl: String, val checksumUrl: String)

sealed interface UpdateResult {
    data object UpToDate : UpdateResult
    data class Ready(val version: String, val apk: File) : UpdateResult
    data class Failed(val reason: String) : UpdateResult
}

class GitHubUpdateManager(private val context: Context) {
    private val apiUrl = "https://api.github.com/repos/ninjin0802/SnapPurge/releases/latest"

    suspend fun checkAndDownload(): UpdateResult = withContext(Dispatchers.IO) {
        runCatching {
            val release = requestText(apiUrl).let(::JSONObject)
            val version = release.getString("tag_name").removePrefix("v")
            if (compareVersions(version, currentVersion()) <= 0) return@runCatching UpdateResult.UpToDate
            val assets = release.getJSONArray("assets")
            var apkUrl: String? = null
            var checksumUrl: String? = null
            for (index in 0 until assets.length()) {
                val asset = assets.getJSONObject(index)
                val name = asset.getString("name")
                when {
                    name.endsWith(".apk") -> apkUrl = asset.getString("browser_download_url")
                    name.endsWith(".apk.sha256") -> checksumUrl = asset.getString("browser_download_url")
                }
            }
            val update = AvailableUpdate(version, requireNotNull(apkUrl), requireNotNull(checksumUrl))
            val expectedHash = requestText(update.checksumUrl).trim().substringBefore(' ').lowercase()
            val directory = File(context.cacheDir, "updates").apply { mkdirs() }
            val apk = File(directory, "SnapPurge-v${update.version}.apk")
            download(update.apkUrl, apk)
            check(sha256(apk) == expectedHash) { "SHA-256が一致しません" }
            check(hasCurrentSigningCertificate(apk)) { "APKの署名がninjinの現在の署名と一致しません" }
            UpdateResult.Ready(update.version, apk)
        }.getOrElse { UpdateResult.Failed(it.message ?: "更新確認に失敗しました") }
    }

    fun launchInstaller(apk: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", apk)
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun requestText(url: String): String = open(url).use { it.reader(Charsets.UTF_8).readText() }

    private fun download(url: String, target: File) {
        open(url).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
    }

    private fun open(url: String) = (URL(url).openConnection() as HttpURLConnection).run {
        connectTimeout = 15_000
        readTimeout = 30_000
        instanceFollowRedirects = true
        setRequestProperty("Accept", "application/vnd.github+json")
        setRequestProperty("User-Agent", "SnapPurge/${currentVersion()}")
        connect()
        check(responseCode in 200..299) { "GitHub応答エラー: $responseCode" }
        inputStream
    }

    private fun currentVersion(): String {
        @Suppress("DEPRECATION")
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
    }

    private fun compareVersions(left: String, right: String): Int {
        val a = left.split('.').map { it.toIntOrNull() ?: 0 }
        val b = right.split('.').map { it.toIntOrNull() ?: 0 }
        return (0 until maxOf(a.size, b.size)).firstNotNullOfOrNull { i ->
            (a.getOrElse(i) { 0 } - b.getOrElse(i) { 0 }).takeIf { it != 0 }
        } ?: 0
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    @Suppress("DEPRECATION")
    private fun hasCurrentSigningCertificate(apk: File): Boolean {
        val current: Array<android.content.pm.Signature>
        val archive: Array<android.content.pm.Signature>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val flags = android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            current = context.packageManager.getPackageInfo(context.packageName, flags).signingInfo?.apkContentsSigners ?: return false
            archive = context.packageManager.getPackageArchiveInfo(apk.path, flags)?.signingInfo?.apkContentsSigners ?: return false
        } else {
            val flags = android.content.pm.PackageManager.GET_SIGNATURES
            current = context.packageManager.getPackageInfo(context.packageName, flags).signatures ?: return false
            archive = context.packageManager.getPackageArchiveInfo(apk.path, flags)?.signatures ?: return false
        }
        return current.any { installed -> archive.any { downloaded -> installed.toByteArray().contentEquals(downloaded.toByteArray()) } }
    }
}
