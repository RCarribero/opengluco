package com.example.opengluco.mobile.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DISMISS_ALARM) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            
            if (notificationId != -1) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }
    }

    companion object {
        const val ACTION_DISMISS_ALARM = "com.example.opengluco.DISMISS_ALARM"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
