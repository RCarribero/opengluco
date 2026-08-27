package com.example.opengluco.wear.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.example.opengluco.wear.ui.dashboard.components.ClinicalSparklineWithSensor
import com.example.opengluco.wear.ui.dashboard.components.DetailModalType
import com.example.opengluco.wear.ui.dashboard.components.DualFloatingOrbs
import com.example.opengluco.wear.ui.dashboard.components.PatientSelectorChip
import com.example.opengluco.wear.ui.dashboard.components.WearStatDetailModal
import com.example.opengluco.wear.ui.theme.ClinicalBackground
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard
import com.example.opengluco.wear.ui.theme.ClinicalTextPrimary
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary
import com.example.opengluco.wear.ui.settings.WearPassiveLegalFooter

@Composable
fun WearDashboardScreen(
    viewModel: WearDashboardViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScalingLazyListState()

    var activeModal by remember { mutableStateOf(DetailModalType.NONE) }

    LaunchedEffect(uiState) {
        if (uiState is WearDashboardUiState.NeedsLogin) {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground)
    ) {
        TimeText { time() }

        when (val state = uiState) {
            is WearDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            is WearDashboardUiState.Error -> {
                ScalingLazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        Text(
                            text = "Error al sincronizar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171),
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            text = state.message,
                            fontSize = 10.sp,
                            color = ClinicalTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    item {
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalSurfaceCard),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Reintentar", fontSize = 11.sp)
                        }
                    }
                }
            }

            is WearDashboardUiState.Success -> {
                val validValues: List<Double> = state.graphHistory.map { it.numericValue }.filter { it > 0.0 }
                val avgVal: Double = if (validValues.isNotEmpty()) validValues.average() else 0.0
                val minVal: Double = if (validValues.isNotEmpty()) validValues.minOrNull() ?: 0.0 else 0.0
                val maxVal: Double = if (validValues.isNotEmpty()) validValues.maxOrNull() ?: 0.0 else 0.0

                ScalingLazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // 1. FILA SUPERIOR: 2 ESFERAS FLOTANTES (GLUCOSA + TENDENCIA)
                    item {
                        DualFloatingOrbs(
                            measurement = state.currentMeasurement,
                            unit = state.unit,
                            onGlucoseOrbClick = { activeModal = DetailModalType.GLUCOSE_STATS },
                            onTrendOrbClick = { activeModal = DetailModalType.TREND_INFO }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // 2. FILA INFERIOR: GRÁFICA SPARKLINE + BADGE DÍAS DE SENSOR
                    item {
                        ClinicalSparklineWithSensor(
                            history = state.graphHistory,
                            sensor = state.sensor,
                            targetLow = state.selectedPatient.targetLow,
                            targetHigh = state.selectedPatient.targetHigh,
                            onSensorBadgeClick = { activeModal = DetailModalType.SENSOR_INFO }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 3. SELECTOR DE PACIENTE
                    if (state.allPatients.size > 1) {
                        item {
                            PatientSelectorChip(
                                selectedPatient = state.selectedPatient,
                                totalPatients = state.allPatients.size,
                                onSwitchPatient = { viewModel.switchPatient() }
                            )
                        }
                    }

                    // 4. BOTONES DE ACCIÓN: AJUSTES Y REFRESCO
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalSurfaceCard),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refrescar",
                                    tint = ClinicalTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Button(
                                onClick = onNavigateToSettings,
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalSurfaceCard),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ajustes",
                                    tint = ClinicalTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // 5. PIE DE PÁGINA LEGAL PASIVO
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        WearPassiveLegalFooter()
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Modal interactivo desplegable al tocar componentes
                WearStatDetailModal(
                    type = activeModal,
                    avgVal = avgVal,
                    minVal = minVal,
                    maxVal = maxVal,
                    sensorDays = state.sensor?.getRemainingDays() ?: 14,
                    sensorSerial = state.sensor?.serialNumber ?: "",
                    trendText = state.currentMeasurement?.trendText ?: "Estable",
                    onDismiss = { activeModal = DetailModalType.NONE }
                )
            }

            else -> {}
        }
    }
}
