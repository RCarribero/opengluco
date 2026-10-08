package com.example.opengluco.auto.screen

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.opengluco.auto.AutoMobileSyncHelper
import com.example.opengluco.auto.AutoTtsAlertManager
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.NetworkException
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.data.UserSettings
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.SensorLifecycleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GlucoseDashboardCarScreen(
    carContext: CarContext,
    initialReading: GlucoseMeasurement? = null
) : Screen(carContext) {

    private val repository = OpenGlucoRepository()
    private val preferencesRepository = UserPreferencesRepository(carContext)
    private val scope = CoroutineScope(Dispatchers.Main)

    private var allPatients: List<ConnectionItem> = emptyList()
    private var currentPatient: ConnectionItem? = null
    private var lastMeasurement: GlucoseMeasurement? = initialReading
    private var currentSettings: UserSettings = UserSettings()
    private var lastUpdated: String = initialReading?.getDisplayTime() ?: "Cargando..."
    private var isLoading = initialReading == null
    private val ttsAlertManager = AutoTtsAlertManager(carContext)
    private var refreshJob: Job? = null
    private var loadingJob: Job? = null

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                loadGlucoseData(silent = lastMeasurement != null)
                startPeriodicRefresh()
            }

            override fun onStop(owner: LifecycleOwner) {
                stopPeriodicRefresh()
                loadingJob?.cancel()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                stopPeriodicRefresh()
                loadingJob?.cancel()
                ttsAlertManager.shutdown()
            }
        })

        if (initialReading == null) {
            loadInitialCache()
        }
    }

    private fun startPeriodicRefresh() {
        stopPeriodicRefresh()
        refreshJob = scope.launch {
            while (isActive) {
                delay(60_000L)
                loadGlucoseData(silent = true)
            }
        }
    }

    private fun stopPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    private fun safeInvalidate() {
        try {
            invalidate()
        } catch (_: Exception) {
        }
    }

    private fun loadInitialCache() {
        scope.launch {
            val settings = preferencesRepository.userSettingsFlow.first()
            currentSettings = settings

            // 1. Lectura instantanea de app-mobile por ContentProvider (< 1 ms)
            val mobileReading = AutoMobileSyncHelper.getLatestReadingFromMobile(
                carContext,
                currentPatient?.patientId ?: settings.selectedPatientId.ifBlank { null }
            )
            if (mobileReading != null) {
                lastMeasurement = mobileReading
                lastUpdated = mobileReading.getDisplayTime()
                isLoading = false
                safeInvalidate()
                return@launch
            }

            // 2. Fallback a base de datos SQLite local
            val localHistory = preferencesRepository.getHistoricalReadings(1).first()
            val lastM = localHistory.lastOrNull()
            if (lastM != null) {
                lastMeasurement = lastM
                lastUpdated = lastM.getDisplayTime()
                isLoading = false
                safeInvalidate()
            }
        }
    }

    private fun loadGlucoseData(silent: Boolean = false) {
        if (!silent && lastMeasurement == null) {
            isLoading = true
            safeInvalidate()
        }

        loadingJob?.cancel()
        loadingJob = scope.launch {
            var settings = preferencesRepository.userSettingsFlow.first()
            // Sincronizar credenciales desde app-mobile de inmediato si no estan configuradas
            if (settings.token.isBlank() || settings.userId.isBlank()) {
                Log.d(TAG, "loadGlucoseData: Session token missing, syncing from app-mobile...")
                val synced = AutoMobileSyncHelper.syncSessionFromMobile(carContext, preferencesRepository)
                if (synced) {
                    settings = preferencesRepository.userSettingsFlow.first()
                }
            }

            currentSettings = settings
            val targetPatientId = currentPatient?.patientId ?: settings.selectedPatientId.ifBlank { null }

            // 1. PRIORIDAD ABSOLUTA LOCAL: Obtener telemetria instantanea (< 1 ms) desde app-mobile
            val mobileReading = AutoMobileSyncHelper.getLatestReadingFromMobile(carContext, targetPatientId)
            if (mobileReading != null) {
                lastMeasurement = mobileReading
                lastUpdated = mobileReading.getDisplayTime()
                isLoading = false
                Log.d(TAG, "loadGlucoseData: Applied instant mobile reading: ${mobileReading.numericValue} mg/dL, arrow=${mobileReading.trendArrow}, time=$lastUpdated, isStale=${mobileReading.isStale()}")
                safeInvalidate()
            }

            // 2. SINCRONIZACIÓN DE RED EN SEGUNDO PLANO (Pacientes, historial, sensor)
            if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
                repository.setSession(settings.token, settings.userId)

                val connRes = repository.getConnections()
                connRes.fold(
                    onSuccess = { patients ->
                        allPatients = patients
                        if (patients.isNotEmpty()) {
                            val p = patients.find { it.patientId == settings.selectedPatientId } ?: patients.first()
                            currentPatient = p

                            val connMeasurement = p.effectiveMeasurement
                            if (connMeasurement != null) {
                                val currentEpoch = lastMeasurement?.getEpochMillis() ?: 0L
                                val netEpoch = connMeasurement.getEpochMillis()
                                if (lastMeasurement == null || netEpoch >= currentEpoch) {
                                    lastMeasurement = connMeasurement
                                    lastUpdated = connMeasurement.getDisplayTime()
                                }
                                isLoading = false
                                safeInvalidate()
                            }

                            loadPatientDetails(p)
                        } else {
                            isLoading = false
                            if (lastMeasurement == null) {
                                lastUpdated = "Sin pacientes vinculados"
                            }
                            safeInvalidate()
                        }
                    },
                    onFailure = { error ->
                        Log.w(TAG, "loadGlucoseData: Network fetch failed, falling back to local storage", error)
                        if (lastMeasurement == null) {
                            val localHistory = preferencesRepository.getHistoricalReadings(1, targetPatientId).first()
                            if (localHistory.isNotEmpty()) {
                                val lastM = localHistory.lastOrNull()
                                lastMeasurement = lastM
                                lastUpdated = "Sin red • ${lastM?.getDisplayTime() ?: "Local"}"
                            } else {
                                lastUpdated = if (error is NetworkException) "Sin conexion a Internet" else "Error al conectar"
                            }
                        }
                        isLoading = false
                        safeInvalidate()
                    }
                )
            } else {
                isLoading = false
                safeInvalidate()
            }
        }
    }

    fun selectPatient(patient: ConnectionItem) {
        scope.launch {
            preferencesRepository.setSelectedPatient(patient.patientId)
            loadPatientDetails(patient)
        }
    }

    private suspend fun loadPatientDetails(patient: ConnectionItem) {
        preferencesRepository.loadPatientHistory(patient.patientId)
        val graphRes = repository.getPatientGraph(patient.patientId)
        val history = graphRes.getOrNull()?.graphData.orEmpty()
        val graphObj = graphRes.getOrNull()
        val resolvedSensor = if (graphObj != null) graphObj.resolvedSensor else patient.sensor?.takeIf { it.isValid }
        currentPatient = patient.copy(sensor = resolvedSensor)
        lastMeasurement = patient.effectiveMeasurement ?: history.lastOrNull()
        lastUpdated = lastMeasurement?.getDisplayTime() ?: "Ahora"
        if (history.isNotEmpty()) {
            preferencesRepository.saveHistoricalReadings(history, patient.patientId)
        }
        isLoading = false

        // Sensor activo: si resolvedSensor es null, no penalizamos como inactivo si la medicion es reciente
        val isSensorActive = resolvedSensor == null || ((resolvedSensor.getRemainingDays() ?: 1) > 0 && resolvedSensor.isSensorActive != false)
        val isStale = lastMeasurement == null || lastMeasurement!!.isStale() || !isSensorActive

        if (!isStale) {
            lastMeasurement?.let { m ->
                ttsAlertManager.speakGlucoseAlertIfNeeded(
                    glucoseMgDl = m.numericValue,
                    trendText = m.trendText,
                    lowThreshold = currentSettings.lowThreshold.toDouble(),
                    highThreshold = currentSettings.highThreshold.toDouble(),
                    measurementTimestampMs = m.getEpochMillis(),
                    isSensorActive = isSensorActive
                )
            }
        }

        safeInvalidate()
    }

    override fun onGetTemplate(): Template {
        val measurement = lastMeasurement
        val sensor = currentPatient?.sensor
        val isSensorActive = sensor == null || ((sensor.getRemainingDays() ?: 1) > 0 && sensor.isSensorActive != false)
        val isStale = measurement == null || measurement.isStale() || !isSensorActive

        val isMmol = currentSettings.unit == GlucoseUnit.MMOL
        val displayValue = measurement?.getFormattedValue(isMmol = isMmol) ?: "--"
        val trendSymbol = if (isStale) "--" else (measurement?.trendSymbol ?: "--")
        val trendText = if (measurement == null) {
            "Desconectado"
        } else if (isStale) {
            "Desactualizado"
        } else {
            measurement.trendText ?: "Estable"
        }
        val unitLabel = currentSettings.unit.label
        val lowThreshold = currentSettings.lowThreshold
        val highThreshold = currentSettings.highThreshold

        Log.d(TAG, "onGetTemplate: measurement=${measurement?.numericValue} mg/dL, epoch=${measurement?.getEpochMillis()}, isSensorActive=$isSensorActive, isStale=$isStale, displayValue=$displayValue")

        val paneBuilder = Pane.Builder()

        val showLoading = isLoading && measurement == null
        if (showLoading) {
            paneBuilder.setLoading(true)
        } else {
            // Fila 1: Valor actual y tendencia
            val patientTitle = if (allPatients.size > 1) {
                "Paciente: ${currentPatient?.fullName ?: "Principal"} (${allPatients.size})"
            } else {
                "Paciente: ${currentPatient?.fullName ?: "Principal"}"
            }

            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("$displayValue $unitLabel  $trendSymbol $trendText")
                    .addText(patientTitle)
                    .build()
            )

            // Fila 2: Estado del rango dinamico
            val statusText = if (isStale) {
                if (!isSensorActive) {
                    "[Desconectado] Sensor inactivo o expirado"
                } else {
                    "[Desactualizado] Telemetria anterior (> 20 min)"
                }
            } else {
                val mgdl = measurement?.numericValue ?: 0.0
                when {
                    mgdl <= 55 -> "[Urgente] Nivel muy bajo de glucosa (<= 55)"
                    mgdl < lowThreshold -> "[Alerta] Nivel bajo de glucosa (< $lowThreshold)"
                    mgdl > 250 -> "[Urgente] Nivel muy alto de glucosa (>= 250)"
                    mgdl > highThreshold -> "[Alerta] Nivel alto de glucosa (> $highThreshold)"
                    mgdl > 0 -> "[Normal] Nivel dentro del rango objetivo ($lowThreshold - $highThreshold)"
                    else -> "[Info] Sin datos recientes"
                }
            }
            val formattedTime = measurement?.getDisplayTime() ?: lastUpdated
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle(statusText)
                    .addText("Ultima actualizacion: $formattedTime")
                    .build()
            )

            // Fila 3: Sensor
            val sensorState = sensor?.getLifecycleState() ?: SensorLifecycleState.NoSensor
            val sensorRowText = when (sensorState) {
                is SensorLifecycleState.NoSensor -> {
                    if (measurement != null && !measurement.isStale()) {
                        "Sensor vinculado via OpenGluco"
                    } else {
                        "Sin sensor activo vinculado"
                    }
                }
                is SensorLifecycleState.Expired -> "Sensor expirado. Sustituir sensor."
                is SensorLifecycleState.WarmingUp -> "Sensor en calentamiento (listo en ${sensorState.remainingMinutes} min)"
                is SensorLifecycleState.Active -> "Dias restantes de uso: ${sensorState.remainingDays} dias"
            }
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Sensor FreeStyle Libre")
                    .addText(sensorRowText)
                    .build()
            )

            // Fila 4: Descargo legal pasivo obligatorio (MDR UE 2017/745 / FDA MDDS)
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle(LEGAL_DISCLAIMER_TITLE)
                    .addText(LEGAL_DISCLAIMER_SUBTEXT)
                    .build()
            )

            // Boton de refresco manual
            paneBuilder.addAction(
                Action.Builder()
                    .setTitle("Refrescar")
                    .setOnClickListener { loadGlucoseData(silent = false) }
                    .build()
            )
        }

        val templateBuilder = PaneTemplate.Builder(paneBuilder.build())
            .setTitle("OpenGluco Auto")
            .setHeaderAction(Action.APP_ICON)

        if (allPatients.size > 1) {
            val actionStrip = ActionStrip.Builder()
                .addAction(
                    Action.Builder()
                        .setTitle("Pacientes (${allPatients.size})")
                        .setOnClickListener {
                            screenManager.push(
                                PatientListCarScreen(
                                    carContext,
                                    allPatients,
                                    currentPatient?.patientId
                                ) { selected ->
                                    selectPatient(selected)
                                }
                            )
                        }
                        .build()
                )
                .build()
            templateBuilder.setActionStrip(actionStrip)
        }

        return templateBuilder.build()
    }

    companion object {
        private const val TAG = "OpenGlucoAuto"
        const val LEGAL_DISCLAIMER_TITLE = "Visualizador pasivo no médico"
        const val LEGAL_DISCLAIMER_SUBTEXT = "Uso informativo. Prohibido dosificar insulina en conducción."
    }
}
