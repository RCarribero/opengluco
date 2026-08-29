# Graph Report - librelinkup-ecosystem-master  (2026-08-29)

## Corpus Check
- 122 files · ~70,346 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 980 nodes · 1778 edges · 67 communities (46 shown, 21 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 42 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `a2cdc657`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MobileDashboardScreen.kt
- OpenGlucoRepository
- AlarmRepository
- UserPreferencesRepository
- QrPairingPayload
- WearDashboardScreen
- ClinicalReportsCalculator.kt
- GlucoseMonitorForegroundService
- GlucoseWidgetUpdater.kt
- wear/MainActivity.kt
- GlucoseMeasurement
- QrScannerScreen.kt
- WearBluetoothRfcommService
- GlucoseDashboardCarScreen
- OpenGluco Ecosystem
- E2ETier1FeatureCoverageTest
- SensorInfo
- EmpiricalStressChallengeTest
- EmpiricalStressChallengeTest.kt
- .renderSparkline
- MobileLegalComplianceTest
- HealthDataExporter
- ClinicalReportsCalculatorTest
- GlucoseChartWidgetProvider.kt
- GlucoseCompactWidgetProvider.kt
- QrAuthHelper
- WearLegalTextsTest
- E2ETier2BoundaryCornerCasesTest.kt
- GlucoseAlarm
- AlarmDismissReceiver.kt
- WearClinicalDesignAndSafetyTest
- Sistema de Diseno: OpenGluco (Minimalista Clinico)
- AutoClinicalSafetyTest
- AutoManifestAndSecurityTest
- WearSparklineChart.kt
- ModelSanityTest
- WearQrLoginScreen.kt
- KeystoreCryptoHelperTest
- QrAuthHelperTest
- 2. Test Tiers & Methodology
- WearBluetoothSecurityTest
- Acceptance Criteria
- 1. Reglas Innegociables del Proyecto
- Acceptance Criteria
- PROJECT.md
- Reglas de Proyecto: OpenGluco Ecosystem
- BRIEFING.md
- HealthDataExporterTest
- clinical_design.md
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
- PULL_REQUEST_TEMPLATE.md
- TEST_READY.md
- Politica de Seguridad: OpenGluco Ecosystem
- no_emojis.md
- PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)
- rules/graphify.md
- workflows/graphify.md

## God Nodes (most connected - your core abstractions)
1. `GlucoseMeasurement` - 96 edges
2. `UserPreferencesRepository` - 70 edges
3. `AlarmRepository` - 42 edges
4. `GlucoseAlarm` - 40 edges
5. `GlucoseUnit` - 38 edges
6. `OpenGlucoRepository` - 35 edges
7. `ConnectionItem` - 30 edges
8. `QrAuthHelper` - 25 edges
9. `SensorInfo` - 23 edges
10. `ClinicalModelsTest` - 22 edges

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

## Communities (67 total, 21 thin omitted)

### Community 0 - "MobileDashboardScreen.kt"
Cohesion: 0.06
Nodes (57): androidx, LegalNoticeDialog(), LegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS (+49 more)

### Community 1 - "OpenGlucoRepository"
Cohesion: 0.05
Nodes (27): Result, OpenGlucoRegion, AP, DE, EU, FR, JP, US (+19 more)

### Community 2 - "AlarmRepository"
Cohesion: 0.07
Nodes (18): GlucoseAlarmWorker, Context, CoroutineWorker, Result, Context, MobileAlarmSyncHelper, MessageEvent, WearableListenerService (+10 more)

### Community 3 - "UserPreferencesRepository"
Cohesion: 0.05
Nodes (35): Bundle, ComponentActivity, MainActivity, MobileAppNavigation(), BootReceiver, BroadcastReceiver, Context, Intent (+27 more)

### Community 4 - "QrPairingPayload"
Cohesion: 0.15
Nodes (6): Context, MobilePairingHelper, QrEncryptedPayload, QrPairingPayload, QrSessionExchange, QrAuthModelsTest

