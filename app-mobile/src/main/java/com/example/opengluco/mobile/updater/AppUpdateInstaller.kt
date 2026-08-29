package com.example.opengluco.mobile.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.opengluco.core.data.AppUpdateRepository
import java.io.File

/**
 * Gestor de descarga e instalacion de actualizaciones de OpenGluco.
 * Realiza la descarga en la cache local de la app y abre el instalador oficial de Android via FileProvider.
 */
object AppUpdateInstaller {

    private val repository = AppUpdateRepository()

    /**
     * Descarga el archivo APK de GitHub Releases en el directorio de cache privado.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit = {}
    ): Result<File> {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val destinationFile = File(updatesDir, "opengluco_update.apk")
        return repository.downloadApk(apkUrl, destinationFile, onProgress)
    }

    /**
     * Inicia el instalador de paquetes nativo de Android con el APK descargado.
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        // En Android 8.0+ (Oreo), verificar si la app tiene permiso para instalar paquetes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }
        }

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
    }
}
