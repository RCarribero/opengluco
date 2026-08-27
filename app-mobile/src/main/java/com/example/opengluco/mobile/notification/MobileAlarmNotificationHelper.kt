package com.example.opengluco.mobile.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import com.example.opengluco.mobile.MainActivity

object MobileAlarmNotificationHelper {
    private const val CHANNEL_URGENT = "cgm_urgent_alarms_v2"
    private const val CHANNEL_ALERT = "cgm_alert_alarms_v2"
    private const val CHANNEL_INFO = "cgm_info_alarms_v2"
    private const val CHANNEL_LIVE_STATUS = "cgm_live_status_v2"

    private const val NOTIFICATION_ID_LIVE_STATUS = 9001

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val urgentAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val alertAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Alarmas Urgentes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas críticas de hipoglucemia e hiperglucemia urgente con sonido de alarma"
                vibrationPattern = longArrayOf(0, 600, 150, 600, 150, 600)
                enableVibration(true)
                setSound(alarmSoundUri, urgentAudioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERT,
                "Alarmas de Glucosa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de glucosa fuera de rango objetivo con tono sonoro"
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                enableVibration(true)
                setSound(notificationSoundUri, alertAudioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "Avisos de Información",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Información del sensor y estado"
                enableVibration(false)
                setSound(null, null)
            }

            val liveStatusChannel = NotificationChannel(
                CHANNEL_LIVE_STATUS,
                "Monitor de Glucosa",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra la última lectura en tiempo real en la barra de notificaciones"
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(urgentChannel)
            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(infoChannel)
            notificationManager.createNotificationChannel(liveStatusChannel)
        }
    }

    /**
     * Actualiza la tarjeta continua en la barra de notificaciones con la lectura en tiempo real.
     */
    fun updateLiveGlucoseNotification(
        context: Context,
        glucoseValueMgDl: Double,
        trendArrow: String = "->",
        patientName: String = ""
    ) {
        if (glucoseValueMgDl <= 0.0) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)

        val headerText = if (patientName.isNotBlank()) "OpenGluco • $patientName" else "OpenGluco"
        val bodyText = "${glucoseValueMgDl.toInt()} $trendArrow mg/dL"

        val builder = NotificationCompat.Builder(context, CHANNEL_LIVE_STATUS)
            .setSmallIcon(com.example.opengluco.mobile.R.drawable.ic_notification_glucose)
            .setContentTitle(headerText)
            .setContentText(bodyText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(NOTIFICATION_ID_LIVE_STATUS, builder.build())
    }

    fun triggerAlarm(context: Context, alarm: GlucoseAlarm, glucoseValueMgDl: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = when (alarm.severity) {
            AlarmSeverity.URGENT -> CHANNEL_URGENT
            AlarmSeverity.ALERT -> CHANNEL_ALERT
            AlarmSeverity.INFORMATIVE -> CHANNEL_INFO
        }

        val title = when (alarm.severity) {
            AlarmSeverity.URGENT -> if (alarm.type == AlarmType.LOW) "HIPOGLUCEMIA URGENTE" else "HIPERGLUCEMIA URGENTE"
            AlarmSeverity.ALERT -> if (alarm.type == AlarmType.LOW) "Glucosa Baja" else "Glucosa Alta"
            AlarmSeverity.INFORMATIVE -> if (alarm.type == AlarmType.LOW) "Aviso: Glucosa Baja" else "Aviso: Glucosa Alta"
        }

        val contentText = "Glucosa actual: ${glucoseValueMgDl.toInt()} mg/dL. Umbral: ${alarm.thresholdMgDl} mg/dL."
        val notificationId = alarm.id.hashCode()

        val dismissIntent = Intent(context, AlarmDismissReceiver::class.java).apply {
            action = AlarmDismissReceiver.ACTION_DISMISS_ALARM
            putExtra(AlarmDismissReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmDismissReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }

        val dismissFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            dismissIntent,
            dismissFlags
        )

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(context, notificationId, mainIntent, dismissFlags)

        val icon = when (alarm.severity) {
            AlarmSeverity.URGENT -> android.R.drawable.stat_notify_error
            AlarmSeverity.ALERT -> android.R.drawable.stat_sys_warning
            AlarmSeverity.INFORMATIVE -> android.R.drawable.ic_dialog_info
        }

        val priority = when (alarm.severity) {
            AlarmSeverity.URGENT -> NotificationCompat.PRIORITY_MAX
            AlarmSeverity.ALERT -> NotificationCompat.PRIORITY_HIGH
            AlarmSeverity.INFORMATIVE -> NotificationCompat.PRIORITY_LOW
        }

        val category = when (alarm.severity) {
            AlarmSeverity.URGENT -> NotificationCompat.CATEGORY_ALARM
            else -> NotificationCompat.CATEGORY_STATUS
        }

        val soundUri = when (alarm.severity) {
            AlarmSeverity.URGENT -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AlarmSeverity.ALERT -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AlarmSeverity.INFORMATIVE -> null
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(priority)
            .setCategory(category)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, "Silenciar", dismissPendingIntent)

        if (soundUri != null) {
            builder.setSound(soundUri)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun dismissAlarm(context: Context, alarmId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId.hashCode())
    }
}
