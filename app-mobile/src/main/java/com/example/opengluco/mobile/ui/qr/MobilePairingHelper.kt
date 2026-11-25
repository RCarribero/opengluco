package com.example.opengluco.mobile.ui.qr

import android.content.Context
import android.util.Log
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.model.QrPairingPayload
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object MobilePairingHelper {

    fun transferSessionToDevice(
        context: Context,
        payload: QrPairingPayload,
        email: String,
        token: String,
        userId: String,
        region: String = "eu",
        onResult: (Boolean) -> Unit
    ) {
        val sessionJson = QrAuthHelper.createSessionExchange(
            sessionId = payload.sessionId,
            email = email,
            token = token,
            userId = userId,
            region = region
        )

        CoroutineScope(Dispatchers.IO).launch {
            var delivered = false

            // 1. Canal Bluetooth Oficial Play Services Wearable
            try {
                val dataClient = Wearable.getMessageClient(context)
                val nodeClient = Wearable.getNodeClient(context)
                val nodes = Tasks.await(nodeClient.connectedNodes)
                for (node in nodes) {
                    try {
                        Tasks.await(
                            dataClient.sendMessage(
                                node.id,
                                "/opengluco_auth_sync",
                                sessionJson.toByteArray(Charsets.UTF_8)
                            )
                        )
                        delivered = true
                    } catch (_: Exception) {}
                }
                if (nodes.isNotEmpty()) {
                    val alarmRepo = com.example.opengluco.core.data.AlarmRepository(context)
                    com.example.opengluco.mobile.service.MobileAlarmSyncHelper.syncAlarmsToWear(context, alarmRepo)
                }
            } catch (e: Exception) {
                Log.w("MobilePairing", "Canal Wearable no disponible: ${e.message}")
            }

            // 2. Canal Directo por Socket Local Cifrado sobre Wi-Fi / Hotspot
            val targetIp = payload.ip
            val targetPort = payload.port ?: 8888
            if (!targetIp.isNullOrBlank()) {
                val networkOk = QrAuthHelper.sendSessionOverNetwork(
                    targetIp = targetIp,
                    targetPort = targetPort,
                    sessionJson = sessionJson,
                    secretKeyHex = payload.secretKeyHex,
                    nonceHex = payload.nonceHex,
                    sessionId = payload.sessionId
                )
                if (networkOk) {
                    delivered = true
                }
            }

            withContext(Dispatchers.Main) {
                onResult(delivered)
            }
        }
    }
}
