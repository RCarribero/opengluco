# Graph Report - librelinkup-ecosystem-master  (2026-08-29)

## Corpus Check
- 135 files · ~101,993 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2671 nodes · 5697 edges · 161 communities (103 shown, 58 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 296 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f282108c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- three.min.js
- OpenGlucoRepository.kt
- AlarmRepository
- UserPreferencesRepository
- QrAuthHelper
- WearDashboardScreen
- ClinicalReportsCalculator.kt
- GlucoseMonitorForegroundService
- MobileDashboardScreen.kt
- wear/MainActivity.kt
- GlucoseMeasurement
- MobileAlarmNotificationHelper
- WearBluetoothRfcommService
- GlucoseDashboardCarScreen
- OpenGluco Ecosystem
- E2ETier1FeatureCoverageTest
- SensorInfo
- EmpiricalStressChallengeTest
- WearSettingsScreen
- .renderSparkline
- MobileLegalComplianceTest
- sn
- ClinicalReportsCalculatorTest
- GlucoseChartWidgetProvider.kt
- GlucoseCompactWidgetProvider.kt
- Lt
- WearLegalTextsTest
- e
- GlucoseAlarm
- AlarmDismissReceiver.kt
- WearClinicalDesignAndSafetyTest
- Sistema de Diseno: OpenGluco (Minimalista Clinico)
- AutoClinicalSafetyTest
- AutoManifestAndSecurityTest
- en
- .multiplyScalar
- AppUpdateRepository
- EmpiricalStressChallengeTest.kt
- QrAuthHelperTest
- 2. Test Tiers & Methodology
- GlucoseTileService.kt
- Acceptance Criteria
- 1. Reglas Innegociables del Proyecto
- Acceptance Criteria
- AlarmSeverity
- PROJECT.md
- Reglas de Proyecto: OpenGluco Ecosystem
- BRIEFING.md
- tn
- GlucoseUnit
- ConfigurationDiagnosticsDialog
- Procedimiento Paso a Paso:
- clinical_design.md
- AlarmConfigSection.kt
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
- LegalNoticeType
- WearAlarmNotificationHelper
- GlucoseAlarmWorker.kt
- PULL_REQUEST_TEMPLATE.md
- TEST_READY.md
- Politica de Seguridad: OpenGluco Ecosystem
- no_emojis.md
- PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)
- rules/graphify.md
- workflows/graphify.md
- DetailModalType
- DashboardTimeframe
- MobileWearableMessageListenerService.kt
- WearAuthMessageListenerService.kt
- WearGlucoseSyncWorker.kt
- MobileAlarmSyncHelper
- se
- ConnectionItem
- MetricPeriod
- At
- Ce
- vt
- St
- constructor
- copy
- Lc
- gt
- .constructor
- update
- jc
- ja
- ws
- OpenGlucoRepository
- ml
- .distanceTo
- yc
- xc
- Wn
- eh
- jt
- pt
- Kn
- parseObject
- InteractiveMedical3DScene
- .toJSON
- Ne
- yt
- bl
- ei
- yo
- ct
- nl
- nc
- .fromJSON
- ReportTimeBlock
- .fromArray
- .toArray
- je
- getClinicalStatusColor
- wo
- kl
- mo
- GlucoseComplicationService.kt
- mc
- bt
- qc
- GlucoseWidgetUpdater.kt
- ge
- ia
- Jr
- .parse
- BootReceiver.kt
- Al
- el
- Ko
- pl
- Tt
- OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema
- Rl
- $c
- Mh
- ms
- ss
- cs
- Et
- .constructor
- Zc
- an
- Ba
- dn

## God Nodes (most connected - your core abstractions)
1. `Lt` - 135 edges
2. `copy()` - 128 edges
3. `GlucoseMeasurement` - 98 edges
4. `vt` - 90 edges
5. `UserPreferencesRepository` - 70 edges
6. `ws()` - 67 edges
7. `tn` - 65 edges
8. `St` - 60 edges
9. `se` - 49 edges
10. `Ce` - 46 edges

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

## Communities (161 total, 58 thin omitted)

### Community 0 - "three.min.js"
Cohesion: 0.03
Nodes (36): Ah, bi(), bs, cn, er(), fh, fn, fs (+28 more)

### Community 1 - "OpenGlucoRepository.kt"
Cohesion: 0.06
Nodes (27): AuthExpiredException, Result, NetworkException, AuthExpired, AuthTicket, BaseResponse, ClinicalErrorType, Generic (+19 more)