### Community 5 - "WearDashboardScreen"
Cohesion: 0.08
Nodes (31): DualFloatingOrbs(), Modifier, Modifier, PatientSelectorChip(), Modifier, WearGlucoseGauge(), DetailModalType, GLUCOSE_STATS (+23 more)

### Community 6 - "ClinicalReportsCalculator.kt"
Cohesion: 0.10
Nodes (24): ClinicalReportsCalculator, AverageGlucoseReport, DailyGraphDaySummary, DailyGraphReport, DailyPatternsReport, EstimatedA1cReport, HourlyPercentile, LowGlucoseEvent (+16 more)

### Community 7 - "GlucoseMonitorForegroundService"
Cohesion: 0.15
Nodes (12): GlucoseMonitorForegroundService, Context, IBinder, Intent, Service, ConfigurationDiagnosticsDialog(), DiagnosticItemCard(), Context (+4 more)

### Community 8 - "GlucoseWidgetUpdater.kt"
Cohesion: 0.57
Nodes (3): GlucoseWidgetUpdater, Context, RemoteViews

### Community 9 - "wear/MainActivity.kt"
Cohesion: 0.09
Nodes (23): Bundle, ComponentActivity, MainActivity, WearAppNavigation(), Modifier, WearLoginScreen(), Error, Idle (+15 more)

### Community 11 - "QrScannerScreen.kt"
Cohesion: 0.33
Nodes (12): CameraPreview(), decodeQrFromImage(), android, ByteArray, Modifier, QrScannerScreen(), rotateYUV420Degree180(), rotateYUV420Degree270() (+4 more)

### Community 12 - "WearBluetoothRfcommService"
Cohesion: 0.18
Nodes (8): Context, IBinder, Intent, Service, WearBluetoothRfcommService, BluetoothServerSocket, BluetoothSocket, Notification

### Community 13 - "GlucoseDashboardCarScreen"
Cohesion: 0.08
Nodes (20): GlucoseCarAppService, Session, GlucoseCarSession, Intent, Screen, Session, GlucoseDashboardCarScreen, com (+12 more)

### Community 14 - "OpenGluco Ecosystem"
Cohesion: 0.05
Nodes (37): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+29 more)

### Community 16 - "SensorInfo"
Cohesion: 0.13
Nodes (6): ClinicalSparklineWithSensor(), Modifier, Modifier, WearSensorChip(), E2ETier4RealWorldScenariosTest, SensorInfo

### Community 18 - "EmpiricalStressChallengeTest.kt"
Cohesion: 0.22
Nodes (5): KeystoreCryptoHelper, QrDeviceType, ANDROID_AUTO, WEAR_OS, SecretKey

### Community 19 - ".renderSparkline"
Cohesion: 0.27
Nodes (4): Bitmap, WidgetChartRenderer, CgmCurveSmoother, CubicBezierSegment

### Community 21 - "HealthDataExporter"
Cohesion: 0.24
Nodes (3): HealthDataExporter, Context, E2ETier3CrossFeatureCombinationsTest

### Community 23 - "GlucoseChartWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseChartWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 24 - "GlucoseCompactWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseCompactWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 28 - "GlucoseAlarm"
Cohesion: 0.06
Nodes (32): android, Context, MobileAlarmNotificationHelper, AlarmCard(), AlarmConfigSection(), AlarmCreationDialog(), AlarmSubsection(), ClinicalRangeVisualCard() (+24 more)

### Community 29 - "AlarmDismissReceiver.kt"
Cohesion: 0.53
Nodes (4): AlarmDismissReceiver, BroadcastReceiver, Context, Intent

### Community 31 - "Sistema de Diseno: OpenGluco (Minimalista Clinico)"
Cohesion: 0.14
Nodes (13): 1. Filosofia y Estilo Visual, 2. Paleta de Colores Oficial, 3. Jerarquia y Distribucion Espacial en Wear OS, 4. Interactividad y Feedback Haptico, 5. Mapeo de Codigo Jetpack Compose, 6. Mencion a Abbott Laboratories y Marcas Registradas, A. Esferas Flotantes Superiores (`DualFloatingOrbs`), B. Grafica Sparkline + Badge de Sensor (+5 more)

