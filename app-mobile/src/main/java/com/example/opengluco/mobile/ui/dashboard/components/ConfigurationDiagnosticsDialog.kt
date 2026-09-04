package com.example.opengluco.mobile.ui.dashboard.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.opengluco.mobile.ui.theme.ClinicalTheme

data class SystemDiagnosticsState(
    val isBatteryIgnored: Boolean,
    val hasNotificationPermission: Boolean,
    val canScheduleExactAlarms: Boolean
) {
    val hasAnyIssue: Boolean
        get() = !isBatteryIgnored || !hasNotificationPermission || !canScheduleExactAlarms
}

object SystemDiagnosticsHelper {

    fun checkDiagnostics(context: Context): SystemDiagnosticsState {
        val isBatteryIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else {
            true
        }

        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            am?.canScheduleExactAlarms() == true
        } else {
            true
        }

        return SystemDiagnosticsState(
            isBatteryIgnored = isBatteryIgnored,
            hasNotificationPermission = hasNotificationPermission,
            canScheduleExactAlarms = canScheduleExactAlarms
        )
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (_: Exception) {
                    openAppSettings(context)
                }
            }
        }
    }

    fun openNotificationSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                openAppSettings(context)
            }
        } else {
            openAppSettings(context)
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                openAppSettings(context)
            }
        }
    }

    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

/**
 * Pop-Up Emergente que se abre automáticamente al iniciar la app si hay configuraciones pendientes.
 */
@Composable
fun ConfigurationDiagnosticsDialog(
    diagnostics: SystemDiagnosticsState,
    onDismiss: () -> Unit,
    onRefreshState: () -> Unit
) {
    val colors = ClinicalTheme.colors
    val context = LocalContext.current
    val responsive = ClinicalTheme.responsive

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                border = BorderStroke(1.dp, colors.surfaceBorder),
                modifier = Modifier
                    .widthIn(max = responsive.dialogMaxWidth)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(if (responsive.isNarrowPhone) 16.dp else 20.dp)
                ) {
                    // Cabecera
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = colors.highAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Diagnóstico de Permisos del Sistema",
                                fontSize = responsive.clampedSp(15f, maxScale = 1.20f),
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Para garantizar la captura continua de lecturas del sensor y la activación de alertas de glucosa con la pantalla apagada, verifica los siguientes permisos de Android:",
                        fontSize = responsive.clampedSp(12.5f, maxScale = 1.25f),
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Elemento: Optimización de Batería
                    DiagnosticItemCard(
                        isCorrect = diagnostics.isBatteryIgnored,
                        icon = Icons.Default.Power,
                        title = "Optimización de Batería",
                        description = if (diagnostics.isBatteryIgnored) "Exclusión activa. El servicio continuará operando en reposo." else "El sistema operativo podría suspender la recepción de lecturas con pantalla apagada.",
                        actionButtonText = "Desactivar Ahorro",
                        onActionClick = {
                            SystemDiagnosticsHelper.requestIgnoreBatteryOptimization(context)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Elemento: Permiso de Notificaciones
                    DiagnosticItemCard(
                        isCorrect = diagnostics.hasNotificationPermission,
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones del Sistema",
                        description = if (diagnostics.hasNotificationPermission) "Autorizadas para telemetría en tiempo real y alertas clínicas." else "Necesario para mostrar la glucosa actual y emitir alertas acústicas.",
                        actionButtonText = "Permitir",
                        onActionClick = {
                            SystemDiagnosticsHelper.openNotificationSettings(context)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Elemento: Alarmas Exactas
                    DiagnosticItemCard(
                        isCorrect = diagnostics.canScheduleExactAlarms,
                        icon = Icons.Default.Tune,
                        title = "Alarmas de Alta Precisión",
                        description = if (diagnostics.canScheduleExactAlarms) "Programación exacta habilitada. Comprobaciones continuas sin retardo." else "Necesario para evaluar eventos clínicos de hipo e hiperglucemia de forma inmediata.",
                        actionButtonText = "Habilitar",
                        onActionClick = {
                            SystemDiagnosticsHelper.openExactAlarmSettings(context)
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Botones de acción inferior
                    if (responsive.isExtraLargeFont) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onRefreshState()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.mint,
                                    contentColor = if (colors.isDark) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 46.dp)
                            ) {
                                Text("Finalizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { SystemDiagnosticsHelper.openAppSettings(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surfaceBorder,
                                    contentColor = colors.textPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 46.dp)
                            ) {
                                Text("Ajustes del Sistema", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { SystemDiagnosticsHelper.openAppSettings(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surfaceBorder,
                                    contentColor = colors.textPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 46.dp)
                            ) {
                                Text("Ajustes del Sistema", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    onRefreshState()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.mint,
                                    contentColor = if (colors.isDark) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .heightIn(min = 46.dp)
                            ) {
                                Text("Finalizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItemCard(
    isCorrect: Boolean,
    icon: ImageVector,
    title: String,
    description: String,
    actionButtonText: String,
    onActionClick: () -> Unit
) {
    val colors = ClinicalTheme.colors

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) colors.surfaceCard else colors.highAmber.copy(alpha = 0.08f)
        ),
        border = BorderStroke(
            1.dp,
            if (isCorrect) colors.surfaceBorder else colors.highAmber.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isCorrect) colors.mint.copy(alpha = 0.20f) else colors.highAmber.copy(alpha = 0.20f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isCorrect) colors.mint else colors.highAmber,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp
                )
            }

            if (!isCorrect) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.highAmber,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(actionButtonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