### Community 2 - "AlarmRepository"
Cohesion: 0.16
Nodes (3): AlarmRepository, Flow, Result

### Community 3 - "UserPreferencesRepository"
Cohesion: 0.12
Nodes (3): Flow, PreferencesKeys, UserPreferencesRepository

### Community 4 - "QrAuthHelper"
Cohesion: 0.06
Nodes (27): Context, MobilePairingHelper, CameraPreview(), decodeQrFromImage(), android, ByteArray, Modifier, QrScannerScreen() (+19 more)

### Community 5 - "WearDashboardScreen"
Cohesion: 0.15
Nodes (16): ClinicalSparklineWithSensor(), Modifier, DualFloatingOrbs(), Modifier, Modifier, PatientSelectorChip(), DetailModalType, GLUCOSE_STATS (+8 more)

### Community 6 - "ClinicalReportsCalculator.kt"
Cohesion: 0.16
Nodes (15): ClinicalReportsCalculator, com, AverageGlucoseReport, DailyGraphDaySummary, DailyGraphReport, DailyPatternsReport, EstimatedA1cReport, HourlyPercentile (+7 more)

### Community 7 - "GlucoseMonitorForegroundService"
Cohesion: 0.24
Nodes (6): GlucoseMonitorForegroundService, Context, IBinder, Intent, Service, PowerManager

### Community 8 - "MobileDashboardScreen.kt"
Cohesion: 0.30
Nodes (14): androidx, DailyStatItem(), formatChartTime(), Color, com, Modifier, MobileDashboardScreen(), MobileGlucoseChart() (+6 more)

### Community 9 - "wear/MainActivity.kt"
Cohesion: 0.14
Nodes (15): Bundle, ComponentActivity, MainActivity, WearAppNavigation(), Modifier, WearLoginScreen(), Error, Idle (+7 more)

### Community 10 - "GlucoseMeasurement"
Cohesion: 0.08
Nodes (7): Modifier, WearSparklineChart(), E2ETier3CrossFeatureCombinationsTest, HealthDataExporterTest, GlucoseMeasurement, ClinicalModelsTest, ModelSanityTest

### Community 11 - "MobileAlarmNotificationHelper"
Cohesion: 0.29
Nodes (5): android, com, Context, MobileAlarmNotificationHelper, MediaPlayer

### Community 12 - "WearBluetoothRfcommService"
Cohesion: 0.18
Nodes (8): Context, IBinder, Intent, Service, WearBluetoothRfcommService, BluetoothServerSocket, BluetoothSocket, Notification

### Community 13 - "GlucoseDashboardCarScreen"
Cohesion: 0.07
Nodes (22): AutoTtsAlertManager, GlucoseCarAppService, Session, GlucoseCarSession, Intent, Screen, Session, GlucoseDashboardCarScreen (+14 more)

### Community 14 - "OpenGluco Ecosystem"
Cohesion: 0.05
Nodes (37): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+29 more)

### Community 16 - "SensorInfo"
Cohesion: 0.10
Nodes (5): Modifier, WearSensorChip(), E2ETier2BoundaryCornerCasesTest, E2ETier4RealWorldScenariosTest, SensorInfo

### Community 18 - "WearSettingsScreen"
Cohesion: 0.19
Nodes (16): Modifier, WearLegalNoticeDialog(), WearLegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS (+8 more)

### Community 19 - ".renderSparkline"
Cohesion: 0.27
Nodes (4): Bitmap, WidgetChartRenderer, CgmCurveSmoother, CubicBezierSegment

### Community 21 - "sn"
Cohesion: 0.06
Nodes (3): ls(), sn, Yh()

### Community 23 - "GlucoseChartWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseChartWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 24 - "GlucoseCompactWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseCompactWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 27 - "e"
Cohesion: 0.07
Nodes (22): cl, hl, ii, il(), load(), oc, ol, or() (+14 more)

### Community 28 - "GlucoseAlarm"
Cohesion: 0.22
Nodes (3): AlarmEvaluator, AlarmEvaluatorTest, GlucoseAlarm

### Community 29 - "AlarmDismissReceiver.kt"
Cohesion: 0.53
Nodes (4): AlarmDismissReceiver, BroadcastReceiver, Context, Intent

