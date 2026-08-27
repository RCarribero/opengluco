package com.example.opengluco.wear.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.data.UserSettings
import com.example.opengluco.wear.ui.theme.ClinicalLowCoral
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary
import com.example.opengluco.wear.ui.theme.WearDarkBackground
import com.example.opengluco.wear.ui.theme.WearPrimary
import com.example.opengluco.wear.ui.theme.WearSurface
import kotlinx.coroutines.launch

@Composable
fun WearSettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = UserSettings())
    val scrollState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    var activeNotice by remember { mutableStateOf(WearLegalNoticeType.NONE) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val alarmRepository = remember { AlarmRepository(context) }
    val alarms by alarmRepository.alarmsFlow.collectAsState(initial = emptyList())
    val activeAlarmCount = alarms.count { it.enabled }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WearDarkBackground)
    ) {
        TimeText {
            time()
        }

        ScalingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    text = "Ajustes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WearPrimary,
                    textAlign = TextAlign.Center
                )
            }

            // 1. SELECTOR UNIDAD DE GLUCOSA
            item {
                Text(
                    text = "Unidad de medida",
                    fontSize = 11.sp,
                    color = ClinicalTextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        val nextUnit = if (settings.unit == GlucoseUnit.MGDL) GlucoseUnit.MMOL else GlucoseUnit.MGDL
                        scope.launch { preferencesRepository.setUnit(nextUnit) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Unidad: ${settings.unit.label}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2. ALERTAS HÁPTICAS
            item {
                Button(
                    onClick = {
                        scope.launch { preferencesRepository.setHapticAlerts(!settings.hapticAlertsEnabled) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    val status = if (settings.hapticAlertsEnabled) "Activadas" else "Desactivadas"
                    Text(
                        text = "Vibración: $status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2.5 ESTADO DE ALARMAS (solo lectura - gestion desde movil)
            item {
                Button(
                    onClick = { /* Solo informativo - gestion desde movil */ },
                    colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
                    enabled = false
                ) {
                    Text(
                        text = if (activeAlarmCount > 0) {
                            "Alarmas: $activeAlarmCount activas"
                        } else {
                            "Alarmas: Sin configurar"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (activeAlarmCount > 0) WearPrimary else ClinicalTextSecondary
                    )
                }
            }

            item {
                Text(
                    text = "Gestiona alarmas desde el movil",
                    fontSize = 9.sp,
                    color = ClinicalTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 3. SECCIÓN INFORMACIÓN LEGAL Y REGULATORIA
            item {
                Text(
                    text = "Información Legal",
                    fontSize = 11.sp,
                    color = ClinicalTextSecondary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            item {
                Button(
                    onClick = { activeNotice = WearLegalNoticeType.MEDICAL_DISCLAIMER },
                    colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Descargo Médico",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Button(
                    onClick = { activeNotice = WearLegalNoticeType.TRADEMARKS },
                    colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Marcas y No Afiliación",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Button(
                    onClick = { activeNotice = WearLegalNoticeType.PRIVACY_GDPR },
                    colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Privacidad y Salud (RGPD)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 4. SECCIÓN DATOS Y PRIVACIDAD / DERECHO AL OLVIDO
            item {
                Text(
                    text = "Datos y Sesión",
                    fontSize = 11.sp,
                    color = ClinicalTextSecondary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            item {
                Button(
                    onClick = { activeNotice = WearLegalNoticeType.DELETE_CONFIRMATION },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalLowCoral.copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Borrar Datos Locales",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            preferencesRepository.clearSession()
                            onLogoutSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalSurfaceCard),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Cerrar Sesión",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF87171)
                    )
                }
            }

            // 5. PIE DE PÁGINA LEGAL PASIVO PERMANENTE
            item {
                Spacer(modifier = Modifier.height(10.dp))
                WearPassiveLegalFooter()
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Modal emergente de descargos legales o confirmación de borrado
        WearLegalNoticeDialog(
            type = activeNotice,
            onDismiss = { activeNotice = WearLegalNoticeType.NONE },
            onConfirmDelete = {
                scope.launch {
                    preferencesRepository.purgeAllLocalData()
                    onLogoutSuccess()
                }
            }
        )
    }
}
