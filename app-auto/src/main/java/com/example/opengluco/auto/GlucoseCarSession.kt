package com.example.opengluco.auto

import android.content.Intent
import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import com.example.opengluco.auto.screen.GlucoseDashboardCarScreen
import com.example.opengluco.auto.screen.QrLoginCarScreen
import com.example.opengluco.core.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class GlucoseCarSession : Session() {

    companion object {
        private const val TAG = "OpenGlucoAuto"
    }

    override fun onCreateScreen(intent: Intent): Screen {
        Log.d(TAG, "onCreateScreen: Starting Android Auto session...")
        val prefs = UserPreferencesRepository(carContext)
        var settings = runBlocking { prefs.userSettingsFlow.first() }

        // Sincronizacion transparente con la app principal del movil si no hay sesion guardada
        if (settings.token.isBlank() || settings.userId.isBlank()) {
            Log.d(TAG, "onCreateScreen: Local token missing, attempting sync from app-mobile ContentProvider...")
            runBlocking {
                withTimeoutOrNull(2000L) {
                    AutoMobileSyncHelper.syncSessionFromMobile(carContext, prefs)
                }
            }
            settings = runBlocking { prefs.userSettingsFlow.first() }
        }

        // Obtener lectura inicial instantanea desde el ContentProvider del movil
        val targetPatientId = settings.selectedPatientId.ifBlank { null }
        val initialReading = runBlocking {
            withTimeoutOrNull(2000L) {
                AutoMobileSyncHelper.getLatestReadingFromMobile(carContext, targetPatientId)
            }
        }

        Log.d(TAG, "onCreateScreen: session ready? tokenPresent=${settings.token.isNotBlank()}, initialReading=${initialReading?.numericValue} mg/dL (${initialReading?.getDisplayTime()})")

        return if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
            GlucoseDashboardCarScreen(carContext, initialReading)
        } else {
            QrLoginCarScreen(carContext)
        }
    }
}