### Community 31 - "Sistema de Diseno: OpenGluco (Minimalista Clinico)"
Cohesion: 0.14
Nodes (13): 1. Filosofia y Estilo Visual, 2. Paleta de Colores Oficial, 3. Jerarquia y Distribucion Espacial en Wear OS, 4. Interactividad y Feedback Haptico, 5. Mapeo de Codigo Jetpack Compose, 6. Mencion a Abbott Laboratories y Marcas Registradas, A. Esferas Flotantes Superiores (`DualFloatingOrbs`), B. Grafica Sparkline + Badge de Sensor (+5 more)

### Community 34 - "en"
Cohesion: 0.08
Nodes (11): ca, Da, v(), en, v(), mn, i(), m() (+3 more)

### Community 35 - ".multiplyScalar"
Cohesion: 0.08
Nodes (5): Ea(), ht(), re, setFromCartesianCoords(), setFromVector3()

### Community 44 - "AppUpdateRepository"
Cohesion: 0.15
Nodes (9): UpdateAvailableDialog(), AppUpdateInstaller, Context, Result, AppUpdateRepository, OkHttpClient, Result, AppUpdateRepositoryTest (+1 more)

### Community 45 - "EmpiricalStressChallengeTest.kt"
Cohesion: 0.10
Nodes (8): HealthDataExporter, Context, KeystoreCryptoHelper, KeystoreCryptoHelperTest, QrDeviceType, ANDROID_AUTO, WEAR_OS, SecretKey

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

### Community 52 - "AlarmSeverity"
Cohesion: 0.13
Nodes (12): AlarmSerializationSyncTest, AlarmCooldown, MIN_10, MIN_15, MIN_30, MIN_5, NONE, AlarmEvaluationResult (+4 more)

### Community 53 - "PROJECT.md"
Cohesion: 0.22
Nodes (8): Architecture, Code Layout, Cryptographic Contract (`KeystoreCryptoHelper`), Data Portability CSV Contract (`HealthDataExporter`), Feature Inventory, Interface Contracts, Legal & Regulatory String Constants, Milestones

### Community 54 - "Reglas de Proyecto: OpenGluco Ecosystem"
Cohesion: 0.20
Nodes (9): 1. Directrices de Interfaz y Tokens Clinicos, 2. App Movil (`app-mobile`), 3. App Reloj (`app-wear`), 4. Persistencia y Datos (`core:data`), 5. Invariantes de Telemetria y Formato, 6. Mencion a Abbott Laboratories y Blindaje Legal, 7. Arquitectura y Grafo de Conocimiento (Graphify), 8. Protocolo de Publicación y Actualizaciones OTA (Bajo Demanda) (+1 more)

### Community 55 - "BRIEFING.md"
Cohesion: 0.25
Nodes (7): Artifact Index, Key Constraints, Mission, My Identity, Project Status, User Context, Victory Audit Status

### Community 56 - "tn"
Cohesion: 0.05
Nodes (9): _a, bo, Do, dt(), $e(), Qe(), tn, ut() (+1 more)

### Community 57 - "GlucoseUnit"
Cohesion: 0.19
Nodes (16): Modifier, MobileDualFloatingOrbs(), Modifier, PatientHeaderChip(), PatientSelectorModal(), TargetRangeDialog(), ClinicalColorScheme, ClinicalTheme (+8 more)

### Community 58 - "ConfigurationDiagnosticsDialog"
Cohesion: 0.41
Nodes (6): ConfigurationDiagnosticsDialog(), DiagnosticItemCard(), Context, ImageVector, SystemDiagnosticsHelper, SystemDiagnosticsState

### Community 59 - "Procedimiento Paso a Paso:"
Cohesion: 0.18
Nodes (10): 1. Confirmación de Versión y Changelog, 2. Sincronización de Versiones en Gradle, 3. Validación y Pruebas Unitarias, 4. Compilación Local de APKs, 5. Empaquetado y Organización de Artefactos, 6. Versionado en Git, 7. Publicación de la Release, Principios Obligatorios: (+2 more)

### Community 60 - "clinical_design.md"
Cohesion: 0.29
Nodes (6): 1. Paleta de Colores y Tokens Clinicos Oficiales, 2. Directrices de Interfaz Movil (`app-mobile`), 3. Directrices Wear OS (`app-wear`), 4. Persistencia y Telemetria Historica (`core:data` & `core:model`), 5. Invariantes de Telemetria y Formateo, Superficies OLED

