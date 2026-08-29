package com.example.opengluco.wear.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.data.UserSettings
import com.example.opengluco.wear.ui.theme.ClinicalArcticCyan
import com.example.opengluco.wear.ui.theme.ClinicalBackground
import com.example.opengluco.wear.ui.theme.ClinicalLowCoral
import com.example.opengluco.wear.ui.theme.ClinicalMint
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceOrb
import com.example.opengluco.wear.ui.theme.ClinicalTextMuted
import com.example.opengluco.wear.ui.theme.ClinicalTextPrimary
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary
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

    LaunchedEffect(Unit) {
        try {
            val nodeClient = com.google.android.gms.wearable.Wearable.getNodeClient(context)
            val messageClient = com.google.android.gms.wearable.Wearable.getMessageClient(context)
            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                for (node in nodes) {
                    messageClient.sendMessage(node.id, "/opengluco_request_alarms_sync", byteArrayOf())
                }
            }
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground)
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

            // TÍTULO DE AJUSTES
            item {
                Text(
                    text = "Ajustes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClinicalMint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 1. SECCIÓN PREFERENCIAS CLÍNICAS
            item {
                Text(
                    text = "Preferencias",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClinicalArcticCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // Selector de Unidad
            item {
                CompactSettingsRow(
                    title = "Unidad",
                    value = settings.unit.label,
                    valueColor = ClinicalMint,
                    onClick = {
                        val nextUnit = if (settings.unit == GlucoseUnit.MGDL) GlucoseUnit.MMOL else GlucoseUnit.MGDL
                        scope.launch { preferencesRepository.setUnit(nextUnit) }
                    }
                )
            }

            // Vibración / Alertas Hápticas
            item {
                CompactSettingsRow(
                    title = "Vibración",
                    value = if (settings.hapticAlertsEnabled) "Activa" else "Inactiva",
                    valueColor = if (settings.hapticAlertsEnabled) ClinicalMint else ClinicalTextMuted,
                    onClick = {
                        scope.launch { preferencesRepository.setHapticAlerts(!settings.hapticAlertsEnabled) }
                    }
                )
            }

            // Estado de Alarmas
            item {
                CompactSettingsRow(
                    title = "Alarmas",
                    value = if (activeAlarmCount > 0) "$activeAlarmCount activas" else "Configurar",
                    valueColor = if (activeAlarmCount > 0) ClinicalMint else ClinicalTextSecondary,
                    onClick = {
                        scope.launch {
                            try {
                                val nodeClient = com.google.android.gms.wearable.Wearable.getNodeClient(context)
                                val messageClient = com.google.android.gms.wearable.Wearable.getMessageClient(context)
                                nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                                    for (node in nodes) {
                                        messageClient.sendMessage(node.id, "/opengluco_request_alarms_sync", byteArrayOf())
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                )
            }

            // 2. SECCIÓN LEGAL Y REGULATORIA
            item {
                Text(
                    text = "Legal y Seguridad",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClinicalArcticCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                CompactActionRow(
                    title = "Descargo Médico",
                    textColor = ClinicalTextPrimary,
                    onClick = { activeNotice = WearLegalNoticeType.MEDICAL_DISCLAIMER }
                )
            }

            item {
                CompactActionRow(
                    title = "Marcas y No Afiliación",
                    textColor = ClinicalTextPrimary,
                    onClick = { activeNotice = WearLegalNoticeType.TRADEMARKS }
                )
            }

            item {
                CompactActionRow(
                    title = "Privacidad RGPD",
                    textColor = ClinicalTextPrimary,
                    onClick = { activeNotice = WearLegalNoticeType.PRIVACY_GDPR }
                )
            }

            // 3. SECCIÓN DATOS Y SESIÓN
            item {
                Text(
                    text = "Sesión y Datos",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClinicalArcticCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                CompactActionRow(
                    title = "Borrar Datos Locales",
                    textColor = ClinicalLowCoral,
                    isDestructive = true,
                    onClick = { activeNotice = WearLegalNoticeType.DELETE_CONFIRMATION }
                )
            }

            item {
                CompactActionRow(
                    title = "Cerrar Sesión",
                    textColor = ClinicalLowCoral,
                    onClick = {
                        scope.launch {
                            preferencesRepository.clearSession()
                            onLogoutSuccess()
                        }
                    }
                )
            }

            // 4. PIE DE PÁGINA LEGAL PASIVO
            item {
                Spacer(modifier = Modifier.height(8.dp))
                WearPassiveLegalFooter()
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
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

@Composable
private fun CompactSettingsRow(
    title: String,
    value: String,
    valueColor: Color = ClinicalMint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ClinicalSurfaceCard)
            .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ClinicalTextPrimary
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun CompactActionRow(
    title: String,
    textColor: Color = ClinicalTextPrimary,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDestructive) ClinicalLowCoral.copy(alpha = 0.15f) else ClinicalSurfaceCard
    val borderColor = if (isDestructive) ClinicalLowCoral.copy(alpha = 0.4f) else ClinicalSurfaceBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
