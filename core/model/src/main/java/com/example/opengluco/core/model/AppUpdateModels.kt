package com.example.opengluco.core.model

import kotlinx.serialization.Serializable

/**
 * Informacion de una version de la aplicacion disponible en GitHub Releases.
 */
@Serializable
data class AppReleaseInfo(
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val htmlUrl: String,
    val isUpdateAvailable: Boolean,
    val publishedAt: String = ""
)
