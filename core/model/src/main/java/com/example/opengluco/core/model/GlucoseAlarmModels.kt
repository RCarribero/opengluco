package com.example.opengluco.core.model

import kotlinx.serialization.Serializable

/**
 * Tipo de alarma de glucosa.
 * LOW: alarma que se dispara cuando la glucosa cae por debajo del umbral.
 * HIGH: alarma que se dispara cuando la glucosa sube por encima del umbral.
 */
@Serializable
enum class AlarmType {
    LOW,
    HIGH
}

/**
 * Severidad de la alarma con 3 niveles escalonados.
 * URGENT: vibracion intensa + sonido fuerte (heads-up notification).
 * ALERT: vibracion moderada + sonido suave.
 * INFORMATIVE: notificacion silenciosa en bandeja.
 */
@Serializable
enum class AlarmSeverity {
    URGENT,
    ALERT,
    INFORMATIVE
}

/**
 * Opciones de cooldown (periodo minimo entre re-disparos de una misma alarma).
 * @param minutes duracion en minutos (0 = no repetir hasta que vuelva a rango).
 * @param label etiqueta legible para la interfaz.
 */
@Serializable
enum class AlarmCooldown(val minutes: Int, val label: String) {
    MIN_5(5, "5 min"),
    MIN_10(10, "10 min"),
    MIN_15(15, "15 min"),
    MIN_30(30, "30 min"),
    NONE(0, "No repetir")
}

/**
 * Modelo principal de una alarma de glucosa configurable por el usuario.
 *
 * @param id identificador unico (UUID).
 * @param type tipo de alarma (LOW o HIGH).
 * @param thresholdMgDl umbral de activacion en mg/dL (rango valido: 40-400).
 * @param severity nivel de severidad (URGENT, ALERT, INFORMATIVE).
 * @param cooldownMinutes minutos entre re-disparos (0 = no repetir hasta retorno a rango).
 * @param enabled si la alarma esta activa.
 * @param activeStartHour hora de inicio del periodo activo (0-23).
 * @param activeStartMinute minuto de inicio del periodo activo (0-59).
 * @param activeEndHour hora de fin del periodo activo (0-23).
 * @param activeEndMinute minuto de fin del periodo activo (0-59).
 * @param isAllDay si la alarma esta activa las 24 horas.
 */
@Serializable
data class GlucoseAlarm(
    val id: String,
    val type: AlarmType,
    val thresholdMgDl: Int,
    val severity: AlarmSeverity = AlarmSeverity.ALERT,
    val cooldownMinutes: Int = 15,
    val enabled: Boolean = true,
    val activeStartHour: Int = 0,
    val activeStartMinute: Int = 0,
    val activeEndHour: Int = 23,
    val activeEndMinute: Int = 59,
    val isAllDay: Boolean = true
) {
    companion object {
        const val MIN_THRESHOLD = 40
        const val MAX_THRESHOLD = 400
        const val MAX_ALARMS_PER_TYPE = 5
    }
}

/**
 * Resultado de la evaluacion del motor de alarmas.
 *
 * @param triggeredAlarm la alarma mas critica que debe dispararse, o null si ninguna aplica.
 * @param currentValueMgDl valor de glucosa actual en mg/dL.
 * @param isInCooldown true si la alarma candidata esta en periodo de enfriamiento.
 */
data class AlarmEvaluationResult(
    val triggeredAlarm: GlucoseAlarm? = null,
    val currentValueMgDl: Double = 0.0,
    val isInCooldown: Boolean = false
)
