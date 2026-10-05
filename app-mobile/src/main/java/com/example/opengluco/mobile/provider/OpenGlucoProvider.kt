package com.example.opengluco.mobile.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.example.opengluco.core.data.LocalGlucoseDatabase
import com.example.opengluco.core.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * ContentProvider seguro para compartir credenciales de sesion y telemetria en tiempo real
 * entre el modulo movil (app-mobile) y la pantalla del vehiculo (app-auto) en el mismo dispositivo Android.
 */
class OpenGlucoProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.opengluco.provider"
        private const val CODE_SESSION = 1
        private const val CODE_LATEST_READING = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "session", CODE_SESSION)
            addURI(AUTHORITY, "latest_reading", CODE_LATEST_READING)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val ctx = context ?: return null
        return when (uriMatcher.match(uri)) {
            CODE_SESSION -> {
                val cursor = MatrixCursor(arrayOf(
                    "token", "user_id", "selected_patient_id", "low_threshold",
                    "high_threshold", "unit", "is_dark_mode", "sensor_duration_days"
                ))
                try {
                    val prefs = UserPreferencesRepository(ctx)
                    val settings = runBlocking(Dispatchers.IO) {
                        withTimeoutOrNull(1500L) { prefs.userSettingsFlow.first() }
                    }
                    if (settings != null && settings.token.isNotBlank()) {
                        cursor.addRow(arrayOf(
                            settings.token,
                            settings.userId,
                            settings.selectedPatientId,
                            settings.lowThreshold,
                            settings.highThreshold,
                            settings.unit.name,
                            if (settings.isDarkMode) 1 else 0,
                            settings.sensorDurationDays
                        ))
                        Log.d("OpenGlucoProvider", "Providing session: token present=${settings.token.isNotBlank()}, user=${settings.userId}")
                    }
                } catch (e: Exception) {
                    Log.e("OpenGlucoProvider", "Error reading session in provider", e)
                }
                cursor
            }
            CODE_LATEST_READING -> {
                val cursor = MatrixCursor(arrayOf(
                    "value", "trend_arrow", "trend_message", "epoch_ms",
                    "display_time", "timestamp", "factory_timestamp", "glucose_units",
                    "is_high", "is_low"
                ))
                try {
                    val db = LocalGlucoseDatabase.getInstance(ctx)
                    val patientId = selectionArgs?.firstOrNull()?.trim()?.takeIf { it.isNotBlank() }
                    val latest = if (patientId != null) {
                        db.getLatestReading(patientId) ?: db.getLatestReading(null)
                    } else {
                        db.getLatestReading(null)
                    }

                    val finalReading = if (latest != null && latest.numericValue > 0.0) {
                        latest
                    } else {
                        val prefs = UserPreferencesRepository(ctx)
                        runBlocking(Dispatchers.IO) {
                            withTimeoutOrNull(500L) {
                                prefs.getHistoricalReadings(1, patientId).first().lastOrNull()
                            }
                        }
                    }

                    if (finalReading != null && finalReading.numericValue > 0.0) {
                        val epoch = finalReading.getEpochMillis()
                        val displayTime = finalReading.getDisplayTime()
                        Log.d("OpenGlucoProvider", "Providing latest reading: ${finalReading.numericValue} mg/dL, arrow=${finalReading.trendArrow}, time=$displayTime, epoch=$epoch")
                        cursor.addRow(arrayOf(
                            finalReading.numericValue,
                            finalReading.trendArrow ?: 3,
                            finalReading.trendMessage ?: "Estable",
                            epoch,
                            displayTime,
                            finalReading.timestamp.orEmpty(),
                            finalReading.factoryTimestamp.orEmpty(),
                            finalReading.glucoseUnits ?: 1,
                            if (finalReading.isHigh == true) 1 else 0,
                            if (finalReading.isLow == true) 1 else 0
                        ))
                    } else {
                        Log.w("OpenGlucoProvider", "No latest reading found for patientId=$patientId")
                    }
                } catch (e: Exception) {
                    Log.e("OpenGlucoProvider", "Error reading latest reading in provider", e)
                }
                cursor
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        CODE_SESSION -> "vnd.android.cursor.item/vnd.com.example.opengluco.session"
        CODE_LATEST_READING -> "vnd.android.cursor.item/vnd.com.example.opengluco.latest_reading"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
