package com.example.opengluco.core.data

import com.example.opengluco.core.model.AlarmEvaluationResult
import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import java.util.Calendar

/**
 * Motor de evaluacion de alarmas de glucosa.
 *
 * Dada una lectura de glucosa actual y la lista de alarmas configuradas,
 * determina cual (si alguna) debe dispararse segun las reglas:
 * 1. Solo alarmas habilitadas.
 * 2. Solo alarmas dentro de su horario activo.
 * 3. Solo alarmas cuyo umbral ha sido cruzado.
 * 4. Solo alarmas fuera de cooldown.
 * 5. De las candidatas, la de mayor severidad (URGENT > ALERT > INFORMATIVE).
 * 6. A igual severidad: para LOW la de umbral mas alto, para HIGH la de umbral mas bajo.
 */
object AlarmEvaluator {

    /**
     * Evalua la lectura de glucosa contra las alarmas configuradas.
     *
     * @param currentValueMgDl valor actual de glucosa en mg/dL.
     * @param alarms lista completa de alarmas configuradas.
     * @param lastFiredTimestamps mapa de alarmId -> timestamp (ms) del ultimo disparo.
     * @param nowMillis timestamp actual en milisegundos (inyectable para testing).
     * @return resultado con la alarma mas critica a disparar o null.
     */
    fun evaluate(
        currentValueMgDl: Double,
        alarms: List<GlucoseAlarm>,
        lastFiredTimestamps: Map<String, Long>,
        nowMillis: Long = System.currentTimeMillis()
    ): AlarmEvaluationResult {
        if (alarms.isEmpty() || currentValueMgDl <= 0.0) {
            return AlarmEvaluationResult(
                triggeredAlarm = null,
                currentValueMgDl = currentValueMgDl,
                isInCooldown = false
            )
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Paso 1: Filtrar alarmas habilitadas
        val enabledAlarms = alarms.filter { it.enabled }

        // Paso 2: Filtrar alarmas dentro de su horario activo
        val activeAlarms = enabledAlarms.filter { alarm ->
            isWithinActiveSchedule(alarm, currentHour, currentMinute)
        }

        // Paso 3: Filtrar alarmas cuyo umbral se ha cruzado
        val triggeredAlarms = activeAlarms.filter { alarm ->
            isThresholdCrossed(alarm, currentValueMgDl)
        }

        if (triggeredAlarms.isEmpty()) {
            return AlarmEvaluationResult(
                triggeredAlarm = null,
                currentValueMgDl = currentValueMgDl,
                isInCooldown = false
            )
        }

        // Paso 4: Separar las que estan en cooldown de las que no
        val (inCooldown, available) = triggeredAlarms.partition { alarm ->
            isInCooldown(alarm, lastFiredTimestamps, nowMillis)
        }

        if (available.isEmpty()) {
            // Todas las alarmas candidatas estan en cooldown
            return AlarmEvaluationResult(
                triggeredAlarm = null,
                currentValueMgDl = currentValueMgDl,
                isInCooldown = true
            )
        }

        // Paso 5-6: Seleccionar la mas critica
        val mostCritical = selectMostCritical(available)

        return AlarmEvaluationResult(
            triggeredAlarm = mostCritical,
            currentValueMgDl = currentValueMgDl,
            isInCooldown = false
        )
    }

    /**
     * Comprueba si la alarma esta dentro de su horario activo.
     * Soporta horarios que cruzan medianoche (ej: 22:00 a 08:00).
     */
    internal fun isWithinActiveSchedule(
        alarm: GlucoseAlarm,
        currentHour: Int,
        currentMinute: Int
    ): Boolean {
        if (alarm.isAllDay) return true

        val currentTotalMinutes = currentHour * 60 + currentMinute
        val startTotalMinutes = alarm.activeStartHour * 60 + alarm.activeStartMinute
        val endTotalMinutes = alarm.activeEndHour * 60 + alarm.activeEndMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            // Horario normal (ej: 08:00 a 22:00)
            currentTotalMinutes in startTotalMinutes..endTotalMinutes
        } else {
            // Horario que cruza medianoche (ej: 22:00 a 08:00)
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
        }
    }

    /**
     * Comprueba si el umbral de la alarma ha sido cruzado.
     */
    internal fun isThresholdCrossed(alarm: GlucoseAlarm, valueMgDl: Double): Boolean {
        return when (alarm.type) {
            AlarmType.LOW -> valueMgDl <= alarm.thresholdMgDl
            AlarmType.HIGH -> valueMgDl >= alarm.thresholdMgDl
        }
    }

    /**
     * Comprueba si la alarma esta en periodo de cooldown.
     * Una alarma con cooldownMinutes == 0 (No repetir) se considera en cooldown
     * indefinidamente hasta que el valor regrese a rango normal y se limpie manualmente.
     */
    internal fun isInCooldown(
        alarm: GlucoseAlarm,
        lastFiredTimestamps: Map<String, Long>,
        nowMillis: Long
    ): Boolean {
        val lastFired = lastFiredTimestamps[alarm.id] ?: return false

        return if (alarm.cooldownMinutes == 0) {
            // "No repetir": siempre en cooldown una vez disparada
            true
        } else {
            val cooldownMs = alarm.cooldownMinutes * 60_000L
            (nowMillis - lastFired) < cooldownMs
        }
    }

    /**
     * Selecciona la alarma mas critica de una lista de candidatas.
     * Prioridad: URGENT > ALERT > INFORMATIVE.
     * A igual severidad: para LOW la de umbral mas alto, para HIGH la de umbral mas bajo.
     */
    internal fun selectMostCritical(alarms: List<GlucoseAlarm>): GlucoseAlarm? {
        if (alarms.isEmpty()) return null

        return alarms.sortedWith(
            compareBy<GlucoseAlarm> { severityPriority(it.severity) }
                .thenBy { thresholdPriority(it) }
        ).first()
    }

    private fun severityPriority(severity: AlarmSeverity): Int {
        return when (severity) {
            AlarmSeverity.URGENT -> 0
            AlarmSeverity.ALERT -> 1
            AlarmSeverity.INFORMATIVE -> 2
        }
    }

    /**
     * Para alarmas LOW: umbral mas alto es mas critico (la alarma de 70 mg/dL es
     * mas conservadora que la de 55 mg/dL, pero ambas se cruzan a 50 mg/dL,
     * asi que la de 70 mg/dL es la que primero aplico, pero la de 55 puede ser
     * mas urgente por severidad).
     *
     * Para alarmas HIGH: umbral mas bajo es mas critico (la alarma de 180 mg/dL
     * se cruza antes que la de 250 mg/dL).
     */
    private fun thresholdPriority(alarm: GlucoseAlarm): Int {
        return when (alarm.type) {
            AlarmType.LOW -> -alarm.thresholdMgDl  // Mayor umbral = mayor prioridad
            AlarmType.HIGH -> alarm.thresholdMgDl   // Menor umbral = mayor prioridad
        }
    }
}
