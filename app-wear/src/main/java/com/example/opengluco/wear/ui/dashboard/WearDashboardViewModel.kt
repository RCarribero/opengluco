package com.example.opengluco.wear.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.SensorInfo
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.data.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface WearDashboardUiState {
    object Loading : WearDashboardUiState
    object NeedsLogin : WearDashboardUiState
    data class Success(
        val selectedPatient: ConnectionItem,
        val allPatients: List<ConnectionItem>,
        val currentMeasurement: GlucoseMeasurement?,
        val graphHistory: List<GlucoseMeasurement>,
        val sensor: SensorInfo?,
        val unit: GlucoseUnit,
        val lowThreshold: Int = 70,
        val highThreshold: Int = 180,
        val lastUpdatedText: String,
        val isRefreshing: Boolean = false
    ) : WearDashboardUiState
    data class Error(val message: String) : WearDashboardUiState
}

class WearDashboardViewModel(
    private val repository: OpenGlucoRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WearDashboardUiState>(getInitialUiState())
    val uiState: StateFlow<WearDashboardUiState> = _uiState.asStateFlow()

    private var userSettings: UserSettings = UserSettings()

    private fun getInitialUiState(): WearDashboardUiState {
        return try {
            val localLatest = preferencesRepository.localDatabase.getLatestReading(null)
            val localHistory = preferencesRepository.localDatabase.getHistoricalReadingsList(1, null)
            if (localLatest != null || localHistory.isNotEmpty()) {
                val patientId = "principal"
                val dummyPatient = ConnectionItem(
                    id = patientId,
                    patientId = patientId,
                    firstName = "Paciente",
                    glucoseItem = localLatest
                )
                val isStale = localLatest == null || localLatest.isStale()
                val timeText = localLatest?.getDisplayTime() ?: "Ahora"
                val updatedText = if (isStale) "Desactualizado • $timeText" else timeText

                WearDashboardUiState.Success(
                    selectedPatient = dummyPatient,
                    allPatients = listOf(dummyPatient),
                    currentMeasurement = localLatest,
                    graphHistory = localHistory,
                    sensor = null,
                    unit = GlucoseUnit.MGDL,
                    lowThreshold = 70,
                    highThreshold = 180,
                    lastUpdatedText = updatedText,
                    isRefreshing = true
                )
            } else {
                WearDashboardUiState.Loading
            }
        } catch (_: Exception) {
            WearDashboardUiState.Loading
        }
    }

    init {
        viewModelScope.launch {
            preferencesRepository.userSettingsFlow.collect { settings ->
                val previousToken = userSettings.token
                val previousUserId = userSettings.userId
                userSettings = settings

                if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
                    repository.setSession(settings.token, settings.userId)
                    val authChanged = previousToken != settings.token || previousUserId != settings.userId

                    val current = _uiState.value
                    if (current is WearDashboardUiState.Success) {
                        _uiState.value = current.copy(
                            unit = settings.unit,
                            lowThreshold = settings.lowThreshold,
                            highThreshold = settings.highThreshold
                        )
                    } else {
                        tryLoadInstantCache(settings)
                    }

                    if (authChanged || _uiState.value !is WearDashboardUiState.Success) {
                        loadDashboardDataInternal(isRefreshing = _uiState.value is WearDashboardUiState.Success)
                    }
                } else {
                    _uiState.value = WearDashboardUiState.NeedsLogin
                }
            }
        }

        // Escucha reactiva continua de la base de datos local SQLite (stream Bluetooth / DataLayer)
        viewModelScope.launch {
            preferencesRepository.localDatabase.dbUpdateEvents.collect {
                val current = _uiState.value as? WearDashboardUiState.Success ?: return@collect
                val targetPatientId = current.selectedPatient.patientId.ifBlank { null }
                val localLatest = preferencesRepository.localDatabase.getLatestReading(targetPatientId)
                    ?: preferencesRepository.localDatabase.getLatestReading(null)
                if (localLatest != null) {
                    val currentEpoch = current.currentMeasurement?.getEpochMillis() ?: 0L
                    if (localLatest.getEpochMillis() >= currentEpoch) {
                        val localHistory = preferencesRepository.localDatabase.getHistoricalReadingsList(1, targetPatientId)
                            .ifEmpty { preferencesRepository.localDatabase.getHistoricalReadingsList(1, null) }
                        val isSensorActive = current.sensor == null || ((current.sensor.getRemainingDays() ?: 1) > 0 && current.sensor.isSensorActive != false)
                        val isStale = localLatest.isStale() || !isSensorActive
                        val updatedText = if (isStale) "Desactualizado • ${localLatest.getDisplayTime()}" else localLatest.getDisplayTime()
                        _uiState.value = current.copy(
                            currentMeasurement = localLatest,
                            graphHistory = if (localHistory.isNotEmpty()) localHistory else current.graphHistory,
                            lastUpdatedText = updatedText
                        )
                    }
                }
            }
        }

        // Bucle de actualizacion automatica periodica cada 60 segundos con ahorro energetico
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000)
                val current = _uiState.value
                if (current is WearDashboardUiState.Success) {
                    val m = current.currentMeasurement
                    val isFresh = m != null && !m.isStale()
                    // Si los datos estan actualizados y se reciben por Bluetooth / DataLayer, evitar peticion HTTP innecesaria
                    if (isFresh) {
                        continue
                    }
                    loadDashboardDataInternal(isRefreshing = true)
                }
            }
        }
    }

    private fun tryLoadInstantCache(settings: UserSettings): Boolean {
        return try {
            val targetPatientId = settings.selectedPatientId.ifBlank { null }
            val localLatest = preferencesRepository.localDatabase.getLatestReading(targetPatientId)
                ?: preferencesRepository.localDatabase.getLatestReading(null)
            val localHistory = preferencesRepository.localDatabase.getHistoricalReadingsList(1, targetPatientId)
                .ifEmpty { preferencesRepository.localDatabase.getHistoricalReadingsList(1, null) }

            if (localLatest != null || localHistory.isNotEmpty()) {
                val patientId = targetPatientId ?: "principal"
                val dummyPatient = ConnectionItem(
                    id = patientId,
                    patientId = patientId,
                    firstName = "Paciente",
                    glucoseItem = localLatest
                )
                val isStale = localLatest == null || localLatest.isStale()
                val timeText = localLatest?.getDisplayTime() ?: "Ahora"
                val updatedText = if (isStale) "Desactualizado • $timeText" else timeText

                _uiState.value = WearDashboardUiState.Success(
                    selectedPatient = dummyPatient,
                    allPatients = listOf(dummyPatient),
                    currentMeasurement = localLatest,
                    graphHistory = localHistory,
                    sensor = null,
                    unit = settings.unit,
                    lowThreshold = settings.lowThreshold,
                    highThreshold = settings.highThreshold,
                    lastUpdatedText = updatedText,
                    isRefreshing = true
                )
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadDashboardDataInternal(isRefreshing = true)
        }
    }

    fun switchPatient() {
        val currentState = _uiState.value as? WearDashboardUiState.Success ?: return
        if (currentState.allPatients.size <= 1) return

        val currentIndex = currentState.allPatients.indexOfFirst { it.patientId == currentState.selectedPatient.patientId }
        val nextIndex = (currentIndex + 1) % currentState.allPatients.size
        val nextPatient = currentState.allPatients[nextIndex]

        selectPatient(nextPatient)
    }

    fun selectPatient(patient: ConnectionItem) {
        val currentState = _uiState.value as? WearDashboardUiState.Success ?: return
        viewModelScope.launch {
            preferencesRepository.setSelectedPatient(patient.patientId)
            loadPatientDetails(patient, currentState.allPatients)
        }
    }

    private fun loadDashboardData(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            loadDashboardDataInternal(isRefreshing)
        }
    }

    private suspend fun loadDashboardDataInternal(isRefreshing: Boolean = false) {
        if (!isRefreshing && _uiState.value !is WearDashboardUiState.Success) {
            _uiState.value = WearDashboardUiState.Loading
        }

        val connectionsResult = repository.getConnections()
        connectionsResult.fold(
            onSuccess = { patients ->
                if (patients.isEmpty()) {
                    _uiState.value = WearDashboardUiState.Error("No hay pacientes asociados a esta cuenta.")
                    return@fold
                }

                val savedPatientId = userSettings.selectedPatientId
                val targetPatient = patients.find { it.patientId == savedPatientId } ?: patients.first()

                // Carga ultrarrápida: reflejar la última medición de getConnections de inmediato
                val quickMeasurement = targetPatient.effectiveMeasurement
                val current = _uiState.value
                if (quickMeasurement != null && current is WearDashboardUiState.Success) {
                    val isSensorActive = current.sensor == null || ((current.sensor.getRemainingDays() ?: 1) > 0 && current.sensor.isSensorActive != false)
                    val isStale = quickMeasurement.isStale() || !isSensorActive
                    val updatedText = if (isStale) "Desactualizado • ${quickMeasurement.getDisplayTime()}" else quickMeasurement.getDisplayTime()
                    _uiState.value = current.copy(
                        selectedPatient = targetPatient,
                        allPatients = patients,
                        currentMeasurement = quickMeasurement,
                        lastUpdatedText = updatedText,
                        isRefreshing = true
                    )
                }

                loadPatientDetails(targetPatient, patients)
            },
            onFailure = { error ->
                if (error is com.example.opengluco.core.data.AuthExpiredException) {
                    _uiState.value = WearDashboardUiState.NeedsLogin
                    return@fold
                }

                // Stale-While-Revalidate: si ya tenemos Success, no destruir la vista con pantalla de error
                val current = _uiState.value
                if (current is WearDashboardUiState.Success) {
                    _uiState.value = current.copy(isRefreshing = false)
                    return@fold
                }

                // Fallback a lecturas locales si hay un error de red
                val localHistory = preferencesRepository.localDatabase.getHistoricalReadingsList(1, null)
                if (localHistory.isNotEmpty()) {
                    val lastM = localHistory.lastOrNull()
                    val dummyPatient = ConnectionItem(
                        id = userSettings.selectedPatientId.ifBlank { "local" },
                        patientId = userSettings.selectedPatientId.ifBlank { "local" },
                        firstName = "Paciente",
                        glucoseItem = lastM
                    )
                    _uiState.value = WearDashboardUiState.Success(
                        selectedPatient = dummyPatient,
                        allPatients = listOf(dummyPatient),
                        currentMeasurement = lastM,
                        graphHistory = localHistory,
                        sensor = null,
                        unit = userSettings.unit,
                        lowThreshold = userSettings.lowThreshold,
                        highThreshold = userSettings.highThreshold,
                        lastUpdatedText = "Sin red • ${lastM?.getDisplayTime() ?: "Local"}",
                        isRefreshing = false
                    )
                } else {
                    val msg = if (error is com.example.opengluco.core.data.NetworkException) {
                        "Sin conexión con el servidor"
                    } else {
                        error.message ?: "Error al conectar con OpenGluco"
                    }
                    _uiState.value = WearDashboardUiState.Error(msg)
                }
            }
        )
    }

    private suspend fun loadPatientDetails(patient: ConnectionItem, allPatients: List<ConnectionItem>) {
        preferencesRepository.loadPatientHistory(patient.patientId)
        val graphResult = repository.getPatientGraph(patient.patientId)
        val history = graphResult.getOrNull()?.graphData.orEmpty()
        val latestMeasurement = patient.effectiveMeasurement ?: history.lastOrNull()
            ?: preferencesRepository.localDatabase.getLatestReading(patient.patientId)
            ?: preferencesRepository.localDatabase.getLatestReading(null)
        val graphObj = graphResult.getOrNull()
        val activeSensor = if (graphObj != null) graphObj.resolvedSensor else patient.sensor?.takeIf { it.isValid }

        val baseHistory = if (history.isNotEmpty()) {
            history
        } else {
            preferencesRepository.localDatabase.getHistoricalReadingsList(1, patient.patientId)
                .ifEmpty { preferencesRepository.localDatabase.getHistoricalReadingsList(1, null) }
        }

        // Unificar historial y medición actual en tiempo real ordenada por timestamp
        val combinedHistory = if (latestMeasurement != null && baseHistory.none { it.timestamp == latestMeasurement.timestamp && !it.timestamp.isNullOrBlank() }) {
            (baseHistory + latestMeasurement).distinctBy { it.timestamp ?: it.factoryTimestamp ?: it.numericValue.toString() }
        } else {
            baseHistory
        }.filter { it.numericValue > 0 }.sortedBy { it.getEpochMillis() }

        // Guardar lecturas en el historial aislado del paciente y en DataStore para Complicaciones
        if (combinedHistory.isNotEmpty()) {
            preferencesRepository.saveHistoricalReadings(combinedHistory, patient.patientId)
        }
        latestMeasurement?.let {
            preferencesRepository.saveLastMeasurement(
                value = it.numericValue,
                trend = it.trendArrow ?: 3,
                timestamp = it.timestamp ?: ""
            )
        }

        val isSensorActive = activeSensor != null && (activeSensor.getRemainingDays() ?: 0) > 0 && activeSensor.isSensorActive != false
        val isStale = latestMeasurement == null || latestMeasurement.isStale() || !isSensorActive
        val updatedText = if (isStale) {
            val t = latestMeasurement?.getDisplayTime() ?: "--:--"
            if (!isSensorActive) "Sin sensor • $t" else "Desactualizado • $t"
        } else {
            latestMeasurement.getDisplayTime()
        }

        _uiState.value = WearDashboardUiState.Success(
            selectedPatient = patient,
            allPatients = allPatients,
            currentMeasurement = latestMeasurement,
            graphHistory = combinedHistory,
            sensor = activeSensor,
            unit = userSettings.unit,
            lowThreshold = userSettings.lowThreshold,
            highThreshold = userSettings.highThreshold,
            lastUpdatedText = updatedText,
            isRefreshing = false
        )
    }
}
