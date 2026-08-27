package com.example.opengluco.core.data

import android.content.Context
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Repositorio para operaciones CRUD de alarmas de glucosa.
 * Las alarmas se persisten en un archivo JSON cifrado con AES-256-GCM
 * via KeystoreCryptoHelper. Los timestamps de ultimo disparo se almacenan
 * en un archivo separado para gestionar cooldowns.
 */
class AlarmRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val alarmsFile: File
        get() = File(context.filesDir, "glucose_alarms.json")

    private val cooldownFile: File
        get() = File(context.filesDir, "alarm_cooldowns.json")

    companion object {
        private val _alarmsFlow = MutableStateFlow<List<GlucoseAlarm>>(emptyList())
        private val _lastFiredTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())
        private var isInitialized = false
    }

    val alarmsFlow: Flow<List<GlucoseAlarm>> = _alarmsFlow.asStateFlow()

    init {
        if (!isInitialized || _alarmsFlow.value.isEmpty()) {
            loadFromDisk()
            loadCooldowns()
            isInitialized = true
        }
    }

    // --- CRUD ---

    suspend fun addAlarm(alarm: GlucoseAlarm): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _alarmsFlow.value
        val countForType = current.count { it.type == alarm.type }
        if (countForType >= GlucoseAlarm.MAX_ALARMS_PER_TYPE) {
            return@withContext Result.failure(
                IllegalStateException(
                    "Limite alcanzado: maximo ${GlucoseAlarm.MAX_ALARMS_PER_TYPE} alarmas de tipo ${alarm.type.name}"
                )
            )
        }
        if (alarm.thresholdMgDl < GlucoseAlarm.MIN_THRESHOLD ||
            alarm.thresholdMgDl > GlucoseAlarm.MAX_THRESHOLD
        ) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Umbral fuera de rango: ${alarm.thresholdMgDl} mg/dL (valido: ${GlucoseAlarm.MIN_THRESHOLD}-${GlucoseAlarm.MAX_THRESHOLD})"
                )
            )
        }
        val updated = current + alarm
        saveToDisk(updated)
        _alarmsFlow.value = updated
        Result.success(Unit)
    }

    suspend fun updateAlarm(alarm: GlucoseAlarm): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _alarmsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == alarm.id }
        if (index == -1) {
            return@withContext Result.failure(
                NoSuchElementException("Alarma con ID ${alarm.id} no encontrada")
            )
        }
        current[index] = alarm
        saveToDisk(current)
        _alarmsFlow.value = current
        Result.success(Unit)
    }

    suspend fun deleteAlarm(alarmId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _alarmsFlow.value
        val filtered = current.filter { it.id != alarmId }
        if (filtered.size == current.size) {
            return@withContext Result.failure(
                NoSuchElementException("Alarma con ID $alarmId no encontrada")
            )
        }
        saveToDisk(filtered)
        _alarmsFlow.value = filtered
        // Limpiar cooldown asociado
        val cooldowns = _lastFiredTimestamps.value.toMutableMap()
        cooldowns.remove(alarmId)
        saveCooldowns(cooldowns)
        _lastFiredTimestamps.value = cooldowns
        Result.success(Unit)
    }

    suspend fun toggleAlarm(alarmId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _alarmsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == alarmId }
        if (index == -1) {
            return@withContext Result.failure(
                NoSuchElementException("Alarma con ID $alarmId no encontrada")
            )
        }
        current[index] = current[index].copy(enabled = !current[index].enabled)
        saveToDisk(current)
        _alarmsFlow.value = current
        Result.success(Unit)
    }

    fun getAllAlarms(): List<GlucoseAlarm> = _alarmsFlow.value

    fun getAlarmsByType(type: AlarmType): List<GlucoseAlarm> =
        _alarmsFlow.value.filter { it.type == type }

    // --- Cooldown Management ---

    fun getLastFiredTimestamps(): Map<String, Long> = _lastFiredTimestamps.value

    suspend fun recordAlarmFired(alarmId: String) = withContext(Dispatchers.IO) {
        val cooldowns = _lastFiredTimestamps.value.toMutableMap()
        cooldowns[alarmId] = System.currentTimeMillis()
        saveCooldowns(cooldowns)
        _lastFiredTimestamps.value = cooldowns
    }

    suspend fun clearCooldownForAlarm(alarmId: String) = withContext(Dispatchers.IO) {
        val cooldowns = _lastFiredTimestamps.value.toMutableMap()
        cooldowns.remove(alarmId)
        saveCooldowns(cooldowns)
        _lastFiredTimestamps.value = cooldowns
    }

    suspend fun clearAllCooldowns() = withContext(Dispatchers.IO) {
        saveCooldowns(emptyMap())
        _lastFiredTimestamps.value = emptyMap()
    }

    fun getDefaultAlarms(): List<GlucoseAlarm> {
        return listOf(
            GlucoseAlarm(
                id = "default_urgent_low",
                type = AlarmType.LOW,
                thresholdMgDl = 55,
                severity = com.example.opengluco.core.model.AlarmSeverity.URGENT,
                enabled = true,
                cooldownMinutes = 15
            ),
            GlucoseAlarm(
                id = "default_low",
                type = AlarmType.LOW,
                thresholdMgDl = 70,
                severity = com.example.opengluco.core.model.AlarmSeverity.ALERT,
                enabled = true,
                cooldownMinutes = 30
            ),
            GlucoseAlarm(
                id = "default_high",
                type = AlarmType.HIGH,
                thresholdMgDl = 180,
                severity = com.example.opengluco.core.model.AlarmSeverity.ALERT,
                enabled = true,
                cooldownMinutes = 60
            ),
            GlucoseAlarm(
                id = "default_urgent_high",
                type = AlarmType.HIGH,
                thresholdMgDl = 250,
                severity = com.example.opengluco.core.model.AlarmSeverity.URGENT,
                enabled = true,
                cooldownMinutes = 30
            )
        )
    }

    // --- Persistencia cifrada ---

    private fun loadFromDisk() {
        try {
            if (alarmsFile.exists()) {
                val raw = alarmsFile.readText()
                if (raw.isNotBlank()) {
                    val decrypted = KeystoreCryptoHelper.decrypt(raw)
                    val content = if (decrypted.isNotBlank()) decrypted else raw
                    val loaded = json.decodeFromString<List<GlucoseAlarm>>(content)
                    _alarmsFlow.value = if (loaded.isNotEmpty()) loaded else getDefaultAlarms()
                } else {
                    val defaults = getDefaultAlarms()
                    saveToDisk(defaults)
                    _alarmsFlow.value = defaults
                }
            } else {
                val defaults = getDefaultAlarms()
                saveToDisk(defaults)
                _alarmsFlow.value = defaults
            }
        } catch (_: Exception) {
            _alarmsFlow.value = getDefaultAlarms()
        }
    }

    private fun saveToDisk(alarms: List<GlucoseAlarm>) {
        try {
            val jsonStr = json.encodeToString(alarms)
            val encrypted = KeystoreCryptoHelper.encrypt(jsonStr)
            alarmsFile.writeText(encrypted)
        } catch (_: Exception) {}
    }

    private fun loadCooldowns() {
        try {
            if (cooldownFile.exists()) {
                val raw = cooldownFile.readText()
                if (raw.isNotBlank()) {
                    val decrypted = KeystoreCryptoHelper.decrypt(raw)
                    val content = if (decrypted.isNotBlank()) decrypted else raw
                    _lastFiredTimestamps.value = json.decodeFromString<Map<String, Long>>(content)
                }
            }
        } catch (_: Exception) {
            _lastFiredTimestamps.value = emptyMap()
        }
    }

    private fun saveCooldowns(cooldowns: Map<String, Long>) {
        try {
            val jsonStr = json.encodeToString(cooldowns)
            val encrypted = KeystoreCryptoHelper.encrypt(jsonStr)
            cooldownFile.writeText(encrypted)
        } catch (_: Exception) {}
    }

    /**
     * Purga todas las alarmas y cooldowns. Operacion destructiva e irreversible.
     * Utilizado para el derecho al olvido RGPD Art. 17.
     */
    suspend fun purgeAll() = withContext(Dispatchers.IO) {
        try {
            if (alarmsFile.exists()) alarmsFile.delete()
            if (cooldownFile.exists()) cooldownFile.delete()
        } catch (_: Exception) {}
        _alarmsFlow.value = emptyList()
        _lastFiredTimestamps.value = emptyMap()
    }
}
