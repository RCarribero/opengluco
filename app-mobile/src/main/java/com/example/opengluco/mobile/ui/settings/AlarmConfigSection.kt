package com.example.opengluco.mobile.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.GlucoseAlarm
import com.example.opengluco.mobile.service.MobileAlarmSyncHelper
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

// Paleta clinica oficial
private val BgCard = Color(0xFF161A22)
private val BgCardInner = Color(0xFF1E232D)
private val BorderColor = Color(0xFF2D3748)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF94A3B8)
private val TextMuted = Color(0xFF64748B)

private val ColorMint = Color(0xFF4ADE80)
private val ColorLowYellow = Color(0xFFFBBF24)
private val ColorUrgentRed = Color(0xFFEF4444)
private val ColorAmberHigh = Color(0xFFFBBF24)
private val ColorOrangeHigh = Color(0xFFFB923C)
private val ColorArcticCyan = Color(0xFF38BDF8)

/**
 * Seccion de configuracion de alarmas con soporte bidireccional mg/dL y mmol/L.
 */
@Composable
fun AlarmConfigSection(
    alarmRepository: AlarmRepository,
    unit: GlucoseUnit = GlucoseUnit.MGDL,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    onConfigureTargetRange: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val alarms by alarmRepository.alarmsFlow.collectAsState(initial = emptyList())

    LaunchedEffect(alarms) {
        if (alarms.isNotEmpty()) {
            MobileAlarmSyncHelper.syncAlarmsToWear(context, alarmRepository)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedAlarmTypeForCreation by remember { mutableStateOf(AlarmType.LOW) }
    var alarmToEdit by remember { mutableStateOf<GlucoseAlarm?>(null) }

    val lowAlarms = alarms.filter { it.type == AlarmType.LOW }
    val highAlarms = alarms.filter { it.type == AlarmType.HIGH }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Alarmas de Glucosa",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Alertas sonoras y en pantalla (${unit.label})",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorArcticCyan.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${alarms.size}/10 activas",
                    color = ColorArcticCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 0. Resumen Visual de Zonas Clínicas
        ClinicalRangeVisualCard(
            unit = unit,
            targetLow = targetLow,
            targetHigh = targetHigh,
            onConfigureClick = onConfigureTargetRange
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Subseccion: Alarmas de Glucosa Baja
        AlarmSubsection(
            title = "Alarmas de Glucosa Baja",
            subtitle = "Alertas cuando la glucosa desciende de tu objetivo",
            accentColor = ColorLowYellow,
            icon = Icons.Default.WarningAmber,
            alarms = lowAlarms,
            unit = unit,
            onToggle = { id -> coroutineScope.launch { alarmRepository.toggleAlarm(id) } },
            onDelete = { id -> coroutineScope.launch { alarmRepository.deleteAlarm(id) } },
            onEdit = { alarm -> alarmToEdit = alarm },
            onAddClick = {
                selectedAlarmTypeForCreation = AlarmType.LOW
                showCreateDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subseccion: Alarmas de Glucosa Alta
        AlarmSubsection(
            title = "Alarmas de Glucosa Alta",
            subtitle = "Alertas cuando la glucosa supera tu objetivo",
            accentColor = ColorAmberHigh,
            icon = Icons.Default.NotificationsActive,
            alarms = highAlarms,
            unit = unit,
            onToggle = { id -> coroutineScope.launch { alarmRepository.toggleAlarm(id) } },
            onDelete = { id -> coroutineScope.launch { alarmRepository.deleteAlarm(id) } },
            onEdit = { alarm -> alarmToEdit = alarm },
            onAddClick = {
                selectedAlarmTypeForCreation = AlarmType.HIGH
                showCreateDialog = true
            }
        )

        if (alarms.size < 10) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = {
                    selectedAlarmTypeForCreation = if (lowAlarms.size <= highAlarms.size) AlarmType.LOW else AlarmType.HIGH
                    showCreateDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorMint,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Añadir Nueva Alarma",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Dialogo de creacion
    if (showCreateDialog) {
        AlarmCreationDialog(
            existingAlarm = null,
            initialType = selectedAlarmTypeForCreation,
            unit = unit,
            onDismiss = { showCreateDialog = false },
            onSave = { newAlarm ->
                coroutineScope.launch { alarmRepository.addAlarm(newAlarm) }
                showCreateDialog = false
            }
        )
    }

    // Dialogo de edicion
    if (alarmToEdit != null) {
        AlarmCreationDialog(
            existingAlarm = alarmToEdit,
            initialType = alarmToEdit!!.type,
            unit = unit,
            onDismiss = { alarmToEdit = null },
            onSave = { updatedAlarm ->
                coroutineScope.launch { alarmRepository.updateAlarm(updatedAlarm) }
                alarmToEdit = null
            }
        )
    }
}

@Composable
private fun ClinicalRangeVisualCard(
    unit: GlucoseUnit,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    onConfigureClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rango Objetivo Clínico",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    val targetLabel = if (unit == GlucoseUnit.MMOL) {
                        String.format(Locale.US, "%.1f - %.1f mmol/L", targetLow / 18.0182, targetHigh / 18.0182)
                    } else {
                        "$targetLow - $targetHigh mg/dL"
                    }
                    Text(
                        text = targetLabel,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorMint
                    )
                }

                if (onConfigureClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorMint.copy(alpha = 0.15f))
                            .clickable(onClick = onConfigureClick)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Ajustar Rango",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorMint
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Barra de 5 Zonas Clínicas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(ColorUrgentRed))
                Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(ColorLowYellow))
                Box(modifier = Modifier.weight(0.40f).fillMaxHeight().background(ColorMint))
                Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(ColorAmberHigh))
                Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(ColorOrangeHigh))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val uLow = if (unit == GlucoseUnit.MMOL) "<3.0 Muy Bajo" else "<54 Muy Bajo"
                val inRange = if (unit == GlucoseUnit.MMOL) "Objetivo" else "$targetLow-$targetHigh Objetivo"
                val uHigh = if (unit == GlucoseUnit.MMOL) ">13.9 Muy Alto" else ">250 Muy Alto"
                Text(uLow, fontSize = 9.sp, color = ColorUrgentRed)
                Text(inRange, fontSize = 9.sp, color = ColorMint)
                Text(uHigh, fontSize = 9.sp, color = ColorOrangeHigh)
            }
        }
    }
}

