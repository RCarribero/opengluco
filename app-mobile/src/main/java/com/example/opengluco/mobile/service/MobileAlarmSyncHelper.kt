package com.example.opengluco.mobile.service

import android.content.Context
import android.util.Log
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.model.AlarmTriggerPayload
import com.example.opengluco.core.model.GlucoseAlarm
import com.google.android.gms.wearable.Wearable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MobileAlarmSyncHelper {

    private const val TAG = "MobileAlarmSync"
    const val ALARMS_SYNC_PATH = "/opengluco_alarms_sync"
    const val ALARM_TRIGGER_PATH = "/opengluco_alarm_trigger"
    const val ALARM_DISMISS_PATH = "/opengluco_alarm_dismiss"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

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

    /**
     * Envia de forma inmediata la orden de disparo de alarma a todos los relojes Wear OS conectados.
     */
    fun sendAlarmTriggerToWear(context: Context, alarm: GlucoseAlarm, glucoseValueMgDl: Double) {
        try {
            val payload = AlarmTriggerPayload(alarm, glucoseValueMgDl)
            val jsonStr = json.encodeToString(payload)
            val bytes = jsonStr.toByteArray(Charsets.UTF_8)
            val nodeClient = Wearable.getNodeClient(context)
            val messageClient = Wearable.getMessageClient(context)

            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                if (nodes.isNotEmpty()) {
                    Log.d(TAG, "Disparando alarma en ${nodes.size} reloj(es) Wear OS conectados")
                    for (node in nodes) {
                        messageClient.sendMessage(node.id, ALARM_TRIGGER_PATH, bytes)
                            .addOnSuccessListener {
                                Log.d(TAG, "Disparo de alarma enviado a ${node.displayName} (${node.id})")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error enviando disparo de alarma a ${node.displayName}: ${e.message}")
                            }
                    }
                } else {
                    Log.d(TAG, "No hay nodos Wear OS conectados para el disparo de alarma")
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error buscando nodos para disparo de alarma: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparando disparo de alarma Wearable: ${e.message}", e)
        }
    }

    /**
     * Envia la orden de silenciar/cancelar una alarma a todos los relojes Wear OS conectados.
     */
    fun sendAlarmDismissToWear(context: Context, alarmId: String) {
        try {
            val bytes = alarmId.toByteArray(Charsets.UTF_8)
            val nodeClient = Wearable.getNodeClient(context)
            val messageClient = Wearable.getMessageClient(context)

            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                for (node in nodes) {
                    messageClient.sendMessage(node.id, ALARM_DISMISS_PATH, bytes)
                        .addOnSuccessListener {
                            Log.d(TAG, "Orden de silenciar enviada a ${node.displayName}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error enviando orden de silenciar a ${node.displayName}: ${e.message}")
                        }
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error buscando nodos para silenciar: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando silenciar alarma a Wear OS: ${e.message}", e)
        }
    }
}