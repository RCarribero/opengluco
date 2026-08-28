package com.example.opengluco.mobile.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Power
import androidx.compose.runtime.DisposableEffect
import com.example.opengluco.core.model.QrPairingPayload
import com.example.opengluco.core.data.QrAuthHelper
import com.example.opengluco.mobile.ui.qr.MobilePairingHelper
import com.example.opengluco.mobile.service.GlucoseMonitorForegroundService
import com.example.opengluco.mobile.ui.dashboard.components.ConfigurationDiagnosticsDialog
import com.example.opengluco.mobile.ui.dashboard.components.SystemDiagnosticsHelper
import com.example.opengluco.mobile.ui.dashboard.components.SystemDiagnosticsState
import com.example.opengluco.mobile.notification.MobileAlarmNotificationHelper
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.example.opengluco.mobile.ui.dashboard.components.TargetRangeDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.SolidColor
import com.example.opengluco.core.data.AlarmRepository
import com.example.opengluco.core.data.ClinicalReportsCalculator
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.HealthDataExporter
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.model.AlarmSeverity
import com.example.opengluco.core.model.AlarmType
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.core.model.GlucoseAlarm
import com.example.opengluco.core.model.GlucoseMeasurement
import com.example.opengluco.mobile.ui.dashboard.components.DetailModalType
import com.example.opengluco.mobile.ui.dashboard.components.LegalNoticeDialog
import com.example.opengluco.mobile.ui.dashboard.components.LegalNoticeType
import com.example.opengluco.mobile.ui.dashboard.components.MobileDualFloatingOrbs
import com.example.opengluco.mobile.ui.dashboard.components.MobileStatDetailModal
import com.example.opengluco.mobile.ui.dashboard.components.PatientHeaderChip
import com.example.opengluco.mobile.ui.dashboard.components.PatientSelectorModal
import com.example.opengluco.mobile.ui.reports.ReportsHubScreen
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import com.example.opengluco.mobile.ui.theme.getClinicalStatusColor
import androidx.compose.material.icons.filled.Assessment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

enum class DashboardTimeframe(val label: String, val hours: Int) {
    H24("24h", 24),
    H12("12h", 12),
    H6("6h", 6),
    H2("2h", 2),
    H1("1h", 1);

    fun next(): DashboardTimeframe {
        val list = entries
        val nextIdx = (ordinal + 1) % list.size
        return list[nextIdx]
    }
}

