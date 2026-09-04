package com.example.opengluco.mobile.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.opengluco.core.data.ClinicalReportsCalculator
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.HealthDataExporter
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.core.model.TirCategory
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsHubScreen(
    patient: ConnectionItem?,
    historicalReadings: List<GlucoseMeasurement>,
    unit: GlucoseUnit,
    onBack: () -> Unit
) {
    val colors = ClinicalTheme.colors
    val responsive = ClinicalTheme.responsive
    val context = LocalContext.current

    // Calcular el rango real de días con datos disponibles
    val availableDays = remember(historicalReadings) {
        ClinicalReportsCalculator.calculateAvailableDays(historicalReadings).coerceAtLeast(1)
    }

    // Opciones de periodos estándar
    val daysOptions = remember { listOf(1, 7, 14, 30, 90) }

    var selectedDays by remember {
        mutableIntStateOf(1)
    }

    val isPeriodSufficient = availableDays >= selectedDays

    val tirReport = remember(historicalReadings, selectedDays) {
        ClinicalReportsCalculator.calculateTimeInRange(historicalReadings, selectedDays)
    }

    val a1cReport = remember(historicalReadings, selectedDays) {
        ClinicalReportsCalculator.calculateEstimatedA1c(historicalReadings, selectedDays)
    }

    val avgReport = remember(historicalReadings, selectedDays) {
        ClinicalReportsCalculator.calculateAverageGlucose(historicalReadings, selectedDays)
    }

    val dailyPatterns = remember(historicalReadings, selectedDays) {
        ClinicalReportsCalculator.calculateDailyPatterns(historicalReadings, selectedDays)
    }

    val patientName = patient?.let { "${it.firstName} ${it.lastName}".trim() }.takeIf { !it.isNullOrBlank() } ?: "Paciente"

    val veryHighBucket = tirReport.buckets.find { it.category == TirCategory.VERY_HIGH }
    val highBucket = tirReport.buckets.find { it.category == TirCategory.HIGH }
    val inRangeBucket = tirReport.buckets.find { it.category == TirCategory.IN_RANGE }
    val lowBucket = tirReport.buckets.find { it.category == TirCategory.LOW }
    val veryLowBucket = tirReport.buckets.find { it.category == TirCategory.VERY_LOW }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Informe Clínico",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "$patientName • $availableDays días de histórico",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = colors.mint
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            HealthDataExporter.shareCsv(context, historicalReadings, unit, patientName)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Exportar CSV",
                            tint = colors.mint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.mint
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = responsive.contentMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = responsive.horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(responsive.cardSpacing)
            ) {
            // 1. Selector de Periodos (Solo los que tienen datos reales)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    daysOptions.forEach { days ->
                        val label = when (days) {
                            1 -> "Hoy (1d)"
                            7 -> "7 días"
                            14 -> "14 días"
                            30 -> "30 días"
                            90 -> "90 días"
                            else -> "${days} días"
                        }
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.mint,
                                selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                containerColor = colors.surfaceOrb,
                                labelColor = colors.textSecondary
                            ),
                            border = BorderStroke(1.dp, if (selectedDays == days) colors.mint else colors.surfaceBorder)
                        )
                    }
                }
            }

            if (!isPeriodSufficient) {
                item {
                    val daysPlural = if (availableDays == 1) "1 día" else "$availableDays días"
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                        border = BorderStroke(1.dp, colors.highAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Aviso clínico",
                                tint = colors.highAmber,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Datos insuficientes para este período",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No tienes todavía datos suficientes para leer las métricas completas de un período de $selectedDays días. Se requieren al menos $selectedDays días de lecturas acumuladas (disponibles actualmente: $daysPlural).",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Tarjeta Tiempo en Rango (TIR)
            item {
                val veryHighPct = veryHighBucket?.percentage?.toInt() ?: 0
                val highPct = highBucket?.percentage?.toInt() ?: 0
                val inRangePct = inRangeBucket?.percentage?.toInt() ?: 0
                val tightRangePct = tirReport.tightRangePercent.toInt()
                val lowPct = lowBucket?.percentage?.toInt() ?: 0
                val veryLowPct = veryLowBucket?.percentage?.toInt() ?: 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Tiempo en Rango (TIR)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Objetivo clínico: mayor al 70%",
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = "${tirReport.inRangePercent.toInt()}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = colors.mint
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra de progreso interactiva
                        LinearProgressIndicator(
                            progress = { ((tirReport.inRangePercent / 100.0).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = colors.mint,
                            trackColor = colors.surfaceBorder,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Desglose clínico: SOLO SE MUESTRAN CATEGORÍAS CON DATOS (> 0%)
                        var renderedBucketsCount = 0

                        if (veryHighPct > 0) {
                            TirCategoryRow("Muy Alto (>= 250 mg/dL)", "$veryHighPct%", colors.veryHighOrange)
                            renderedBucketsCount++
                        }
                        if (highPct > 0) {
                            TirCategoryRow("Alto (181 - 249 mg/dL)", "$highPct%", colors.highAmber)
                            renderedBucketsCount++
                        }
                        if (inRangePct > 0) {
                            TirCategoryRow("En Rango (70 - 180 mg/dL)", "$inRangePct%", colors.mint)
                            renderedBucketsCount++
                        }
                        if (tightRangePct > 0) {
                            TirCategoryRow("Rango Estricto / TiTR (70 - 140 mg/dL)", "$tightRangePct%", colors.arcticCyan)
                            renderedBucketsCount++
                        }
                        if (lowPct > 0) {
                            TirCategoryRow("Bajo (56 - 69 mg/dL)", "$lowPct%", colors.lowCoral)
                            renderedBucketsCount++
                        }
                        if (veryLowPct > 0) {
                            TirCategoryRow("Muy Bajo (<= 55 mg/dL)", "$veryLowPct%", colors.urgentCrimson)
                            renderedBucketsCount++
                        }

                        if (renderedBucketsCount == 0) {
                            Text(
                                text = "Sin lecturas registradas en este periodo.",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ClinicalExplanationBox(
                            title = "Criterio Clínico: Tiempo en Rango (TIR)",
                            description = "Porcentaje de lecturas dentro del intervalo de glucosa saludable (70 - 180 mg/dL). El consenso internacional ATTD recomienda un TIR superior al 70% y menos del 4% en rango bajo (<70 mg/dL) para minimizar el riesgo de complicaciones a largo plazo."
                        )
                    }
                }
            }

            // 3. Tarjeta Índice de Riesgo Glucémico (GRI) con desglose y explicación detallada
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Índice de Riesgo Glucémico (GRI)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Evaluación global de calidad de control glucémico",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", tirReport.gri),
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = if (tirReport.gri <= 40.0) colors.mint else if (tirReport.gri <= 60.0) colors.highAmber else colors.urgentCrimson
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = tirReport.griCategory,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tirReport.gri <= 40.0) colors.mint else if (tirReport.gri <= 60.0) colors.highAmber else colors.urgentCrimson,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ClinicalExplanationBox(
                            title = "Criterio Clínico: Índice de Riesgo Glucémico (GRI)",
                            description = "Puntuación de 0 a 100 avalada por consenso internacional (Klonoff et al., 2022) que pondera la exposición acumulada a hipoglucemias e hiperglucemias dando prioridad a la prevención de eventos bajos:\n\n• Zona A (0 - 20): Riesgo muy bajo / Control óptimo\n• Zona B (21 - 40): Riesgo bajo\n• Zona C (41 - 60): Riesgo moderado\n• Zona D (61 - 80): Riesgo alto\n• Zona E (> 80): Riesgo muy alto"
                        )
                    }
                }
            }

            // 4. Tarjeta Análisis de Patrones Diarios y Curva Anidada (AGP)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Patrones Diarios y Curva Anidada (AGP)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Superposición modal de las 24 horas del día",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ClinicalExplanationBox(
                            title = "Criterio Clínico: Perfil Ambulatorio de Glucosa (AGP)",
                            description = "La curva modal anidada (AGP) superpone todas las lecturas del período en un ciclo diario estándar de 24 horas (00:00 a 23:59). Permite evaluar la recurrencia horaria de fluctuaciones, excursiones postprandiales y descensos nocturnos."
                        )
                    }
                }
            }

            // 5. Tarjeta Promedio y Variabilidad (Glucosa Media, SD, CV% y MAGE) con explicaciones
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Promedio y Variabilidad Glucémica",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Estabilidad y dispersión de los niveles de glucosa",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Métricas en columnas adaptables
                        val useStackedMetrics = responsive.isExtraLargeFont || (responsive.isLargeFont && responsive.isNarrowPhone)
                        if (useStackedMetrics) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricColumn(
                                    title = "Glucosa Media",
                                    value = "${avgReport.overallAverageMgDl.toInt()} mg/dL",
                                    valueColor = colors.textPrimary
                                )
                                MetricColumn(
                                    title = "Desv. Estándar (SD)",
                                    value = "±${String.format(Locale.US, "%.1f", dailyPatterns.standardDeviation)}",
                                    valueColor = colors.textSecondary
                                )
                                MetricColumn(
                                    title = "Coef. Variación (CV)",
                                    value = "${String.format(Locale.US, "%.1f", dailyPatterns.coefficientOfVariation)}%",
                                    valueColor = if (dailyPatterns.coefficientOfVariation <= 36.0) colors.mint else colors.highAmber
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricColumn(
                                    title = "Glucosa Media",
                                    value = "${avgReport.overallAverageMgDl.toInt()} mg/dL",
                                    valueColor = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricColumn(
                                    title = "Desv. Estándar (SD)",
                                    value = "±${String.format(Locale.US, "%.1f", dailyPatterns.standardDeviation)}",
                                    valueColor = colors.textSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricColumn(
                                    title = "Coef. Variación (CV)",
                                    value = "${String.format(Locale.US, "%.1f", dailyPatterns.coefficientOfVariation)}%",
                                    valueColor = if (dailyPatterns.coefficientOfVariation <= 36.0) colors.mint else colors.highAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (dailyPatterns.mage > 0.0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "MAGE (Amplitud de Excursiones):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${dailyPatterns.mage} mg/dL",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ClinicalExplanationBox(
                            title = "Criterio Clínico: Variabilidad y Estabilidad Glucémica",
                            description = "• Glucosa Media: Nivel representativo central del período analizado.\n\n• Desviación Estándar (SD): Dispersión de las lecturas respecto a la media. Valores reducidos evidencian mayor estabilidad.\n\n• Coeficiente de Variación (CV%): Medida porcentual estandarizada. El consenso médico fija el objetivo clínico en CV <= 36%. Cifras mayores indican labilidad glucémica marcada.\n\n• MAGE: Amplitud media de las oscilaciones glucémicas relevantes (> 1 SD)."
                        )
                    }
                }
            }

            // 6. Tarjeta HbA1c Estimada / GMI
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "HbA1c Estimada (GMI)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Estimación orientativa de Hemoglobina Glicosilada",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", a1cReport.gmiPercent),
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = colors.mint
                            )
                            Text(
                                text = " %",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "${a1cReport.gmiMmolMol.toInt()} mmol/mol (IFCC)",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ClinicalExplanationBox(
                            title = "Criterio Clínico: Indicador de Gestión de Glucosa (GMI)",
                            description = "Aproximación matemática a la hemoglobina glicosilada de laboratorio calculada sobre la glucosa media del sensor continuo (Bergenstal et al., 2018). Requiere un período de monitorización representativo para asegurar consistencia."
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Aviso regulatorio MDDS
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.background),
                            border = BorderStroke(1.dp, colors.surfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Aviso",
                                    tint = colors.highAmber,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ClinicalReportsCalculator.GMI_LEGAL_DISCLAIMER,
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 7. Botón de Exportación de Datos
            item {
                Button(
                    onClick = {
                        HealthDataExporter.shareCsv(context, historicalReadings, unit, patientName)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.mint,
                        contentColor = if (colors.isDark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Exportar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Informe en CSV", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
}

@Composable
private fun ClinicalExplanationBox(title: String, description: String) {
    val colors = ClinicalTheme.colors
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.background
        ),
        border = BorderStroke(1.dp, colors.surfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Información clínica",
                    tint = colors.mint,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = colors.textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun TirCategoryRow(label: String, percent: String, dotColor: Color) {
    val colors = ClinicalTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.5.sp, color = colors.textSecondary)
        }
        Text(percent, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
    }
}

@Composable
private fun MetricColumn(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    val colors = ClinicalTheme.colors
    val responsive = ClinicalTheme.responsive
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = responsive.clampedSp(11f, maxScale = 1.25f),
            color = colors.textSecondary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = responsive.clampedSp(15f, maxScale = 1.25f),
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
    }
}
