package com.example.opengluco.mobile.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import com.example.opengluco.mobile.ui.theme.getClinicalStatusColor

@Composable
fun PatientHeaderChip(
    selectedPatient: ConnectionItem?,
    patientCount: Int,
    unit: GlucoseUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ClinicalTheme.colors
    val responsive = ClinicalTheme.responsive
    val displayName = selectedPatient?.fullName ?: "Paciente"
    val measurement = selectedPatient?.effectiveMeasurement
    val statusColor = if (measurement != null) getClinicalStatusColor(measurement.numericValue) else colors.mint

    val chipMaxWidth = when {
        responsive.isNarrowPhone -> 150.dp
        responsive.widthClass == com.example.opengluco.mobile.ui.theme.WindowWidthClass.COMPACT -> 190.dp
        else -> 320.dp
    }
    val nameFontSize = responsive.clampedSp(13f, maxScale = 1.20f)

    Box(
        modifier = modifier
            .widthIn(max = chipMaxWidth)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceCard)
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Avatar Iniciales
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceOrb)
                    .border(1.dp, statusColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                Text(
                    text = initial,
                    fontSize = responsive.clampedSp(11f, maxScale = 1.20f),
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            // Nombre Paciente
            Text(
                text = displayName,
                fontSize = nameFontSize,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            // Indicador de cuenta si hay más de 1
            if (patientCount > 1) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.mint.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$patientCount",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.mint
                    )
                }
            }

            // Chevron ▾
            Text(
                text = "▾",
                fontSize = 12.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PatientSelectorModal(
    patients: List<ConnectionItem>,
    selectedPatient: ConnectionItem?,
    unit: GlucoseUnit,
    onSelectPatient: (ConnectionItem) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = ClinicalTheme.colors
    val responsive = ClinicalTheme.responsive

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
            modifier = Modifier
                .widthIn(max = responsive.dialogMaxWidth)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Pacientes",
                            tint = colors.mint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Seleccionar Paciente",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${patients.size} ${if (patients.size == 1) "paciente conectado" else "pacientes conectados"}",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable List of Patients
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    patients.forEach { patient ->
                        val isSelected = patient.patientId == selectedPatient?.patientId
                        val measurement = patient.effectiveMeasurement
                        val statusColor = if (measurement != null) getClinicalStatusColor(measurement.numericValue) else colors.mint
                        val glucoseStr = measurement?.getFormattedValue(unit == GlucoseUnit.MMOL) ?: "--"
                        val trendStr = measurement?.trendSymbol ?: ""

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) colors.surfaceCard else colors.surfaceCard.copy(alpha = 0.5f))
                                .border(
                                    1.dp,
                                    if (isSelected) colors.mint else colors.surfaceBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onSelectPatient(patient)
                                    onDismiss()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Avatar & Name
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) colors.mint.copy(alpha = 0.15f) else colors.surfaceOrb)
                                            .border(1.dp, if (isSelected) colors.mint else colors.surfaceBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initial = patient.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                                        Text(
                                            text = initial,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) colors.mint else colors.textPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = patient.fullName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val sensorName = patient.sensor?.sensorModelName ?: "FreeStyle Libre"
                                        val daysLeft = patient.sensor?.getRemainingDays()
                                        val subtext = if (daysLeft != null) "$sensorName • $daysLeft d" else sensorName
                                        Text(
                                            text = subtext,
                                            fontSize = 10.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                // Right: Glucose Badge & Checkmark
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (measurement != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(statusColor.copy(alpha = 0.12f))
                                                .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "$glucoseStr $trendStr",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(colors.mint),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Seleccionado",
                                                tint = if (colors.isDark) Color.Black else Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer
                Text(
                    text = "La telemetría, gráficas e historial de 90 días se adaptan en tiempo real al paciente seleccionado.",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