### Community 61 - "AlarmConfigSection.kt"
Cohesion: 0.29
Nodes (11): AlarmCard(), AlarmConfigSection(), AlarmCreationDialog(), AlarmSubsection(), ClinicalRangeVisualCard(), Color, ImageVector, Modifier (+3 more)

### Community 62 - "bug_report.md"
Cohesion: 0.29
Nodes (6): Comportamiento Esperado, Contexto Adicional, Descripcion del Problema, Informacion del Dispositivo, Modulo Afectado, Pasos para Reproducir

### Community 63 - "feature_request.md"
Cohesion: 0.29
Nodes (6): Alternativas Consideradas, Conformidad Regulatoria (MDDS), Contexto Adicional, Descripcion de la Funcionalidad, Justificacion y Caso de Uso, Modulo Objetivo

### Community 64 - "Codigo de Conducta del Contribuyente"
Cohesion: 0.33
Nodes (5): Atribucion, Codigo de Conducta del Contribuyente, Nuestro Compromiso, Nuestros Estandares, Responsabilidades de Aplicacion

### Community 65 - "LegalNoticeType"
Cohesion: 0.28
Nodes (8): LegalNoticeDialog(), LegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS, LegalSectionBox()

### Community 67 - "GlucoseAlarmWorker.kt"
Cohesion: 0.36
Nodes (4): GlucoseAlarmWorker, Context, CoroutineWorker, Result

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

### Community 76 - "DashboardTimeframe"
Cohesion: 0.29
Nodes (6): DashboardTimeframe, H1, H12, H2, H24, H6

### Community 77 - "MobileWearableMessageListenerService.kt"
Cohesion: 0.60
Nodes (3): MessageEvent, WearableListenerService, MobileWearableMessageListenerService

### Community 78 - "WearAuthMessageListenerService.kt"
Cohesion: 0.60
Nodes (3): MessageEvent, WearableListenerService, WearAuthMessageListenerService

### Community 79 - "WearGlucoseSyncWorker.kt"
Cohesion: 0.50
Nodes (3): CoroutineWorker, Result, WearGlucoseSyncWorker

### Community 81 - "se"
Cohesion: 0.05
Nodes (6): as(), is(), os(), se, setFromCamera(), h()

### Community 85 - "ConnectionItem"
Cohesion: 0.21
Nodes (9): Error, StateFlow, ViewModel, Loading, NeedsLogin, Success, WearDashboardUiState, WearDashboardViewModel (+1 more)

### Community 86 - "MetricPeriod"
Cohesion: 0.40
Nodes (5): MetricPeriod, DAY, MONTH, THREE_MONTHS, WEEK

### Community 88 - "Ce"
Cohesion: 0.06
Nodes (5): Ce, gs, Wl, v(), ys

### Community 91 - "constructor"
Cohesion: 0.15
Nodes (25): br(), constructor(), d(), u(), i(), y(), t(), gi() (+17 more)

### Community 92 - "copy"
Cohesion: 0.09
Nodes (7): copy(), hs, qn, _s(), n(), updateMatrixWorld(), w()

### Community 93 - "Lc"
Cohesion: 0.08
Nodes (4): bc, getInput(), getOutput(), Lc

### Community 94 - "gt"
Cohesion: 0.13
Nodes (27): ds(), F(), G(), k(), V(), s(), F(), G() (+19 more)

### Community 95 - ".constructor"
Cohesion: 0.06
Nodes (4): Aa, ec, jn(), sc

### Community 96 - "update"
Cohesion: 0.09
Nodes (22): ci, clone(), S(), fi(), A(), g(), ni, oi() (+14 more)

### Community 97 - "jc"
Cohesion: 0.07
Nodes (4): bind(), bindSkeletons(), getValue(), jc

### Community 98 - "ja"
Cohesion: 0.09
Nodes (18): ao(), co(), eo(), ho(), io(), ja(), ka(), lo() (+10 more)

### Community 99 - "ws"
Cohesion: 0.10
Nodes (17): C(), mi(), f(), m(), y(), setValue(), uh(), ws() (+9 more)

### Community 100 - "OpenGlucoRepository"
Cohesion: 0.09
Nodes (18): Bundle, ComponentActivity, MainActivity, MobileAppNavigation(), Modifier, MobileLoginScreen(), LibreMobileTheme(), OkHttpClient (+10 more)

### Community 105 - "Wn"
Cohesion: 0.11
Nodes (23): _allocateTargets(), _applyPMREM(), _blur(), _cleanup(), compileCubemapShader(), compileEquirectangularShader(), _compileMaterial(), fromCubemap() (+15 more)

