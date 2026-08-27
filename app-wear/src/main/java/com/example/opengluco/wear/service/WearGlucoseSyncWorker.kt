package com.example.opengluco.wear.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.opengluco.core.data.AlarmEvaluator
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.wear.notification.WearAlarmNotificationHelper
import kotlinx.coroutines.flow.first

/**
 * Worker periodico para sincronizacion de glucosa y evaluacion de alarmas en Wear OS.
 * Se ejecuta cada 15 minutos (minimo de WorkManager) en background.
 */
class WearGlucoseSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefsRepo = UserPreferencesRepository(context)
        val settings = prefsRepo.userSettingsFlow.first()

        if (settings.token.isBlank() || settings.userId.isBlank()) {
            return Result.success()
        }

        val repository = OpenGlucoRepository()
        repository.setSession(settings.token, settings.userId)

        val connectionsRes = repository.getConnections()
        val patients = connectionsRes.getOrNull().orEmpty()

        if (patients.isNotEmpty()) {
            val patient = patients.find { it.patientId == settings.selectedPatientId } ?: patients.first()
            val graphRes = repository.getPatientGraph(patient.patientId)
            val history = graphRes.getOrNull()?.graphData.orEmpty()
            val latest = patient.effectiveMeasurement ?: history.lastOrNull()

            latest?.let {
                val value = it.numericValue
                prefsRepo.saveLastMeasurement(
                    value = value,
                    trend = it.trendArrow ?: 3,
                    timestamp = it.timestamp ?: ""
                )

                // Evaluacion de alarmas con el nuevo sistema multi-nivel
                if (settings.hapticAlertsEnabled) {
                    val alarmRepo = AlarmRepository(context)
                    val alarms = alarmRepo.getAllAlarms()
                    val timestamps = alarmRepo.getLastFiredTimestamps()

                    val result = AlarmEvaluator.evaluate(
                        currentValueMgDl = value,
                        alarms = alarms,
                        lastFiredTimestamps = timestamps
                    )

                    val triggered = result.triggeredAlarm
                    if (triggered != null) {
                        WearAlarmNotificationHelper.triggerAlarmBySeverity(
                            context = context,
                            alarm = triggered,
                            glucoseValueMgDl = value
                        )
                        alarmRepo.recordAlarmFired(triggered.id)
                    }
                }
            }
        }

        return Result.success()
    }
}
