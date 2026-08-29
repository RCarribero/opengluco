package com.example.opengluco.mobile.ui.dashboard.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.opengluco.core.model.AppReleaseInfo
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import com.example.opengluco.mobile.updater.AppUpdateInstaller
import kotlinx.coroutines.launch

@Composable
fun UpdateAvailableDialog(
    releaseInfo: AppReleaseInfo,
    onDismiss: () -> Unit
) {
    val colors = ClinicalTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isDownloading, dismissOnClickOutside = !isDownloading)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
            border = BorderStroke(1.dp, colors.surfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.mint.copy(alpha = 0.15f), shape = RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Actualizacion",
                        tint = colors.mint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Nueva Version Disponible",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )

                Text(
                    text = "Version ${releaseInfo.versionName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.mint
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notas de la version (Changelog)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.background),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = releaseInfo.releaseTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = releaseInfo.releaseNotes,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = colors.urgentCrimson
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isDownloading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { (downloadProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = colors.mint,
                            trackColor = colors.surfaceBorder
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Descargando actualizacion: $downloadProgress%",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.surfaceBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                        ) {
                            Text("Mas tarde", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (releaseInfo.apkDownloadUrl.isNotBlank()) {
                                    isDownloading = true
                                    errorMessage = null
                                    scope.launch {
                                        val result = AppUpdateInstaller.downloadApk(
                                            context = context,
                                            apkUrl = releaseInfo.apkDownloadUrl,
                                            onProgress = { p -> downloadProgress = p }
                                        )
                                        isDownloading = false
                                        result.fold(
                                            onSuccess = { apkFile ->
                                                AppUpdateInstaller.installApk(context, apkFile)
                                                onDismiss()
                                            },
                                            onFailure = { err ->
                                                errorMessage = "Error al descargar: ${err.message}"
                                            }
                                        )
                                    }
                                } else if (releaseInfo.htmlUrl.isNotBlank()) {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseInfo.htmlUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(browserIntent)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.mint,
                                contentColor = if (colors.isDark) Color.Black else Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Descargar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Actualizar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
