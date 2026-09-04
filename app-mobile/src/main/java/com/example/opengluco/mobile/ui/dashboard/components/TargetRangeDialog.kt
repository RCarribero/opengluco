package com.example.opengluco.mobile.ui.dashboard.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TargetRangeDialog(
    initialLow: Int,
    initialHigh: Int,
    unit: GlucoseUnit,
    onDismiss: () -> Unit,
    onSave: (low: Int, high: Int) -> Unit
) {
    val colors = ClinicalTheme.colors
    val haptic = LocalHapticFeedback.current
    val isMmol = unit == GlucoseUnit.MMOL

    var lowMg by remember { mutableFloatStateOf(initialLow.toFloat().coerceIn(55f, 110f)) }
    var highMg by remember { mutableFloatStateOf(initialHigh.toFloat().coerceIn(130f, 280f)) }

    val lowDisplay = if (isMmol) {
        String.format(Locale.US, "%.1f", lowMg / 18.0182)
    } else {
        lowMg.toInt().toString()
    }

    val highDisplay = if (isMmol) {
        String.format(Locale.US, "%.1f", highMg / 18.0182)
    } else {
        highMg.toInt().toString()
    }

    val responsive = ClinicalTheme.responsive

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
                modifier = Modifier
                    .widthIn(max = responsive.dialogMaxWidth)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(if (responsive.isNarrowPhone) 16.dp else 20.dp)
                ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = colors.mint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rango Glucémico Objetivo",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Límites clínicos de referencia (${unit.label})",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resumen Visual del Rango Actual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceCard)
                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Rango Seleccionado",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$lowDisplay - $highDisplay ${unit.label}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.mint
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Barra de 5 Zonas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(colors.urgentCrimson))
                            Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(colors.highAmber))
                            Box(modifier = Modifier.weight(0.40f).fillMaxHeight().background(colors.mint))
                            Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(colors.highAmber))
                            Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(colors.veryHighOrange))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Control Limite Inferior (Bajo)
                Text(
                    text = "Limite Inferior (Minimo en Rango)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Por debajo se considera Glucosa Baja",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$lowDisplay ${unit.label}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.highAmber
                    )
                }

                if (isMmol) {
                    val lowMmol = lowMg / 18.0182f
                    Slider(
                        value = lowMmol.coerceIn(3.0f, 6.1f),
                        onValueChange = {
                            val rounded = (it * 10).roundToInt() / 10f
                            lowMg = (rounded * 18.0182f).coerceIn(55f, 110f)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 3.0f..6.1f,
                        steps = 30,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.mint,
                            activeTrackColor = colors.mint,
                            inactiveTrackColor = colors.surfaceBorder
                        )
                    )
                } else {
                    Slider(
                        value = lowMg.coerceIn(55f, 110f),
                        onValueChange = {
                            lowMg = it.roundToInt().toFloat()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 55f..110f,
                        steps = 54,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.mint,
                            activeTrackColor = colors.mint,
                            inactiveTrackColor = colors.surfaceBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Control Limite Superior (Alto)
                Text(
                    text = "Limite Superior (Maximo en Rango)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Por encima se considera Glucosa Alta",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$highDisplay ${unit.label}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.highAmber
                    )
                }

                if (isMmol) {
                    val highMmol = highMg / 18.0182f
                    Slider(
                        value = highMmol.coerceIn(7.2f, 15.5f),
                        onValueChange = {
                            val rounded = (it * 10).roundToInt() / 10f
                            highMg = (rounded * 18.0182f).coerceIn(130f, 280f)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 7.2f..15.5f,
                        steps = 82,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.mint,
                            activeTrackColor = colors.mint,
                            inactiveTrackColor = colors.surfaceBorder
                        )
                    )
                } else {
                    Slider(
                        value = highMg.coerceIn(130f, 280f),
                        onValueChange = {
                            highMg = it.roundToInt().toFloat()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 130f..280f,
                        steps = 149,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.mint,
                            activeTrackColor = colors.mint,
                            inactiveTrackColor = colors.surfaceBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Preajustes Clinicos Rapidos
                Text(
                    text = "Preajustes Clinicos",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Triple("Estandar", 70f, 180f),
                        Triple("Estricto", 70f, 140f),
                        Triple("Gestacional", 63f, 140f)
                    )
                    presets.forEach { (label, l, h) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceCard)
                                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    lowMg = l
                                    highMg = h
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                val pStr = if (isMmol) {
                                    String.format(Locale.US, "%.1f-%.1f", l / 18.0182, h / 18.0182)
                                } else {
                                    "${l.toInt()}-${h.toInt()}"
                                }
                                Text(
                                    text = pStr,
                                    fontSize = 9.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botones Cancelar y Guardar
                if (responsive.isExtraLargeFont) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSave(lowMg.roundToInt(), highMg.roundToInt())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.mint,
                                contentColor = if (colors.isDark) Color.Black else Color.White
                            )
                        ) {
                            Text(
                                text = "Aplicar Rango",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                        ) {
                            Text(
                                text = "Cancelar",
                                color = colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                        ) {
                            Text(
                                text = "Cancelar",
                                color = colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSave(lowMg.roundToInt(), highMg.roundToInt())
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.mint,
                                contentColor = if (colors.isDark) Color.Black else Color.White
                            )
                        ) {
                            Text(
                                text = "Aplicar Rango",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
}
