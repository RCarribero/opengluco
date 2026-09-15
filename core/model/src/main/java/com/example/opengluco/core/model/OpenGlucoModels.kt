package com.example.opengluco.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- AUTENTICACIÓN ---

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class BaseResponse<T>(
    @SerialName("status") val status: Int,
    @SerialName("data") val data: T? = null,
    @SerialName("error") val error: ResponseError? = null,
    @SerialName("ticket") val ticket: AuthTicket? = null
)

@Serializable
data class ResponseError(
    @SerialName("message") val message: String? = null,
    @SerialName("code") val code: Int? = null
)

@Serializable
data class LoginData(
    @SerialName("user") val user: UserProfile? = null,
    @SerialName("authTicket") val authTicket: AuthTicket? = null,
    @SerialName("redirect") val redirect: Boolean? = false,
    @SerialName("region") val region: String? = null
)

@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("uom") val uom: Int? = 1
)

@Serializable
data class AuthTicket(
    @SerialName("token") val token: String,
    @SerialName("expires") val expires: Long,
    @SerialName("duration") val duration: Long
)

// --- PACIENTES Y CONEXIONES ---

@Serializable
data class ConnectionItem(
    @SerialName("id") val id: String,
    @SerialName("patientId") val patientId: String,
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    @SerialName("targetLow") val targetLow: Int = 70,
    @SerialName("targetHigh") val targetHigh: Int = 180,
    @SerialName("uom") val uom: Int = 1,
    @SerialName("sensor") val sensor: SensorInfo? = null,
    @SerialName("glucoseMeasurement") val glucoseMeasurement: GlucoseMeasurement? = null,
    @SerialName("glucoseItem") val glucoseItem: GlucoseMeasurement? = null
) {
    val effectiveMeasurement: GlucoseMeasurement?
        get() = glucoseMeasurement ?: glucoseItem

    val fullName: String
        get() = listOfNotNull(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { "Paciente" }
}

// --- CICLO DE VIDA Y ESTADOS DEL SENSOR ---

sealed interface SensorLifecycleState {
    object NoSensor : SensorLifecycleState
    data class WarmingUp(val remainingMinutes: Int) : SensorLifecycleState
    data class Active(val remainingDays: Int) : SensorLifecycleState
    object Expired : SensorLifecycleState

    val isActive: Boolean
        get() = this is Active
}

@Serializable
data class SensorInfo(
    @SerialName("deviceId") val deviceId: String? = null,
    @SerialName("sn") val serialNumber: String? = null,
    @SerialName("a") val activatedTimestamp: Long? = null,
    @SerialName("w") val warmupDurationMinutes: Int? = null,
    @SerialName("pt") val sensorType: Int? = null,
    @SerialName("l") val lifetimeDays: Int? = null,
    @SerialName("s") val isSensorActive: Boolean? = null
) {
    val isValid: Boolean
        get() = (activatedTimestamp != null && activatedTimestamp > 0L) ||
                !serialNumber.isNullOrBlank() ||
                !deviceId.isNullOrBlank()

    fun getRemainingDays(nowMs: Long = System.currentTimeMillis()): Int? {
        val activated = activatedTimestamp ?: return null
        if (activated <= 0L) return null
        if (isSensorActive == false) return 0
        val activatedSec = if (activated > 10_000_000_000L) activated / 1000.0 else activated.toDouble()
        val nowSec = nowMs / 1000.0

        // Duracion oficial de sensores FreeStyle Libre (14 dias)
        // O el valor explicitamente indicado en lifetimeDays (l) si existe
        val totalDays = lifetimeDays?.takeIf { it > 0 }?.toDouble() ?: 14.0
        val totalSec = totalDays * 24.0 * 3600.0
        val remainingSec = totalSec - (nowSec - activatedSec)

        return if (remainingSec > 0) {
            val days = kotlin.math.ceil(remainingSec / (24.0 * 3600.0)).toInt()
            days.coerceIn(1, 15)
        } else {
            0
        }
    }

    fun getLifecycleState(nowMs: Long = System.currentTimeMillis()): SensorLifecycleState {
        val activated = activatedTimestamp ?: return SensorLifecycleState.NoSensor
        if (activated <= 0L) return SensorLifecycleState.NoSensor

        if (isSensorActive == false) {
            return SensorLifecycleState.Expired
        }

        val activatedMs = if (activated > 10_000_000_000L) activated else activated * 1000L
        val elapsedMs = nowMs - activatedMs

        // Calentamiento durante los primeros 60 min (o warmupDurationMinutes)
        val warmupMinutes = warmupDurationMinutes ?: 60
        val warmupMs = warmupMinutes * 60 * 1000L
        if (elapsedMs in 0 until warmupMs) {
            val remainingMin = kotlin.math.ceil((warmupMs - elapsedMs) / 60_000.0).toInt().coerceAtLeast(1)
            return SensorLifecycleState.WarmingUp(remainingMin)
        }

        val remainingDays = getRemainingDays(nowMs) ?: 0
        if (remainingDays <= 0) {
            return SensorLifecycleState.Expired
        }

        return SensorLifecycleState.Active(remainingDays)
    }

    val sensorModelName: String
        get() = when (sensorType) {
            3 -> "FreeStyle Libre 3"
            2 -> "FreeStyle Libre 2"
            1 -> "FreeStyle Libre 1"
            else -> "FreeStyle Libre Sensor"
        }
}

fun SensorInfo?.lifecycleState(nowMs: Long = System.currentTimeMillis()): SensorLifecycleState =
    this?.getLifecycleState(nowMs) ?: SensorLifecycleState.NoSensor

@Serializable
data class DeviceInfo(
    @SerialName("did") val did: String? = null,
    @SerialName("dtid") val dtid: Int? = null,
    @SerialName("v") val v: String? = null
)

@Serializable
data class ActiveSensorEntry(
    @SerialName("sensor") val sensor: SensorInfo? = null,
    @SerialName("device") val device: DeviceInfo? = null
)

// --- HISTORIAL Y LECTURAS DE GLUCOSA ---

@Serializable
data class GraphData(
    @SerialName("connection") val connection: ConnectionItem? = null,
    @SerialName("activeSensors") val activeSensors: List<ActiveSensorEntry>? = null,
    @SerialName("graphData") val graphData: List<GlucoseMeasurement> = emptyList()
) {
    val resolvedSensor: SensorInfo?
        get() {
            val list = activeSensors
            if (list != null) {
                val validSensors = list.mapNotNull { it.sensor }.filter { it.isValid }
                if (validSensors.isEmpty()) {
                    return connection?.sensor?.takeIf { it.isValid }?.copy(isSensorActive = false)
                }
                val activeOnly = validSensors.filter { it.isSensorActive == true }
                if (activeOnly.isNotEmpty()) {
                    return activeOnly.maxByOrNull { it.activatedTimestamp ?: 0L }
                }
                val potentiallyActive = validSensors.filter { it.isSensorActive != false && (it.getRemainingDays() ?: 0) > 0 }
                if (potentiallyActive.isNotEmpty()) {
                    return potentiallyActive.maxByOrNull { it.activatedTimestamp ?: 0L }
                }
                return validSensors.maxByOrNull { it.activatedTimestamp ?: 0L }
            }
            return connection?.sensor?.takeIf { it.isValid }
        }
}

@Serializable
data class GlucoseMeasurement(
    @SerialName("FactoryTimestamp") val factoryTimestamp: String? = null,
    @SerialName("Timestamp") val timestamp: String? = null,
    @SerialName("ValueInMgPerDl") val valueInMgPerDl: Double? = null,
    @SerialName("Value") val value: Double? = null,
    @SerialName("TrendArrow") val trendArrow: Int? = null,
    @SerialName("TrendMessage") val trendMessage: String? = null,
    @SerialName("MeasurementColor") val measurementColor: Int? = null,
    @SerialName("GlucoseUnits") val glucoseUnits: Int? = 1,
    @SerialName("isHigh") val isHigh: Boolean? = false,
    @SerialName("isLow") val isLow: Boolean? = false
) {
    val numericValue: Double
        get() = valueInMgPerDl ?: value ?: 0.0

    val trendSymbol: String
        get() = when (trendArrow) {
            1 -> "↓"
            2 -> "↘"
            3 -> "→"
            4 -> "↗"
            5 -> "↑"
            else -> "→"
        }

    val trendText: String
        get() = when (trendArrow) {
            1 -> "Cayendo rápido"
            2 -> "Bajando"
            3 -> "Estable"
            4 -> "Subiendo"
            5 -> "Subiendo rápido"
            else -> "Estable"
        }

    fun getFormattedValue(isMmol: Boolean = false): String {
        val mgdl = numericValue
        return if (isMmol) {
            val mmol = mgdl / 18.0182
            String.format(java.util.Locale.US, "%.1f", mmol)
        } else {
            String.format(java.util.Locale.US, "%.0f", mgdl)
        }
    }

    fun getEpochMillis(): Long {
        val raw = timestamp ?: factoryTimestamp ?: return 0L
        val patterns = listOf(
            "M/d/yyyy h:mm:ss a",
            "M/d/yyyy hh:mm:ss a",
            "MM/dd/yyyy hh:mm:ss a",
            "M/d/yyyy H:mm:ss",
            "M/d/yyyy HH:mm:ss",
            "d/M/yyyy h:mm:ss a",
            "d/M/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pat in patterns) {
            try {
                val sdf = java.text.SimpleDateFormat(pat, java.util.Locale.US)
                if (pat.endsWith("'Z'")) {
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val date = sdf.parse(raw)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    fun isStale(nowMs: Long = System.currentTimeMillis(), thresholdMinutes: Long = 20): Boolean {
        val epoch = getEpochMillis()
        if (epoch <= 0L) return true
        val diff = nowMs - epoch
        return diff > thresholdMinutes * 60 * 1000L || diff < -5 * 60 * 1000L
    }

    fun getDisplayTime(): String {
        val epoch = getEpochMillis()
        if (epoch > 0) {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(epoch))
        }
        return timestamp?.takeLast(11)?.trim() ?: factoryTimestamp ?: "Ahora"
    }
}

// --- ESTADOS CLÍNICOS DE ERROR Y CONECTIVIDAD ---

sealed interface ClinicalErrorType {
    object None : ClinicalErrorType
    data class NetworkError(val message: String = "Sin conexión a Internet") : ClinicalErrorType
    data class AuthExpired(val message: String = "Sesión caducada") : ClinicalErrorType
    data class NoSensor(val message: String = "Sin sensor activo vinculado") : ClinicalErrorType
    data class SensorExpired(val message: String = "El sensor ha finalizado") : ClinicalErrorType
    data class SensorWarmingUp(val remainingMinutes: Int = 0, val message: String = "Sensor en calentamiento") : ClinicalErrorType
    data class NoPatients(val message: String = "No hay pacientes vinculados a esta cuenta") : ClinicalErrorType
    data class Generic(val message: String) : ClinicalErrorType
}

