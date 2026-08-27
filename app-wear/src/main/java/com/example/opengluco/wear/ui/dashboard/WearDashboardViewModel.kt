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
        val lastUpdatedText: String,
        val isRefreshing: Boolean = false
    ) : WearDashboardUiState
    data class Error(val message: String) : WearDashboardUiState
}

class WearDashboardViewModel(
    private val repository: OpenGlucoRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WearDashboardUiState>(WearDashboardUiState.Loading)
    val uiState: StateFlow<WearDashboardUiState> = _uiState.asStateFlow()

    private var userSettings: UserSettings = UserSettings()

    init {
        viewModelScope.launch {
            preferencesRepository.userSettingsFlow.collect { settings ->
                val previousToken = userSettings.token
                val previousUserId = userSettings.userId
                val previousUnit = userSettings.unit
                userSettings = settings

                if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
                    repository.setSession(settings.token, settings.userId)
                    val authChanged = previousToken != settings.token || previousUserId != settings.userId
                    if (authChanged || _uiState.value !is WearDashboardUiState.Success) {
                        loadDashboardDataInternal()
                    } else if (previousUnit != settings.unit) {
                        val current = _uiState.value
                        if (current is WearDashboardUiState.Success) {
                            _uiState.value = current.copy(unit = settings.unit)
                        }
                    }
                } else {
                    _uiState.value = WearDashboardUiState.NeedsLogin
                }
            }
        }

        // Bucle de actualización automática periódica cada 60 segundos
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000)
                if (_uiState.value is WearDashboardUiState.Success) {
                    loadDashboardDataInternal(isRefreshing = true)
                }
            }
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
                loadPatientDetails(targetPatient, patients)
            },
            onFailure = { error ->
                _uiState.value = WearDashboardUiState.Error(error.message ?: "Error al conectar con OpenGluco")
            }
        )
    }

    private suspend fun loadPatientDetails(patient: ConnectionItem, allPatients: List<ConnectionItem>) {
        preferencesRepository.loadPatientHistory(patient.patientId)
        val graphResult = repository.getPatientGraph(patient.patientId)
        val history = graphResult.getOrNull()?.graphData.orEmpty()
        val latestMeasurement = patient.effectiveMeasurement ?: history.lastOrNull()
        val activeSensor = patient.sensor ?: graphResult.getOrNull()?.activeSensors?.firstOrNull()

        // Unificar historial y medición actual en tiempo real ordenada por timestamp
        val combinedHistory = if (latestMeasurement != null && history.none { it.timestamp == latestMeasurement.timestamp && !it.timestamp.isNullOrBlank() }) {
            (history + latestMeasurement).distinctBy { it.timestamp ?: it.factoryTimestamp ?: it.numericValue.toString() }
        } else {
            history
        }.filter { it.numericValue > 0 }.sortedBy { it.getEpochMillis() }

        // Guardar lecturas en el historial aislado del paciente y en DataStore para Complicaciones
        preferencesRepository.saveHistoricalReadings(combinedHistory, patient.patientId)
        latestMeasurement?.let {
            preferencesRepository.saveLastMeasurement(
                value = it.numericValue,
                trend = it.trendArrow ?: 3,
                timestamp = it.timestamp ?: ""
            )
        }

        _uiState.value = WearDashboardUiState.Success(
            selectedPatient = patient,
            allPatients = allPatients,
            currentMeasurement = latestMeasurement,
            graphHistory = combinedHistory,
            sensor = activeSensor,
            unit = userSettings.unit,
            lastUpdatedText = latestMeasurement?.getDisplayTime() ?: "Ahora",
            isRefreshing = false
        )
    }
}
