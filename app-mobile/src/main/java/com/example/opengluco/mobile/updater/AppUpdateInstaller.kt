package com.example.opengluco.mobile.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.opengluco.core.data.AppUpdateRepository
import java.io.File
import java.security.MessageDigest

sealed interface InstallOutcome {
    object Launched : InstallOutcome
    object NeedsInstallPermission : InstallOutcome
    object InvalidPackage : InstallOutcome
    object SignatureMismatch : InstallOutcome
    object ChecksumMismatch : InstallOutcome
    data class LaunchError(val message: String) : InstallOutcome
}

/**
 * Gestor de descarga e instalacion de actualizaciones de OpenGluco.
 * Realiza la descarga en la cache local de la app, verifica firma y SHA-256
 * y abre el instalador oficial de Android via FileProvider.
 */
object AppUpdateInstaller {

    private val repository = AppUpdateRepository()

    fun canRequestInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
        }
    }

    /**
     * Descarga el archivo APK de GitHub Releases en el directorio de cache privado.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        expectedSha256: String = "",
        onProgress: (Int) -> Unit = {}
    ): Result<File> {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val destinationFile = File(updatesDir, "opengluco_update.apk")
        return repository.downloadApk(apkUrl, destinationFile, onProgress, expectedSha256)
    }

    /**
     * Valida integridad, firma digital y version del APK antes de invocar el instalador nativo.
     */
    @Suppress("DEPRECATION")
    fun installApk(context: Context, apkFile: File, expectedSha256: String = ""): InstallOutcome {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            return InstallOutcome.InvalidPackage
        }

        if (expectedSha256.isNotBlank()) {
            val actualSha = repository.calculateSha256(apkFile)
            if (!actualSha.equals(expectedSha256.trim(), ignoreCase = true)) {
                apkFile.delete()
                return InstallOutcome.ChecksumMismatch
            }
        }

        val archiveFlags = PackageManager.GET_SIGNING_CERTIFICATES
        val archiveInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, archiveFlags)
        if (archiveInfo == null || archiveInfo.packageName != context.packageName) {
            apkFile.delete()
            return InstallOutcome.InvalidPackage
        }

        // Prevenir downgrade silencioso
        val currentInfo = try {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } catch (_: Exception) {
            null
        }
        if (currentInfo != null && archiveInfo.longVersionCode < currentInfo.longVersionCode) {
            apkFile.delete()
            return InstallOutcome.InvalidPackage
        }

        // Verificar que la firma del APK descargado coincide con el certificado instalado
        val installedDigests = currentInfo?.signingInfo?.apkContentsSigners
            ?.map { sha256Hex(it.toByteArray()) }?.toSet().orEmpty()
        val archiveDigests = archiveInfo.signingInfo?.apkContentsSigners
            ?.map { sha256Hex(it.toByteArray()) }?.toSet().orEmpty()
        if (archiveDigests.isEmpty() || (installedDigests.isNotEmpty() && installedDigests.intersect(archiveDigests).isEmpty())) {
            apkFile.delete()
            return InstallOutcome.SignatureMismatch
        }

        if (!canRequestInstalls(context)) {
            openInstallPermissionSettings(context)
            return InstallOutcome.NeedsInstallPermission
        }

        return try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            InstallOutcome.Launched
        } catch (e: Exception) {
            InstallOutcome.LaunchError(e.message ?: "No se pudo abrir el instalador del sistema")
        }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