### Community 44 - "WearQrLoginScreen.kt"
Cohesion: 0.33
Nodes (6): ClinicalProgressSpinner(), Color, Modifier, WearQrLoginScreen(), Bitmap, ServerSocket

### Community 47 - "2. Test Tiers & Methodology"
Cohesion: 0.17
Nodes (11): 1. Test Architecture Overview, 2. Test Tiers & Methodology, 3. Detailed Feature-to-Test Mapping Matrix, 4. Test Execution & Verification, Global Test Command, Module-Specific Unit Test Tasks, Tier 1: Feature Coverage (>=5 Test Cases per Feature across all 18 Features), Tier 2: Boundary & Corner Cases (>=5 Test Cases per Feature) (+3 more)

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
Cohesion: 0.25
Nodes (7): 1. Directrices de Interfaz y Tokens Clinicos, 2. App Movil (`app-mobile`), 3. App Reloj (`app-wear`), 4. Persistencia y Datos (`core:data`), 5. Invariantes de Telemetria y Formato, 6. Mencion a Abbott Laboratories y Blindaje Legal, Reglas de Proyecto: OpenGluco Ecosystem

### Community 55 - "BRIEFING.md"
Cohesion: 0.25
Nodes (7): Artifact Index, Key Constraints, Mission, My Identity, Project Status, User Context, Victory Audit Status

### Community 60 - "clinical_design.md"
Cohesion: 0.29
Nodes (6): 1. Paleta de Colores y Tokens Clinicos Oficiales, 2. Directrices de Interfaz Movil (`app-mobile`), 3. Directrices Wear OS (`app-wear`), 4. Persistencia y Telemetria Historica (`core:data` & `core:model`), 5. Invariantes de Telemetria y Formateo, Superficies OLED

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

## Knowledge Gaps
- **188 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+183 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GlucoseMeasurement` connect `GlucoseMeasurement` to `MobileDashboardScreen.kt`, `OpenGlucoRepository`, `UserPreferencesRepository`, `WearDashboardScreen`, `ClinicalReportsCalculator.kt`, `GlucoseMonitorForegroundService`, `GlucoseWidgetUpdater.kt`, `wear/MainActivity.kt`, `WearBluetoothRfcommService`, `GlucoseDashboardCarScreen`, `E2ETier1FeatureCoverageTest`, `SensorInfo`, `EmpiricalStressChallengeTest`, `EmpiricalStressChallengeTest.kt`, `.renderSparkline`, `HealthDataExporter`, `ClinicalReportsCalculatorTest`, `QrAuthHelper`, `E2ETier2BoundaryCornerCasesTest.kt`, `WearSparklineChart.kt`, `ModelSanityTest`, `HealthDataExporterTest`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `UserPreferencesRepository` connect `UserPreferencesRepository` to `MobileDashboardScreen.kt`, `AlarmRepository`, `GlucoseMonitorForegroundService`, `GlucoseWidgetUpdater.kt`, `wear/MainActivity.kt`, `WearBluetoothRfcommService`, `GlucoseDashboardCarScreen`, `WearQrLoginScreen.kt`?**
  _High betweenness centrality (0.129) - this node is a cross-community bridge._
- **Why does `OpenGlucoRepository` connect `OpenGlucoRepository` to `MobileDashboardScreen.kt`, `AlarmRepository`, `UserPreferencesRepository`, `GlucoseMonitorForegroundService`, `wear/MainActivity.kt`, `GlucoseDashboardCarScreen`?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._
- **Are the 16 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testConnectionItem_effectiveMeasurement()` and `.testDisplayTime_nullOrInvalidFallback()`) actually correct?**
  _`GlucoseMeasurement` has 16 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _188 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MobileDashboardScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05924920850293985 - nodes in this community are weakly interconnected._
- **Should `OpenGlucoRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.053613053613053616 - nodes in this community are weakly interconnected._