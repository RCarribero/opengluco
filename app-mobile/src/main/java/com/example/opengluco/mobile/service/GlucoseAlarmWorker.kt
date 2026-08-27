package com.example.opengluco.mobile.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.opengluco.core.data.AlarmEvaluator
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.mobile.notification.MobileAlarmNotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Worker periodico para evaluacion de alarmas de glucosa en background.
 * Se ejecuta cada 15 minutos (minimo de WorkManager) y verifica las lecturas
 * contra las alarmas configuradas por el usuario.
 */
class GlucoseAlarmWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        val userPrefsRepo = UserPreferencesRepository(context)
        val settings = userPrefsRepo.userSettingsFlow.first()

        if (settings.token.isBlank() || settings.userId.isBlank()) {
            return Result.success()
        }

        val openGlucoRepo = OpenGlucoRepository()
        openGlucoRepo.setSession(settings.token, settings.userId)

        val connectionsResult = openGlucoRepo.getConnections()
        val patients = connectionsResult.getOrNull().orEmpty()

        if (patients.isEmpty()) {
            return Result.success()
        }

        val patient = patients.find { it.patientId == settings.selectedPatientId }
            ?: patients.first()

        val latest = patient.effectiveMeasurement
        if (latest == null) {
            return Result.success()
        }

        val value = latest.numericValue
        if (value <= 0.0) {
            return Result.success()
        }

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
            MobileAlarmNotificationHelper.triggerAlarm(
                context = context,
                alarm = triggered,
                glucoseValueMgDl = value
            )
            alarmRepo.recordAlarmFired(triggered.id)
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "glucose_alarm_sync"

        fun enqueue(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<GlucoseAlarmWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
