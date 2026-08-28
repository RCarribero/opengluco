package com.example.opengluco.mobile.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.opengluco.core.data.AlarmEvaluator
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.mobile.notification.MobileAlarmNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Servicio en primer plano (Foreground Service) permanente para la monitorización 24/7 de glucosa.
 * Se ejecuta de forma continua con la pantalla apagada (Doze Mode) y sin ser suspendido por Samsung One UI.
 */
class GlucoseMonitorForegroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var openGlucoRepository: OpenGlucoRepository
    private lateinit var alarmRepository: AlarmRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Iniciando GlucoseMonitorForegroundService...")
        MobileAlarmNotificationHelper.createChannels(applicationContext)

        preferencesRepository = UserPreferencesRepository(applicationContext)
        openGlucoRepository = OpenGlucoRepository()
        alarmRepository = AlarmRepository(applicationContext)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "OpenGluco::GlucoseMonitorWakeLock"
        ).apply {
            setReferenceCounted(false)
        }

        // Iniciar inmediatamente como Foreground Service con notificación permanente
        val initialNotification = MobileAlarmNotificationHelper.buildLiveGlucoseNotification(
            context = applicationContext,
            glucoseValueMgDl = 0.0,
            trendArrow = "->",
            patientName = "Monitor Activo"
        )
        startForeground(MobileAlarmNotificationHelper.NOTIFICATION_ID_LIVE_STATUS, initialNotification)

        startTelemetryLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTelemetryLoop() {
        serviceScope.launch {
            while (isActive) {
                try {
                    wakeLock?.acquire(4000) // Despertar CPU durante la consulta
                    fetchAndEvaluateTelemetry()
                } catch (e: Exception) {
                    Log.e(TAG, "Error en bucle de telemetría: ${e.message}")
                } finally {
                    try {
                        if (wakeLock?.isHeld == true) {
                            wakeLock?.release()
                        }
                    } catch (_: Exception) {}
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun fetchAndEvaluateTelemetry() {
        val settings = preferencesRepository.userSettingsFlow.first()
        if (settings.token.isBlank() || settings.userId.isBlank()) {
            return
        }

        openGlucoRepository.setSession(settings.token, settings.userId)
        val connectionsRes = openGlucoRepository.getConnections()
        val patients = connectionsRes.getOrNull().orEmpty()
        if (patients.isEmpty()) return

        val targetPatient = patients.find { it.patientId == settings.selectedPatientId }
            ?: patients.first()

        val graphRes = openGlucoRepository.getPatientGraph(targetPatient.patientId)
        val graphDataObj = graphRes.getOrNull()
        val graphData = graphDataObj?.graphData.orEmpty()

        // Guardar lecturas en caché histórico persistente
        val allToSave = mutableListOf<GlucoseMeasurement>()
        allToSave.addAll(graphData)
        targetPatient.effectiveMeasurement?.let { em ->
            if (allToSave.none { it.timestamp == em.timestamp && !it.timestamp.isNullOrBlank() }) {
                allToSave.add(em)
            }
        }
        preferencesRepository.saveHistoricalReadings(allToSave, targetPatient.patientId)

        val latest = targetPatient.effectiveMeasurement ?: graphData.lastOrNull() ?: return
        val value = latest.numericValue
        if (value <= 0.0) return

        val arrow = latest.trendSymbol
        val name = targetPatient.fullName.ifBlank { "Paciente" }

        // 1. Actualizar la tarjeta persistente de la barra de notificaciones
        MobileAlarmNotificationHelper.updateLiveGlucoseNotification(
            context = applicationContext,
            glucoseValueMgDl = value,
            trendArrow = arrow,
            patientName = name
        )

        // 2. Evaluar alarmas clínicas y disparar si se supera un umbral
        val alarms = alarmRepository.getAllAlarms()
        val timestamps = alarmRepository.getLastFiredTimestamps()
        val result = AlarmEvaluator.evaluate(
            currentValueMgDl = value,
            alarms = alarms,
            lastFiredTimestamps = timestamps
        )

        val triggered = result.triggeredAlarm
        if (triggered != null) {
            Log.w(TAG, "ALARMA DISPARADA: ${triggered.id} ($value mg/dL)")
            MobileAlarmNotificationHelper.triggerAlarm(
                context = applicationContext,
                alarm = triggered,
                glucoseValueMgDl = value
            )
            alarmRepository.recordAlarmFired(triggered.id)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destruyendo GlucoseMonitorForegroundService...")
        serviceJob.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    companion object {
        private const val TAG = "GlucoseMonitorService"
        private const val POLL_INTERVAL_MS = 60_000L // 60 segundos exactos

        fun startService(context: Context) {
            val intent = Intent(context, GlucoseMonitorForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, GlucoseMonitorForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
