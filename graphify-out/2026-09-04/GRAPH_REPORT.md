# Graph Report - librelinkup-ecosystem-master  (2026-09-04)

## Corpus Check
- 143 files · ~97,715 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1173 nodes · 2114 edges · 104 communities (81 shown, 23 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 51 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `aaec74d0`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ConnectionItem
- OpenGlucoInterceptor
- AlarmRepository
- UserPreferencesRepository
- QrPairingPayload
- WearDashboardScreen
- ClinicalReportsCalculator
- GlucoseMonitorForegroundService
- MobileDashboardScreen.kt
- WearBluetoothRfcommService
- GlucoseMeasurement
- UserSettings
- MobileAlarmNotificationHelper
- GlucoseUnit
- OpenGluco Ecosystem
- E2ETier1FeatureCoverageTest
- SensorInfo
- EmpiricalStressChallengeTest
- E2ETier2BoundaryCornerCasesTest.kt
- .renderSparkline
- MobileLegalComplianceTest
- QrAuthHelper
- EmpiricalStressChallengeTest.kt
- GlucoseChartWidgetProvider.kt
- GlucoseCompactWidgetProvider.kt
- WearLoginViewModel
- WearLegalTextsTest
- GlucoseComplicationService.kt
- AlarmType
- AlarmDismissReceiver.kt
- WearClinicalDesignAndSafetyTest
- Sistema de Diseno: OpenGluco (Minimalista Clinico)
- AutoClinicalSafetyTest
- AutoManifestAndSecurityTest
- HealthDataExporter
- DashboardTimeframe
- AppUpdateRepository
- LegalNoticeType
- HealthDataExporterTest
- 2. Test Tiers & Methodology
- GlucoseTileService.kt
- Acceptance Criteria
- 1. Reglas Innegociables del Proyecto
- Acceptance Criteria
- GlucoseWidgetUpdater.kt
- PROJECT.md
- Reglas de Proyecto: OpenGluco Ecosystem
- BRIEFING.md
- CgmCurveSmootherTest
- AlarmConfigSection.kt
- AlarmCooldown
- Procedimiento Paso a Paso:
- clinical_design.md
- WearQrLoginScreen.kt
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
- BootReceiver.kt
- WearSparklineChart.kt
- ModelSanityTest
- PULL_REQUEST_TEMPLATE.md
- TEST_READY.md
- Politica de Seguridad: OpenGluco Ecosystem
- no_emojis.md
- PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)
- rules/graphify.md
- workflows/graphify.md
- DetailModalType
- wear/MainActivity.kt
- WearSettingsScreen.kt
- GlucoseAlarm
- DataCoherenceAndThemeTest
- WearAuthMessageListenerService.kt
- AlarmSeverity
- AlarmEvaluator
- ReportsHubScreen.kt
- MobileWearableMessageListenerService.kt
- MetricPeriod
- WearGlucoseSyncWorker.kt
- MobileAlarmSyncHelper
- OpenGlucoRepository
- InteractiveMedical3DScene
- QrScannerScreen.kt
- [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido
- OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema
- [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion
- [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer
- [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis
- ResponsiveLayout.kt
- QrAuthHelperTest
- write_adrs.js
- WearBluetoothSecurityTest
- KeystoreCryptoHelperTest

## God Nodes (most connected - your core abstractions)
1. `GlucoseMeasurement` - 112 edges
2. `UserPreferencesRepository` - 70 edges
3. `GlucoseAlarm` - 49 edges
4. `AlarmRepository` - 44 edges
5. `GlucoseUnit` - 42 edges
6. `OpenGlucoRepository` - 35 edges
7. `ConnectionItem` - 33 edges
8. `QrAuthHelper` - 25 edges
9. `SensorInfo` - 25 edges
10. `MobileDashboardScreen()` - 24 edges

## Surprising Connections (you probably didn't know these)
- `GlucoseDashboardCarScreen` --calls--> `OpenGlucoRepository`  [EXTRACTED]
  app-auto/src/main/java/com/example/opengluco/auto/screen/GlucoseDashboardCarScreen.kt → core/data/src/main/java/com/example/opengluco/core/data/OpenGlucoRepository.kt
- `GlucoseDashboardCarScreen` --calls--> `UserPreferencesRepository`  [EXTRACTED]
  app-auto/src/main/java/com/example/opengluco/auto/screen/GlucoseDashboardCarScreen.kt → core/data/src/main/java/com/example/opengluco/core/data/UserPreferencesRepository.kt
- `GlucoseDashboardCarScreen` --calls--> `UserSettings`  [EXTRACTED]
  app-auto/src/main/java/com/example/opengluco/auto/screen/GlucoseDashboardCarScreen.kt → core/data/src/main/java/com/example/opengluco/core/data/UserPreferencesRepository.kt
- `GlucoseDashboardCarScreen` --references--> `GlucoseMeasurement`  [EXTRACTED]
  app-auto/src/main/java/com/example/opengluco/auto/screen/GlucoseDashboardCarScreen.kt → core/model/src/main/java/com/example/opengluco/core/model/OpenGlucoModels.kt
- `MobileAppNavigation()` --references--> `OpenGlucoRepository`  [EXTRACTED]
  app-mobile/src/main/java/com/example/opengluco/mobile/MainActivity.kt → core/data/src/main/java/com/example/opengluco/core/data/OpenGlucoRepository.kt

## Import Cycles
- None detected.

## Communities (104 total, 23 thin omitted)

### Community 0 - "ConnectionItem"
Cohesion: 0.05
Nodes (31): AutoTtsAlertManager, GlucoseCarAppService, Session, GlucoseCarSession, Intent, Screen, Session, GlucoseDashboardCarScreen (+23 more)

### Community 1 - "OpenGlucoInterceptor"
Cohesion: 0.20
Nodes (5): Response, OpenGlucoInterceptor, MockWebServer, OpenGlucoInterceptorTest, Interceptor

### Community 2 - "AlarmRepository"
Cohesion: 0.17
Nodes (3): AlarmRepository, Flow, Result

### Community 3 - "UserPreferencesRepository"
Cohesion: 0.11
Nodes (3): Flow, PreferencesKeys, UserPreferencesRepository

### Community 4 - "QrPairingPayload"
Cohesion: 0.14
Nodes (6): Context, MobilePairingHelper, QrEncryptedPayload, QrPairingPayload, QrSessionExchange, QrAuthModelsTest

### Community 5 - "WearDashboardScreen"
Cohesion: 0.08
Nodes (31): DualFloatingOrbs(), Modifier, Modifier, PatientSelectorChip(), Modifier, WearGlucoseGauge(), DetailModalType, GLUCOSE_STATS (+23 more)

### Community 6 - "ClinicalReportsCalculator"
Cohesion: 0.07
Nodes (27): ClinicalReportsCalculator, com, ClinicalReportsCalculatorTest, AverageGlucoseReport, DailyGraphDaySummary, DailyGraphReport, DailyPatternsReport, EstimatedA1cReport (+19 more)

### Community 7 - "GlucoseMonitorForegroundService"
Cohesion: 0.15
Nodes (12): GlucoseMonitorForegroundService, Context, IBinder, Intent, Service, ConfigurationDiagnosticsDialog(), DiagnosticItemCard(), Context (+4 more)

### Community 8 - "MobileDashboardScreen.kt"
Cohesion: 0.23
Nodes (18): androidx, DailyStatItem(), DashboardChartCard(), DashboardClinicalErrorBanner(), DashboardSensorCard(), DashboardStatsCard(), formatChartTime(), Color (+10 more)

### Community 9 - "WearBluetoothRfcommService"
Cohesion: 0.18
Nodes (8): Context, IBinder, Intent, Service, WearBluetoothRfcommService, BluetoothServerSocket, BluetoothSocket, Notification

### Community 11 - "UserSettings"
Cohesion: 0.36
Nodes (7): Bundle, ComponentActivity, MainActivity, MobileAppNavigation(), Modifier, MobileLoginScreen(), UserSettings

### Community 12 - "MobileAlarmNotificationHelper"
Cohesion: 0.14
Nodes (14): android, com, Context, Uri, MobileAlarmNotificationHelper, AlarmSoundType, ALERT_STANDARD, CUSTOM (+6 more)

### Community 13 - "GlucoseUnit"
Cohesion: 0.20
Nodes (17): Modifier, MobileDualFloatingOrbs(), Modifier, PatientHeaderChip(), PatientSelectorModal(), TargetRangeDialog(), DashboardHeroSection(), ClinicalColorScheme (+9 more)

### Community 14 - "OpenGluco Ecosystem"
Cohesion: 0.05
Nodes (37): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+29 more)

### Community 16 - "SensorInfo"
Cohesion: 0.13
Nodes (6): ClinicalSparklineWithSensor(), Modifier, Modifier, WearSensorChip(), E2ETier4RealWorldScenariosTest, SensorInfo

### Community 19 - ".renderSparkline"
Cohesion: 0.26
Nodes (4): Bitmap, WidgetChartRenderer, CgmCurveSmoother, CubicBezierSegment

### Community 21 - "QrAuthHelper"
Cohesion: 0.22
Nodes (4): Bitmap, ByteArray, QrAuthHelper, ServerSocket

### Community 22 - "EmpiricalStressChallengeTest.kt"
Cohesion: 0.20
Nodes (5): KeystoreCryptoHelper, QrDeviceType, ANDROID_AUTO, WEAR_OS, SecretKey

### Community 23 - "GlucoseChartWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseChartWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 24 - "GlucoseCompactWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseCompactWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 25 - "WearLoginViewModel"
Cohesion: 0.27
Nodes (8): Error, Idle, StateFlow, ViewModel, Loading, Success, WearLoginUiState, WearLoginViewModel

### Community 27 - "GlucoseComplicationService.kt"
Cohesion: 0.33
Nodes (6): GlucoseComplicationService, ComplicationData, ComplicationDataSourceService, ComplicationRequest, ComplicationRequestListener, ComplicationType

### Community 28 - "AlarmType"
Cohesion: 0.33
Nodes (4): AlarmEvaluationResult, AlarmType, HIGH, LOW

### Community 29 - "AlarmDismissReceiver.kt"
Cohesion: 0.53
Nodes (4): AlarmDismissReceiver, BroadcastReceiver, Context, Intent

### Community 31 - "Sistema de Diseno: OpenGluco (Minimalista Clinico)"
Cohesion: 0.14
Nodes (13): 1. Filosofia y Estilo Visual, 2. Paleta de Colores Oficial, 3. Jerarquia y Distribucion Espacial en Wear OS, 4. Interactividad y Feedback Haptico, 5. Mapeo de Codigo Jetpack Compose, 6. Mencion a Abbott Laboratories y Marcas Registradas, A. Esferas Flotantes Superiores (`DualFloatingOrbs`), B. Grafica Sparkline + Badge de Sensor (+5 more)

### Community 34 - "HealthDataExporter"
Cohesion: 0.24
Nodes (3): HealthDataExporter, Context, E2ETier3CrossFeatureCombinationsTest

### Community 35 - "DashboardTimeframe"
Cohesion: 0.29
Nodes (6): DashboardTimeframe, H1, H12, H2, H24, H6

### Community 44 - "AppUpdateRepository"
Cohesion: 0.15
Nodes (9): UpdateAvailableDialog(), AppUpdateInstaller, Context, Result, AppUpdateRepository, OkHttpClient, Result, AppUpdateRepositoryTest (+1 more)

### Community 45 - "LegalNoticeType"
Cohesion: 0.28
Nodes (8): LegalNoticeDialog(), LegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS, LegalSectionBox()

### Community 47 - "2. Test Tiers & Methodology"
Cohesion: 0.17
Nodes (11): 1. Test Architecture Overview, 2. Test Tiers & Methodology, 3. Detailed Feature-to-Test Mapping Matrix, 4. Test Execution & Verification, Global Test Command, Module-Specific Unit Test Tasks, Tier 1: Feature Coverage (>=5 Test Cases per Feature across all 18 Features), Tier 2: Boundary & Corner Cases (>=5 Test Cases per Feature) (+3 more)

### Community 48 - "GlucoseTileService.kt"
Cohesion: 0.29
Nodes (9): GlucoseTileService, Context, DeviceParameters, LayoutElementBuilders, ListenableFuture, RequestBuilders, ResourceBuilders, TileBuilders (+1 more)

### Community 49 - "Acceptance Criteria"
Cohesion: 0.20
Nodes (9): Acceptance Criteria, Initial Request — 2026-08-27T10:10:46Z, Paridad de Interfaces y Configuración Legal, Privacidad y Control de Datos, R1. Paridad de Configuración Legal y Avisos Normativos en Todas las Vistas, R2. Privacidad y Gestión de Datos de Salud (RGPD Art. 9, 17 y 20), R3. Invariantes de Interfaz y Sistema de Diseño Clínico, Requirements (+1 more)

### Community 50 - "1. Reglas Innegociables del Proyecto"
Cohesion: 0.20
Nodes (9): 1. Reglas Innegociables del Proyecto, 2. Flujo de Trabajo para Contribuciones, 3. Estructura de Modulos, 4. Convenciones de Codigo, A. Prohibicion Estricta de Emojis, B. Invariante de Seguridad Clinica (MDDS), C. Tokens de Diseno y Formato, D. Mencion a Abbott Laboratories y Marcas (+1 more)

### Community 51 - "Acceptance Criteria"
Cohesion: 0.20
Nodes (9): Acceptance Criteria, Initial Request — 2026-08-27T10:10:46Z, Paridad de Interfaces y Configuración Legal, Privacidad y Control de Datos, R1. Paridad de Configuración Legal y Avisos Normativos en Todas las Vistas, R2. Privacidad y Gestión de Datos de Salud (RGPD Art. 9, 17 y 20), R3. Invariantes de Interfaz y Sistema de Diseño Clínico, Requirements (+1 more)

### Community 52 - "GlucoseWidgetUpdater.kt"
Cohesion: 0.57
Nodes (3): GlucoseWidgetUpdater, Context, RemoteViews

### Community 53 - "PROJECT.md"
Cohesion: 0.22
Nodes (8): Architecture, Code Layout, Cryptographic Contract (`KeystoreCryptoHelper`), Data Portability CSV Contract (`HealthDataExporter`), Feature Inventory, Interface Contracts, Legal & Regulatory String Constants, Milestones

### Community 54 - "Reglas de Proyecto: OpenGluco Ecosystem"
Cohesion: 0.20
Nodes (9): 1. Directrices de Interfaz y Tokens Clinicos, 2. App Movil (`app-mobile`), 3. App Reloj (`app-wear`), 4. Persistencia y Datos (`core:data`), 5. Invariantes de Telemetria y Formato, 6. Mencion a Abbott Laboratories y Blindaje Legal, 7. Arquitectura y Grafo de Conocimiento (Graphify), 8. Protocolo de Publicación y Actualizaciones OTA (Bajo Demanda) (+1 more)

### Community 55 - "BRIEFING.md"
Cohesion: 0.25
Nodes (7): Artifact Index, Key Constraints, Mission, My Identity, Project Status, User Context, Victory Audit Status

### Community 57 - "AlarmConfigSection.kt"
Cohesion: 0.32
Nodes (12): AlarmCard(), AlarmConfigSection(), AlarmCreationDialog(), AlarmSubsection(), ClinicalRangeVisualCard(), copyCustomAudioToInternalStorage(), getFileNameFromUri(), android (+4 more)

### Community 58 - "AlarmCooldown"
Cohesion: 0.20
Nodes (10): AlarmCooldown, MIN_1, MIN_10, MIN_15, MIN_2, MIN_3, MIN_30, MIN_4 (+2 more)

### Community 59 - "Procedimiento Paso a Paso:"
Cohesion: 0.18
Nodes (10): 1. Confirmación de Versión y Changelog, 2. Sincronización de Versiones en Gradle, 3. Validación y Pruebas Unitarias, 4. Compilación Local de APKs, 5. Empaquetado y Organización de Artefactos, 6. Versionado en Git, 7. Publicación de la Release, Principios Obligatorios: (+2 more)

### Community 60 - "clinical_design.md"
Cohesion: 0.29
Nodes (6): 1. Paleta de Colores y Tokens Clinicos Oficiales, 2. Directrices de Interfaz Movil (`app-mobile`), 3. Directrices Wear OS (`app-wear`), 4. Persistencia y Telemetria Historica (`core:data` & `core:model`), 5. Invariantes de Telemetria y Formateo, Superficies OLED

### Community 61 - "WearQrLoginScreen.kt"
Cohesion: 0.80
Nodes (4): ClinicalProgressSpinner(), Color, Modifier, WearQrLoginScreen()

### Community 62 - "bug_report.md"
Cohesion: 0.29
Nodes (6): Comportamiento Esperado, Contexto Adicional, Descripcion del Problema, Informacion del Dispositivo, Modulo Afectado, Pasos para Reproducir

### Community 63 - "feature_request.md"
Cohesion: 0.29
Nodes (6): Alternativas Consideradas, Conformidad Regulatoria (MDDS), Contexto Adicional, Descripcion de la Funcionalidad, Justificacion y Caso de Uso, Modulo Objetivo

### Community 64 - "Codigo de Conducta del Contribuyente"
Cohesion: 0.33
Nodes (5): Atribucion, Codigo de Conducta del Contribuyente, Nuestro Compromiso, Nuestros Estandares, Responsabilidades de Aplicacion

### Community 65 - "BootReceiver.kt"
Cohesion: 0.53
Nodes (4): BootReceiver, BroadcastReceiver, Context, Intent

### Community 68 - "PULL_REQUEST_TEMPLATE.md"
Cohesion: 0.40
Nodes (4): Descripcion del Cambio, Lista de Verificacion de Calidad y Cumplimiento, Modulos Modificados, Tipo de Cambio

### Community 69 - "TEST_READY.md"
Cohesion: 0.40
Nodes (4): 1. Executive Summary, 2. Test Suite Breakdown by Module, 3. Tier Coverage Matrix, 4. How to Run the Tests

### Community 70 - "Politica de Seguridad: OpenGluco Ecosystem"
Cohesion: 0.50
Nodes (3): 1. Arquitectura de Seguridad y Privacidad, 2. Reporte Responsable de Vulnerabilidades, Politica de Seguridad: OpenGluco Ecosystem

### Community 75 - "DetailModalType"
Cohesion: 0.32
Nodes (7): DetailModalType, GLUCOSE_STATS, NONE, SENSOR_INFO, TREND_INFO, MobileStatDetailModal(), StatRow()

### Community 76 - "wear/MainActivity.kt"
Cohesion: 0.24
Nodes (7): Bundle, ComponentActivity, MainActivity, WearAppNavigation(), Modifier, WearLoginScreen(), LibreWearTheme()

### Community 77 - "WearSettingsScreen.kt"
Cohesion: 0.80
Nodes (5): CompactActionRow(), CompactSettingsRow(), Color, Modifier, WearSettingsScreen()

### Community 78 - "GlucoseAlarm"
Cohesion: 0.16
Nodes (3): AlarmEvaluatorTest, AlarmSerializationSyncTest, GlucoseAlarm

### Community 80 - "WearAuthMessageListenerService.kt"
Cohesion: 0.60
Nodes (3): MessageEvent, WearableListenerService, WearAuthMessageListenerService

### Community 86 - "AlarmSeverity"
Cohesion: 0.29
Nodes (6): Context, WearAlarmNotificationHelper, AlarmSeverity, ALERT, INFORMATIVE, URGENT

### Community 88 - "ReportsHubScreen.kt"
Cohesion: 0.57
Nodes (6): ClinicalExplanationBox(), Color, Modifier, MetricColumn(), ReportsHubScreen(), TirCategoryRow()

### Community 94 - "MobileWearableMessageListenerService.kt"
Cohesion: 0.60
Nodes (3): MessageEvent, WearableListenerService, MobileWearableMessageListenerService

### Community 95 - "MetricPeriod"
Cohesion: 0.40
Nodes (5): MetricPeriod, DAY, MONTH, THREE_MONTHS, WEEK

### Community 97 - "WearGlucoseSyncWorker.kt"
Cohesion: 0.50
Nodes (3): CoroutineWorker, Result, WearGlucoseSyncWorker

### Community 108 - "OpenGlucoRepository"
Cohesion: 0.05
Nodes (36): GlucoseAlarmWorker, Context, CoroutineWorker, Result, AuthExpiredException, OkHttpClient, Result, NetworkException (+28 more)

### Community 111 - "InteractiveMedical3DScene"
Cohesion: 0.16
Nodes (8): GLUCOSE_STATES, InteractiveMedical3DScene, renderTelemetryGraph(), setRangeState(), setTheme(), telemetry24h, toggleTheme(), updateGraphPoint()

### Community 133 - "QrScannerScreen.kt"
Cohesion: 0.33
Nodes (12): CameraPreview(), decodeQrFromImage(), android, ByteArray, Modifier, QrScannerScreen(), rotateYUV420Degree180(), rotateYUV420Degree270() (+4 more)

### Community 141 - "[ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido"
Cohesion: 0.20
Nodes (9): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), 6. Reglas de Validacion y Cumplimiento (Enforcement), [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido, Consecuencias Negativas / Riesgos Asumidos: (+1 more)

### Community 148 - "OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema"
Cohesion: 0.40
Nodes (4): Archivos, Novedades Principales, OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema, Visualización

### Community 159 - "[ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion"
Cohesion: 0.29
Nodes (6): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion

### Community 166 - "[ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer"
Cohesion: 0.40
Nodes (4): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Decision Elegida, [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer

### Community 168 - "[ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis"
Cohesion: 0.50
Nodes (3): 1. Contexto y Declaracion del Problema, 2. Reglas Deterministas Forzadas, [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis

### Community 169 - "ResponsiveLayout.kt"
Cohesion: 0.15
Nodes (12): rememberResponsiveDimensions(), ResponsiveDimensions, WindowHeightClass, COMPACT, EXPANDED, MEDIUM, WindowWidthClass, COMPACT (+4 more)

### Community 174 - "write_adrs.js"
Cohesion: 0.50
Nodes (3): docsDir, fs, path

## Knowledge Gaps
- **246 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+241 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GlucoseMeasurement` connect `GlucoseMeasurement` to `ConnectionItem`, `UserPreferencesRepository`, `WearDashboardScreen`, `ClinicalReportsCalculator`, `GlucoseMonitorForegroundService`, `MobileDashboardScreen.kt`, `WearBluetoothRfcommService`, `GlucoseUnit`, `E2ETier1FeatureCoverageTest`, `SensorInfo`, `EmpiricalStressChallengeTest`, `E2ETier2BoundaryCornerCasesTest.kt`, `.renderSparkline`, `QrAuthHelper`, `EmpiricalStressChallengeTest.kt`, `HealthDataExporter`, `HealthDataExporterTest`, `GlucoseWidgetUpdater.kt`, `CgmCurveSmootherTest`, `WearSparklineChart.kt`, `ModelSanityTest`, `DataCoherenceAndThemeTest`, `ReportsHubScreen.kt`, `OpenGlucoRepository`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `UserPreferencesRepository` connect `UserPreferencesRepository` to `ConnectionItem`, `BootReceiver.kt`, `WearGlucoseSyncWorker.kt`, `GlucoseMonitorForegroundService`, `MobileDashboardScreen.kt`, `WearBluetoothRfcommService`, `UserSettings`, `OpenGlucoRepository`, `wear/MainActivity.kt`, `WearSettingsScreen.kt`, `WearAuthMessageListenerService.kt`, `GlucoseTileService.kt`, `GlucoseWidgetUpdater.kt`, `WearLoginViewModel`, `GlucoseComplicationService.kt`, `WearQrLoginScreen.kt`, `MobileWearableMessageListenerService.kt`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Why does `GlucoseUnit` connect `GlucoseUnit` to `ConnectionItem`, `HealthDataExporter`, `UserPreferencesRepository`, `WearDashboardScreen`, `MobileDashboardScreen.kt`, `WearSettingsScreen.kt`, `GlucoseTileService.kt`, `SensorInfo`, `E2ETier2BoundaryCornerCasesTest.kt`, `GlucoseWidgetUpdater.kt`, `EmpiricalStressChallengeTest.kt`, `ReportsHubScreen.kt`, `AlarmConfigSection.kt`, `GlucoseComplicationService.kt`?**
  _High betweenness centrality (0.064) - this node is a cross-community bridge._
- **Are the 20 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testSubsampleOneOfThree_eliminatesMicroOscillationSpikes()` and `.testSubsampleOneOfThree_preservesExactLiveMeasurementAtTheTip()`) actually correct?**
  _`GlucoseMeasurement` has 20 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _246 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ConnectionItem` be split into smaller, more focused modules?**
  _Cohesion score 0.05480225988700565 - nodes in this community are weakly interconnected._
- **Should `UserPreferencesRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.11375661375661375 - nodes in this community are weakly interconnected._