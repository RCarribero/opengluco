package com.example.opengluco.mobile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.opengluco.core.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receiver para iniciar automaticamente el servicio de monitorizacion continua de glucosa
 * tras el reinicio del telefono.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val userPrefs = UserPreferencesRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val settings = userPrefs.userSettingsFlow.first()
                if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
                    GlucoseMonitorForegroundService.startService(context)
                }
            }
        }
    }
}