### Community 109 - "Kn"
Cohesion: 0.13
Nodes (3): Jl, Kn, vl()

### Community 110 - "parseObject"
Cohesion: 0.10
Nodes (7): fa, $l, parseObject(), Rs, ta, tc, Vs

### Community 111 - "InteractiveMedical3DScene"
Cohesion: 0.16
Nodes (8): GLUCOSE_STATES, InteractiveMedical3DScene, renderTelemetryGraph(), setRangeState(), setTheme(), telemetry24h, toggleTheme(), updateGraphPoint()

### Community 112 - ".toJSON"
Cohesion: 0.12
Nodes (4): ac, r(), go, ts()

### Community 116 - "ei"
Cohesion: 0.15
Nodes (10): dispose(), ei, s(), gn(), a(), o(), G(), U() (+2 more)

### Community 118 - "ct"
Cohesion: 0.15
Nodes (4): ct(), es(), ns(), Xe()

### Community 121 - ".fromJSON"
Cohesion: 0.18
Nodes (3): n(), parseShapes(), zl

### Community 122 - "ReportTimeBlock"
Cohesion: 0.15
Nodes (11): ReportTimeBlock, AFTERNOON, EVENING, MORNING, NIGHT, TirCategory, HIGH, IN_RANGE (+3 more)

### Community 126 - "getClinicalStatusColor"
Cohesion: 0.33
Nodes (6): Modifier, WearGlucoseGauge(), getClinicalStatusColor(), getGlucoseStatusColor(), Color, WearSettingsAndDashboardContractTest

### Community 127 - "wo"
Cohesion: 0.18
Nodes (3): jo, wo, xo

### Community 129 - "mo"
Cohesion: 0.24
Nodes (4): fo(), mo(), l(), po

### Community 130 - "GlucoseComplicationService.kt"
Cohesion: 0.33
Nodes (6): GlucoseComplicationService, ComplicationData, ComplicationDataSourceService, ComplicationRequest, ComplicationRequestListener, ComplicationType

### Community 135 - "GlucoseWidgetUpdater.kt"
Cohesion: 0.57
Nodes (3): GlucoseWidgetUpdater, Context, RemoteViews

### Community 139 - "Jr"
Cohesion: 0.25
Nodes (6): Jr(), kr(), nr(), qr(), vr(), zr()

### Community 142 - "BootReceiver.kt"
Cohesion: 0.53
Nodes (4): BootReceiver, BroadcastReceiver, Context, Intent

### Community 148 - "OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema"
Cohesion: 0.40
Nodes (4): Archivos, Novedades Principales, OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema, Visualización

### Community 150 - "$c"
Cohesion: 0.67
Nodes (4): $c(), intersectObject(), intersectObjects(), Kc()

## Knowledge Gaps
- **211 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+206 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **58 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lt` connect `Lt` to `three.min.js`, `.parse`, `.subVectors`, `sn`, `.constructor`, `en`, `.multiplyScalar`, `se`, `At`, `Ce`, `constructor`, `copy`, `gt`, `.constructor`, `update`, `ws`, `ml`, `.distanceTo`, `eh`, `jt`, `pt`, `Ne`, `.fromArray`, `je`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **Why does `vt` connect `vt` to `three.min.js`, `mo`, `kl`, `bt`, `.parse`, `sn`, `Lt`, `.constructor`, `en`, `.multiplyScalar`, `tn`, `se`, `constructor`, `.constructor`, `update`, `ja`, `ws`, `ml`, `.distanceTo`, `Wn`, `eh`, `jt`, `pt`, `parseObject`, `bl`, `.fromArray`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **Why does `tn` connect `tn` to `three.min.js`, `mo`, `mc`, `.parse`, `sn`, `ms`, `ss`, `e`, `en`, `.multiplyScalar`, `se`, `constructor`, `.constructor`, `update`, `ja`, `ws`, `xc`, `Wn`, `parseObject`, `bl`, `ct`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **Are the 16 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testConnectionItem_effectiveMeasurement()` and `.testDisplayTime_nullOrInvalidFallback()`) actually correct?**
  _`GlucoseMeasurement` has 16 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _211 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `three.min.js` be split into smaller, more focused modules?**
  _Cohesion score 0.025490196078431372 - nodes in this community are weakly interconnected._
- **Should `OpenGlucoRepository.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06453634085213032 - nodes in this community are weakly interconnected._