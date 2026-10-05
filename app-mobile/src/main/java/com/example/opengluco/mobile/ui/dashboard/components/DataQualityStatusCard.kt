package com.example.opengluco.mobile.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.opengluco.core.data.ClinicalReportsCalculator
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataQualityStatusCard(
    readings: List<GlucoseMeasurement>,
    isDataStale: Boolean,
    lastSuccessfulSyncMs: Long,
    modifier: Modifier = Modifier
) {
    val colors = ClinicalTheme.colors
    val quality = remember(readings) {
        ClinicalReportsCalculator.calculateDataQuality(readings, periodDays = 14)
    }
    val readingStatusColor = if (isDataStale) colors.highAmber else colors.mint
    val syncText = remember(lastSuccessfulSyncMs) {
        if (lastSuccessfulSyncMs <= 0L) {
            "Sin sincronización registrada"
        } else {
            val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            "Última sincronización: ${formatter.format(Date(lastSuccessfulSyncMs))}"
        }
    }
    val readingAgeText = remember(quality.latestReadingEpochMs, quality.latestReadingAgeMinutes) {
        val latestEpoch = quality.latestReadingEpochMs
        val ageMinutes = quality.latestReadingAgeMinutes
        if (latestEpoch == null || ageMinutes == null) {
            "Sin lecturas locales"
        } else {
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(latestEpoch))
            val age = when {
                ageMinutes < 1 -> "ahora"
                ageMinutes < 60 -> "hace $ageMinutes min"
                ageMinutes < 1_440 -> "hace ${ageMinutes / 60} h"
                else -> "hace ${ageMinutes / 1_440} días"
            }
            "Última lectura: $time · $age"
        }
    }
    val coverageText = quality.coveragePercentage?.let { String.format(Locale.getDefault(), "%.1f%%", it) } ?: "--"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        border = BorderStroke(1.dp, colors.surfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDataStale) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = readingStatusColor,
                    modifier = Modifier.width(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Calidad de datos y sincronización",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (isDataStale) "La lectura más reciente puede estar retrasada" else "Lecturas recientes",
                        fontSize = 11.5.sp,
                        color = readingStatusColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityMetric(
                    title = "Cobertura estimada",
                    value = coverageText,
                    detail = "${quality.daysWithReadings}/14 días",
                    modifier = Modifier.weight(1f)
                )
                QualityMetric(
                    title = "Huecos detectados",
                    value = quality.gapCount.toString(),
                    detail = if (quality.longestGapMinutes > 0) "Máx. ${quality.longestGapMinutes} min" else "Sin huecos largos",
                    modifier = Modifier.weight(1f)
                )
                QualityMetric(
                    title = "Cadencia",
                    value = quality.samplingIntervalMinutes?.let { "$it min" } ?: "--",
                    detail = "Lecturas: ${quality.totalReadings}",
                    modifier = Modifier.weight(1f)
                )
            }

            Text(text = readingAgeText, fontSize = 11.5.sp, color = colors.textSecondary)
            Text(
                text = syncText,
                fontSize = 11.5.sp,
                color = colors.textSecondary
            )

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.width(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        quality.totalReadings == 0 -> "Aún no hay lecturas locales para estimar la continuidad."
                        quality.coveragePercentage == null -> "Se necesitan varias lecturas próximas para estimar la cadencia."
                        quality.isSufficientForStandardSummary -> "Hay al menos 14 días con datos y la cobertura estimada supera el 70%."
                        else -> "Los informes CGM suelen usar 14 días y al menos un 70% de datos activos; todavía no se alcanza esa referencia."
                    },
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    color = colors.textMuted
                )
            }
            Text(
                text = "La cobertura se estima entre la primera y la última lectura del periodo; no incluye tiempo sin sensor antes o después.",
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun QualityMetric(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val colors = ClinicalTheme.colors
    Column(modifier = modifier) {
        Text(text = title, fontSize = 10.sp, color = colors.textMuted, maxLines = 1)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Text(text = detail, fontSize = 9.5.sp, color = colors.textSecondary, maxLines = 1)
    }
}
