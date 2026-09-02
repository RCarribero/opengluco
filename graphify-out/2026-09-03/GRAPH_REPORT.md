# Graph Report - librelinkup-ecosystem-master  (2026-09-03)

## Corpus Check
- 141 files · ~112,602 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2719 nodes · 5758 edges · 163 communities (112 shown, 51 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 297 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `27cbd9b4`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- three.min.js
- OpenGlucoInterceptor
- AlarmRepository
- UserPreferencesRepository
- QrPairingPayload
- WearDashboardScreen
- ClinicalReportsCalculator.kt
- GlucoseMonitorForegroundService
- MobileDashboardScreen.kt
- wear/MainActivity.kt
- GlucoseMeasurement
- MobileAlarmNotificationHelper
- WearBluetoothRfcommService
- At
- OpenGluco Ecosystem
- E2ETier1FeatureCoverageTest
- SensorInfo
- EmpiricalStressChallengeTest
- WearSettingsScreen
- .renderSparkline
- MobileLegalComplianceTest
- ca
- sn
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
- ia
- ol
- AppUpdateRepository
- EmpiricalStressChallengeTest.kt
- .applyMatrix4
- 2. Test Tiers & Methodology
- GlucoseTileService.kt
- Acceptance Criteria
- 1. Reglas Innegociables del Proyecto
- Acceptance Criteria
- AlarmCooldown
- PROJECT.md
- Reglas de Proyecto: OpenGluco Ecosystem
- BRIEFING.md
- tn
- GlucoseUnit
- ConfigurationDiagnosticsDialog
- Procedimiento Paso a Paso:
- clinical_design.md
- .login
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
- .parse
- Aa
- .constructor
- PULL_REQUEST_TEMPLATE.md
- TEST_READY.md
- Politica de Seguridad: OpenGluco Ecosystem
- no_emojis.md
- PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)
- rules/graphify.md
- workflows/graphify.md
- DetailModalType
- WearAuthMessageListenerService.kt
- eh
- re
- se
- GlucoseDashboardCarScreen
- ._onChangeCallback
- Ce
- vt
- St
- .invert
- Lc
- copy
- en
- jc
- ja
- ws
- UserSettings
- ml
- WearDashboardViewModel
- yc
- xc
- Wn
- pt
- jt
- OpenGlucoRepository
- Kn
- InteractiveMedical3DScene
- Ne
- yt
- bl
- pi
- yo
- ct
- nl
- nc
- zl
- ReportTimeBlock
- OpenGlucoModels.kt
- .toJSON
- je
- wo
- HealthDataExporter
- GlucoseComplicationService.kt
- constructor
- update
- QrScannerScreen.kt
- GlucoseWidgetUpdater.kt
- ge
- OpenGlucoApiServiceContractTest
- parseObject
- Hi
- QrAuthHelper
- [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido
- BootReceiver.kt
- .fromJSON
- .slerpFlat
- .setFromMatrixPosition
- OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema
- us
- Et
- Zc
- ClinicalErrorType
- [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion
- WearQrLoginScreen.kt
- [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer
- ClinicalReportsCalculatorTest
- [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis
- ResponsiveLayout.kt
- QrAuthHelperTest
- write_adrs.js
- .toArray
- qc
- WearBluetoothSecurityTest
- rc
- uc
- KeystoreCryptoHelperTest
- bt
- bind
- .isEmpty

## God Nodes (most connected - your core abstractions)
1. `Lt` - 135 edges
2. `copy()` - 128 edges
3. `GlucoseMeasurement` - 100 edges
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

## Communities (163 total, 51 thin omitted)

### Community 0 - "three.min.js"
Cohesion: 0.02
Nodes (41): an, Ba, bi(), bs, $c(), cn, dn, fn (+33 more)

### Community 1 - "OpenGlucoInterceptor"
Cohesion: 0.20
Nodes (5): Response, OpenGlucoInterceptor, MockWebServer, OpenGlucoInterceptorTest, Interceptor

### Community 2 - "AlarmRepository"
Cohesion: 0.10
Nodes (11): Context, MobileAlarmSyncHelper, MessageEvent, WearableListenerService, MobileWearableMessageListenerService, CoroutineWorker, Result, WearGlucoseSyncWorker (+3 more)

### Community 3 - "UserPreferencesRepository"
Cohesion: 0.11
Nodes (3): Flow, PreferencesKeys, UserPreferencesRepository

### Community 4 - "QrPairingPayload"
Cohesion: 0.19
Nodes (5): Context, MobilePairingHelper, QrEncryptedPayload, QrPairingPayload, QrAuthModelsTest

### Community 5 - "WearDashboardScreen"
Cohesion: 0.08
Nodes (31): DualFloatingOrbs(), Modifier, Modifier, PatientSelectorChip(), Modifier, WearGlucoseGauge(), DetailModalType, GLUCOSE_STATS (+23 more)

### Community 6 - "ClinicalReportsCalculator.kt"
Cohesion: 0.18
Nodes (13): ClinicalReportsCalculator, AverageGlucoseReport, DailyGraphDaySummary, DailyGraphReport, DailyPatternsReport, EstimatedA1cReport, HourlyPercentile, LowGlucoseEvent (+5 more)

### Community 7 - "GlucoseMonitorForegroundService"
Cohesion: 0.24
Nodes (6): GlucoseMonitorForegroundService, Context, IBinder, Intent, Service, PowerManager

### Community 8 - "MobileDashboardScreen.kt"
Cohesion: 0.08
Nodes (41): androidx, LegalNoticeDialog(), LegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS (+33 more)

### Community 9 - "wear/MainActivity.kt"
Cohesion: 0.14
Nodes (15): Bundle, ComponentActivity, MainActivity, WearAppNavigation(), Modifier, WearLoginScreen(), Error, Idle (+7 more)

### Community 10 - "GlucoseMeasurement"
Cohesion: 0.08
Nodes (7): Modifier, WearSparklineChart(), E2ETier3CrossFeatureCombinationsTest, HealthDataExporterTest, GlucoseMeasurement, ClinicalModelsTest, ModelSanityTest

### Community 11 - "MobileAlarmNotificationHelper"
Cohesion: 0.16
Nodes (9): android, com, Context, MobileAlarmNotificationHelper, GlucoseAlarmWorker, Context, CoroutineWorker, Result (+1 more)

### Community 12 - "WearBluetoothRfcommService"
Cohesion: 0.18
Nodes (8): Context, IBinder, Intent, Service, WearBluetoothRfcommService, BluetoothServerSocket, BluetoothSocket, Notification

### Community 14 - "OpenGluco Ecosystem"
Cohesion: 0.05
Nodes (37): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+29 more)

### Community 16 - "SensorInfo"
Cohesion: 0.11
Nodes (8): ClinicalSparklineWithSensor(), Modifier, Modifier, WearSensorChip(), com, E2ETier4RealWorldScenariosTest, SensorExpirationAlert, SensorInfo

### Community 18 - "WearSettingsScreen"
Cohesion: 0.80
Nodes (5): CompactActionRow(), CompactSettingsRow(), Color, Modifier, WearSettingsScreen()

### Community 19 - ".renderSparkline"
Cohesion: 0.27
Nodes (4): Bitmap, WidgetChartRenderer, CgmCurveSmoother, CubicBezierSegment

### Community 21 - "ca"
Cohesion: 0.25
Nodes (4): ca, fh, hh, uh()

### Community 22 - "sn"
Cohesion: 0.05
Nodes (4): Ga, ls(), sn, Yh()

### Community 23 - "GlucoseChartWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseChartWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 24 - "GlucoseCompactWidgetProvider.kt"
Cohesion: 0.39
Nodes (5): GlucoseCompactWidgetProvider, AppWidgetManager, AppWidgetProvider, Context, IntArray

### Community 25 - "Lt"
Cohesion: 0.04
Nodes (8): S(), s(), A(), ht(), Lt, setDirection(), setFromCamera(), wh()

### Community 27 - "e"
Cohesion: 0.15
Nodes (13): br(), cl, i(), er(), hl, ii, il(), load() (+5 more)

### Community 28 - "GlucoseAlarm"
Cohesion: 0.10
Nodes (14): Context, WearAlarmNotificationHelper, AlarmEvaluator, AlarmEvaluatorTest, AlarmSerializationSyncTest, AlarmEvaluationResult, AlarmSeverity, ALERT (+6 more)

### Community 29 - "AlarmDismissReceiver.kt"
Cohesion: 0.53
Nodes (4): AlarmDismissReceiver, BroadcastReceiver, Context, Intent

### Community 31 - "Sistema de Diseno: OpenGluco (Minimalista Clinico)"
Cohesion: 0.14
Nodes (13): 1. Filosofia y Estilo Visual, 2. Paleta de Colores Oficial, 3. Jerarquia y Distribucion Espacial en Wear OS, 4. Interactividad y Feedback Haptico, 5. Mapeo de Codigo Jetpack Compose, 6. Mencion a Abbott Laboratories y Marcas Registradas, A. Esferas Flotantes Superiores (`DualFloatingOrbs`), B. Grafica Sparkline + Badge de Sensor (+5 more)

### Community 44 - "AppUpdateRepository"
Cohesion: 0.17
Nodes (7): AppUpdateInstaller, Context, Result, AppUpdateRepository, OkHttpClient, Result, AppUpdateRepositoryTest

### Community 45 - "EmpiricalStressChallengeTest.kt"
Cohesion: 0.20
Nodes (5): KeystoreCryptoHelper, QrDeviceType, ANDROID_AUTO, WEAR_OS, SecretKey

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

### Community 52 - "AlarmCooldown"
Cohesion: 0.33
Nodes (6): AlarmCooldown, MIN_10, MIN_15, MIN_30, MIN_5, NONE

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
Cohesion: 0.04
Nodes (11): _a, bo, Do, dt(), $e(), ms(), Qe(), ss() (+3 more)

### Community 57 - "GlucoseUnit"
Cohesion: 0.14
Nodes (24): Modifier, MobileDualFloatingOrbs(), Modifier, PatientHeaderChip(), PatientSelectorModal(), AlarmCard(), AlarmConfigSection(), AlarmCreationDialog() (+16 more)

### Community 58 - "ConfigurationDiagnosticsDialog"
Cohesion: 0.41
Nodes (6): ConfigurationDiagnosticsDialog(), DiagnosticItemCard(), Context, ImageVector, SystemDiagnosticsHelper, SystemDiagnosticsState

### Community 59 - "Procedimiento Paso a Paso:"
Cohesion: 0.18
Nodes (10): 1. Confirmación de Versión y Changelog, 2. Sincronización de Versiones en Gradle, 3. Validación y Pruebas Unitarias, 4. Compilación Local de APKs, 5. Empaquetado y Organización de Artefactos, 6. Versionado en Git, 7. Publicación de la Release, Principios Obligatorios: (+2 more)

### Community 60 - "clinical_design.md"
Cohesion: 0.29
Nodes (6): 1. Paleta de Colores y Tokens Clinicos Oficiales, 2. Directrices de Interfaz Movil (`app-mobile`), 3. Directrices Wear OS (`app-wear`), 4. Persistencia y Telemetria Historica (`core:data` & `core:model`), 5. Invariantes de Telemetria y Formateo, Superficies OLED

### Community 61 - ".login"
Cohesion: 0.67
Nodes (4): AuthExpiredException, Result, NetworkException, Exception

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

### Community 78 - "WearAuthMessageListenerService.kt"
Cohesion: 0.60
Nodes (3): MessageEvent, WearableListenerService, WearAuthMessageListenerService

### Community 80 - "re"
Cohesion: 0.06
Nodes (6): fo(), mo(), l(), po, re, rh

### Community 81 - "se"
Cohesion: 0.08
Nodes (4): as(), is(), os(), se

### Community 85 - "GlucoseDashboardCarScreen"
Cohesion: 0.07
Nodes (22): AutoTtsAlertManager, GlucoseCarAppService, Session, GlucoseCarSession, Intent, Screen, Session, GlucoseDashboardCarScreen (+14 more)

### Community 88 - "Ce"
Cohesion: 0.08
Nodes (3): Ce, Ql, Wl

### Community 92 - ".invert"
Cohesion: 0.10
Nodes (4): qn, _s(), n(), updateMatrixWorld()

### Community 93 - "Lc"
Cohesion: 0.08
Nodes (4): bc, getInput(), getOutput(), Lc

### Community 95 - "copy"
Cohesion: 0.10
Nodes (4): Ah, clone(), copy(), Tt

### Community 96 - "en"
Cohesion: 0.09
Nodes (7): Da, v(), en, v(), mn, i(), yn()

### Community 98 - "ja"
Cohesion: 0.09
Nodes (18): ao(), co(), eo(), ho(), io(), ja(), ka(), lo() (+10 more)

### Community 99 - "ws"
Cohesion: 0.06
Nodes (45): dispose(), ds(), G(), k(), V(), z(), C(), F() (+37 more)

### Community 100 - "UserSettings"
Cohesion: 0.29
Nodes (8): Bundle, ComponentActivity, MainActivity, MobileAppNavigation(), Modifier, MobileLoginScreen(), LibreMobileTheme(), UserSettings

### Community 102 - "WearDashboardViewModel"
Cohesion: 0.23
Nodes (8): Error, StateFlow, ViewModel, Loading, NeedsLogin, Success, WearDashboardUiState, WearDashboardViewModel

### Community 105 - "Wn"
Cohesion: 0.10
Nodes (24): _allocateTargets(), _applyPMREM(), _blur(), _cleanup(), compileCubemapShader(), compileEquirectangularShader(), _compileMaterial(), fromCubemap() (+16 more)

### Community 108 - "OpenGlucoRepository"
Cohesion: 0.12
Nodes (10): OkHttpClient, OpenGlucoRegion, AP, DE, EU, FR, JP, US (+2 more)

### Community 109 - "Kn"
Cohesion: 0.11
Nodes (3): Jl, Kn, vl()

### Community 111 - "InteractiveMedical3DScene"
Cohesion: 0.16
Nodes (8): GLUCOSE_STATES, InteractiveMedical3DScene, renderTelemetryGraph(), setRangeState(), setTheme(), telemetry24h, toggleTheme(), updateGraphPoint()

### Community 115 - "bl"
Cohesion: 0.10
Nodes (3): bl, dc, fl

### Community 116 - "pi"
Cohesion: 0.15
Nodes (8): ci, ni, pi(), p(), Si(), ti, Xn(), yi()

### Community 117 - "yo"
Cohesion: 0.14
Nodes (3): jo, qo, yo

### Community 118 - "ct"
Cohesion: 0.15
Nodes (4): ct(), es(), ns(), Xe()

### Community 122 - "ReportTimeBlock"
Cohesion: 0.15
Nodes (11): ReportTimeBlock, AFTERNOON, EVENING, MORNING, NIGHT, TirCategory, HIGH, IN_RANGE (+3 more)

### Community 123 - "OpenGlucoModels.kt"
Cohesion: 0.27
Nodes (9): AuthTicket, BaseResponse, GraphData, LoginData, LoginRequest, ResponseError, UserProfile, Response (+1 more)

### Community 124 - ".toJSON"
Cohesion: 0.10
Nodes (5): ac, r(), go, sc, ts()

### Community 125 - "je"
Cohesion: 0.12
Nodes (3): ec, je, jn()

### Community 128 - "HealthDataExporter"
Cohesion: 0.18
Nodes (3): HealthDataExporter, Context, E2ETier2BoundaryCornerCasesTest

### Community 130 - "GlucoseComplicationService.kt"
Cohesion: 0.33
Nodes (6): GlucoseComplicationService, ComplicationData, ComplicationDataSourceService, ComplicationRequest, ComplicationRequestListener, ComplicationType

### Community 131 - "constructor"
Cohesion: 0.16
Nodes (27): constructor(), d(), u(), y(), t(), gi(), l(), o() (+19 more)

### Community 132 - "update"
Cohesion: 0.08
Nodes (16): ei, s(), fi(), gn(), gs, o(), oi(), V() (+8 more)

### Community 133 - "QrScannerScreen.kt"
Cohesion: 0.33
Nodes (12): CameraPreview(), decodeQrFromImage(), android, ByteArray, Modifier, QrScannerScreen(), rotateYUV420Degree180(), rotateYUV420Degree270() (+4 more)

### Community 135 - "GlucoseWidgetUpdater.kt"
Cohesion: 0.57
Nodes (3): GlucoseWidgetUpdater, Context, RemoteViews

### Community 138 - "parseObject"
Cohesion: 0.10
Nodes (7): fa, $l, parseObject(), Rs, ta, tc, Vs

### Community 139 - "Hi"
Cohesion: 0.15
Nodes (11): Hi(), ji(), Jr(), ki(), kr(), nr(), qi(), qr() (+3 more)

### Community 141 - "[ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido"
Cohesion: 0.20
Nodes (9): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), 6. Reglas de Validacion y Cumplimiento (Enforcement), [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido, Consecuencias Negativas / Riesgos Asumidos: (+1 more)

### Community 142 - "BootReceiver.kt"
Cohesion: 0.53
Nodes (4): BootReceiver, BroadcastReceiver, Context, Intent

### Community 143 - ".fromJSON"
Cohesion: 0.07
Nodes (5): Al, el, parseShapes(), pl, Rl

### Community 148 - "OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema"
Cohesion: 0.40
Nodes (4): Archivos, Novedades Principales, OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema, Visualización

### Community 154 - "us"
Cohesion: 0.16
Nodes (5): cs, hs, kl, us(), w()

### Community 158 - "ClinicalErrorType"
Cohesion: 0.29
Nodes (7): AuthExpired, ClinicalErrorType, Generic, NetworkError, None, NoPatients, NoSensor

### Community 159 - "[ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion"
Cohesion: 0.29
Nodes (6): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion

### Community 160 - "WearQrLoginScreen.kt"
Cohesion: 0.24
Nodes (7): ClinicalProgressSpinner(), Color, Modifier, WearQrLoginScreen(), Bitmap, QrSessionExchange, ServerSocket

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

### Community 187 - "bind"
Cohesion: 0.29
Nodes (5): bind(), bindSkeletons(), getValue(), parseSkeletons(), setValue()

## Knowledge Gaps
- **237 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+232 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lt` connect `Lt` to `three.min.js`, `constructor`, `update`, `.setFromMatrixPosition`, `sn`, `.length`, `us`, `.applyMatrix4`, `.copy`, `.isEmpty`, `.parse`, `.constructor`, `re`, `se`, `._onChangeCallback`, `Ce`, `.constructor`, `.invert`, `en`, `ws`, `ml`, `pt`, `jt`, `Ne`, `je`?**
  _High betweenness centrality (0.051) - this node is a cross-community bridge._
- **Why does `vt` connect `vt` to `three.min.js`, `constructor`, `parseObject`, `sn`, `.length`, `Lt`, `us`, `.toArray`, `bt`, `tn`, `.isEmpty`, `.parse`, `.constructor`, `eh`, `re`, `se`, `Ce`, `en`, `ja`, `ws`, `ml`, `Wn`, `pt`, `jt`, `bl`, `je`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `GlucoseMeasurement` connect `GlucoseMeasurement` to `HealthDataExporter`, `UserPreferencesRepository`, `WearDashboardScreen`, `ClinicalReportsCalculator.kt`, `GlucoseWidgetUpdater.kt`, `MobileDashboardScreen.kt`, `GlucoseMonitorForegroundService`, `WearBluetoothRfcommService`, `QrAuthHelper`, `E2ETier1FeatureCoverageTest`, `SensorInfo`, `EmpiricalStressChallengeTest`, `.renderSparkline`, `ClinicalReportsCalculatorTest`, `EmpiricalStressChallengeTest.kt`, `GlucoseUnit`, `GlucoseDashboardCarScreen`, `WearDashboardViewModel`, `ReportTimeBlock`, `OpenGlucoModels.kt`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Are the 16 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testConnectionItem_effectiveMeasurement()` and `.testDisplayTime_nullOrInvalidFallback()`) actually correct?**
  _`GlucoseMeasurement` has 16 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _237 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `three.min.js` be split into smaller, more focused modules?**
  _Cohesion score 0.02284434490481523 - nodes in this community are weakly interconnected._
- **Should `AlarmRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.09915966386554621 - nodes in this community are weakly interconnected._