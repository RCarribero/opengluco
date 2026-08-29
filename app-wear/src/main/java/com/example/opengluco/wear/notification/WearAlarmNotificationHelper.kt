package com.example.opengluco.wear.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import com.example.opengluco.wear.MainActivity
import com.example.opengluco.wear.R

/**
 * Gestor de notificaciones y alarmas hapticas para Wear OS.
 * Soporta 3 niveles de severidad: URGENT, ALERT, INFORMATIVE.
 */
object WearAlarmNotificationHelper {

    private const val CHANNEL_URGENT = "cgm_urgent_alarms"
    private const val CHANNEL_ALERT = "cgm_alert_alarms"
    private const val CHANNEL_INFO = "cgm_info_alarms"
    private const val NOTIFICATION_ID_ALARM = 1001

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Alarmas Urgentes de Glucosa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas criticas de hipoglucemia e hiperglucemia urgente"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 150, 600, 150, 600)
                setBypassDnd(true)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERT,
                "Alertas de Glucosa",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas moderadas de niveles fuera de rango"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "Avisos de Glucosa",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones informativas silenciosas de glucosa"
                enableVibration(false)
            }

            manager.createNotificationChannel(urgentChannel)
            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(infoChannel)
        }
    }

    /**
     * Dispara una alarma segun el nivel de severidad de la alarma configurada.
     */
    fun triggerAlarmBySeverity(
        context: Context,
        alarm: GlucoseAlarm,
        glucoseValueMgDl: Double
    ) {
        val channelId = when (alarm.severity) {
            AlarmSeverity.URGENT -> CHANNEL_URGENT
            AlarmSeverity.ALERT -> CHANNEL_ALERT
            AlarmSeverity.INFORMATIVE -> CHANNEL_INFO
        }

        val title = when {
            alarm.severity == AlarmSeverity.URGENT && alarm.type == AlarmType.LOW ->
                "HIPOGLUCEMIA URGENTE"
            alarm.severity == AlarmSeverity.URGENT && alarm.type == AlarmType.HIGH ->
                "HIPERGLUCEMIA URGENTE"
            alarm.type == AlarmType.LOW -> "Glucosa Baja"
            else -> "Glucosa Alta"
        }

        val contentText = "Glucosa: ${glucoseValueMgDl.toInt()} mg/dL. Umbral: ${alarm.thresholdMgDl} mg/dL."

        // Haptica segun severidad
        triggerHapticAlarm(context, alarm.severity)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (alarm.severity) {
            AlarmSeverity.URGENT -> NotificationCompat.PRIORITY_MAX
            AlarmSeverity.ALERT -> NotificationCompat.PRIORITY_DEFAULT
            AlarmSeverity.INFORMATIVE -> NotificationCompat.PRIORITY_LOW
        }

        val category = when (alarm.severity) {
            AlarmSeverity.URGENT -> NotificationCompat.CATEGORY_ALARM
            else -> NotificationCompat.CATEGORY_STATUS
        }

        val icon = when (alarm.severity) {
            AlarmSeverity.URGENT -> android.R.drawable.stat_notify_error
            AlarmSeverity.ALERT -> android.R.drawable.stat_sys_warning
            AlarmSeverity.INFORMATIVE -> android.R.drawable.ic_dialog_info
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(priority)
            .setCategory(category)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = alarm.id.hashCode().let { if (it == 0) NOTIFICATION_ID_ALARM else it }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    /**
     * Retrocompatibilidad: dispara alarma de hipoglucemia urgente con el sistema legacy.
     */
    fun triggerUrgentLowAlarm(context: Context, glucoseVal: Double) {
        triggerHapticAlarm(context, AlarmSeverity.URGENT)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("HIPOGLUCEMIA URGENTE")
            .setContentText("Glucosa en ${glucoseVal.toInt()} mg/dL. Actua de inmediato.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_ALARM, notification)
    }

    fun dismissAlarm(context: Context, alarmId: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = alarmId.hashCode().let { if (it == 0) NOTIFICATION_ID_ALARM else it }
        manager.cancel(notificationId)
    }

    private fun triggerHapticAlarm(context: Context, severity: AlarmSeverity) {
        if (severity == AlarmSeverity.INFORMATIVE) return

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = when (severity) {
                    AlarmSeverity.URGENT -> longArrayOf(0, 600, 150, 600, 150, 600)
                    AlarmSeverity.ALERT -> longArrayOf(0, 300, 200, 300)
                    else -> return
                }
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            }
        } catch (_: Exception) {}
    }
}
