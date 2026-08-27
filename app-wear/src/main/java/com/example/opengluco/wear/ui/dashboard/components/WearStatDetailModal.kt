package com.example.opengluco.wear.ui.dashboard.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import com.example.opengluco.wear.ui.theme.ClinicalBackground
import com.example.opengluco.wear.ui.theme.ClinicalMint
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceOrb
import com.example.opengluco.wear.ui.theme.ClinicalTextPrimary
import com.example.opengluco.wear.ui.theme.ClinicalTextSecondary

enum class DetailModalType {
    NONE,
    GLUCOSE_STATS,
    TREND_INFO,
    SENSOR_INFO
}

@Composable
fun WearStatDetailModal(
    type: DetailModalType,
    avgVal: Double,
    minVal: Double,
    maxVal: Double,
    sensorDays: Int,
    sensorSerial: String,
    trendText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (type == DetailModalType.NONE) return

    val scrollState = rememberScalingLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground.copy(alpha = 0.94f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
            }

            when (type) {
                DetailModalType.GLUCOSE_STATS -> {
                    item {
                        Text(
                            text = "Estadísticas del Día",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalMint,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClinicalSurfaceOrb)
                                .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StatRow("Promedio:", "${avgVal.toInt()} mg/dL")
                                StatRow("Mínimo:", "${minVal.toInt()} mg/dL")
                                StatRow("Máximo:", "${maxVal.toInt()} mg/dL")
                            }
                        }
                    }
                }

                DetailModalType.TREND_INFO -> {
                    item {
                        Text(
                            text = "Tendencia de Glucosa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalMint,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClinicalSurfaceOrb)
                                .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = trendText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClinicalTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Medición cada 1 min continua vía FreeStyle Libre",
                                    fontSize = 10.sp,
                                    color = ClinicalTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                DetailModalType.SENSOR_INFO -> {
                    item {
                        Text(
                            text = "Sensor FreeStyle Libre",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalMint,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClinicalSurfaceOrb)
                                .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StatRow("Días restantes:", "$sensorDays días")
                                StatRow("Número de Serie:", sensorSerial.ifBlank { "Activo" })
                                StatRow("Estado:", if (sensorDays > 0) "Operativo" else "Caducado")
                            }
                        }
                    }
                }

                DetailModalType.NONE -> {}
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalSurfaceCard),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Cerrar", fontSize = 11.sp, color = ClinicalTextPrimary)
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 10.sp, color = ClinicalTextSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ClinicalTextPrimary)
    }
}
