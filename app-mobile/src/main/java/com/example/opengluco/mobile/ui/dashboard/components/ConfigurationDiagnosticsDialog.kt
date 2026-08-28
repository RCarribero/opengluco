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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
            border = BorderStroke(1.dp, colors.surfaceBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.highAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ajustes de Telemetría Requeridos",
                            fontSize = 15.sp,
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
                    text = "Para que OpenGluco no se cierre en segundo plano y las alarmas de glucosa suenen con la pantalla apagada, configura los siguientes permisos:",
                    fontSize = 12.5.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Elemento: Optimización de Batería
                DiagnosticItemCard(
                    isCorrect = diagnostics.isBatteryIgnored,
                    icon = Icons.Default.Power,
                    title = "Ahorro de Batería (Sin Restricciones)",
                    description = if (diagnostics.isBatteryIgnored) "Configurado correctamente. No se suspenderá." else "Samsung suspenderá la app con pantalla apagada.",
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
                    title = "Permiso de Notificaciones",
                    description = if (diagnostics.hasNotificationPermission) "Activadas para glucosa en vivo y alertas." else "Las alertas de glucosa no podrán mostrarse.",
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
                    title = "Alarmas Clínicas Exactas",
                    description = if (diagnostics.canScheduleExactAlarms) "Disparo exacto en milisegundos activo." else "Permite disparar alarmas críticas al instante.",
                    actionButtonText = "Habilitar",
                    onActionClick = {
                        SystemDiagnosticsHelper.openExactAlarmSettings(context)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Botones de acción inferior
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ajustes App", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Continuar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
