package com.example.opengluco.auto

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.GlucoseMeasurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utilidad de sincronizacion local de alta velocidad entre OpenGluco Mobile y OpenGluco Auto en el mismo telefono.
 * Permite que Android Auto disponga de la sesion autenticada y de la ultima lectura clinica de forma instantanea (< 1 ms).
 */
object AutoMobileSyncHelper {

    private const val TAG = "OpenGlucoAuto"
    private const val PROVIDER_AUTHORITY = "com.example.opengluco.provider"
    private val SESSION_URI = Uri.parse("content://$PROVIDER_AUTHORITY/session")
    private val LATEST_READING_URI = Uri.parse("content://$PROVIDER_AUTHORITY/latest_reading")

    suspend fun syncSessionFromMobile(context: Context, prefsRepo: UserPreferencesRepository): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "syncSessionFromMobile: Querying session from $SESSION_URI")
            val cursor = context.contentResolver.query(SESSION_URI, null, null, null, null)
            if (cursor == null) {
                Log.w(TAG, "syncSessionFromMobile: ContentResolver returned null cursor")
                return@withContext false
            }
            cursor.use {
                if (it.moveToFirst()) {
                    val tokenIdx = it.getColumnIndex("token")
                    val userIdIdx = it.getColumnIndex("user_id")
                    val patientIdIdx = it.getColumnIndex("selected_patient_id")
                    val lowIdx = it.getColumnIndex("low_threshold")
                    val highIdx = it.getColumnIndex("high_threshold")
                    val unitIdx = it.getColumnIndex("unit")

                    val token = if (tokenIdx >= 0) it.getString(tokenIdx) else null
                    val userId = if (userIdIdx >= 0) it.getString(userIdIdx) else null
                    val patientId = if (patientIdIdx >= 0) it.getString(patientIdIdx) else null
                    val low = if (lowIdx >= 0) it.getInt(lowIdx) else 70
                    val high = if (highIdx >= 0) it.getInt(highIdx) else 180
                    val unitStr = if (unitIdx >= 0) it.getString(unitIdx) else "MGDL"

                    if (!token.isNullOrBlank() && !userId.isNullOrBlank()) {
                        Log.d(TAG, "syncSessionFromMobile: Found valid session for user=$userId, patient=$patientId")
                        prefsRepo.saveAuthSession(
                            email = "auto_synced@opengluco.local",
                            token = token,
                            userId = userId
                        )
                        if (!patientId.isNullOrBlank()) {
                            prefsRepo.saveSelectedPatientId(patientId)
                        }
                        prefsRepo.setTargetRange(low, high)
                        val unit = if (unitStr == "MMOL") GlucoseUnit.MMOL else GlucoseUnit.MGDL
                        prefsRepo.setUnit(unit)
                        Log.d(TAG, "syncSessionFromMobile: Session successfully synced and saved into Auto preferences")
                        return@withContext true
                    } else {
                        Log.w(TAG, "syncSessionFromMobile: Session columns present but token or userId is blank")
                    }
                } else {
                    Log.w(TAG, "syncSessionFromMobile: Session cursor was empty (no rows)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncSessionFromMobile: Exception querying session provider", e)
        }
        return@withContext false
    }

    suspend fun getLatestReadingFromMobile(context: Context, patientId: String? = null): GlucoseMeasurement? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getLatestReadingFromMobile: Querying latest reading from $LATEST_READING_URI (patientId=$patientId)")
            val args = if (!patientId.isNullOrBlank()) arrayOf(patientId) else null
            val cursor = context.contentResolver.query(LATEST_READING_URI, null, null, args, null)
            if (cursor == null) {
                Log.w(TAG, "getLatestReadingFromMobile: ContentResolver returned null cursor")
                return@withContext null
            }
            cursor.use {
                if (it.moveToFirst()) {
                    val valIdx = it.getColumnIndex("value")
                    val arrowIdx = it.getColumnIndex("trend_arrow")
                    val msgIdx = it.getColumnIndex("trend_message")
                    val epochIdx = it.getColumnIndex("epoch_ms")
                    val displayTimeIdx = it.getColumnIndex("display_time")
                    val tsIdx = it.getColumnIndex("timestamp")
                    val ftsIdx = it.getColumnIndex("factory_timestamp")
                    val unitsIdx = it.getColumnIndex("glucose_units")
                    val highIdx = it.getColumnIndex("is_high")
                    val lowIdx = it.getColumnIndex("is_low")

                    val value = if (valIdx >= 0) it.getDouble(valIdx) else 0.0
                    val epochMs = if (epochIdx >= 0) it.getLong(epochIdx) else 0L
                    val displayTime = if (displayTimeIdx >= 0) it.getString(displayTimeIdx) else null

                    if (value > 0.0) {
                        val measurement = GlucoseMeasurement(
                            value = value,
                            valueInMgPerDl = value,
                            trendArrow = if (arrowIdx >= 0) it.getInt(arrowIdx) else 3,
                            trendMessage = if (msgIdx >= 0) it.getString(msgIdx) else "Estable",
                            timestamp = if (tsIdx >= 0) it.getString(tsIdx) else null,
                            factoryTimestamp = if (ftsIdx >= 0) it.getString(ftsIdx) else null,
                            glucoseUnits = if (unitsIdx >= 0) it.getInt(unitsIdx) else 1,
                            isHigh = if (highIdx >= 0) it.getInt(highIdx) == 1 else false,
                            isLow = if (lowIdx >= 0) it.getInt(lowIdx) == 1 else false
                        )
                        if (epochMs > 0L) {
                            measurement.setCachedTime(epochMs, displayTime)
                        }
                        Log.d(TAG, "getLatestReadingFromMobile: SUCCESS! Value=$value, Arrow=${measurement.trendArrow}, DisplayTime=${measurement.getDisplayTime()}, Epoch=${measurement.getEpochMillis()}, isStale=${measurement.isStale()}")
                        return@withContext measurement
                    } else {
                        Log.w(TAG, "getLatestReadingFromMobile: Reading found but value <= 0.0 ($value)")
                    }
                } else {
                    Log.w(TAG, "getLatestReadingFromMobile: Cursor was empty (0 rows)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLatestReadingFromMobile: Exception querying latest reading", e)
        }
        return@withContext null
    }
}
