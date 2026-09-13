package com.example.opengluco.wear.service

import android.util.Log
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.AlarmTriggerPayload
import com.example.opengluco.wear.notification.WearAlarmNotificationHelper
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class WearAuthMessageListenerService : WearableListenerService() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/opengluco_auth_sync" -> {
                val rawJson = String(messageEvent.data, Charsets.UTF_8)
                val session = QrAuthHelper.parseSessionExchange(rawJson)
                if (session != null) {
                    val prefs = UserPreferencesRepository(applicationContext)
                    CoroutineScope(Dispatchers.IO).launch {
                        prefs.saveAuthSession(
                            email = session.email,
                            token = session.token,
                            userId = session.userId,
                            phoneMac = session.phoneBluetoothMac
                        )
                        WearBluetoothRfcommService.start(applicationContext)
                    }
                }
            }
            "/opengluco_alarms_sync" -> {
                val rawJson = String(messageEvent.data, Charsets.UTF_8)
                if (rawJson.isNotBlank()) {
                    val alarmRepository = AlarmRepository(applicationContext)
                    CoroutineScope(Dispatchers.IO).launch {
                        alarmRepository.importAlarmsFromJson(rawJson)
                    }
                }
            }
            "/opengluco_alarm_trigger" -> {
                val rawJson = String(messageEvent.data, Charsets.UTF_8)
                if (rawJson.isNotBlank()) {
                    Log.i(TAG, "Disparo de alarma recibido desde el móvil: $rawJson")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val payload = json.decodeFromString<AlarmTriggerPayload>(rawJson)
                            val prefs = UserPreferencesRepository(applicationContext)
                            val settings = prefs.userSettingsFlow.first()
                            if (settings.hapticAlertsEnabled) {
                                WearAlarmNotificationHelper.triggerAlarmBySeverity(
                                    context = applicationContext,
                                    alarm = payload.alarm,
                                    glucoseValueMgDl = payload.glucoseValueMgDl
                                )
                            } else {
                                Log.i(TAG, "Alerta descartada en reloj: alertas hápticas desactivadas por el usuario")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al procesar trigger de alarma en Wear OS: ${e.message}", e)
                        }
                    }
                }
            }
            "/opengluco_alarm_dismiss" -> {
                val alarmId = String(messageEvent.data, Charsets.UTF_8)
                if (alarmId.isNotBlank()) {
                    Log.i(TAG, "Orden de silenciar recibida desde el móvil para alarma: $alarmId")
                    WearAlarmNotificationHelper.dismissAlarm(applicationContext, alarmId)
                }
            }
        }
    }

    companion object {
        private const val TAG = "WearAuthMessage"
    }
}