@Composable
private fun AlarmSubsection(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    alarms: List<GlucoseAlarm>,
    unit: GlucoseUnit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (GlucoseAlarm) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }

            if (alarms.size < GlucoseAlarm.MAX_ALARMS_PER_TYPE) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .clickable(onClick = onAddClick)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Anadir",
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Anadir",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (alarms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            alarms.forEach { alarm ->
                AlarmCard(
                    alarm = alarm,
                    unit = unit,
                    onToggle = { onToggle(alarm.id) },
                    onDelete = { onDelete(alarm.id) },
                    onClick = { onEdit(alarm) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No hay alarmas configuradas para esta categoria.",
                color = TextMuted,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: GlucoseAlarm,
    unit: GlucoseUnit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isLow = alarm.type == AlarmType.LOW

    val severityColor = when (alarm.severity) {
        AlarmSeverity.URGENT -> ColorUrgentRed
        AlarmSeverity.ALERT -> if (isLow) ColorLowYellow else ColorOrangeHigh
        AlarmSeverity.INFORMATIVE -> ColorArcticCyan
    }

    val severityLabel = when (alarm.severity) {
        AlarmSeverity.URGENT -> "Urgente"
        AlarmSeverity.ALERT -> "Alerta"
        AlarmSeverity.INFORMATIVE -> "Informativa"
    }

    val valueDisplay = if (unit == GlucoseUnit.MMOL) {
        String.format(Locale.US, "%.1f %s", alarm.thresholdMgDl / 18.0182, unit.label)
    } else {
        "${alarm.thresholdMgDl} ${unit.label}"
    }

    val cooldownText = when (alarm.cooldownMinutes) {
        5 -> "Cada 5 min"
        10 -> "Cada 10 min"
        15 -> "Cada 15 min"
        30 -> "Cada 30 min"
        else -> "No repetir"
    }

    val scheduleText = if (alarm.isAllDay) {
        "24 horas"
    } else {
        "${alarm.activeStartHour.toString().padStart(2, '0')}:${alarm.activeStartMinute.toString().padStart(2, '0')} - ${alarm.activeEndHour.toString().padStart(2, '0')}:${alarm.activeEndMinute.toString().padStart(2, '0')}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCardInner)
            .border(1.dp, if (alarm.enabled) severityColor.copy(alpha = 0.35f) else BorderColor, RoundedCornerShape(12.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de severidad / valor
        Box(
            modifier = Modifier
                .size(4.dp, 40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (alarm.enabled) severityColor else TextMuted)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueDisplay,
                    color = if (alarm.enabled) TextPrimary else TextMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(severityColor.copy(alpha = if (alarm.enabled) 0.18f else 0.08f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = severityLabel,
                        color = if (alarm.enabled) severityColor else TextMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = scheduleText,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "•  $cooldownText",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = alarm.enabled,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = severityColor,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = BgCard
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = TextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Dialogo modal de creacion / edicion con:
 * 1. Soporte completo de unidades (mg/dL o mmol/L).
 * 2. Entrada de teclado directa editable tocando el numero.
 * 3. Deslizador sincronizado en tiempo real.
 * 4. Botones de preajuste clinico rapido.
 * 5. Selector visual de severidad (Urgente, Alerta, Informativa).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCreationDialog(
    existingAlarm: GlucoseAlarm?,
    initialType: AlarmType = AlarmType.LOW,
    unit: GlucoseUnit = GlucoseUnit.MGDL,
    onDismiss: () -> Unit,
    onSave: (GlucoseAlarm) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var alarmType by remember { mutableStateOf(existingAlarm?.type ?: initialType) }
    val isMmol = unit == GlucoseUnit.MMOL

    // Umbral interno en mg/dL (rango 40..400)
    var thresholdMgDl by remember {
        mutableFloatStateOf(
            existingAlarm?.thresholdMgDl?.toFloat()
                ?: if (initialType == AlarmType.LOW) 70f else 180f
        )
    }

    // Texto editable directamente por el usuario
    var textInput by remember(thresholdMgDl, unit) {
        mutableStateOf(
            if (isMmol) {
                String.format(Locale.US, "%.1f", thresholdMgDl / 18.0182)
            } else {
                thresholdMgDl.toInt().toString()
            }
        )
    }

    var severity by remember { mutableStateOf(existingAlarm?.severity ?: AlarmSeverity.ALERT) }
    var cooldownMinutes by remember { mutableIntStateOf(existingAlarm?.cooldownMinutes ?: 15) }
    var isAllDay by remember { mutableStateOf(existingAlarm?.isAllDay ?: true) }
    var startHour by remember { mutableIntStateOf(existingAlarm?.activeStartHour ?: 22) }
    var startMinute by remember { mutableIntStateOf(existingAlarm?.activeStartMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(existingAlarm?.activeEndHour ?: 8) }
    var endMinute by remember { mutableIntStateOf(existingAlarm?.activeEndMinute ?: 0) }

    var expandedCooldown by remember { mutableStateOf(false) }

    // Color dinamico segun severidad y nivel
    val dynamicColor = when (severity) {
        AlarmSeverity.URGENT -> ColorUrgentRed
        AlarmSeverity.ALERT -> if (alarmType == AlarmType.LOW) ColorLowYellow else ColorOrangeHigh
        AlarmSeverity.INFORMATIVE -> ColorArcticCyan
    }

    val categoryTag = when {
        thresholdMgDl <= 55 -> "[HIPOGLUCEMIA URGENTE]"
        thresholdMgDl < 70 -> "[GLUCOSA BAJA]"
        thresholdMgDl in 70.0..180.0 -> "[RANGO OBJETIVO]"
        thresholdMgDl <= 250 -> "[GLUCOSA ELEVADA]"
        else -> "[HIPERGLUCEMIA URGENTE]"
    }

    val cooldownOptions = listOf(
        5 to "5 minutos",
        10 to "10 minutos",
        15 to "15 minutos",
        30 to "30 minutos",
        0 to "No repetir"
    )

    val cooldownLabel = cooldownOptions.find { it.first == cooldownMinutes }?.second ?: "15 minutos"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0A0C10)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (existingAlarm == null) "Nueva Alarma" else "Editar Alarma",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configuracion de disparo (${unit.label})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Text("X", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Selector de Tipo de Alarma (Baja vs Alta)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (alarmType == AlarmType.LOW) ColorLowYellow.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable {
                                alarmType = AlarmType.LOW
                                if (thresholdMgDl > 100) {
                                    thresholdMgDl = 70f
                                    textInput = if (isMmol) String.format(Locale.US, "%.1f", 70f / 18.0182) else "70"
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Glucosa Baja",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarmType == AlarmType.LOW) ColorLowYellow else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (alarmType == AlarmType.HIGH) ColorAmberHigh.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable {
                                alarmType = AlarmType.HIGH
                                if (thresholdMgDl < 140) {
                                    thresholdMgDl = 180f
                                    textInput = if (isMmol) String.format(Locale.US, "%.1f", 180f / 18.0182) else "180"
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Glucosa Alta",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarmType == AlarmType.HIGH) ColorAmberHigh else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Tarjeta Interactiva de Umbral con ENTRADA NUMERICA DIRECTA y DESLIZADOR
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, dynamicColor.copy(alpha = 0.40f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Umbral de Disparo",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Campo de Texto Grande y Directamente Editable
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { input ->
                                    textInput = input
                                    if (isMmol) {
                                        val parsed = input.replace(',', '.').toDoubleOrNull()
                                        if (parsed != null && parsed in 2.2..22.2) {
                                            thresholdMgDl = (parsed * 18.0182).toFloat().coerceIn(40f, 400f)
                                        }
                                    } else {
                                        val parsed = input.toIntOrNull()
                                        if (parsed != null && parsed in 40..400) {
                                            thresholdMgDl = parsed.toFloat()
                                        }
                                    }
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dynamicColor,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (isMmol) KeyboardType.Decimal else KeyboardType.Number
                                ),
                                singleLine = true,
                                modifier = Modifier.width(140.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = dynamicColor,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = BgCardInner,
                                    unfocusedContainerColor = BgCardInner
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = unit.label,
                                color = TextSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toca el numero para escribirlo directamente",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tag de categoria clinica
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(dynamicColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = categoryTag,
                                color = dynamicColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Deslizador continuo
                        if (isMmol) {
                            val currentMmol = thresholdMgDl / 18.0182f
                            Slider(
                                value = currentMmol.coerceIn(2.2f, 22.2f),
                                onValueChange = { newMmol ->
                                    val rounded = (newMmol * 10).roundToInt() / 10f
                                    thresholdMgDl = (rounded * 18.0182f).coerceIn(40f, 400f)
                                    textInput = String.format(Locale.US, "%.1f", rounded)
                                },
                                valueRange = 2.2f..22.2f,
                                steps = 199,
                                colors = SliderDefaults.colors(
                                    thumbColor = dynamicColor,
                                    activeTrackColor = dynamicColor,
                                    inactiveTrackColor = BorderColor
                                )
                            )
                        } else {
                            Slider(
                                value = thresholdMgDl.coerceIn(40f, 400f),
                                onValueChange = { newMg ->
                                    val rounded = newMg.roundToInt()
                                    thresholdMgDl = rounded.toFloat()
                                    textInput = rounded.toString()
                                },
                                valueRange = 40f..400f,
                                steps = 359,
                                colors = SliderDefaults.colors(
                                    thumbColor = dynamicColor,
                                    activeTrackColor = dynamicColor,
                                    inactiveTrackColor = BorderColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Preajustes Clinicos Rapidos
                Text("Preajustes Rapidos", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (alarmType == AlarmType.LOW) {
                        val presets = listOf(
                            Triple(55f, "55", AlarmSeverity.URGENT),
                            Triple(70f, "70", AlarmSeverity.ALERT),
                            Triple(80f, "80", AlarmSeverity.INFORMATIVE)
                        )
                        presets.forEach { (mgVal, labelMg, sev) ->
                            val label = if (isMmol) String.format(Locale.US, "%.1f", mgVal / 18.0182) else labelMg
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BgCard)
                                    .border(1.dp, if (thresholdMgDl.roundToInt() == mgVal.roundToInt()) dynamicColor else BorderColor, RoundedCornerShape(10.dp))
                                    .clickable {
                                        thresholdMgDl = mgVal
                                        severity = sev
                                        textInput = if (isMmol) String.format(Locale.US, "%.1f", mgVal / 18.0182) else mgVal.toInt().toString()
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$label ${unit.label}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = sev.name, color = TextMuted, fontSize = 9.sp)
                                }
                            }
                        }
                    } else {
                        val presets = listOf(
                            Triple(160f, "160", AlarmSeverity.INFORMATIVE),
                            Triple(180f, "180", AlarmSeverity.ALERT),
                            Triple(240f, "240", AlarmSeverity.URGENT)
                        )
                        presets.forEach { (mgVal, labelMg, sev) ->
                            val label = if (isMmol) String.format(Locale.US, "%.1f", mgVal / 18.0182) else labelMg
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BgCard)
                                    .border(1.dp, if (thresholdMgDl.roundToInt() == mgVal.roundToInt()) dynamicColor else BorderColor, RoundedCornerShape(10.dp))
                                    .clickable {
                                        thresholdMgDl = mgVal
                                        severity = sev
                                        textInput = if (isMmol) String.format(Locale.US, "%.1f", mgVal / 18.0182) else mgVal.toInt().toString()
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$label ${unit.label}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = sev.name, color = TextMuted, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Selector de Severidad con Tarjetas Descriptivas
                Text("Severidad y Comportamiento", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val severityCards = listOf(
                        Triple(AlarmSeverity.URGENT, "Urgente (Rojo)", "Vibracion continua y sonido de maxima prioridad"),
                        Triple(AlarmSeverity.ALERT, "Alerta (Amarillo/Naranja)", "Aviso sonoro prioritario en pantalla y reloj"),
                        Triple(AlarmSeverity.INFORMATIVE, "Informativa (Azul)", "Notificacion silenciosa y discreta")
                    )

                    severityCards.forEach { (sev, title, desc) ->
                        val isSelected = severity == sev
                        val sColor = when (sev) {
                            AlarmSeverity.URGENT -> ColorUrgentRed
                            AlarmSeverity.ALERT -> ColorOrangeHigh
                            AlarmSeverity.INFORMATIVE -> ColorArcticCyan
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) sColor.copy(alpha = 0.12f) else BgCard)
                                .border(1.dp, if (isSelected) sColor else BorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    severity = sev
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(sColor.copy(alpha = if (isSelected) 1f else 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Text("v", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = desc,
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Cooldown / Repeticion
                Text("Intervalo de Repeticion", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCooldown,
                    onExpandedChange = { expandedCooldown = it }
                ) {
                    OutlinedTextField(
                        value = cooldownLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCooldown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorMint,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgCard,
                            unfocusedContainerColor = BgCard
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCooldown,
                        onDismissRequest = { expandedCooldown = false }
                    ) {
                        cooldownOptions.forEach { (minutes, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    cooldownMinutes = minutes
                                    expandedCooldown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Horario Activo (24 Horas vs Personalizado)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgCard)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = ColorMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Activa 24 Horas", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            Text("Monitoreo continuo ininterrumpido", color = TextMuted, fontSize = 10.5.sp)
                        }
                    }
                    Switch(
                        checked = isAllDay,
                        onCheckedChange = { isAllDay = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ColorMint,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BgCardInner
                        )
                    )
                }

                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgCardInner)
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hora Inicio", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = startHour.toString().padStart(2, '0'),
                                    onValueChange = { v ->
                                        val num = v.toIntOrNull()
                                        if (num != null && num in 0..23) startHour = num
                                    },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(color = TextPrimary, textAlign = TextAlign.Center)
                                )
                                Text(" : ", color = TextPrimary, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = startMinute.toString().padStart(2, '0'),
                                    onValueChange = { v ->
                                        val num = v.toIntOrNull()
                                        if (num != null && num in 0..59) startMinute = num
                                    },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(color = TextPrimary, textAlign = TextAlign.Center)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(BorderColor)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hora Fin", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = endHour.toString().padStart(2, '0'),
                                    onValueChange = { v ->
                                        val num = v.toIntOrNull()
                                        if (num != null && num in 0..23) endHour = num
                                    },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(color = TextPrimary, textAlign = TextAlign.Center)
                                )
                                Text(" : ", color = TextPrimary, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = endMinute.toString().padStart(2, '0'),
                                    onValueChange = { v ->
                                        val num = v.toIntOrNull()
                                        if (num != null && num in 0..59) endMinute = num
                                    },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(color = TextPrimary, textAlign = TextAlign.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // 7. Botones de Accion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BgCardInner,
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val finalThresholdMgDl = thresholdMgDl.roundToInt().coerceIn(40, 400)
                            val alarmToSave = GlucoseAlarm(
                                id = existingAlarm?.id ?: UUID.randomUUID().toString(),
                                type = alarmType,
                                thresholdMgDl = finalThresholdMgDl,
                                severity = severity,
                                cooldownMinutes = cooldownMinutes,
                                enabled = existingAlarm?.enabled ?: true,
                                activeStartHour = startHour,
                                activeStartMinute = startMinute,
                                activeEndHour = endHour,
                                activeEndMinute = endMinute,
                                isAllDay = isAllDay
                            )
                            onSave(alarmToSave)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dynamicColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Guardar Alarma", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
