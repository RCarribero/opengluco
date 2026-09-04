# Graph Report - librelinkup-ecosystem-master  (2026-09-04)

## Corpus Check
- 143 files · ~96,761 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1162 nodes · 2077 edges · 107 communities (79 shown, 28 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 51 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `833aa60c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- GlucoseDashboardCarScreen
- OpenGlucoInterceptor
- AlarmRepository
- UserPreferencesRepository
- QrPairingPayload
- WearSettingsScreen
- ClinicalReportsCalculator.kt
- GlucoseMonitorForegroundService
- MobileDashboardScreen.kt
- GlucoseWidgetUpdater.kt
- GlucoseMeasurement
- GlucoseAlarmWorker.kt
- MobileAlarmNotificationHelper
- ClinicalTheme
- OpenGluco Ecosystem
- E2ETier1FeatureCoverageTest
- SensorInfo
- EmpiricalStressChallengeTest
- WearDashboardViewModel
- .renderSparkline
- MobileLegalComplianceTest
- QrAuthHelper
- EmpiricalStressChallengeTest.kt
- GlucoseChartWidgetProvider.kt
- GlucoseCompactWidgetProvider.kt
- E2ETier4RealWorldScenariosTest.kt
- WearLegalTextsTest
- ClinicalReportsCalculatorTest
- AlarmSeverity
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
- PROJECT.md
- Reglas de Proyecto: OpenGluco Ecosystem
- BRIEFING.md
- CgmCurveSmootherTest
- GlucoseUnit
- AlarmCooldown
- Procedimiento Paso a Paso:
- clinical_design.md
- WearQrLoginScreen.kt
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
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
- OpenGlucoRepository
- GlucoseAlarm
- DataCoherenceAndThemeTest
- WearLoginViewModel
- GlucoseComplicationService.kt
- WearAlarmNotificationHelper
- AlarmEvaluator
- ReportsHubScreen.kt
- BootReceiver.kt
- ConnectionItem
- MobileWearableMessageListenerService.kt
- MetricPeriod
- WearAuthMessageListenerService.kt
- WearGlucoseSyncWorker.kt
- MobileAlarmSyncHelper
- AppReleaseInfo
- OpenGlucoRepository.kt
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
3. `GlucoseAlarm` - 47 edges
4. `AlarmRepository` - 42 edges
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
- `GlucoseDashboardCarScreen` --references--> `ConnectionItem`  [EXTRACTED]
  app-auto/src/main/java/com/example/opengluco/auto/screen/GlucoseDashboardCarScreen.kt → core/model/src/main/java/com/example/opengluco/core/model/OpenGlucoModels.kt
- `GlucoseDashboardCarScreen` --references--> `GlucoseMeasurement`  [EXTRACTED]
  app-auto/src/main/java/com/example/opengluco/auto/screen/GlucoseDashboardCarScreen.kt → core/model/src/main/java/com/example/opengluco/core/model/OpenGlucoModels.kt

## Import Cycles
- None detected.

## Communities (107 total, 28 thin omitted)

### Community 0 - "GlucoseDashboardCarScreen"
Cohesion: 0.07
Nodes (22): AutoTtsAlertManager, GlucoseCarAppService, Session, GlucoseCarSession, Intent, Screen, Session, GlucoseDashboardCarScreen (+14 more)

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
Cohesion: 0.19
Nodes (5): Context, MobilePairingHelper, QrEncryptedPayload, QrPairingPayload, QrAuthModelsTest

### Community 5 - "WearSettingsScreen"
Cohesion: 0.07
Nodes (36): DualFloatingOrbs(), Modifier, Modifier, PatientSelectorChip(), Modifier, WearGlucoseGauge(), DetailModalType, GLUCOSE_STATS (+28 more)

### Community 6 - "ClinicalReportsCalculator.kt"
Cohesion: 0.09
Nodes (26): ClinicalReportsCalculator, com, AverageGlucoseReport, DailyGraphDaySummary, DailyGraphReport, DailyPatternsReport, EstimatedA1cReport, HourlyPercentile (+18 more)

### Community 7 - "GlucoseMonitorForegroundService"
Cohesion: 0.15
Nodes (12): GlucoseMonitorForegroundService, Context, IBinder, Intent, Service, ConfigurationDiagnosticsDialog(), DiagnosticItemCard(), Context (+4 more)

### Community 8 - "MobileDashboardScreen.kt"
Cohesion: 0.20
Nodes (20): androidx, TargetRangeDialog(), DailyStatItem(), DashboardChartCard(), DashboardClinicalErrorBanner(), DashboardHeroSection(), DashboardSensorCard(), DashboardStatsCard() (+12 more)

### Community 9 - "GlucoseWidgetUpdater.kt"
Cohesion: 0.57
Nodes (3): GlucoseWidgetUpdater, Context, RemoteViews

### Community 11 - "GlucoseAlarmWorker.kt"
Cohesion: 0.36
Nodes (4): GlucoseAlarmWorker, Context, CoroutineWorker, Result

### Community 12 - "MobileAlarmNotificationHelper"
Cohesion: 0.08
Nodes (20): android, com, Context, MobileAlarmNotificationHelper, Context, IBinder, Intent, Service (+12 more)

### Community 13 - "ClinicalTheme"
Cohesion: 0.32
Nodes (9): Modifier, MobileDualFloatingOrbs(), ClinicalColorScheme, ClinicalTheme, getClinicalStatusColor(), getGlucoseStatusColor(), getGlucoseValueColor(), Color (+1 more)

### Community 14 - "OpenGluco Ecosystem"
Cohesion: 0.05
Nodes (37): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+29 more)

### Community 16 - "SensorInfo"
Cohesion: 0.11
Nodes (6): ClinicalSparklineWithSensor(), Modifier, Modifier, WearSensorChip(), E2ETier2BoundaryCornerCasesTest, SensorInfo

### Community 18 - "WearDashboardViewModel"
Cohesion: 0.23
Nodes (8): Error, StateFlow, ViewModel, Loading, NeedsLogin, Success, WearDashboardUiState, WearDashboardViewModel

### Community 19 - ".renderSparkline"
Cohesion: 0.26
Nodes (4): Bitmap, WidgetChartRenderer, CgmCurveSmoother, CubicBezierSegment

### Community 22 - "EmpiricalStressChallengeTest.kt"
Cohesion: 0.20
Nodes (5): KeystoreCryptoHelper, QrDeviceType, ANDROID_AUTO, WEAR_OS, SecretKey

### Community 23 - "GlucoseChartWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseChartWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 24 - "GlucoseCompactWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseCompactWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 28 - "AlarmSeverity"
Cohesion: 0.14
Nodes (9): AlarmSerializationSyncTest, AlarmEvaluationResult, AlarmSeverity, ALERT, INFORMATIVE, URGENT, AlarmType, HIGH (+1 more)

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
Cohesion: 0.17
Nodes (7): AppUpdateInstaller, Context, Result, AppUpdateRepository, OkHttpClient, Result, AppUpdateRepositoryTest

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

### Community 53 - "PROJECT.md"
Cohesion: 0.22
Nodes (8): Architecture, Code Layout, Cryptographic Contract (`KeystoreCryptoHelper`), Data Portability CSV Contract (`HealthDataExporter`), Feature Inventory, Interface Contracts, Legal & Regulatory String Constants, Milestones

### Community 54 - "Reglas de Proyecto: OpenGluco Ecosystem"
Cohesion: 0.20
Nodes (9): 1. Directrices de Interfaz y Tokens Clinicos, 2. App Movil (`app-mobile`), 3. App Reloj (`app-wear`), 4. Persistencia y Datos (`core:data`), 5. Invariantes de Telemetria y Formato, 6. Mencion a Abbott Laboratories y Blindaje Legal, 7. Arquitectura y Grafo de Conocimiento (Graphify), 8. Protocolo de Publicación y Actualizaciones OTA (Bajo Demanda) (+1 more)

### Community 55 - "BRIEFING.md"
Cohesion: 0.25
Nodes (7): Artifact Index, Key Constraints, Mission, My Identity, Project Status, User Context, Victory Audit Status

### Community 57 - "GlucoseUnit"
Cohesion: 0.35
Nodes (11): AlarmCard(), AlarmConfigSection(), AlarmCreationDialog(), AlarmSubsection(), ClinicalRangeVisualCard(), Color, ImageVector, Modifier (+3 more)

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
Cohesion: 0.24
Nodes (7): ClinicalProgressSpinner(), Color, Modifier, WearQrLoginScreen(), Bitmap, QrSessionExchange, ServerSocket

### Community 62 - "bug_report.md"
Cohesion: 0.29
Nodes (6): Comportamiento Esperado, Contexto Adicional, Descripcion del Problema, Informacion del Dispositivo, Modulo Afectado, Pasos para Reproducir

### Community 63 - "feature_request.md"
Cohesion: 0.29
Nodes (6): Alternativas Consideradas, Conformidad Regulatoria (MDDS), Contexto Adicional, Descripcion de la Funcionalidad, Justificacion y Caso de Uso, Modulo Objetivo

### Community 64 - "Codigo de Conducta del Contribuyente"
Cohesion: 0.33
Nodes (5): Atribucion, Codigo de Conducta del Contribuyente, Nuestro Compromiso, Nuestros Estandares, Responsabilidades de Aplicacion

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
Cohesion: 0.26
Nodes (7): Bundle, ComponentActivity, MainActivity, WearAppNavigation(), Modifier, WearLoginScreen(), LibreWearTheme()

### Community 77 - "OpenGlucoRepository"
Cohesion: 0.16
Nodes (10): Bundle, ComponentActivity, MainActivity, MobileAppNavigation(), Modifier, MobileLoginScreen(), LibreMobileTheme(), OpenGlucoRepository (+2 more)

### Community 80 - "WearLoginViewModel"
Cohesion: 0.27
Nodes (8): Error, Idle, StateFlow, ViewModel, Loading, Success, WearLoginUiState, WearLoginViewModel

### Community 81 - "GlucoseComplicationService.kt"
Cohesion: 0.33
Nodes (6): GlucoseComplicationService, ComplicationData, ComplicationDataSourceService, ComplicationRequest, ComplicationRequestListener, ComplicationType

### Community 88 - "ReportsHubScreen.kt"
Cohesion: 0.57
Nodes (6): ClinicalExplanationBox(), Color, Modifier, MetricColumn(), ReportsHubScreen(), TirCategoryRow()

### Community 90 - "BootReceiver.kt"
Cohesion: 0.53
Nodes (4): BootReceiver, BroadcastReceiver, Context, Intent

### Community 91 - "ConnectionItem"
Cohesion: 0.53
Nodes (4): Modifier, PatientHeaderChip(), PatientSelectorModal(), ConnectionItem

### Community 94 - "MobileWearableMessageListenerService.kt"
Cohesion: 0.60
Nodes (3): MessageEvent, WearableListenerService, MobileWearableMessageListenerService

### Community 95 - "MetricPeriod"
Cohesion: 0.40
Nodes (5): MetricPeriod, DAY, MONTH, THREE_MONTHS, WEEK

### Community 96 - "WearAuthMessageListenerService.kt"
Cohesion: 0.60
Nodes (3): MessageEvent, WearableListenerService, WearAuthMessageListenerService

### Community 97 - "WearGlucoseSyncWorker.kt"
Cohesion: 0.50
Nodes (3): CoroutineWorker, Result, WearGlucoseSyncWorker

### Community 108 - "OpenGlucoRepository.kt"
Cohesion: 0.07
Nodes (30): AuthExpiredException, OkHttpClient, Result, NetworkException, OpenGlucoRegion, AP, DE, EU (+22 more)

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
Cohesion: 0.18
Nodes (11): rememberResponsiveDimensions(), ResponsiveDimensions, WindowHeightClass, COMPACT, EXPANDED, MEDIUM, WindowWidthClass, COMPACT (+3 more)

### Community 174 - "write_adrs.js"
Cohesion: 0.50
Nodes (3): docsDir, fs, path

## Knowledge Gaps
- **245 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+240 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **28 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GlucoseMeasurement` connect `GlucoseMeasurement` to `GlucoseDashboardCarScreen`, `UserPreferencesRepository`, `WearSettingsScreen`, `ClinicalReportsCalculator.kt`, `GlucoseMonitorForegroundService`, `MobileDashboardScreen.kt`, `GlucoseWidgetUpdater.kt`, `MobileAlarmNotificationHelper`, `ClinicalTheme`, `E2ETier1FeatureCoverageTest`, `SensorInfo`, `EmpiricalStressChallengeTest`, `WearDashboardViewModel`, `.renderSparkline`, `QrAuthHelper`, `EmpiricalStressChallengeTest.kt`, `E2ETier4RealWorldScenariosTest.kt`, `ClinicalReportsCalculatorTest`, `HealthDataExporter`, `HealthDataExporterTest`, `CgmCurveSmootherTest`, `WearSparklineChart.kt`, `ModelSanityTest`, `DataCoherenceAndThemeTest`, `ReportsHubScreen.kt`, `ConnectionItem`, `OpenGlucoRepository.kt`?**
  _High betweenness centrality (0.228) - this node is a cross-community bridge._
- **Why does `UserPreferencesRepository` connect `UserPreferencesRepository` to `GlucoseDashboardCarScreen`, `WearAuthMessageListenerService.kt`, `WearGlucoseSyncWorker.kt`, `WearSettingsScreen`, `GlucoseMonitorForegroundService`, `MobileDashboardScreen.kt`, `GlucoseWidgetUpdater.kt`, `GlucoseAlarmWorker.kt`, `wear/MainActivity.kt`, `OpenGlucoRepository`, `MobileAlarmNotificationHelper`, `GlucoseTileService.kt`, `GlucoseComplicationService.kt`, `WearLoginViewModel`, `WearDashboardViewModel`, `BootReceiver.kt`, `WearQrLoginScreen.kt`, `MobileWearableMessageListenerService.kt`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Why does `OpenGlucoRepository` connect `OpenGlucoRepository` to `GlucoseDashboardCarScreen`, `WearGlucoseSyncWorker.kt`, `OpenGlucoInterceptor`, `GlucoseMonitorForegroundService`, `MobileDashboardScreen.kt`, `GlucoseAlarmWorker.kt`, `wear/MainActivity.kt`, `OpenGlucoRepository.kt`, `WearLoginViewModel`, `WearDashboardViewModel`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **Are the 20 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testSubsampleOneOfThree_eliminatesMicroOscillationSpikes()` and `.testSubsampleOneOfThree_preservesExactLiveMeasurementAtTheTip()`) actually correct?**
  _`GlucoseMeasurement` has 20 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _245 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `GlucoseDashboardCarScreen` be split into smaller, more focused modules?**
  _Cohesion score 0.06976744186046512 - nodes in this community are weakly interconnected._
- **Should `UserPreferencesRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.11375661375661375 - nodes in this community are weakly interconnected._