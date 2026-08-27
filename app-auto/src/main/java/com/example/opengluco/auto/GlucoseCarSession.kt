package com.example.opengluco.auto

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import com.example.opengluco.auto.screen.GlucoseDashboardCarScreen
import com.example.opengluco.auto.screen.QrLoginCarScreen
import com.example.opengluco.core.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GlucoseCarSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        val prefs = UserPreferencesRepository(carContext)
        val settings = runBlocking { prefs.userSettingsFlow.first() }

        return if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
            GlucoseDashboardCarScreen(carContext)
        } else {
            QrLoginCarScreen(carContext)
        }
    }
}
