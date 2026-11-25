package com.example.opengluco.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.opengluco.core.model.BlockMetric
import com.example.opengluco.core.model.DailyPatternsReport
import com.example.opengluco.core.model.FastDateParser
import com.example.opengluco.core.model.GlucoseEventMarker
import com.example.opengluco.core.model.GlucoseEventType
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.PeriodSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val trustedPhoneMac: String = "",
    val sensorDurationDays: Int = 0,
    val region: OpenGlucoRegion = OpenGlucoRegion.EU
)

class UserPreferencesRepository(private val context: Context) {

    val localDatabase: LocalGlucoseDatabase by lazy { LocalGlucoseDatabase.getInstance(context) }

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

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _historicalReadingsFlow = MutableStateFlow<List<GlucoseMeasurement>>(emptyList())
    private var currentLoadedPatientId: String? = null

    companion object {
        private val legacyMigrationDone = java.util.concurrent.atomic.AtomicBoolean(false)
    }

    init {
        if (legacyMigrationDone.compareAndSet(false, true)) {
            repoScope.launch {
                migrateLegacyJsonFilesAsync()
            }
        }
    }

    private fun migrateLegacyJsonFilesAsync() {
        try {
            val jsonFiles = context.filesDir.listFiles { _, name ->
                name.startsWith("glucose_history") && name.endsWith(".json")
            } ?: emptyArray()

            for (file in jsonFiles) {
                if (!file.exists()) continue
                try {
                    val rawContent = file.readText()
                    if (rawContent.isNotBlank()) {
                        val decrypted = KeystoreCryptoHelper.decrypt(rawContent)
                        val contentToParse = if (decrypted.isNotBlank()) decrypted else rawContent
                        val list = json.decodeFromString<List<GlucoseMeasurement>>(contentToParse)
                        if (list.isNotEmpty()) {
                            val patientId = if (file.name == "glucose_history.json") {
                                ""
                            } else {
                                file.name.removePrefix("glucose_history_").removeSuffix(".json")
                            }
                            localDatabase.insertReadings(list, patientId)
                        }
                    }
                } catch (_: Exception) {}
                try { file.delete() } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    suspend fun loadPatientHistory(patientId: String?) = withContext(Dispatchers.IO) {
        currentLoadedPatientId = patientId
        val readings = localDatabase.getHistoricalReadingsList(90, patientId)
        if (readings.isNotEmpty()) {
            _historicalReadingsFlow.value = readings
        } else {
            loadHistoryFromDisk(patientId)
        }
    }

    private fun loadHistoryFromDisk(patientId: String?) {
        try {
            val readings = localDatabase.getHistoricalReadingsList(90, patientId)
            if (readings.isNotEmpty()) {
                _historicalReadingsFlow.value = readings
                return
            }

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
                    if (list.isNotEmpty()) {
                        localDatabase.insertReadings(list, patientId.orEmpty())
                    }
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
        val SENSOR_DURATION_DAYS = intPreferencesKey("sensor_duration_days")
        val REGION = stringPreferencesKey("libreview_region")
    }

    @Volatile private var emailCache: Pair<String, String>? = null
    @Volatile private var tokenCache: Pair<String, String>? = null
    @Volatile private var userIdCache: Pair<String, String>? = null
    @Volatile private var macCache: Pair<String, String>? = null

    private fun getDecryptedEmail(raw: String): String {
        if (raw.isBlank()) return ""
        emailCache?.let { if (it.first == raw) return it.second }
        val dec = KeystoreCryptoHelper.decrypt(raw)
        emailCache = raw to dec
        return dec
    }

    private fun getDecryptedToken(raw: String): String {
        if (raw.isBlank()) return ""
        tokenCache?.let { if (it.first == raw) return it.second }
        val dec = KeystoreCryptoHelper.decrypt(raw)
        tokenCache = raw to dec
        return dec
    }

    private fun getDecryptedUserId(raw: String): String {
        if (raw.isBlank()) return ""
        userIdCache?.let { if (it.first == raw) return it.second }
        val dec = KeystoreCryptoHelper.decrypt(raw)
        userIdCache = raw to dec
        return dec
    }

    private fun getDecryptedMac(raw: String): String {
        if (raw.isBlank()) return ""
        macCache?.let { if (it.first == raw) return it.second }
        val dec = KeystoreCryptoHelper.decrypt(raw)
        macCache = raw to dec
        return dec
    }

    private fun clearDecryptedCache() {
        emailCache = null
        tokenCache = null
        userIdCache = null
        macCache = null
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val unitStr = preferences[PreferencesKeys.UNIT] ?: GlucoseUnit.MGDL.name
        val unit = try { GlucoseUnit.valueOf(unitStr) } catch (e: Exception) { GlucoseUnit.MGDL }

        val rawEmail = preferences[PreferencesKeys.EMAIL] ?: ""
        val rawToken = preferences[PreferencesKeys.TOKEN] ?: ""
        val rawUserId = preferences[PreferencesKeys.USER_ID] ?: ""
        val rawMac = preferences[PreferencesKeys.TRUSTED_PHONE_MAC] ?: ""
        val regionStr = preferences[PreferencesKeys.REGION] ?: OpenGlucoRegion.EU.name
        val region = try { OpenGlucoRegion.valueOf(regionStr.uppercase()) } catch (_: Exception) { OpenGlucoRegion.EU }

        UserSettings(
            email = getDecryptedEmail(rawEmail),
            token = getDecryptedToken(rawToken),
            userId = getDecryptedUserId(rawUserId),
            selectedPatientId = preferences[PreferencesKeys.SELECTED_PATIENT_ID] ?: "",
            unit = unit,
            autoLogin = preferences[PreferencesKeys.AUTO_LOGIN] ?: true,
            lowThreshold = preferences[PreferencesKeys.LOW_THRESHOLD] ?: 70,
            highThreshold = preferences[PreferencesKeys.HIGH_THRESHOLD] ?: 180,
            urgentLowThreshold = preferences[PreferencesKeys.URGENT_LOW_THRESHOLD] ?: 55,
            hapticAlertsEnabled = preferences[PreferencesKeys.HAPTIC_ALERTS] ?: true,
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true,
            trustedPhoneMac = getDecryptedMac(rawMac),
            sensorDurationDays = preferences[PreferencesKeys.SENSOR_DURATION_DAYS] ?: 0,
            region = region
        )
    }.distinctUntilChanged()

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: true
    }.distinctUntilChanged()

    val hasSessionFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val rawToken = preferences[PreferencesKeys.TOKEN] ?: ""
        if (rawToken.isBlank()) false else getDecryptedToken(rawToken).isNotBlank()
    }.distinctUntilChanged()

    suspend fun saveAuthSession(
        email: String,
        token: String,
        userId: String,
        phoneMac: String? = null,
        region: OpenGlucoRegion = OpenGlucoRegion.EU
    ) {
        clearDecryptedCache()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL] = KeystoreCryptoHelper.encrypt(email)
            preferences[PreferencesKeys.TOKEN] = KeystoreCryptoHelper.encrypt(token)
            preferences[PreferencesKeys.USER_ID] = KeystoreCryptoHelper.encrypt(userId)
            preferences[PreferencesKeys.REGION] = region.name
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

    suspend fun saveSensorDurationDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SENSOR_DURATION_DAYS] = days
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

    suspend fun clearSession(clearHistory: Boolean = false) {
        clearDecryptedCache()
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.EMAIL)
            preferences.remove(PreferencesKeys.TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.SELECTED_PATIENT_ID)
            preferences.remove(PreferencesKeys.TRUSTED_PHONE_MAC)
            preferences.remove(PreferencesKeys.LAST_GLUCOSE)
            preferences.remove(PreferencesKeys.LAST_TREND)
            preferences.remove(PreferencesKeys.LAST_TIMESTAMP)
            preferences.remove(PreferencesKeys.REGION)
        }
        if (clearHistory) {
            clearAllHistoricalData(null)
        }
    }

    suspend fun clearAllHistoricalData(patientId: String? = null) = withContext(Dispatchers.IO) {
        try {
            localDatabase.clearData(patientId)
            if (!patientId.isNullOrBlank()) {
                context.dataStore.edit { preferences -> preferences.remove(syncTimestampKey(patientId)) }
            }
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
        clearDecryptedCache()
        clearAllHistoricalData(null)
        try {
            File(context.filesDir, "custom_alarms").deleteRecursively()
            File(context.cacheDir, "exports").deleteRecursively()
            File(context.cacheDir, "updates").deleteRecursively()
        } catch (_: Exception) {}
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    // --- PERSISTENCIA Y HISTORIAL DE GLUCOSA (HASTA 90 DÍAS) ---

    private fun parseTimestamp(ts: String?): Long? {
        if (ts.isNullOrBlank()) return null
        val epoch = FastDateParser.parseEpoch(ts)
        return if (epoch > 0L) epoch else null
    }

    private fun getMeasurementTimestampMillis(m: GlucoseMeasurement): Long? {
        val ep = m.getEpochMillis()
        if (ep > 0L) return ep
        return parseTimestamp(m.timestamp) ?: parseTimestamp(m.factoryTimestamp)
    }

    private fun getMeasurementKey(m: GlucoseMeasurement): String {
        return m.timestamp?.takeIf { it.isNotBlank() }
            ?: m.factoryTimestamp?.takeIf { it.isNotBlank() }
            ?: "${m.numericValue}_${m.trendArrow}_${m.hashCode()}"
    }

    suspend fun saveHistoricalReadings(readings: List<GlucoseMeasurement>, patientId: String? = null) = withContext(Dispatchers.IO) {
        if (readings.isEmpty()) return@withContext

        val targetPatientId = patientId ?: currentLoadedPatientId.orEmpty()

        // 1. Guardar y deduplicar en base de datos SQLite embebida
        val inserted = localDatabase.insertReadings(readings, targetPatientId)
        if (inserted > 0) {
            localDatabase.purgeOldReadings(targetPatientId, 90)

            // 2. Notificar flujo local en memoria si aplica
            if (targetPatientId == (currentLoadedPatientId ?: "") || currentLoadedPatientId == null) {
                val finalList = localDatabase.getHistoricalReadingsList(90, targetPatientId)
                _historicalReadingsFlow.value = finalList
            }
        }
    }

    fun getHistoricalReadings(days: Int, patientId: String? = null): Flow<List<GlucoseMeasurement>> {
        val targetPatientId = patientId ?: currentLoadedPatientId
        return localDatabase.getReadingsFlow(targetPatientId, days)
    }

    suspend fun getHistoricalReadingsList(days: Int, patientId: String? = null): List<GlucoseMeasurement> = withContext(Dispatchers.IO) {
        val targetPatientId = patientId ?: currentLoadedPatientId
        val dbList = localDatabase.getHistoricalReadingsList(days, targetPatientId)
        if (dbList.isNotEmpty()) {
            dbList
        } else {
            val targetFile = getHistoryFileForPatient(targetPatientId)
            val fallbackFile = File(context.filesDir, "glucose_history.json")
            val fileToRead = if (targetFile.exists()) targetFile else fallbackFile
            val all = try {
                if (fileToRead.exists()) {
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
    }

    fun getPeriodSummary(periodDays: Int, patientId: String? = null): PeriodSummary {
        return localDatabase.getPeriodSummary(patientId ?: currentLoadedPatientId, periodDays)
    }

    fun getTimeBlockSummary(periodDays: Int, patientId: String? = null): List<BlockMetric> {
        return localDatabase.getTimeBlockSummary(patientId ?: currentLoadedPatientId, periodDays)
    }

    fun getDailyPatterns(periodDays: Int, patientId: String? = null): DailyPatternsReport {
        return localDatabase.getDailyPatterns(patientId ?: currentLoadedPatientId, periodDays)
    }

    fun importLibreViewCsv(csvContent: String, patientId: String? = null): Int {
        val targetId = patientId ?: currentLoadedPatientId.orEmpty()
        val count = localDatabase.importLibreViewCsv(targetId, csvContent)
        if (count > 0) {
            _historicalReadingsFlow.value = localDatabase.getHistoricalReadingsList(90, targetId)
        }
        return count
    }

    fun getAvailableDays(patientId: String? = null): Int {
        val targetId = patientId ?: currentLoadedPatientId
        val days = localDatabase.getAvailableDays(targetId)
        return if (days > 0) days else ClinicalReportsCalculator.calculateAvailableDays(_historicalReadingsFlow.value)
    }

    fun lastSuccessfulSyncFlow(patientId: String): Flow<Long> {
        val key = syncTimestampKey(patientId)
        return context.dataStore.data.map { preferences -> preferences[key] ?: 0L }
    }

    suspend fun recordSuccessfulSync(patientId: String, timestampMs: Long = System.currentTimeMillis()) {
        if (patientId.isBlank() || timestampMs <= 0L) return
        val key = syncTimestampKey(patientId)
        context.dataStore.edit { preferences -> preferences[key] = timestampMs }
    }

    fun getGlucoseEventMarkers(patientId: String?): Flow<List<GlucoseEventMarker>> =
        localDatabase.getEventMarkersFlow(patientId)

    suspend fun addGlucoseEventMarker(
        patientId: String,
        timestampMs: Long,
        type: GlucoseEventType,
        note: String? = null
    ): GlucoseEventMarker? = withContext(Dispatchers.IO) {
        localDatabase.insertEventMarker(patientId, timestampMs, type, note)
    }

    suspend fun removeGlucoseEventMarker(patientId: String, markerId: String): Boolean = withContext(Dispatchers.IO) {
        localDatabase.deleteEventMarker(patientId, markerId)
    }

    private fun syncTimestampKey(patientId: String) = longPreferencesKey(
        "last_successful_sync_${patientId.replace(Regex("[^a-zA-Z0-9_-]"), "_")}"
    )

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

