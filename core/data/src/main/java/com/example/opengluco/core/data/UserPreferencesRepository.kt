package com.example.opengluco.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.opengluco.core.model.GlucoseMeasurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "opengluco_cgm_prefs")

enum class GlucoseUnit(val label: String) {
    MGDL("mg/dL"),
    MMOL("mmol/L")
}

data class UserSettings(
    val email: String = "",
    val token: String = "",
    val userId: String = "",
    val selectedPatientId: String = "",
    val unit: GlucoseUnit = GlucoseUnit.MGDL,
    val autoLogin: Boolean = true,
    val lowThreshold: Int = 70,
    val highThreshold: Int = 180,
    val urgentLowThreshold: Int = 55,
    val hapticAlertsEnabled: Boolean = true,
    val isDarkMode: Boolean = true,
    val trustedPhoneMac: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private fun getHistoryFileForPatient(patientId: String?): File {
        return if (!patientId.isNullOrBlank()) {
            val safeId = patientId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            File(context.filesDir, "glucose_history_$safeId.json")
        } else {
            File(context.filesDir, "glucose_history.json")
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _historicalReadingsFlow = MutableStateFlow<List<GlucoseMeasurement>>(emptyList())
    private var currentLoadedPatientId: String? = null

    init {
        loadHistoryFromDisk(null)
    }

    suspend fun loadPatientHistory(patientId: String?) = withContext(Dispatchers.IO) {
        currentLoadedPatientId = patientId
        loadHistoryFromDisk(patientId)
    }

    private fun loadHistoryFromDisk(patientId: String?) {
        try {
            val targetFile = getHistoryFileForPatient(patientId)
            val fallbackFile = File(context.filesDir, "glucose_history.json")
            val fileToRead = if (targetFile.exists()) targetFile else fallbackFile

            if (fileToRead.exists()) {
                val rawContent = fileToRead.readText()
                if (rawContent.isNotBlank()) {
                    val decrypted = KeystoreCryptoHelper.decrypt(rawContent)
                    val contentToParse = if (decrypted.isNotBlank()) decrypted else rawContent
                    val list = json.decodeFromString<List<GlucoseMeasurement>>(contentToParse)
                    _historicalReadingsFlow.value = list
                    return
                }
            }
            _historicalReadingsFlow.value = emptyList()
        } catch (_: Exception) {
            _historicalReadingsFlow.value = emptyList()
        }
    }

    private object PreferencesKeys {
        val EMAIL = stringPreferencesKey("email")
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val SELECTED_PATIENT_ID = stringPreferencesKey("selected_patient_id")
        val UNIT = stringPreferencesKey("glucose_unit")
        val AUTO_LOGIN = booleanPreferencesKey("auto_login")
        val LOW_THRESHOLD = intPreferencesKey("low_threshold")
        val HIGH_THRESHOLD = intPreferencesKey("high_threshold")
        val URGENT_LOW_THRESHOLD = intPreferencesKey("urgent_low_threshold")
        val HAPTIC_ALERTS = booleanPreferencesKey("haptic_alerts")
        val LAST_GLUCOSE = doublePreferencesKey("last_glucose_val")
        val LAST_TREND = intPreferencesKey("last_trend_arrow")
        val LAST_TIMESTAMP = stringPreferencesKey("last_timestamp")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val TRUSTED_PHONE_MAC = stringPreferencesKey("trusted_phone_mac")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val unitStr = preferences[PreferencesKeys.UNIT] ?: GlucoseUnit.MGDL.name
        val unit = try { GlucoseUnit.valueOf(unitStr) } catch (e: Exception) { GlucoseUnit.MGDL }

        val rawEmail = preferences[PreferencesKeys.EMAIL] ?: ""
        val rawToken = preferences[PreferencesKeys.TOKEN] ?: ""
        val rawUserId = preferences[PreferencesKeys.USER_ID] ?: ""
        val rawMac = preferences[PreferencesKeys.TRUSTED_PHONE_MAC] ?: ""

        UserSettings(
            email = KeystoreCryptoHelper.decrypt(rawEmail),
            token = KeystoreCryptoHelper.decrypt(rawToken),
            userId = KeystoreCryptoHelper.decrypt(rawUserId),
            selectedPatientId = preferences[PreferencesKeys.SELECTED_PATIENT_ID] ?: "",
            unit = unit,
            autoLogin = preferences[PreferencesKeys.AUTO_LOGIN] ?: true,
            lowThreshold = preferences[PreferencesKeys.LOW_THRESHOLD] ?: 70,
            highThreshold = preferences[PreferencesKeys.HIGH_THRESHOLD] ?: 180,
            urgentLowThreshold = preferences[PreferencesKeys.URGENT_LOW_THRESHOLD] ?: 55,
            hapticAlertsEnabled = preferences[PreferencesKeys.HAPTIC_ALERTS] ?: true,
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true,
            trustedPhoneMac = KeystoreCryptoHelper.decrypt(rawMac)
        )
    }

    suspend fun saveAuthSession(email: String, token: String, userId: String, phoneMac: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL] = KeystoreCryptoHelper.encrypt(email)
            preferences[PreferencesKeys.TOKEN] = KeystoreCryptoHelper.encrypt(token)
            preferences[PreferencesKeys.USER_ID] = KeystoreCryptoHelper.encrypt(userId)
            if (!phoneMac.isNullOrBlank()) {
                preferences[PreferencesKeys.TRUSTED_PHONE_MAC] = KeystoreCryptoHelper.encrypt(phoneMac.trim().uppercase())
            }
        }
    }

    suspend fun saveTrustedPhoneMac(macAddress: String) {
        if (macAddress.isBlank()) return
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRUSTED_PHONE_MAC] = KeystoreCryptoHelper.encrypt(macAddress.trim().uppercase())
        }
    }

    suspend fun getTrustedPhoneMac(): String? = withContext(Dispatchers.IO) {
        val prefs = context.dataStore.data.first()
        val raw = prefs[PreferencesKeys.TRUSTED_PHONE_MAC]
        if (raw.isNullOrBlank()) null else KeystoreCryptoHelper.decrypt(raw).takeIf { it.isNotBlank() }
    }

    suspend fun isTrustedPhoneMac(incomingMac: String): Boolean = withContext(Dispatchers.IO) {
        if (incomingMac.isBlank()) return@withContext false
        val trusted = getTrustedPhoneMac() ?: return@withContext false
        val normalizedIncoming = incomingMac.trim().replace("-", ":").uppercase()
        val normalizedTrusted = trusted.trim().replace("-", ":").uppercase()
        normalizedIncoming == normalizedTrusted
    }

    suspend fun saveSelectedPatientId(patientId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PATIENT_ID] = patientId
        }
    }

    suspend fun saveLastMeasurement(value: Double, trend: Int, timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_GLUCOSE] = value
            preferences[PreferencesKeys.LAST_TREND] = trend
            preferences[PreferencesKeys.LAST_TIMESTAMP] = timestamp
        }
    }

    suspend fun setUnit(unit: GlucoseUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNIT] = unit.name
        }
    }

    suspend fun setHapticAlerts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_ALERTS] = enabled
        }
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun setTargetRange(low: Int, high: Int, urgentLow: Int = 55) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOW_THRESHOLD] = low
            preferences[PreferencesKeys.HIGH_THRESHOLD] = high
            preferences[PreferencesKeys.URGENT_LOW_THRESHOLD] = urgentLow
        }
    }

    suspend fun setSelectedPatient(patientId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PATIENT_ID] = patientId
        }
        loadPatientHistory(patientId)
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.SELECTED_PATIENT_ID)
            preferences.remove(PreferencesKeys.TRUSTED_PHONE_MAC)
        }
    }

    suspend fun clearAllHistoricalData(patientId: String? = null) = withContext(Dispatchers.IO) {
        try {
            if (!patientId.isNullOrBlank()) {
                val targetFile = getHistoryFileForPatient(patientId)
                if (targetFile.exists()) {
                    targetFile.delete()
                }
            } else {
                context.filesDir.listFiles { file ->
                    file.name.startsWith("glucose_history") && file.name.endsWith(".json")
                }?.forEach { it.delete() }
            }
        } catch (_: Exception) {}
        _historicalReadingsFlow.value = emptyList()
    }

    suspend fun purgeAllLocalData() = withContext(Dispatchers.IO) {
        clearAllHistoricalData(null)
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    // --- PERSISTENCIA Y HISTORIAL DE GLUCOSA (HASTA 90 DÍAS) ---

    private fun parseTimestamp(ts: String?): Long? {
        if (ts.isNullOrBlank()) return null
        ts.toLongOrNull()?.let { return if (it < 10_000_000_000L) it * 1000 else it }

        val patterns = listOf(
            "M/d/yyyy h:mm:ss a",
            "M/d/yyyy H:mm:ss",
            "M/d/yyyy h:mm a",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                if (pattern.endsWith("'Z'")) {
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = sdf.parse(ts)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun getMeasurementTimestampMillis(m: GlucoseMeasurement): Long? {
        return parseTimestamp(m.timestamp) ?: parseTimestamp(m.factoryTimestamp)
    }

    private fun getMeasurementKey(m: GlucoseMeasurement): String {
        return m.timestamp?.takeIf { it.isNotBlank() }
            ?: m.factoryTimestamp?.takeIf { it.isNotBlank() }
            ?: "${m.numericValue}_${m.trendArrow}_${m.hashCode()}"
    }

    suspend fun saveHistoricalReadings(readings: List<GlucoseMeasurement>, patientId: String? = null) = withContext(Dispatchers.IO) {
        if (readings.isEmpty()) return@withContext

        val targetPatientId = patientId ?: currentLoadedPatientId
        val targetFile = getHistoryFileForPatient(targetPatientId)

        val existing = try {
            if (targetFile.exists()) {
                val raw = targetFile.readText()
                val decrypted = KeystoreCryptoHelper.decrypt(raw)
                val toParse = if (decrypted.isNotBlank()) decrypted else raw
                if (toParse.isNotBlank()) json.decodeFromString<List<GlucoseMeasurement>>(toParse) else emptyList()
            } else if (_historicalReadingsFlow.value.isNotEmpty() && targetPatientId == currentLoadedPatientId) {
                _historicalReadingsFlow.value
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        // Deduplicación preservando claves
        val map = LinkedHashMap<String, GlucoseMeasurement>()
        for (item in existing) {
            map[getMeasurementKey(item)] = item
        }
        for (item in readings) {
            map[getMeasurementKey(item)] = item
        }

        val merged = map.values.toList()

        // Límite de retención de 90 días
        val now = System.currentTimeMillis()
        val ninetyDaysAgo = now - (90L * 24 * 3600 * 1000)

        val filtered = merged.filter { item ->
            val ts = getMeasurementTimestampMillis(item)
            if (ts != null) {
                ts >= ninetyDaysAgo
            } else {
                true
            }
        }.sortedBy { item ->
            getMeasurementTimestampMillis(item) ?: 0L
        }

        val finalList = if (filtered.size > 20000) filtered.takeLast(20000) else filtered

        try {
            val jsonStr = json.encodeToString(finalList)
            val encryptedPayload = KeystoreCryptoHelper.encrypt(jsonStr)
            targetFile.writeText(encryptedPayload)
        } catch (_: Exception) {}

        if (targetPatientId == currentLoadedPatientId || currentLoadedPatientId == null) {
            _historicalReadingsFlow.value = finalList
        }
    }

    fun getHistoricalReadings(days: Int, patientId: String? = null): Flow<List<GlucoseMeasurement>> {
        return _historicalReadingsFlow.map { allReadings ->
            filterReadingsByDays(allReadings, days)
        }
    }

    suspend fun getHistoricalReadingsList(days: Int, patientId: String? = null): List<GlucoseMeasurement> = withContext(Dispatchers.IO) {
        val targetFile = getHistoryFileForPatient(patientId ?: currentLoadedPatientId)
        val fallbackFile = File(context.filesDir, "glucose_history.json")
        val anyHistoryFile = context.filesDir.listFiles { _, name -> name.startsWith("glucose_history") && name.endsWith(".json") }?.firstOrNull()
        val fileToRead = when {
            targetFile.exists() -> targetFile
            fallbackFile.exists() -> fallbackFile
            anyHistoryFile != null -> anyHistoryFile
            else -> null
        }
        val all = try {
            if (fileToRead != null && fileToRead.exists()) {
                val text = fileToRead.readText()
                val decrypted = KeystoreCryptoHelper.decrypt(text)
                val toParse = if (decrypted.isNotBlank()) decrypted else text
                if (toParse.isNotBlank()) json.decodeFromString<List<GlucoseMeasurement>>(toParse) else emptyList()
            } else _historicalReadingsFlow.value
        } catch (_: Exception) {
            _historicalReadingsFlow.value
        }
        filterReadingsByDays(all, days)
    }

    private fun filterReadingsByDays(allReadings: List<GlucoseMeasurement>, days: Int): List<GlucoseMeasurement> {
        if (allReadings.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()
        val cutoff = now - (days.toLong() * 24 * 3600 * 1000)

        val filtered = allReadings.filter { item ->
            val ts = getMeasurementTimestampMillis(item)
            if (ts != null) {
                ts >= cutoff
            } else {
                true
            }
        }
        return if (filtered.isNotEmpty()) filtered else allReadings
    }
}
