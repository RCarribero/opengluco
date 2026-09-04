package com.example.opengluco.mobile.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmSoundType
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import com.example.opengluco.mobile.MainActivity
import com.example.opengluco.mobile.R

object MobileAlarmNotificationHelper {
    const val CHANNEL_URGENT = "cgm_urgent_alarms_v5"
    const val CHANNEL_ALERT = "cgm_alert_alarms_v5"
    const val CHANNEL_INFO = "cgm_info_alarms_v5"
    const val CHANNEL_LIVE_STATUS = "cgm_live_status_v5"

    const val NOTIFICATION_ID_LIVE_STATUS = 9001

    private var emergencyMediaPlayer: MediaPlayer? = null
    private var previewMediaPlayer: MediaPlayer? = null

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val urgentSoundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_urgent_extreme}")
            val alertSoundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_urgent_medical}")
            val infoSoundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_alert}")

            val urgentAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val alertAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Alarmas Urgentes (Sirena Extrema)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas criticas de hipoglucemia e hiperglucemia urgente con sirena estroboscopica de maxima intensidad"
                vibrationPattern = longArrayOf(0, 800, 150, 800, 150, 800)
                enableVibration(true)
                setSound(urgentSoundUri, urgentAudioAttributes)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERT,
                "Alarmas de Glucosa Fuera de Rango",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas sonoras de glucosa fuera de rango objetivo con tono medico urgente"
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                enableVibration(true)
                setSound(alertSoundUri, alertAudioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val infoChannel = NotificationChannel(
                CHANNEL_INFO,
                "Avisos de Informacion Sonora",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos informativos de glucosa con alerta sonora estandar"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250)
                setSound(infoSoundUri, alertAudioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val liveStatusChannel = NotificationChannel(
                CHANNEL_LIVE_STATUS,
                "Monitor de Glucosa en Tiempo Real",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra la ultima lectura en tiempo real en la barra de notificaciones"
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
     * Resuelve el recurso de audio crudo (R.raw.*) asignado a una alarma concreta.
     * Considera la configuracion individual soundType o delega en la severidad por defecto.
     */
    fun resolveSoundRawRes(alarm: GlucoseAlarm): Int? {
        return when (alarm.soundType) {
            AlarmSoundType.SILENT, AlarmSoundType.CUSTOM -> null
            AlarmSoundType.URGENT_EXTREME -> R.raw.alarm_urgent_extreme
            AlarmSoundType.URGENT_MEDICAL -> R.raw.alarm_urgent_medical
            AlarmSoundType.ALERT_STANDARD -> R.raw.alarm_alert
            AlarmSoundType.DISCRETE_CHIME -> R.raw.alarm_discrete
            AlarmSoundType.DEFAULT -> when (alarm.severity) {
                AlarmSeverity.URGENT -> R.raw.alarm_urgent_extreme
                AlarmSeverity.ALERT -> R.raw.alarm_urgent_medical
                AlarmSeverity.INFORMATIVE -> R.raw.alarm_alert
            }
        }
    }

    /**
     * Resuelve el URI final de audio (recurso nativo R.raw o archivo personalizado en almacenamiento interno).
     */
    fun resolveSoundUri(context: Context, alarm: GlucoseAlarm): Uri? {
        if (alarm.soundType == AlarmSoundType.SILENT) return null
        if (alarm.soundType == AlarmSoundType.CUSTOM && !alarm.customSoundUri.isNullOrBlank()) {
            return Uri.parse(alarm.customSoundUri)
        }
        val rawRes = resolveSoundRawRes(alarm) ?: return null
        return Uri.parse("android.resource://${context.packageName}/$rawRes")
    }

    /**
     * Resuelve el recurso sonoro segun tipo de sonido y severidad para preescucha en UI.
     */
    fun resolveSoundRawResForType(soundType: AlarmSoundType, severity: AlarmSeverity = AlarmSeverity.ALERT): Int? {
        return when (soundType) {
            AlarmSoundType.SILENT, AlarmSoundType.CUSTOM -> null
            AlarmSoundType.URGENT_EXTREME -> R.raw.alarm_urgent_extreme
            AlarmSoundType.URGENT_MEDICAL -> R.raw.alarm_urgent_medical
            AlarmSoundType.ALERT_STANDARD -> R.raw.alarm_alert
            AlarmSoundType.DISCRETE_CHIME -> R.raw.alarm_discrete
            AlarmSoundType.DEFAULT -> when (severity) {
                AlarmSeverity.URGENT -> R.raw.alarm_urgent_extreme
                AlarmSeverity.ALERT -> R.raw.alarm_urgent_medical
                AlarmSeverity.INFORMATIVE -> R.raw.alarm_alert
            }
        }
    }

    /**
     * Reproduce una vista previa sonora en la interfaz de configuracion.
     * Soporta tanto tonos del catalogo clinico como archivos personalizados del usuario.
     */
    fun previewSound(
        context: Context,
        soundType: AlarmSoundType,
        severity: AlarmSeverity = AlarmSeverity.ALERT,
        customSoundUri: String? = null
    ) {
        stopPreview()
        if (soundType == AlarmSoundType.CUSTOM && !customSoundUri.isNullOrBlank()) {
            previewCustomSound(context, Uri.parse(customSoundUri))
            return
        }
        val soundRes = resolveSoundRawResForType(soundType, severity) ?: return
        try {
            val uri = Uri.parse("android.resource://${context.packageName}/$soundRes")
            previewCustomSound(context, uri)
        } catch (_: Exception) {}
    }

    /**
     * Reproduce una vista previa de un URI de audio especifico.
     */
    fun previewCustomSound(context: Context, uri: Uri) {
        stopPreview()
        try {
            previewMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                if (uri.scheme == "file" && uri.path != null) {
                    setDataSource(uri.path!!)
                } else {
                    setDataSource(context, uri)
                }
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    if (previewMediaPlayer == it) {
                        previewMediaPlayer = null
                    }
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Detiene la reproduccion de vista previa.
     */
    fun stopPreview() {
        try {
            previewMediaPlayer?.stop()
            previewMediaPlayer?.release()
            previewMediaPlayer = null
        } catch (_: Exception) {}
    }

    /**
     * Construye la notificación persistente continua para el Foreground Service.
     */
    fun buildLiveGlucoseNotification(
        context: Context,
        glucoseValueMgDl: Double,
        trendArrow: String = "->",
        patientName: String = ""
    ): android.app.Notification {
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
        val bodyText = if (glucoseValueMgDl > 0.0) "${glucoseValueMgDl.toInt()} $trendArrow mg/dL" else "Conectando al sensor..."

        return NotificationCompat.Builder(context, CHANNEL_LIVE_STATUS)
            .setSmallIcon(R.drawable.ic_notification_glucose)
            .setContentTitle(headerText)
            .setContentText(bodyText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * Actualiza la tarjeta continua en la barra de notificaciones.
     */
    fun updateLiveGlucoseNotification(
        context: Context,
        glucoseValueMgDl: Double,
        trendArrow: String = "->",
        patientName: String = ""
    ) {
        if (glucoseValueMgDl <= 0.0) return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = buildLiveGlucoseNotification(context, glucoseValueMgDl, trendArrow, patientName)
        notificationManager.notify(NOTIFICATION_ID_LIVE_STATUS, notification)
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
            AlarmSeverity.INFORMATIVE -> NotificationCompat.PRIORITY_DEFAULT
        }

        val category = when (alarm.severity) {
            AlarmSeverity.URGENT -> NotificationCompat.CATEGORY_ALARM
            else -> NotificationCompat.CATEGORY_STATUS
        }

        val soundUri = resolveSoundUri(context, alarm)

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

        // Reproducir audio forzado por canal de alarma en casos urgentes, extrema urgencia o audio personalizado
        if (alarm.soundType != AlarmSoundType.SILENT) {
            if (alarm.severity == AlarmSeverity.URGENT || alarm.soundType == AlarmSoundType.URGENT_EXTREME || alarm.soundType == AlarmSoundType.CUSTOM) {
                if (soundUri != null) {
                    playEmergencyAlarmSoundUri(context, soundUri)
                }
            }
        }
    }

    private fun playEmergencyAlarmSoundUri(context: Context, uri: Uri) {
        try {
            stopEmergencyAlarmSound()
            emergencyMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                if (uri.scheme == "file" && uri.path != null) {
                    setDataSource(uri.path!!)
                } else {
                    setDataSource(context, uri)
                }
                prepare()
                start()
            }
        } catch (_: Exception) {}
    }

    fun stopEmergencyAlarmSound() {
        try {
            emergencyMediaPlayer?.stop()
            emergencyMediaPlayer?.release()
            emergencyMediaPlayer = null
        } catch (_: Exception) {}
    }

    fun dismissAlarm(context: Context, alarmId: String) {
        stopEmergencyAlarmSound()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId.hashCode())
    }

    /**
     * Emite una notificacion preventiva sobre la expiracion del sensor FreeStyle Libre.
     */
    fun notifySensorExpiration(context: Context, alert: com.example.opengluco.core.model.SensorExpirationAlert) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 8001, intent, flags)

        val channelId = if (alert.isCritical) CHANNEL_ALERT else CHANNEL_INFO
        val icon = if (alert.isCritical) android.R.drawable.stat_sys_warning else android.R.drawable.ic_dialog_info
        val priority = if (alert.isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(alert.title)
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(8001, notification)
    }
}
