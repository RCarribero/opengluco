package com.example.opengluco.mobile.service

import android.content.Context
import android.util.Log
import com.example.opengluco.core.data.AlarmRepository
import com.google.android.gms.wearable.Wearable

object MobileAlarmSyncHelper {

    private const val TAG = "MobileAlarmSync"
    const val ALARMS_SYNC_PATH = "/opengluco_alarms_sync"

    fun syncAlarmsToWear(context: Context, alarmRepository: AlarmRepository) {
        syncAlarmsToWear(context, alarmRepository.exportAlarmsToJson())
    }

    fun syncAlarmsToWear(context: Context, jsonStr: String) {
        try {
            val bytes = jsonStr.toByteArray(Charsets.UTF_8)
            val nodeClient = Wearable.getNodeClient(context)
            val messageClient = Wearable.getMessageClient(context)

            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                if (nodes.isNotEmpty()) {
                    Log.d(TAG, "Sincronizando alarmas con ${nodes.size} reloj(es) Wear OS")
                    for (node in nodes) {
                        messageClient.sendMessage(node.id, ALARMS_SYNC_PATH, bytes)
                            .addOnSuccessListener {
                                Log.d(TAG, "Alarmas enviadas con exito a ${node.displayName} (${node.id})")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error enviando alarmas a ${node.displayName}: ${e.message}")
                            }
                    }
                } else {
                    Log.d(TAG, "No hay relojes Wear OS conectados en este momento")
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error buscando nodos Wear OS: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronizacion de alarmas: ${e.message}", e)
        }
    }
}