enum class MetricPeriod(val label: String, val days: Int) {
    DAY("Día", 1),
    WEEK("Semana", 7),
    MONTH("Mes", 30),
    THREE_MONTHS("3 Meses", 90)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileDashboardScreen(
    repository: OpenGlucoRepository,
    preferencesRepository: UserPreferencesRepository,
    onOpenQrScanner: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var patients by remember { mutableStateOf<List<ConnectionItem>>(emptyList()) }
    var selectedPatient by remember { mutableStateOf<ConnectionItem?>(null) }
    var history by remember { mutableStateOf<List<GlucoseMeasurement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var activeModal by remember { mutableStateOf(DetailModalType.NONE) }
    var showPatientSelector by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(MetricPeriod.DAY) }
    var activeLegalNotice by remember { mutableStateOf(LegalNoticeType.NONE) }
    var showReportsScreen by remember { mutableStateOf(false) }
    var showAlarmsManagementDialog by remember { mutableStateOf(false) }
    var currentSensor by remember { mutableStateOf<com.example.opengluco.core.model.SensorInfo?>(null) }
    var selectedChartTimeframe by remember { mutableStateOf(DashboardTimeframe.H24) }
    var showTargetRangeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var autoDiscoveredPairingPayload by remember { mutableStateOf<QrPairingPayload?>(null) }
    var autoPairingSuccess by remember { mutableStateOf(false) }
    var diagnosticsState by remember { mutableStateOf(SystemDiagnosticsHelper.checkDiagnostics(context)) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val periodReadings by preferencesRepository.getHistoricalReadings(90).collectAsState(initial = emptyList())
    val alarmRepo = remember { AlarmRepository(context) }
    val configuredAlarms by alarmRepo.alarmsFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val colors = ClinicalTheme.colors
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Detección automática en segundo plano de solicitudes de emparejamiento desde el reloj
    DisposableEffect(Unit) {
        val messageClient = Wearable.getMessageClient(context)
        val listener = MessageClient.OnMessageReceivedListener { messageEvent ->
            if (messageEvent.path == "/opengluco_pairing_request") {
                val rawJson = String(messageEvent.data, Charsets.UTF_8)
                val payload = QrAuthHelper.parsePairingPayload(rawJson)
                if (payload != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    autoDiscoveredPairingPayload = payload
                    autoPairingSuccess = false
                }
            }
        }
        messageClient.addListener(listener)
        onDispose {
            messageClient.removeListener(listener)
        }
    }

    fun evaluateAndNotify(patient: ConnectionItem?) {
        val em = patient?.effectiveMeasurement ?: return
        val value = em.numericValue
        if (value <= 0.0) return

        val arrow = em.trendSymbol
        val name = patient.fullName.ifBlank { "Paciente" }

        // 1. Actualizar la tarjeta persistente de estado en tiempo real en la barra de notificaciones
        MobileAlarmNotificationHelper.updateLiveGlucoseNotification(
            context = context,
            glucoseValueMgDl = value,
            trendArrow = arrow,
            patientName = name
        )

        // 2. Actualizar Widgets de escritorio
        com.example.opengluco.mobile.widget.GlucoseWidgetUpdater.updateAllWidgets(
            context = context,
            latestMeasurement = em,
            patientName = name
        )

        // 3. Evaluar alarmas configuradas
        scope.launch {
            val alarms = alarmRepo.getAllAlarms()
            val timestamps = alarmRepo.getLastFiredTimestamps()
            val result = com.example.opengluco.core.data.AlarmEvaluator.evaluate(
                currentValueMgDl = value,
                alarms = alarms,
                lastFiredTimestamps = timestamps
            )
            val triggered = result.triggeredAlarm
            if (triggered != null) {
                MobileAlarmNotificationHelper.triggerAlarm(
                    context = context,
                    alarm = triggered,
                    glucoseValueMgDl = value
                )
                alarmRepo.recordAlarmFired(triggered.id)
            }
        }
    }

    fun loadPatientData(patient: ConnectionItem, silent: Boolean = false) {
        if (!silent && history.isEmpty()) {
            isLoading = true
        }
        scope.launch {
            selectedPatient = patient
            preferencesRepository.setSelectedPatient(patient.patientId)
            evaluateAndNotify(patient)
            val graphRes = repository.getPatientGraph(patient.patientId)
            val graphDataObj = graphRes.getOrNull()
            val graphData = graphDataObj?.graphData.orEmpty()
            history = graphData
            currentSensor = graphDataObj?.activeSensors?.firstOrNull() ?: graphDataObj?.connection?.sensor ?: patient.sensor

            // Guardar lecturas continuas y última medición en el caché histórico persistente del paciente
            val allToSave = mutableListOf<GlucoseMeasurement>()
            allToSave.addAll(graphData)
            patient.effectiveMeasurement?.let { em ->
                if (allToSave.none { it.timestamp == em.timestamp && !it.timestamp.isNullOrBlank() }) {
                    allToSave.add(em)
                }
            }
            preferencesRepository.saveHistoricalReadings(allToSave, patient.patientId)
            com.example.opengluco.mobile.widget.GlucoseWidgetUpdater.updateAllWidgets(
                context = context,
                latestMeasurement = patient.effectiveMeasurement,
                history = allToSave,
                patientName = patient.fullName.ifBlank { "Paciente" }
            )
            isLoading = false
        }
    }

    fun loadData(silent: Boolean = false) {
        if (!silent && patients.isEmpty()) {
            isLoading = true
        }
        scope.launch {
            val res = repository.getConnections()
            val freshPatients = res.getOrNull().orEmpty()
            if (freshPatients.isNotEmpty()) {
                patients = freshPatients
                val savedId = settings?.selectedPatientId.orEmpty()
                val targetPatient = freshPatients.find { it.patientId == savedId } ?: freshPatients.first()
                loadPatientData(targetPatient, silent)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        // Registrar canales de notificacion, servicio foreground y worker de alarmas
        MobileAlarmNotificationHelper.createChannels(context)
        GlucoseMonitorForegroundService.startService(context)
        com.example.opengluco.mobile.service.GlucoseAlarmWorker.enqueue(context)

        // Autodetección al abrir la aplicación: si hay cualquier problema de permisos o batería, abrir Pop-Up
        val currentDiagnostics = SystemDiagnosticsHelper.checkDiagnostics(context)
        diagnosticsState = currentDiagnostics
        if (currentDiagnostics.hasAnyIssue) {
            showDiagnosticsDialog = true
        }

        loadData(silent = false)
        while (true) {
            kotlinx.coroutines.delay(60_000)
            loadData(silent = true)
            evaluateAndNotify(selectedPatient)
        }
    }

    val currentMeasurement = selectedPatient?.effectiveMeasurement ?: history.lastOrNull()

    // Unificación y deduplicación completa de historial (caché persistente + datos API en vivo + última medición)
    val combinedHistory = remember(history, periodReadings, currentMeasurement) {
        val map = LinkedHashMap<String, GlucoseMeasurement>()
        for (item in periodReadings) {
            val key = item.timestamp?.takeIf { it.isNotBlank() }
                ?: item.factoryTimestamp?.takeIf { it.isNotBlank() }
                ?: "${item.numericValue}_${item.trendArrow}_${item.hashCode()}"
            map[key] = item
        }
        for (item in history) {
            val key = item.timestamp?.takeIf { it.isNotBlank() }
                ?: item.factoryTimestamp?.takeIf { it.isNotBlank() }
                ?: "${item.numericValue}_${item.trendArrow}_${item.hashCode()}"
            map[key] = item
        }
        if (currentMeasurement != null) {
            val key = currentMeasurement.timestamp?.takeIf { it.isNotBlank() }
                ?: currentMeasurement.factoryTimestamp?.takeIf { it.isNotBlank() }
                ?: "${currentMeasurement.numericValue}_${currentMeasurement.trendArrow}_${currentMeasurement.hashCode()}"
            map[key] = currentMeasurement
        }
        map.values.filter { it.numericValue > 0 }.sortedBy { it.getEpochMillis() }
    }

    // Filtrado de historial según el intervalo temporal seleccionado (1h, 2h, 6h, 12h, 24h)
    val chartHistory = remember(combinedHistory, selectedChartTimeframe) {
        val maxEpoch = maxOf(System.currentTimeMillis(), combinedHistory.lastOrNull()?.getEpochMillis() ?: 0L)
        val cutoff = maxEpoch - (selectedChartTimeframe.hours * 3600 * 1000L)
        val filtered = combinedHistory.filter { it.getEpochMillis() >= cutoff }
        if (filtered.isNotEmpty()) {
            filtered
        } else {
            combinedHistory.takeLast(maxOf(2, selectedChartTimeframe.hours * 4))
        }
    }

    // Calculo dinamico de estadisticas reales a partir de los datos acumulados en UserPreferencesRepository
    val targetLow = selectedPatient?.targetLow ?: settings?.lowThreshold ?: 70
    val targetHigh = selectedPatient?.targetHigh ?: settings?.highThreshold ?: 180

    val availableDataDays = remember(combinedHistory) {
        ClinicalReportsCalculator.calculateAvailableDays(combinedHistory)
    }
    var insufficientDataNotice by remember { mutableStateOf<String?>(null) }

    // Auto-ajuste de periodo si los dias disponibles son menores que el periodo solicitado
    LaunchedEffect(availableDataDays) {
        if (availableDataDays > 0 && selectedPeriod.days > availableDataDays) {
            val valid = MetricPeriod.entries.filter { it.days <= availableDataDays }
            selectedPeriod = valid.maxByOrNull { it.days } ?: MetricPeriod.DAY
        }
    }

    val metricsSource = if (periodReadings.isNotEmpty()) periodReadings else history
    val validHistory = metricsSource.map { it.numericValue }.filter { it > 0 }

    val avgVal = if (validHistory.isNotEmpty()) validHistory.average() else (currentMeasurement?.numericValue ?: 110.0)
    val minVal = if (validHistory.isNotEmpty()) validHistory.minOrNull() ?: 70.0 else (currentMeasurement?.numericValue ?: 70.0)
    val maxVal = if (validHistory.isNotEmpty()) validHistory.maxOrNull() ?: 180.0 else (currentMeasurement?.numericValue ?: 180.0)
    val inRangeCount = validHistory.count { it in targetLow.toDouble()..targetHigh.toDouble() }
    val tirPercent = if (validHistory.isNotEmpty()) ((inRangeCount.toDouble() / validHistory.size) * 100).toInt() else 100

    val sensor = currentSensor ?: selectedPatient?.sensor
    val sensorDays = sensor?.getRemainingDays() ?: 2
    val sensorSerial = sensor?.serialNumber ?: "0M001A8934"
    val sensorModel = sensor?.sensorModelName ?: "FreeStyle Libre 3"
    val isSensorActive = (sensor?.getRemainingDays() ?: 1) > 0

    if (showReportsScreen) {
        ReportsHubScreen(
            patient = selectedPatient,
            historicalReadings = combinedHistory,
            unit = settings?.unit ?: GlucoseUnit.MGDL,
            onBack = { showReportsScreen = false }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.background,
                drawerContentColor = colors.textPrimary,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                SettingsDrawerContent(
                    selectedPatient = selectedPatient,
                    sensor = sensor,
                    isDarkMode = settings?.isDarkMode ?: true,
                    onToggleDarkMode = { isDark ->
                        scope.launch {
                            preferencesRepository.saveDarkMode(isDark)
                        }
                    },
                    currentUnit = settings?.unit ?: GlucoseUnit.MGDL,
                    onToggleUnit = { unit ->
                        scope.launch {
                            preferencesRepository.setUnit(unit)
                        }
                    },
                    targetLow = targetLow,
                    targetHigh = targetHigh,
                    onOpenTargetRange = {
                        scope.launch { drawerState.close() }
                        showTargetRangeDialog = true
                    },
                    alarmsCount = configuredAlarms.size,
                    onOpenAlarms = {
                        scope.launch { drawerState.close() }
                        showAlarmsManagementDialog = true
                    },
                    onOpenReports = {
                        scope.launch { drawerState.close() }
                        showReportsScreen = true
                    },
                    onOpenQrScanner = {
                        scope.launch { drawerState.close() }
                        onOpenQrScanner()
                    },
                    onExportCsv = {
                        HealthDataExporter.shareCsv(
                            context,
                            history,
                            settings?.unit ?: GlucoseUnit.MGDL,
                            selectedPatient?.fullName ?: "Paciente"
                        )
                    },
                    onShowLegalNotice = { type ->
                        activeLegalNotice = type
                    },
                    onLogout = {
                        scope.launch {
                            drawerState.close()
                            preferencesRepository.clearSession()
                            onLogout()
                        }
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu lateral de configuracion",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    title = {
                        PatientHeaderChip(
                            selectedPatient = selectedPatient,
                            patientCount = patients.size,
                            unit = settings?.unit ?: GlucoseUnit.MGDL,
                            onClick = {
                                if (patients.isNotEmpty()) {
                                    showPatientSelector = true
                                }
                            }
                        )
                    },
                    actions = {
                        // Boton Informes Clinicos (Reports)
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showReportsScreen = true
                        }) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = "Informes Clinicos",
                                tint = colors.mint,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Boton Refrescar
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            loadData()
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refrescar",
                                tint = colors.mint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background,
                        titleContentColor = colors.textPrimary,
                        actionIconContentColor = colors.mint,
                        navigationIconContentColor = colors.textPrimary
                    )
                )
            }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
        ) {
            if (isLoading && selectedPatient == null) {
                CircularProgressIndicator(
                    color = colors.mint,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 1. HERO SECTION: DUAL FLOATING ORBS (GLUCOSA & TENDENCIA)
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MobileDualFloatingOrbs(
                                measurement = currentMeasurement,
                                unit = settings?.unit ?: GlucoseUnit.MGDL,
                                targetLow = targetLow,
                                targetHigh = targetHigh,
                                alarms = configuredAlarms,
                                onGlucoseOrbClick = {
                                    activeModal = DetailModalType.GLUCOSE_STATS
                                },
                                onTrendOrbClick = {
                                    activeModal = DetailModalType.TREND_INFO
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Última medición: ${currentMeasurement?.timestamp ?: "Ahora"}",
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 2. GRÁFICA CONTINUA DE BÉZIER CON SENSOR COMPACTO Y SCRUBBING TÁCTIL
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = colors.mint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Curva Continua",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = colors.textPrimary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Boton interactivo de ciclo horario: 24h -> 12h -> 6h -> 2h -> 1h -> 24h
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors.surfaceOrb)
                                                .border(1.dp, colors.mint.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    selectedChartTimeframe = selectedChartTimeframe.next()
                                                }
                                                .padding(horizontal = 9.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.AccessTime,
                                                    contentDescription = "Cambiar intervalo",
                                                    tint = colors.mint,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = selectedChartTimeframe.label,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    color = colors.mint
                                                )
                                            }
                                        }

                                        // Pastilla compacta del sensor
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors.surfaceOrb)
                                                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    activeModal = DetailModalType.SENSOR_INFO
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${sensorDays}d",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = if (sensorDays <= 2) colors.highAmber else colors.mint
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                MobileGlucoseChart(
                                    history = chartHistory,
                                    timeframe = selectedChartTimeframe,
                                    targetLow = targetLow,
                                    targetHigh = targetHigh,
                                    alarms = configuredAlarms,
                                    unit = settings?.unit ?: GlucoseUnit.MGDL
                                )
                            }
                        }
                    }

