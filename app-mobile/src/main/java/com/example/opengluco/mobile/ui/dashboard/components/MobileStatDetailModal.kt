package com.example.opengluco.mobile.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.opengluco.mobile.ui.theme.ClinicalTheme

enum class DetailModalType {
    NONE,
    GLUCOSE_STATS,
    TREND_INFO,
    SENSOR_INFO
}

@Composable
fun MobileStatDetailModal(
    type: DetailModalType,
    avgVal: Double,
    minVal: Double,
    maxVal: Double,
    tirPercent: Int = 100,
    sensorDays: Int = 14,
    sensorSerial: String = "",
    trendText: String = "Estable",
    trendSymbol: String = "→",
    onDismiss: () -> Unit
) {
    if (type == DetailModalType.NONE) return
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
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val title = when (type) {
                        DetailModalType.GLUCOSE_STATS -> "Estadísticas del Período"
                        DetailModalType.TREND_INFO -> "Dinámica de Glucosa"
                        DetailModalType.SENSOR_INFO -> "Estado del Sensor"
                        DetailModalType.NONE -> ""
                    }
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.mint
                    )
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

                when (type) {
                    DetailModalType.GLUCOSE_STATS -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceCard)
                                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatRow("Tiempo en Rango (TIR):", "$tirPercent %", highlight = true)
                                StatRow("Promedio Registrado:", "${avgVal.toInt()} mg/dL")
                                StatRow("Mínimo Registrado:", "${minVal.toInt()} mg/dL")
                                StatRow("Máximo Registrado:", "${maxVal.toInt()} mg/dL")
                                StatRow("Rango Clínico Objetivo:", "70 - 180 mg/dL")
                            }
                        }
                    }

                    DetailModalType.TREND_INFO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceCard)
                                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "$trendSymbol $trendText",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StatRow("Velocidad de cambio:", when {
                                    trendText.contains("rápido", ignoreCase = true) -> "> 2.0 mg/dL / min"
                                    trendText.contains("Bajando", ignoreCase = true) || trendText.contains("Subiendo", ignoreCase = true) -> "1.0 - 2.0 mg/dL / min"
                                    else -> "< 1.0 mg/dL / min"
                                })
                                StatRow("Intervalo de telemetría:", "1 minuto continuo")
                                StatRow("Algoritmo de predicción:", "FreeStyle Libre Continuous")
                            }
                        }
                    }

                    DetailModalType.SENSOR_INFO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceCard)
                                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatRow("Días restantes:", "$sensorDays días", highlight = sensorDays > 2)
                                StatRow("Número de Serie (S/N):", sensorSerial.ifBlank { "Sensor Vinculado" })
                                StatRow("Estado Operativo:", if (sensorDays > 0) "Activo y Calibrado" else "Caducado")
                                StatRow("Tipo de Dispositivo:", "FreeStyle Libre Sensor")
                            }
                        }
                    }

                    DetailModalType.NONE -> {}
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.mint,
                        contentColor = if (colors.isDark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, highlight: Boolean = false) {
    val colors = ClinicalTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = colors.textSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) colors.mint else colors.textPrimary
        )
    }
}
