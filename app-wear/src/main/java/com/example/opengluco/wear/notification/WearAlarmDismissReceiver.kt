package com.example.opengluco.wear.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.wearable.Wearable

class WearAlarmDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
        Log.d(TAG, "Silenciando alarma desde reloj: $alarmId")

        // 1. Cancelar la notificación en el reloj
        WearAlarmNotificationHelper.dismissAlarm(context, alarmId)

        // 2. Transmitir el silenciamiento al móvil vía Wearable MessageClient
        try {
            val messageClient = Wearable.getMessageClient(context)
            val nodeClient = Wearable.getNodeClient(context)
            val bytes = alarmId.toByteArray(Charsets.UTF_8)

            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                for (node in nodes) {
                    messageClient.sendMessage(node.id, "/opengluco_alarm_dismiss", bytes)
                        .addOnSuccessListener {
                            Log.d(TAG, "Silenciado enviado al móvil: ${node.displayName}")
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error transmitiendo silenciado al móvil: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "WearAlarmDismiss"
        const val ACTION_DISMISS = "com.example.opengluco.wear.ACTION_DISMISS_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun createPendingIntent(context: Context, alarmId: String): PendingIntent {
            val intent = Intent(context, WearAlarmDismissReceiver::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, alarmId.hashCode(), intent, flags)
        }
    }
}