                    // 3. SECCIÓN DE ESTADÍSTICAS CON SELECTOR DE PERÍODO (DÍA | SEMANA | MES | 3 MESES)
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Métricas Estadísticas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = selectedPeriod.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.mint
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Period Selector Tabs (Dia | Semana | Mes | 3 Meses)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceOrb)
                                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    MetricPeriod.entries.forEach { period ->
                                        val isSelected = selectedPeriod == period
                                        val isAvailable = availableDataDays >= period.days || (period == MetricPeriod.DAY && availableDataDays >= 1)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(9.dp))
                                                .background(
                                                    if (isSelected) colors.mint else Color.Transparent
                                                )
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    if (isAvailable) {
                                                        selectedPeriod = period
                                                        insufficientDataNotice = null
                                                    } else {
                                                        insufficientDataNotice = "Periodo de ${period.label} no disponible: se requieren al menos ${period.days} dias de historial (disponibles: $availableDataDays dia(s))."
                                                    }
                                                }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = period.label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) {
                                                    if (colors.isDark) Color.Black else Color.White
                                                } else if (!isAvailable) {
                                                    colors.textMuted.copy(alpha = 0.35f)
                                                } else {
                                                    colors.textSecondary
                                                },
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                insufficientDataNotice?.let { notice ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.lowCoral.copy(alpha = 0.12f))
                                            .border(1.dp, colors.lowCoral.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "[Aviso] $notice",
                                            fontSize = 11.5.sp,
                                            color = colors.lowCoral,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 4 Métricas reales calculadas
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    DailyStatItem(
                                        title = "En Rango",
                                        value = "$tirPercent%",
                                        unit = "TIR",
                                        accentColor = colors.mint,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DailyStatItem(
                                        title = "Media",
                                        value = "${avgVal.toInt()}",
                                        unit = "mg/dL",
                                        accentColor = colors.arcticCyan,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DailyStatItem(
                                        title = "Mín",
                                        value = "${minVal.toInt()}",
                                        unit = "mg/dL",
                                        accentColor = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DailyStatItem(
                                        title = "Máx",
                                        value = "${maxVal.toInt()}",
                                        unit = "mg/dL",
                                        accentColor = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // 4. TARJETA DEDICADA DE INFORMACIÓN DEL SENSOR
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    activeModal = DetailModalType.SENSOR_INFO
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(colors.surfaceOrb)
                                            .border(1.dp, colors.surfaceBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Sensors,
                                            contentDescription = "Sensor",
                                            tint = colors.mint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sensorModel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "$sensorDays días restantes",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (sensorDays <= 2) colors.lowCoral else colors.mint
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSensorActive) colors.mint.copy(alpha = if (colors.isDark) 0.2f else 0.15f)
                                                else colors.lowCoral.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (isSensorActive) "Activo" else "Caducado",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSensorActive) colors.mint else colors.lowCoral
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Detalle de datos del sensor
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceOrb)
                                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Nº de Serie (S/N)",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = sensorSerial,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Tipo / Modelo",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = sensorModel,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // Modal de Estadísticas Detalladas
        if (activeModal != DetailModalType.NONE) {
            MobileStatDetailModal(
                type = activeModal,
                avgVal = avgVal,
                minVal = minVal,
                maxVal = maxVal,
                tirPercent = tirPercent,
                sensorDays = sensorDays,
                sensorSerial = sensorSerial,
                trendText = currentMeasurement?.trendText ?: "Estable",
                trendSymbol = currentMeasurement?.trendSymbol ?: "→",
                onDismiss = { activeModal = DetailModalType.NONE }
            )
        }

        // Modal de Selección y Conmutación de Paciente
        if (showPatientSelector && patients.isNotEmpty()) {
            PatientSelectorModal(
                patients = patients,
                selectedPatient = selectedPatient,
                unit = settings?.unit ?: GlucoseUnit.MGDL,
                onSelectPatient = { newPatient ->
                    loadPatientData(newPatient)
                },
                onDismiss = { showPatientSelector = false }
            )
        }

        // Diálogos de Conformidad Legal y Descargos Médicos
        if (activeLegalNotice != LegalNoticeType.NONE) {
            LegalNoticeDialog(
                type = activeLegalNotice,
                onDismiss = { activeLegalNotice = LegalNoticeType.NONE },
                onConfirmDelete = {
                    scope.launch {
                        preferencesRepository.purgeAllLocalData()
                        onLogout()
                    }
                }
            )
        }

        // Pop-up Automático de Detección y Emparejamiento con el Reloj
        if (autoDiscoveredPairingPayload != null) {
            Dialog(
                onDismissRequest = { autoDiscoveredPairingPayload = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        if (!autoPairingSuccess) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = "Reloj Detectado",
                                tint = colors.mint,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Reloj Detectado: ${autoDiscoveredPairingPayload?.deviceName ?: "Galaxy Watch"}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Código de Seguridad",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = autoDiscoveredPairingPayload?.verificationCode?.takeIf { it.isNotBlank() } ?: "--- ---",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.mint,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Comprueba que este código coincide con el de tu Reloj. Si coincide, pulsa el tick para emparejar.",
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { autoDiscoveredPairingPayload = null },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.surfaceBorder,
                                        contentColor = colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        val payload = autoDiscoveredPairingPayload
                                        if (payload != null && settings != null) {
                                            MobilePairingHelper.transferSessionToDevice(
                                                context = context,
                                                payload = payload,
                                                email = settings!!.email,
                                                token = settings!!.token,
                                                userId = settings!!.userId
                                            ) {
                                                autoPairingSuccess = true
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.mint,
                                        contentColor = if (colors.isDark) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Emparejar (✓)", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = "Vinculado",
                                tint = colors.mint,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "¡Reloj Vinculado!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sesión transferida con éxito a ${autoDiscoveredPairingPayload?.deviceName}",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { autoDiscoveredPairingPayload = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.mint,
                                    contentColor = if (colors.isDark) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Aceptar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Modal de Gestión Integral de Alarmas (Pantalla Completa Dedicada)
        if (showAlarmsManagementDialog) {
            Dialog(
                onDismissRequest = { showAlarmsManagementDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colors.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header con botón de volver atrás arriba a la izquierda
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceOrb)
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showAlarmsManagementDialog = false },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Text(
                                    text = "Alarmas de Glucosa",
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Umbrales y alertas clínicas personalizadas",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.surfaceBorder)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            com.example.opengluco.mobile.ui.settings.AlarmConfigSection(
                                alarmRepository = alarmRepo,
                                unit = settings?.unit ?: GlucoseUnit.MGDL,
                                targetLow = targetLow,
                                targetHigh = targetHigh,
                                onConfigureTargetRange = { showTargetRangeDialog = true }
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }
                }
            }
        }

        // Modal de Configuración Interactiva de Rango Objetivo
        if (showTargetRangeDialog) {
            TargetRangeDialog(
                initialLow = targetLow,
                initialHigh = targetHigh,
                unit = settings?.unit ?: GlucoseUnit.MGDL,
                onDismiss = { showTargetRangeDialog = false },
                onSave = { newLow, newHigh ->
                    scope.launch {
                        preferencesRepository.setTargetRange(newLow, newHigh)
                    }
                    showTargetRangeDialog = false
                }
            )
        }

        // Pop-Up Emergente de Diagnósticos y Permisos del Sistema
        if (showDiagnosticsDialog) {
            ConfigurationDiagnosticsDialog(
                diagnostics = diagnosticsState,
                onDismiss = { showDiagnosticsDialog = false },
                onRefreshState = {
                    diagnosticsState = SystemDiagnosticsHelper.checkDiagnostics(context)
                }
            )
        }
    }
}
}

@Composable
private fun SettingsDrawerContent(
    selectedPatient: ConnectionItem?,
    sensor: com.example.opengluco.core.model.SensorInfo?,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    currentUnit: GlucoseUnit,
    onToggleUnit: (GlucoseUnit) -> Unit,
    targetLow: Int,
    targetHigh: Int,
    onOpenTargetRange: () -> Unit,
    alarmsCount: Int,
    onOpenAlarms: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onExportCsv: () -> Unit,
    onShowLegalNotice: (LegalNoticeType) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val colors = ClinicalTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header del Navbar Lateral con Boton de Volver Atras
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceOrb)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cerrar menú lateral",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = "Configuración",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Panel de control y preferencias",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.surfaceBorder)
        )

        // Cuerpo con Scroll y Diseño Agrupado Limpio
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Tarjeta informativa del Paciente y Sensor (Mini Profile)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.mint.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.mint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedPatient?.fullName ?: "Paciente Conectado",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${sensor?.sensorModelName ?: "FreeStyle Libre 3"} • ${sensor?.getRemainingDays() ?: 2} dias restantes",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // 2. GRUPO: PREFERENCIAS CLÍNICAS (Tema, Unidades y Alarmas)
            DrawerSectionCard(title = "Preferencias Clínicas") {
                // Tarjeta de Acción: Tema de la Aplicación
                DrawerActionToggleCard(
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = if (isDarkMode) "Tema: Oscuro OLED" else "Tema: Claro Clínico",
                    subtitle = if (isDarkMode) "Negros puros #000000 para pantallas OLED" else "Contraste clínico para alta visibilidad",
                    currentValue = if (isDarkMode) "Oscuro" else "Claro",
                    onClick = { onToggleDarkMode(!isDarkMode) }
                )

                DrawerDivider()

                // Tarjeta de Acción: Unidad de Glucosa
                DrawerActionToggleCard(
                    icon = Icons.Default.Sensors,
                    title = if (currentUnit == GlucoseUnit.MGDL) "Unidad: mg/dL" else "Unidad: mmol/L",
                    subtitle = if (currentUnit == GlucoseUnit.MGDL) "Miligramos por decilitro (estándar ES/US)" else "Milimoles por litro (estándar internacional)",
                    currentValue = if (currentUnit == GlucoseUnit.MGDL) "mg/dL" else "mmol/L",
                    onClick = {
                        onToggleUnit(if (currentUnit == GlucoseUnit.MGDL) GlucoseUnit.MMOL else GlucoseUnit.MGDL)
                    }
                )

                DrawerDivider()

                // Tarjeta de Acción: Rango Objetivo
                val targetRangeLabel = if (currentUnit == GlucoseUnit.MMOL) {
                    String.format(java.util.Locale.US, "%.1f - %.1f mmol/L", targetLow / 18.0182, targetHigh / 18.0182)
                } else {
                    "$targetLow - $targetHigh mg/dL"
                }
                DrawerActionToggleCard(
                    icon = Icons.Default.Tune,
                    title = "Rango: $targetRangeLabel",
                    subtitle = "Límites deseados para glucosa en rango",
                    currentValue = "Ajustar",
                    onClick = onOpenTargetRange
                )

                DrawerDivider()

                // Fila: Alarmas de Glucosa
                DrawerNavigationRow(
                    icon = Icons.Default.Notifications,
                    title = "Alarmas de Glucosa",
                    subtitle = if (alarmsCount > 0) "$alarmsCount configuradas" else "Sin configurar",
                    badge = if (alarmsCount > 0) "$alarmsCount activas" else null,
                    badgeColor = colors.mint,
                    onClick = onOpenAlarms
                )
            }

            // 3. GRUPO: DISPOSITIVOS Y ANÁLISIS
            DrawerSectionCard(title = "Dispositivos y Análisis") {
                DrawerNavigationRow(
                    icon = Icons.Default.Assessment,
                    title = "Informes Clínicos",
                    subtitle = "6 informes ATTD / AGP",
                    onClick = onOpenReports
                )

                DrawerDivider()

                DrawerNavigationRow(
                    icon = Icons.Default.QrCodeScanner,
                    title = "Vincular Reloj / Coche",
                    subtitle = "Sincronización mediante QR",
                    onClick = onOpenQrScanner
                )
            }

            // 4. GRUPO: PRIVACIDAD Y PORTABILIDAD (RGPD)
            DrawerSectionCard(title = "Privacidad y Datos (RGPD)") {
                DrawerNavigationRow(
                    icon = Icons.Default.Assessment,
                    title = "Exportar Historial (CSV)",
                    subtitle = "Portabilidad de datos (Art. 20)",
                    onClick = onExportCsv
                )

                DrawerDivider()

                DrawerNavigationRow(
                    icon = Icons.Default.Delete,
                    title = "Borrar Datos Locales",
                    subtitle = "Supresión irreversible (Art. 17)",
                    titleColor = colors.urgentCrimson,
                    onClick = { onShowLegalNotice(LegalNoticeType.DELETE_CONFIRMATION) }
                )
            }

            // 5. GRUPO: CUMPLIMIENTO LEGAL
            DrawerSectionCard(title = "Cumplimiento y Legal") {
                DrawerNavigationRow(
                    icon = Icons.Default.Close,
                    title = "Descargo Médico",
                    subtitle = "MDR UE 2017/745 / FDA MDDS",
                    onClick = { onShowLegalNotice(LegalNoticeType.MEDICAL_DISCLAIMER) }
                )

                DrawerDivider()

                DrawerNavigationRow(
                    icon = Icons.Default.Close,
                    title = "Marcas Registradas",
                    subtitle = "Abbott Laboratories / FreeStyle",
                    onClick = { onShowLegalNotice(LegalNoticeType.TRADEMARKS) }
                )

                DrawerDivider()

                DrawerNavigationRow(
                    icon = Icons.Default.Close,
                    title = "Privacidad de Salud",
                    subtitle = "Cifrado local AES-256 (Art. 9)",
                    onClick = { onShowLegalNotice(LegalNoticeType.PRIVACY_GDPR) }
                )
            }

            // 6. CERRAR SESIÓN
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.urgentCrimson.copy(alpha = if (colors.isDark) 0.12f else 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    colors.urgentCrimson.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = colors.urgentCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.urgentCrimson
                    )
                }
            }

            // 7. FOOTER LEGAL PERMANENTE
            Text(
                text = "Visualizador secundario pasivo. No es un dispositivo médico ni sustituye al lector oficial ni a la consulta profesional.",
                fontSize = 9.5.sp,
                lineHeight = 13.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DrawerSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = ClinicalTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
        )
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun DrawerActionToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    currentValue: String,
    onClick: () -> Unit
) {
    val colors = ClinicalTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.mint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.mint,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                lineHeight = 13.sp,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.mint)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentValue,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (colors.isDark) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun DrawerNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    badgeColor: Color? = null,
    titleColor: Color? = null,
    onClick: () -> Unit
) {
    val colors = ClinicalTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = titleColor ?: colors.mint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor ?: colors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
        if (badge != null) {
            val bg = badgeColor ?: colors.mint
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = bg
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = "›",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun DrawerDivider() {
    val colors = ClinicalTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.surfaceBorder.copy(alpha = 0.5f))
    )
}

