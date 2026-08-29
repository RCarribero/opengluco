package com.example.opengluco.mobile.ui.qr

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.model.QrPairingPayload
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object MobilePairingHelper {

    fun transferSessionToDevice(
        context: Context,
        payload: QrPairingPayload,
        email: String,
        token: String,
        userId: String,
        onSuccess: () -> Unit
    ) {
        var phoneMac: String? = null
        try {
            @SuppressLint("MissingPermission", "HardwareIds")
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            phoneMac = btAdapter?.address?.takeIf { it != "02:00:00:00:00:00" }
        } catch (_: Exception) {}

        val sessionJson = QrAuthHelper.createSessionExchange(
            sessionId = payload.sessionId,
            email = email,
            token = token,
            userId = userId,
            phoneBluetoothMac = phoneMac
        )

        // 1. Canal Bluetooth Oficial Play Services Wearable
        try {
            val dataClient = Wearable.getMessageClient(context)
            val nodeClient = Wearable.getNodeClient(context)
            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                Log.d("MobilePairing", "Nodos Wearable detectados: ${nodes.size}")
                for (node in nodes) {
                    Log.d("MobilePairing", "Enviando auth a nodo Wearable: ${node.displayName} (${node.id})")
                    dataClient.sendMessage(node.id, "/opengluco_auth_sync", sessionJson.toByteArray(Charsets.UTF_8))
                }
                // Sincronizar también las alarmas configuradas
                val alarmRepo = com.example.opengluco.core.data.AlarmRepository(context)
                com.example.opengluco.mobile.service.MobileAlarmSyncHelper.syncAlarmsToWear(context, alarmRepo)
            }
        } catch (e: Exception) {
            Log.e("MobilePairing", "Error al enviar mensaje Wearable: ${e.message}")
        }

        // 2. Canal Directo por Socket Local Cifrado sobre Wi-Fi / Hotspot
        val targetIp = payload.ip
        val targetPort = payload.port ?: 8888
        if (!targetIp.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("MobilePairing", "Enviando auth cifrada por AES-GCM a http://$targetIp:$targetPort/auth")
                QrAuthHelper.sendSessionOverNetwork(
                    targetIp = targetIp,
                    targetPort = targetPort,
                    sessionJson = sessionJson,
                    secretKeyHex = payload.secretKeyHex,
                    nonceHex = payload.nonceHex,
                    sessionId = payload.sessionId
                )
            }
        }

        onSuccess()
    }
}
