package com.example.opengluco.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GlucoseDashboardCarScreen(carContext: CarContext) : Screen(carContext) {

    private val repository = OpenGlucoRepository()
    private val preferencesRepository = UserPreferencesRepository(carContext)
    private val scope = CoroutineScope(Dispatchers.Main)

    private var allPatients: List<ConnectionItem> = emptyList()
    private var currentPatient: ConnectionItem? = null
    private var lastMeasurement: GlucoseMeasurement? = null
    private var currentSettings: com.example.opengluco.core.data.UserSettings = com.example.opengluco.core.data.UserSettings()
    private var lastUpdated: String = "Cargando..."
    private var isLoading = true
    private val ttsAlertManager = com.example.opengluco.auto.AutoTtsAlertManager(carContext)

    init {
        loadGlucoseData()
    }

    private fun loadGlucoseData() {
        isLoading = true
        invalidate()

        scope.launch {
            val settings = preferencesRepository.userSettingsFlow.first()
            currentSettings = settings
            if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
                repository.setSession(settings.token, settings.userId)

                val connRes = repository.getConnections()
                val patients = connRes.getOrNull().orEmpty()
                allPatients = patients
                if (patients.isNotEmpty()) {
                    val p = patients.find { it.patientId == settings.selectedPatientId } ?: patients.first()
                    loadPatientDetails(p)
                } else {
                    isLoading = false
                    invalidate()
                }
            } else {
                isLoading = false
                invalidate()
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
        currentPatient = patient
        preferencesRepository.loadPatientHistory(patient.patientId)
        val graphRes = repository.getPatientGraph(patient.patientId)
        val history = graphRes.getOrNull()?.graphData.orEmpty()
        lastMeasurement = patient.effectiveMeasurement ?: history.lastOrNull()
        lastUpdated = lastMeasurement?.timestamp ?: "Ahora"
        preferencesRepository.saveHistoricalReadings(history, patient.patientId)
        isLoading = false

        // Alerta de voz TTS si la medicion esta fuera de rango
        lastMeasurement?.let { m ->
            ttsAlertManager.speakGlucoseAlertIfNeeded(
                glucoseMgDl = m.numericValue,
                trendText = m.trendText,
                lowThreshold = currentSettings.lowThreshold.toDouble(),
                highThreshold = currentSettings.highThreshold.toDouble()
            )
        }

        invalidate()
    }

    override fun onGetTemplate(): Template {
        val measurement = lastMeasurement
        val mgdl = measurement?.numericValue ?: 0.0

        val isMmol = currentSettings.unit == com.example.opengluco.core.data.GlucoseUnit.MMOL
        val displayValue = measurement?.getFormattedValue(isMmol = isMmol) ?: "--"
        val trendSymbol = measurement?.trendSymbol ?: "→"
        val trendText = measurement?.trendText ?: "Estable"
        val unitLabel = currentSettings.unit.label
        val lowThreshold = currentSettings.lowThreshold
        val highThreshold = currentSettings.highThreshold

        val paneBuilder = Pane.Builder()

        if (isLoading) {
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

            // Fila 2: Estado del rango dinámico
            val statusText = when {
                mgdl <= 55 -> "[Urgente] Nivel muy bajo de glucosa (<= 55)"
                mgdl < lowThreshold -> "[Alerta] Nivel bajo de glucosa (< $lowThreshold)"
                mgdl > 250 -> "[Urgente] Nivel muy alto de glucosa (>= 250)"
                mgdl > highThreshold -> "[Alerta] Nivel alto de glucosa (> $highThreshold)"
                mgdl > 0 -> "[Normal] Nivel dentro del rango objetivo ($lowThreshold - $highThreshold)"
                else -> "[Info] Sin datos recientes"
            }
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle(statusText)
                    .addText("Última actualización: $lastUpdated")
                    .build()
            )

            // Fila 3: Sensor
            val sensor = currentPatient?.sensor
            if (sensor != null) {
                val days = sensor.getRemainingDays() ?: 14
                paneBuilder.addRow(
                    Row.Builder()
                        .setTitle("Sensor FreeStyle Libre")
                        .addText("Días restantes de uso: $days días")
                        .build()
                )
            } else {
                paneBuilder.addRow(
                    Row.Builder()
                        .setTitle("Sensor FreeStyle Libre")
                        .addText("Sensor activo / Vinculado")
                        .build()
                )
            }

            // Fila 4: Descargo legal pasivo obligatorio (MDR UE 2017/745 / FDA MDDS)
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle(LEGAL_DISCLAIMER_TITLE)
                    .addText(LEGAL_DISCLAIMER_SUBTEXT)
                    .build()
            )

            // Botón de refresco
            paneBuilder.addAction(
                Action.Builder()
                    .setTitle("Refrescar")
                    .setOnClickListener { loadGlucoseData() }
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
        const val LEGAL_DISCLAIMER_TITLE = "Visualizador pasivo no médico"
        const val LEGAL_DISCLAIMER_SUBTEXT = "Uso informativo. Prohibido dosificar insulina en conducción."
    }
}