@Composable
private fun SettingsOptionRow(
    title: String,
    subtitle: String,
    accentColor: Color? = null,
    onClick: () -> Unit
) {
    val colors = ClinicalTheme.colors
    val textColor = accentColor ?: colors.textPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceCard)
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
        Text(
            text = "›",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary
        )
    }
}

@Composable
fun DailyStatItem(
    title: String,
    value: String,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = ClinicalTheme.colors
    Box(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceOrb)
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatChartTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "--:--"
    val patterns = listOf(
        "M/d/yyyy h:mm:ss a",
        "M/d/yyyy H:mm:ss",
        "M/d/yyyy h:mm a",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.US)
            if (p.endsWith("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(raw)
            if (date != null) {
                return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
        } catch (_: Exception) {}
    }
    if (raw.contains(":")) {
        val parts = raw.split(" ")
        val timePart = parts.find { it.contains(":") } ?: raw
        val timeTokens = timePart.split(":")
        if (timeTokens.size >= 2) {
            val h = timeTokens[0].padStart(2, '0')
            val m = timeTokens[1].padStart(2, '0')
            return "$h:$m"
        }
    }
    return raw
}

@Composable
fun MobileGlucoseChart(
    history: List<GlucoseMeasurement>,
    timeframe: DashboardTimeframe = DashboardTimeframe.H24,
    targetLow: Int = 70,
    targetHigh: Int = 180,
    alarms: List<GlucoseAlarm> = emptyList(),
    unit: GlucoseUnit = GlucoseUnit.MGDL,
    modifier: Modifier = Modifier
) {
    val colors = ClinicalTheme.colors
    val haptic = LocalHapticFeedback.current

    val rawValidMeasurements = remember(history) {
        history.filter { it.numericValue > 0 }.sortedBy { it.getEpochMillis() }
    }

    val validMeasurements = remember(rawValidMeasurements) {
        val consolidated = com.example.opengluco.core.model.CgmCurveSmoother.consolidateTemporalBuckets(rawValidMeasurements)
        com.example.opengluco.core.model.CgmCurveSmoother.smoothMeasurements(consolidated)
    }

    if (validMeasurements.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceOrb),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Recibiendo telemetria del sensor...",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
        return
    }

    var isInteracting by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val activeIndex = if (isInteracting && selectedIndex != null) {
        selectedIndex!!.coerceIn(0, validMeasurements.size - 1)
    } else {
        validMeasurements.size - 1
    }

    val activeMeasurement = validMeasurements[activeIndex]
    val activeValue = activeMeasurement.numericValue
    val activeStatusColor = com.example.opengluco.mobile.ui.theme.getGlucoseValueColor(activeValue, targetLow, targetHigh, alarms, colors)

    val minVal = remember(validMeasurements, alarms) {
        val alarmMins = alarms.filter { it.enabled }.map { it.thresholdMgDl.toDouble() }
        val allMins = validMeasurements.map { it.numericValue } + alarmMins
        (allMins.minOrNull() ?: 60.0).coerceAtMost(50.0).toFloat()
    }
    val maxVal = remember(validMeasurements, alarms) {
        val alarmMaxs = alarms.filter { it.enabled }.map { it.thresholdMgDl.toDouble() }
        val allMaxs = validMeasurements.map { it.numericValue } + alarmMaxs
        (allMaxs.maxOrNull() ?: 200.0).coerceAtLeast(220.0).toFloat()
    }
    val valRange = remember(minVal, maxVal) {
        (maxVal - minVal).coerceAtLeast(40f)
    }

    val maxTime = remember(validMeasurements) {
        val lastTs = validMeasurements.lastOrNull()?.getEpochMillis() ?: 0L
        if (lastTs > 0) lastTs else System.currentTimeMillis()
    }
    val minTime = remember(maxTime, timeframe) {
        maxTime - (timeframe.hours * 3600 * 1000L)
    }
    val timeSpan = remember(maxTime, minTime) {
        (maxTime - minTime).coerceAtLeast(60_000L)
    }

    val formattedTime = remember(activeMeasurement.timestamp, activeMeasurement.factoryTimestamp) {
        val raw = activeMeasurement.timestamp?.takeIf { it.isNotBlank() }
            ?: activeMeasurement.factoryTimestamp?.takeIf { it.isNotBlank() }
        formatChartTime(raw)
    }

    val formattedValue = activeMeasurement.getFormattedValue(isMmol = unit == GlucoseUnit.MMOL) + " " + unit.label

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tooltip flotante / Barra de estado interactivo con estatus clinico, valor y hora
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeStatusColor.copy(alpha = if (colors.isDark) 0.18f else 0.12f))
                        .border(1.dp, activeStatusColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(activeStatusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedValue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeStatusColor
                    )
                    if (isInteracting) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${activeMeasurement.trendText})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceOrb)
                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Hora",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isInteracting) formattedTime else "Ultima: $formattedTime",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lienzo grafico continuo con interaccion tactil (scrubbing y arrastre)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 4.dp)
                    .pointerInput(validMeasurements.size, timeframe) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isInteracting = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val calcIndex = { xPos: Float ->
                                if (validMeasurements.isNotEmpty()) {
                                    val targetEpoch = minTime + ((xPos / size.width.toFloat()).coerceIn(0f, 1f) * timeSpan).toLong()
                                    validMeasurements.indices.minByOrNull { i ->
                                        val epoch = validMeasurements[i].getEpochMillis()
                                        if (epoch > 0) kotlin.math.abs(epoch - targetEpoch) else kotlin.math.abs((i * (size.width.toFloat() / maxOf(1, validMeasurements.size - 1))) - xPos).toLong()
                                    } ?: (validMeasurements.size - 1)
                                } else 0
                            }
                            var lastIdx = calcIndex(down.position.x)
                            selectedIndex = lastIdx

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    break
                                }
                                change.consume()
                                val currentIdx = calcIndex(change.position.x)
                                if (currentIdx != lastIdx) {
                                    lastIdx = currentIdx
                                    selectedIndex = currentIdx
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            isInteracting = false
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val totalPoints = validMeasurements.size
                val stepX = if (totalPoints > 1) width / (totalPoints - 1).toFloat() else 1f

                // 1. Linea objetivo superior (targetHigh, ej: 180 mg/dL)
                val yTargetHigh = height - ((targetHigh - minVal) / valRange * height).coerceIn(0f, height)
                drawLine(
                    color = colors.mint.copy(alpha = 0.25f),
                    start = Offset(0f, yTargetHigh),
                    end = Offset(width, yTargetHigh),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // 2. Linea objetivo inferior (targetLow, ej: 70 mg/dL)
                val yTargetLow = height - ((targetLow - minVal) / valRange * height).coerceIn(0f, height)
                drawLine(
                    color = colors.mint.copy(alpha = 0.25f),
                    start = Offset(0f, yTargetLow),
                    end = Offset(width, yTargetLow),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // 3. Lineas de Alarmas Activas Configuradas (Lineas Discontinuas)
                val enabledAlarms = alarms.filter { it.enabled }
                for (alarm in enabledAlarms) {
                    val threshold = alarm.thresholdMgDl.toFloat()
                    val yAlarm = height - ((threshold - minVal) / valRange * height).coerceIn(0f, height)
                    val alarmColor = when (alarm.severity) {
                        AlarmSeverity.URGENT -> colors.urgentCrimson
                        AlarmSeverity.ALERT -> if (alarm.type == AlarmType.LOW) colors.highAmber else colors.veryHighOrange
                        AlarmSeverity.INFORMATIVE -> colors.arcticCyan
                    }

                    drawLine(
                        color = alarmColor.copy(alpha = 0.85f),
                        start = Offset(0f, yAlarm),
                        end = Offset(width, yAlarm),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                }

                // Calcular posiciones de todos los puntos de medicion usando coordenadas proporcionales de tiempo
                val points = validMeasurements.mapIndexed { index, measurement ->
                    val mEpoch = measurement.getEpochMillis()
                    val x = if (mEpoch > 0) {
                        val frac = ((mEpoch - minTime).toFloat() / timeSpan.toFloat()).coerceIn(0f, 1f)
                        frac * width
                    } else {
                        if (totalPoints > 1) (index * stepX) else width / 2f
                    }
                    val y = height - ((measurement.numericValue.toFloat() - minVal) / valRange * height).coerceIn(0f, height)
                    Offset(x, y)
                }

                fun getLevelColor(valMg: Double): Color = com.example.opengluco.mobile.ui.theme.getGlucoseValueColor(valMg, targetLow, targetHigh, alarms, colors)

                val pointPairs = points.map { Pair(it.x, it.y) }
                val splineSegments = com.example.opengluco.core.model.CgmCurveSmoother.computeCatmullRomSpline(pointPairs)

                // 4. Sombreado de área bajo la curva con segmentos suaves Catmull-Rom
                val fillAlpha = if (colors.isDark) 0.30f else 0.20f
                for (i in splineSegments.indices) {
                    val seg = splineSegments[i]
                    val v0 = validMeasurements[i].numericValue
                    val v1 = validMeasurements[i + 1].numericValue
                    val midVal = (v0 + v1) / 2.0
                    val segmentColor = getLevelColor(midVal)

                    val segmentFill = Path().apply {
                        moveTo(seg.startX, height)
                        lineTo(seg.startX, seg.startY)
                        cubicTo(seg.cp1X, seg.cp1Y, seg.cp2X, seg.cp2Y, seg.endX, seg.endY)
                        lineTo(seg.endX, height)
                        close()
                    }

                    val fillBrush = Brush.verticalGradient(
                        colors = listOf(segmentColor.copy(alpha = fillAlpha), Color.Transparent),
                        startY = minOf(seg.startY, seg.endY),
                        endY = height
                    )

                    drawPath(
                        path = segmentFill,
                        brush = fillBrush
                    )
                }

                // 5. Trazo de curva Catmull-Rom continua y suave (libre de dientes de sierra)
                for (i in splineSegments.indices) {
                    val seg = splineSegments[i]
                    val v0 = validMeasurements[i].numericValue
                    val v1 = validMeasurements[i + 1].numericValue
                    val midVal = (v0 + v1) / 2.0
                    val segmentColor = getLevelColor(midVal)

                    val segmentStroke = Path().apply {
                        moveTo(seg.startX, seg.startY)
                        cubicTo(seg.cp1X, seg.cp1Y, seg.cp2X, seg.cp2Y, seg.endX, seg.endY)
                    }

                    drawPath(
                        path = segmentStroke,
                        brush = SolidColor(segmentColor),
                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 6. Linea vertical discontinua de inspeccion al frotar (scrubbing)
                val activePoint = points[activeIndex]
                if (isInteracting) {
                    drawLine(
                        color = colors.textSecondary.copy(alpha = 0.55f),
                        start = Offset(activePoint.x, 0f),
                        end = Offset(activePoint.x, height),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                }

                // 7. Circulo resaltado en la curva Bézier con anillo brillante y nucleo solido
                drawCircle(
                    color = activeStatusColor.copy(alpha = 0.25f),
                    radius = 9.dp.toPx(),
                    center = activePoint
                )
                drawCircle(
                    color = activeStatusColor.copy(alpha = 0.60f),
                    radius = 6.dp.toPx(),
                    center = activePoint
                )
                drawCircle(
                    color = if (colors.isDark) Color(0xFF161A22) else Color.White,
                    radius = 4.5.dp.toPx(),
                    center = activePoint
                )
                drawCircle(
                    color = activeStatusColor,
                    radius = 3.dp.toPx(),
                    center = activePoint
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 8. Marcadores de tiempo en la parte inferior del eje X
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val startLabel = when (timeframe) {
                    DashboardTimeframe.H24 -> "-24h"
                    DashboardTimeframe.H12 -> "-12h"
                    DashboardTimeframe.H6 -> "-6h"
                    DashboardTimeframe.H2 -> "-2h"
                    DashboardTimeframe.H1 -> "-60m"
                }
                val midLabel = when (timeframe) {
                    DashboardTimeframe.H24 -> "-12h"
                    DashboardTimeframe.H12 -> "-6h"
                    DashboardTimeframe.H6 -> "-3h"
                    DashboardTimeframe.H2 -> "-1h"
                    DashboardTimeframe.H1 -> "-30m"
                }
                Text(
                    text = startLabel,
                    fontSize = 9.5.sp,
                    color = colors.textMuted
                )
                Text(
                    text = midLabel,
                    fontSize = 9.5.sp,
                    color = colors.textMuted
                )
                Text(
                    text = "Ahora",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.mint
                )
            }
        }
    }
}


