package com.example.opengluco.mobile.service

import android.util.Log
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.mobile.ui.qr.MobilePairingHelper
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MobileWearableMessageListenerService : WearableListenerService() {

    private val TAG = "MobileWearListener"

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Mensaje recibido desde Wear OS: ${messageEvent.path} de nodo ${messageEvent.sourceNodeId}")

        when (messageEvent.path) {
            "/opengluco_request_alarms_sync", "/opengluco_request_sync" -> {
                val alarmRepo = AlarmRepository(applicationContext)
                val jsonStr = alarmRepo.exportAlarmsToJson()
                val bytes = jsonStr.toByteArray(Charsets.UTF_8)
                val messageClient = Wearable.getMessageClient(applicationContext)

                messageClient.sendMessage(messageEvent.sourceNodeId, MobileAlarmSyncHelper.ALARMS_SYNC_PATH, bytes)
                    .addOnSuccessListener {
                        Log.d(TAG, "Alarmas enviadas con éxito en respuesta a ${messageEvent.sourceNodeId}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error respondiendo con alarmas: ${e.message}")
                    }
            }
        }
    }
}