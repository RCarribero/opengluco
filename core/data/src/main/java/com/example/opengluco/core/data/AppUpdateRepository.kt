package com.example.opengluco.core.data

import com.example.opengluco.core.model.AppReleaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Repositorio para la comprobacion y descarga de actualizaciones mediante la API publica de GitHub Releases.
 * 100% gratuito y sin dependencias de servicios de pago.
 */
class AppUpdateRepository {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_GITHUB_OWNER = "RCarribero"
        const val DEFAULT_GITHUB_REPO = "opengluco"
        private const val GITHUB_API_URL = "https://api.github.com/repos/%s/%s/releases/latest"
    }

    /**
     * Consulta la API publica de GitHub para obtener la ultima version publicada.
     */
    suspend fun checkLatestRelease(
        currentVersionName: String,
        targetKeyword: String = "Mobile",
        owner: String = DEFAULT_GITHUB_OWNER,
        repo: String = DEFAULT_GITHUB_REPO
    ): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val url = String.format(GITHUB_API_URL, owner, repo)
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "OpenGluco-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 404) {
                    // No hay releases creadas en GitHub aún: la app está al día
                    val upToDateInfo = AppReleaseInfo(
                        versionName = currentVersionName,
                        releaseTitle = "OpenGluco v$currentVersionName",
                        releaseNotes = "No hay nuevas versiones publicadas en el repositorio.",
                        apkDownloadUrl = "",
                        htmlUrl = "https://github.com/$owner/$repo",
                        isUpdateAvailable = false
                    )
                    return@withContext Result.success(upToDateInfo)
                }

                if (!response.isSuccessful) {
                    val code = response.code
                    return@withContext Result.failure(
                        Exception("GitHub API devolvió código HTTP $code")
                    )
                }

                val bodyString = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Respuesta vacia de GitHub"))

                val json = JSONObject(bodyString)
                val rawTag = json.optString("tag_name", "").trim()
                val versionName = rawTag.removePrefix("v").removePrefix("V")
                val releaseTitle = json.optString("name", "Version $versionName")
                val releaseNotes = json.optString("body", "Sin notas de la version.")
                val htmlUrl = json.optString("html_url", "")
                val publishedAt = json.optString("published_at", "")

                // Buscar el archivo APK en los assets de la release filtrando por el módulo correspondiente
                var apkUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    // Prioridad 1: Coincidencia con el nombre del módulo (ej. "Mobile", "Wear", "Auto")
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true) &&
                            name.contains(targetKeyword, ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                    // Prioridad 2: Si no encuentra la keyword exacta, fallback a cualquier APK
                    if (apkUrl.isBlank()) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.optString("browser_download_url", "")
                                break
                            }
                        }
                    }
                }

                val isNewer = isVersionNewer(versionName, currentVersionName)

                val releaseInfo = AppReleaseInfo(
                    versionName = versionName,
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    apkDownloadUrl = apkUrl,
                    htmlUrl = htmlUrl,
                    isUpdateAvailable = isNewer,
                    publishedAt = publishedAt
                )

                Result.success(releaseInfo)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga el archivo APK de GitHub Releases en el destino especificado.
     */
    suspend fun downloadApk(
        apkUrl: String,
        destinationFile: File,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apkUrl)
                .header("User-Agent", "OpenGluco-Android-App")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al descargar APK: HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Cuerpo de respuesta vacio"))
            val contentLength = body.contentLength()

            if (destinationFile.exists()) destinationFile.delete()
            destinationFile.parentFile?.mkdirs()

            body.byteStream().use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead.toLong()
                        if (contentLength > 0L) {
                            val percent = ((totalRead * 100L) / contentLength).toInt().coerceIn(0, 100)
                            onProgress(percent)
                        }
                    }
                    outputStream.flush()
                }
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compara semánticamente dos versiones.
     */
    fun isVersionNewer(remoteVersion: String, currentVersion: String): Boolean {
        if (remoteVersion.isBlank() || currentVersion.isBlank()) return false
        if (remoteVersion == currentVersion) return false

        val cleanRemote = remoteVersion.removePrefix("v").removePrefix("V").split("-")[0]
        val cleanCurrent = currentVersion.removePrefix("v").removePrefix("V").split("-")[0]

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
