package com.example.opengluco.wear.service

import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.core.data.UserPreferencesRepository
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearAuthMessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/opengluco_auth_sync") {
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
    }
}
