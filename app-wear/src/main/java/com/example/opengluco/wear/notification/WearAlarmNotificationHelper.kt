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

    private const val CHANNEL_URGENT = "cgm_urgent_alarms_v6"
    private const val CHANNEL_ALERT = "cgm_alert_alarms_v6"
    private const val CHANNEL_INFO = "cgm_info_alarms_v6"
    private const val NOTIFICATION_ID_ALARM = 1001

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alarmAudioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Alarmas Urgentes de Glucosa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas criticas de hipoglucemia e hiperglucemia urgente con vibracion maxima"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 150, 800, 150, 800)
                setBypassDnd(true)
                setSound(null, alarmAudioAttributes)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERT,
                "Alertas de Glucosa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas clinicas fuera de rango con vibracion firme"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(null, alarmAudioAttributes)
            }

            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "Avisos de Glucosa",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones informativas de glucosa con vibracion suave"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250)
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

        // Haptica reforzada segun severidad
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
            AlarmSeverity.ALERT -> NotificationCompat.PRIORITY_HIGH
            AlarmSeverity.INFORMATIVE -> NotificationCompat.PRIORITY_DEFAULT
        }

        val category = when (alarm.severity) {
            AlarmSeverity.URGENT -> NotificationCompat.CATEGORY_ALARM
            AlarmSeverity.ALERT -> NotificationCompat.CATEGORY_ALARM
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

    /**
     * Permite probar la vibracion háptica directamente desde la interfaz de Ajustes del reloj.
     */
    fun testHapticVibration(context: Context, severity: AlarmSeverity = AlarmSeverity.URGENT) {
        triggerHapticAlarm(context, severity)
    }

    private fun triggerHapticAlarm(context: Context, severity: AlarmSeverity) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val wakeLock = powerManager?.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "OpenGluco:WearHapticWakeLock")
            wakeLock?.acquire(3500)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings: LongArray
                val amplitudes: IntArray

                when (severity) {
                    AlarmSeverity.URGENT -> {
                        // Patron triple maximo: 800ms activo, 150ms pausa, 800ms activo, 150ms pausa, 800ms activo
                        timings = longArrayOf(0, 800, 150, 800, 150, 800)
                        amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    }
                    AlarmSeverity.ALERT -> {
                        // Patron doble firme: 400ms activo, 200ms pausa, 400ms activo
                        timings = longArrayOf(0, 400, 200, 400)
                        amplitudes = intArrayOf(0, 215, 0, 215)
                    }
                    AlarmSeverity.INFORMATIVE -> {
                        // Pulso suave: 250ms activo
                        timings = longArrayOf(0, 250)
                        amplitudes = intArrayOf(0, 120)
                    }
                }

                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = android.os.VibrationAttributes.Builder()
                        .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                        .build()
                    vibrator.vibrate(effect, attrs)
                } else {
                    val attrs = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .build()
                    vibrator.vibrate(effect, attrs)
                }
            } else {
                @Suppress("DEPRECATION")
                val timings = when (severity) {
                    AlarmSeverity.URGENT -> longArrayOf(0, 800, 150, 800, 150, 800)
                    AlarmSeverity.ALERT -> longArrayOf(0, 400, 200, 400)
                    AlarmSeverity.INFORMATIVE -> longArrayOf(0, 250)
                }
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }
}
