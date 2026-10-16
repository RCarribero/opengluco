# Graph Report - librelinkup-ecosystem-master  (2026-10-16)

## Corpus Check
- 154 files · ~127,019 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 37 file(s) not represented in the graph (top: .xml 26, .bat 4, (none) 3)

## Summary
- 1888 nodes · 5051 edges · 131 communities (69 shown, 62 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 95 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b3009841`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OpenGlucoApiServiceContractTest.kt
- AppUpdateRepository
- MobileAlarmNotificationHelper
- UserPreferencesRepository
- wear/MainActivity.kt
- ConfigurationDiagnosticsDialog.kt
- WearBluetoothRfcommService.kt
- MobileDashboardScreen
- ClinicalReportsCalculatorTest
- GlucoseMeasurement
- AlarmRepository
- GlucoseMonitorForegroundService.kt
- Dossier de Cumplimiento Normativo y Respaldo Legal: OpenGluco Ecosystem
- OpenGluco Ecosystem
- WearDashboardScreen.kt
- .generateCsv
- E2ETier2BoundaryCornerCasesTest.kt
- MobileGlucoseChart
- MobileLegalComplianceTest
- AlarmSeverity
- ClinicalReportsCalculator
- WearSparklineChart.kt
- QrAuthHelper
- SensorDurationDialog
- Test
- ConnectionItem
- 2.3. Perfil, Consentimientos y Reglas Regulatorias
- GlucoseDashboardCarScreen.kt
- .parseEpoch
- dispatchers
- AlarmConfigSection.kt
- DetailModalType
- LocalGlucoseDatabase
- mobile/ui/theme/Color.kt
- WearSettingsAndDashboardContractTest.kt
- LegalNoticeType
- OpenGlucoProvider.kt
- SensorLifecycleState
- GlucoseTileService.kt
- Acceptance Criteria
- 1. Reglas Innegociables del Proyecto
- Acceptance Criteria
- .decryptAesGcm
- .purgeAllLocalData
- 2. Test Tiers & Methodology
- BRIEFING.md
- ReportsHubScreen
- E2ETier1FeatureCoverageTest
- AlarmSoundType
- Procedimiento Paso a Paso:
- .calculateAvailableDays
- .subsampleOneOfThree
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
- ConfigurationDiagnosticsDialog
- [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido
- QrPairingPayload
- PULL_REQUEST_TEMPLATE.md
- TEST_READY.md
- WearStatDetailModal
- no_emojis.md
- PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)
- rules/graphify.md
- workflows/graphify.md
- LocalGlucoseDatabase.kt
- ClinicalErrorType
- WearLoginViewModel.kt
- GlucoseAlarm
- WearSensorChip.kt
- QrScannerScreen
- ClinicalReportsCalculator.kt
- GlucoseUnit
- GlucoseAlarmWorker.kt
- MetricPeriod
- Sistema de Diseno: OpenGluco (Minimalista Clinico)
- AlarmSerializationSyncTest
- .decrypt
- 4.2 Invariantes de Seguridad Implementadas en OpenGluco:
- OpenGlucoRepository
- log
- DashboardTimeframe
- GlucoseWidgetUpdater.kt
- WearBluetoothSecurityTest
- HealthDataExporter.kt
- file
- .testCombination_qrSync_encryption_csvExport
- EmpiricalStressChallengeTest.kt
- QrAuthHelper.kt
- WearLegalNoticeType
- MobileAlarmNotificationHelper.kt
- first
- LocalGlucoseDatabaseTest
- app.js
- AlarmSerializationSyncTest.kt
- DataQualityStatusCard
- WearClinicalDesignAndSafetyTest
- Test
- E2ETier4RealWorldScenariosTest.kt
- MobileAppNavigation
- Caracteristicas Principales
- MobileDualFloatingOrbs
- DualFloatingOrbs
- .notifySensorExpiration
- WearSettingsScreen
- wear/ui/theme/Theme.kt
- ModelSanityTest.kt
- OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema
- [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion
- [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis
- ResponsiveLayout.kt
- write_adrs.js

## God Nodes (most connected - your core abstractions)
1. `GlucoseMeasurement` - 147 edges
2. `UserPreferencesRepository` - 97 edges
3. `GlucoseAlarm` - 58 edges
4. `GlucoseUnit` - 49 edges
5. `SensorInfo` - 48 edges
6. `AlarmRepository` - 44 edges
7. `ConnectionItem` - 40 edges
8. `OpenGlucoRepository` - 39 edges
9. `ClinicalModelsTest` - 37 edges
10. `LocalGlucoseDatabase` - 34 edges

## Surprising Connections (you probably didn't know these)
- `1. App Movil (`app-mobile`)` --references--> `MobileDualFloatingOrbs()`  [INFERRED]
  README.md → app-mobile/src/main/java/com/example/opengluco/mobile/ui/dashboard/components/MobileDualFloatingOrbs.kt
- `Data Portability CSV Contract (`HealthDataExporter`)` --references--> `HealthDataExporter`  [INFERRED]
  PROJECT.md → core/data/src/main/java/com/example/opengluco/core/data/HealthDataExporter.kt
- `Cryptographic Contract (`KeystoreCryptoHelper`)` --references--> `KeystoreCryptoHelper`  [INFERRED]
  PROJECT.md → core/data/src/main/java/com/example/opengluco/core/data/KeystoreCryptoHelper.kt
- `4. Persistencia y Datos (`core:data`)` --references--> `UserPreferencesRepository`  [INFERRED]
  AGENTS.md → core/data/src/main/java/com/example/opengluco/core/data/UserPreferencesRepository.kt
- `4. Persistencia y Telemetria Historica (`core:data` & `core:model`)` --references--> `UserPreferencesRepository`  [INFERRED]
  .agents/rules/clinical_design.md → core/data/src/main/java/com/example/opengluco/core/data/UserPreferencesRepository.kt

## Import Cycles
- None detected.

## Communities (131 total, 62 thin omitted)

### Community 0 - "OpenGlucoApiServiceContractTest.kt"
Cohesion: 0.08
Nodes (3): OpenGlucoInterceptor, OpenGlucoApiServiceContractTest, OpenGlucoInterceptorTest

### Community 1 - "AppUpdateRepository"
Cohesion: 0.10
Nodes (5): GlucoseCarAppService, UpdateAvailableDialog(), AppUpdateInstaller, AppUpdateRepository, AppReleaseInfo

### Community 5 - "wear/MainActivity.kt"
Cohesion: 0.16
Nodes (3): MainActivity, WearAppNavigation(), WearLoginScreen()

### Community 7 - "WearBluetoothRfcommService.kt"
Cohesion: 0.12
Nodes (5): WearBluetoothRfcommService, 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Decision Elegida, [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer

### Community 8 - "MobileDashboardScreen"
Cohesion: 0.26
Nodes (11): DailyStatItem(), DashboardClinicalErrorBanner(), DashboardSensorCard(), DashboardStatsCard(), MobileDashboardScreen(), MobileSettingsScreen(), SettingsDivider(), SettingsGroupCard() (+3 more)

### Community 10 - "GlucoseMeasurement"
Cohesion: 0.10
Nodes (3): GlucoseMeasurement, SensorInfo, ClinicalModelsTest

### Community 14 - "Dossier de Cumplimiento Normativo y Respaldo Legal: OpenGluco Ecosystem"
Cohesion: 0.13
Nodes (13): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+5 more)

### Community 15 - "OpenGluco Ecosystem"
Cohesion: 0.15
Nodes (13): 1. Exencion de Dispositivo Medico (MDR UE 2017/745 y FDA MDDS 21 CFR 880.6310), 2. Aviso de Marcas y No Afiliacion, 3. Dossier de Conformidad Legal y Respaldo Normativo, Comandos Principales, Descargos Legales y Regulatorios, Licencia, Modulos del Ecosistema, OpenGluco Ecosystem (+5 more)

### Community 16 - "WearDashboardScreen.kt"
Cohesion: 0.17
Nodes (3): ClinicalSparklineWithSensor(), object@L118, WearLegalTexts

### Community 19 - "MobileGlucoseChart"
Cohesion: 0.14
Nodes (17): DialogHeader(), EmptyNotesState(), eventColor(), eventIcon(), EventNoteCard(), GlucoseEventEditorDialog(), GlucoseEventListDialog(), GlucoseEventNotesMenuButton() (+9 more)

### Community 21 - "AlarmSeverity"
Cohesion: 0.15
Nodes (7): WearAlarmNotificationHelper, WearAuthMessageListenerService, WearGlucoseSyncWorker, AlarmSeverity, ALERT, INFORMATIVE, URGENT

### Community 24 - "QrAuthHelper"
Cohesion: 0.17
Nodes (3): ClinicalProgressSpinner(), WearQrLoginScreen(), QrAuthHelper

### Community 25 - "SensorDurationDialog"
Cohesion: 0.83
Nodes (3): DurationOption, DurationOptionRow(), SensorDurationDialog()

### Community 27 - "ConnectionItem"
Cohesion: 0.24
Nodes (11): ActiveSensorEntry, AuthTicket, BaseResponse, ConnectionItem, DeviceInfo, GraphData, LoginData, LoginRequest (+3 more)

### Community 28 - "2.3. Perfil, Consentimientos y Reglas Regulatorias"
Cohesion: 0.09
Nodes (21): 1. Topología de Servidores y Puertas de Enlace (Gateways), 2.1. Autenticación y Gestión de Sesión, 2.2. Subida de Datos de Glucosa y Telemetría del Sensor (`POST api/measurements`), 2.3. Perfil, Consentimientos y Reglas Regulatorias, 2. Endpoints del Gateway Clínico Principal (`NumeraWebApi` en `/lsl/`), 3. Endpoints del CDN de Distribución (`fsll.freestyleserver.com`), 4. Endpoints de Consulta y Lectura (Ecosistema LibreLinkUp `/llu/`), 5. Cuadro Resumen de Cabeceras Requeridas (+13 more)

### Community 29 - "GlucoseDashboardCarScreen.kt"
Cohesion: 0.06
Nodes (9): AutoMobileSyncHelper, AutoTtsAlertManager, GlucoseCarSession, GlucoseDashboardCarScreen, onDestroy(), onStart(), onStop(), PatientListCarScreen (+1 more)

### Community 30 - ".parseEpoch"
Cohesion: 0.18
Nodes (3): formatChartTime(), FastDateParser, FastDateParserTest

### Community 31 - "dispatchers"
Cohesion: 0.18
Nodes (6): Error, Loading, NeedsLogin, Success, WearDashboardUiState, WearDashboardViewModel

### Community 33 - "DetailModalType"
Cohesion: 0.25
Nodes (7): DetailModalType, GLUCOSE_STATS, NONE, SENSOR_INFO, TREND_INFO, MobileStatDetailModal(), StatRow()

### Community 35 - "mobile/ui/theme/Color.kt"
Cohesion: 0.17
Nodes (6): PatientHeaderChip(), PatientSelectorModal(), ClinicalColorScheme, getClinicalStatusColor(), getGlucoseStatusColor(), getGlucoseValueColor()

### Community 44 - "WearSettingsAndDashboardContractTest.kt"
Cohesion: 0.21
Nodes (4): WearGlucoseGauge(), getClinicalStatusColor(), getGlucoseStatusColor(), WearSettingsAndDashboardContractTest

### Community 45 - "LegalNoticeType"
Cohesion: 0.25
Nodes (8): LegalNoticeDialog(), LegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS, LegalSectionBox()

### Community 47 - "SensorLifecycleState"
Cohesion: 0.43
Nodes (5): Active, Expired, lifecycleState(), SensorLifecycleState, WarmingUp

### Community 48 - "GlucoseTileService.kt"
Cohesion: 0.05
Nodes (18): 1. Directrices de Interfaz y Tokens Clinicos, 2. App Movil (`app-mobile`), 3. App Reloj (`app-wear`), 4. Persistencia y Datos (`core:data`), 5. Invariantes de Telemetria y Formato, 6. Mencion a Abbott Laboratories y Blindaje Legal, 7. Arquitectura y Grafo de Conocimiento (Graphify), 8. Protocolo de Publicación y Actualizaciones OTA (Bajo Demanda) (+10 more)

### Community 49 - "Acceptance Criteria"
Cohesion: 0.20
Nodes (9): Acceptance Criteria, Initial Request — 2026-08-27T10:10:46Z, Paridad de Interfaces y Configuración Legal, Privacidad y Control de Datos, R1. Paridad de Configuración Legal y Avisos Normativos en Todas las Vistas, R2. Privacidad y Gestión de Datos de Salud (RGPD Art. 9, 17 y 20), R3. Invariantes de Interfaz y Sistema de Diseño Clínico, Requirements (+1 more)

### Community 50 - "1. Reglas Innegociables del Proyecto"
Cohesion: 0.20
Nodes (9): 1. Reglas Innegociables del Proyecto, 2. Flujo de Trabajo para Contribuciones, 3. Estructura de Modulos, 4. Convenciones de Codigo, A. Prohibicion Estricta de Emojis, B. Invariante de Seguridad Clinica (MDDS), C. Tokens de Diseno y Formato, D. Mencion a Abbott Laboratories y Marcas (+1 more)

### Community 51 - "Acceptance Criteria"
Cohesion: 0.20
Nodes (9): Acceptance Criteria, Initial Request — 2026-08-27T10:10:46Z, Paridad de Interfaces y Configuración Legal, Privacidad y Control de Datos, R1. Paridad de Configuración Legal y Avisos Normativos en Todas las Vistas, R2. Privacidad y Gestión de Datos de Salud (RGPD Art. 9, 17 y 20), R3. Invariantes de Interfaz y Sistema de Diseño Clínico, Requirements (+1 more)

### Community 53 - ".purgeAllLocalData"
Cohesion: 0.12
Nodes (15): Architecture, Code Layout, Cryptographic Contract (`KeystoreCryptoHelper`), Data Portability CSV Contract (`HealthDataExporter`), Feature Inventory, Interface Contracts, Legal & Regulatory String Constants, Milestones (+7 more)

### Community 54 - "2. Test Tiers & Methodology"
Cohesion: 0.17
Nodes (11): 1. Test Architecture Overview, 2. Test Tiers & Methodology, 3. Detailed Feature-to-Test Mapping Matrix, 4. Test Execution & Verification, Global Test Command, Module-Specific Unit Test Tasks, Tier 1: Feature Coverage (>=5 Test Cases per Feature across all 18 Features), Tier 2: Boundary & Corner Cases (>=5 Test Cases per Feature) (+3 more)

### Community 55 - "BRIEFING.md"
Cohesion: 0.25
Nodes (7): Artifact Index, Key Constraints, Mission, My Identity, Project Status, User Context, Victory Audit Status

### Community 56 - "ReportsHubScreen"
Cohesion: 0.29
Nodes (4): ClinicalExplanationBox(), MetricColumn(), ReportsHubScreen(), TirCategoryRow()

### Community 58 - "AlarmSoundType"
Cohesion: 0.09
Nodes (20): MobileAlarmSyncHelper, AlarmCooldown, MIN_1, MIN_10, MIN_15, MIN_2, MIN_3, MIN_30 (+12 more)

### Community 59 - "Procedimiento Paso a Paso:"
Cohesion: 0.18
Nodes (10): 1. Confirmación de Versión y Changelog, 2. Sincronización de Versiones en Gradle, 3. Validación y Pruebas Unitarias, 4. Compilación Local de APKs, 5. Empaquetado y Organización de Artefactos, 6. Versionado en Git, 7. Publicación de la Release, Principios Obligatorios: (+2 more)

### Community 61 - ".subsampleOneOfThree"
Cohesion: 0.13
Nodes (4): WidgetChartRenderer, CgmCurveSmoother, CubicBezierSegment, CgmCurveSmootherTest

### Community 62 - "bug_report.md"
Cohesion: 0.29
Nodes (6): Comportamiento Esperado, Contexto Adicional, Descripcion del Problema, Informacion del Dispositivo, Modulo Afectado, Pasos para Reproducir

### Community 63 - "feature_request.md"
Cohesion: 0.29
Nodes (6): Alternativas Consideradas, Conformidad Regulatoria (MDDS), Contexto Adicional, Descripcion de la Funcionalidad, Justificacion y Caso de Uso, Modulo Objetivo

### Community 64 - "Codigo de Conducta del Contribuyente"
Cohesion: 0.33
Nodes (5): Atribucion, Codigo de Conducta del Contribuyente, Nuestro Compromiso, Nuestros Estandares, Responsabilidades de Aplicacion

### Community 65 - "ConfigurationDiagnosticsDialog"
Cohesion: 0.31
Nodes (4): ConfigurationDiagnosticsDialog(), DiagnosticItemCard(), SystemDiagnosticsHelper, SystemDiagnosticsState

### Community 66 - "[ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido"
Cohesion: 0.20
Nodes (9): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), 6. Reglas de Validacion y Cumplimiento (Enforcement), [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido, Consecuencias Negativas / Riesgos Asumidos: (+1 more)

### Community 67 - "QrPairingPayload"
Cohesion: 0.32
Nodes (4): QrEncryptedPayload, QrPairingPayload, QrSessionExchange, QrAuthModelsTest

### Community 68 - "PULL_REQUEST_TEMPLATE.md"
Cohesion: 0.40
Nodes (4): Descripcion del Cambio, Lista de Verificacion de Calidad y Cumplimiento, Modulos Modificados, Tipo de Cambio

### Community 69 - "TEST_READY.md"
Cohesion: 0.40
Nodes (4): 1. Executive Summary, 2. Test Suite Breakdown by Module, 3. Tier Coverage Matrix, 4. How to Run the Tests

### Community 70 - "WearStatDetailModal"
Cohesion: 0.22
Nodes (7): DetailModalType, GLUCOSE_STATS, NONE, SENSOR_INFO, TREND_INFO, StatRow(), WearStatDetailModal()

### Community 76 - "ClinicalErrorType"
Cohesion: 0.22
Nodes (9): AuthExpired, ClinicalErrorType, Generic, NetworkError, None, NoPatients, NoSensor, SensorExpired (+1 more)

### Community 77 - "WearLoginViewModel.kt"
Cohesion: 0.19
Nodes (6): Error, Idle, Loading, Success, WearLoginUiState, WearLoginViewModel

### Community 78 - "GlucoseAlarm"
Cohesion: 0.25
Nodes (4): AlarmEvaluator, AlarmEvaluatorTest, AlarmEvaluationResult, GlucoseAlarm

### Community 80 - "QrScannerScreen"
Cohesion: 0.19
Nodes (7): CameraPreview(), decodeQrFromImage(), QrScannerScreen(), rotateYUV420Degree180(), rotateYUV420Degree270(), rotateYUV420Degree90(), transferSessionToDevice()

### Community 85 - "ClinicalReportsCalculator.kt"
Cohesion: 0.12
Nodes (18): AverageGlucoseReport, BlockMetric, DailyGraphDaySummary, DailyGraphReport, DailyPatternsReport, DataQualityReport, EstimatedA1cReport, HourlyPercentile (+10 more)

### Community 86 - "GlucoseUnit"
Cohesion: 0.18
Nodes (10): AlarmCard(), AlarmConfigSection(), AlarmCreationDialog(), AlarmSubsection(), ClinicalRangeVisualCard(), copyCustomAudioToInternalStorage(), getFileNameFromUri(), GlucoseUnit (+2 more)

### Community 88 - "MetricPeriod"
Cohesion: 0.40
Nodes (5): MetricPeriod, DAY, MONTH, THREE_MONTHS, WEEK

### Community 90 - "Sistema de Diseno: OpenGluco (Minimalista Clinico)"
Cohesion: 0.20
Nodes (9): 1. Filosofia y Estilo Visual, 2. Paleta de Colores Oficial, 4. Interactividad y Feedback Haptico, 5. Mapeo de Codigo Jetpack Compose, 6. Mencion a Abbott Laboratories y Marcas Registradas, Estados Clinicos de Glucosa, Fondos y Superficies OLED, Sistema de Diseno: OpenGluco (Minimalista Clinico) (+1 more)

### Community 93 - "4.2 Invariantes de Seguridad Implementadas en OpenGluco:"
Cohesion: 0.32
Nodes (4): AutoClinicalSafetyTest, 4.1 Análisis bajo la Guía MDCG 2019-11 (Regla 11 del Anexo VIII MDR), 4.2 Invariantes de Seguridad Implementadas en OpenGluco:, 4. Clasificación Regulatoria de Dispositivos Médicos (MDR UE 2017/745 y FDA MDDS)

### Community 94 - "OpenGlucoRepository"
Cohesion: 0.13
Nodes (10): AuthExpiredException, NetworkException, OpenGlucoRegion, AP, DE, EU, FR, JP (+2 more)

### Community 96 - "DashboardTimeframe"
Cohesion: 0.29
Nodes (6): DashboardTimeframe, H1, H12, H2, H24, H6

### Community 97 - "GlucoseWidgetUpdater.kt"
Cohesion: 0.13
Nodes (3): GlucoseChartWidgetProvider, GlucoseCompactWidgetProvider, GlucoseWidgetUpdater

### Community 101 - "HealthDataExporter.kt"
Cohesion: 0.13
Nodes (6): TirCategory, HIGH, IN_RANGE, LOW, VERY_HIGH, VERY_LOW

### Community 106 - "QrAuthHelper.kt"
Cohesion: 0.17
Nodes (3): QrDeviceType, ANDROID_AUTO, WEAR_OS

### Community 107 - "WearLegalNoticeType"
Cohesion: 0.22
Nodes (9): WearLegalNoticeDialog(), WearLegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS, WearLegalSectionBox() (+1 more)

### Community 108 - "MobileAlarmNotificationHelper.kt"
Cohesion: 0.11
Nodes (5): MainActivity, WearAlarmDismissReceiver, AlarmType, HIGH, LOW

### Community 109 - "first"
Cohesion: 0.13
Nodes (3): AlarmDismissReceiver, BootReceiver, MobileWearableMessageListenerService

### Community 111 - "app.js"
Cohesion: 0.17
Nodes (35): bindAutoEvents(), bindDashboardEvents(), bindLoginEvents(), bindQrEvents(), bindReportsEvents(), bindSettingsEvents(), bindWearEvents(), closeModal() (+27 more)

### Community 118 - "Caracteristicas Principales"
Cohesion: 0.50
Nodes (4): 1. App Movil (`app-mobile`), 2. App Reloj (`app-wear`), 3. App Coche (`app-auto`), Caracteristicas Principales

### Community 120 - "DualFloatingOrbs"
Cohesion: 0.15
Nodes (7): DualFloatingOrbs(), PatientSelectorChip(), WearDashboardScreen(), 3. Jerarquia y Distribucion Espacial en Wear OS, A. Esferas Flotantes Superiores (`DualFloatingOrbs`), B. Grafica Sparkline + Badge de Sensor, Especificaciones de Componentes:

### Community 124 - "WearSettingsScreen"
Cohesion: 0.67
Nodes (3): CompactActionRow(), CompactSettingsRow(), WearSettingsScreen()

### Community 148 - "OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema"
Cohesion: 0.40
Nodes (4): Archivos, Novedades Principales, OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema, Visualización

### Community 159 - "[ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion"
Cohesion: 0.29
Nodes (6): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion

### Community 168 - "[ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis"
Cohesion: 0.50
Nodes (3): 1. Contexto y Declaracion del Problema, 2. Reglas Deterministas Forzadas, [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis

### Community 169 - "ResponsiveLayout.kt"
Cohesion: 0.11
Nodes (11): rememberResponsiveDimensions(), ResponsiveDimensions, WindowHeightClass, COMPACT, EXPANDED, MEDIUM, WindowWidthClass, COMPACT (+3 more)

### Community 174 - "write_adrs.js"
Cohesion: 0.33
Nodes (3): docsDir, fs, path

## Knowledge Gaps
- **248 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+243 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 505 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **62 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GlucoseMeasurement` connect `GlucoseMeasurement` to `UserPreferencesRepository`, `ModelSanityTest.kt`, `ConfigurationDiagnosticsDialog.kt`, `WearBluetoothRfcommService.kt`, `MobileDashboardScreen`, `ClinicalReportsCalculatorTest`, `GlucoseMonitorForegroundService.kt`, `WearDashboardScreen.kt`, `.generateCsv`, `E2ETier2BoundaryCornerCasesTest.kt`, `MobileGlucoseChart`, `ClinicalReportsCalculator`, `WearSparklineChart.kt`, `ConnectionItem`, `GlucoseDashboardCarScreen.kt`, `dispatchers`, `LocalGlucoseDatabase`, `WearSettingsAndDashboardContractTest.kt`, `.purgeAllLocalData`, `ReportsHubScreen`, `E2ETier1FeatureCoverageTest`, `.calculateAvailableDays`, `.subsampleOneOfThree`, `LocalGlucoseDatabase.kt`, `MobileDashboardScreen.kt`, `ClinicalReportsCalculator.kt`, `OpenGlucoRepository`, `GlucoseWidgetUpdater.kt`, `WearBluetoothSecurityTest`, `UserPreferencesRepository.kt`, `HealthDataExporter.kt`, `.testCombination_qrSync_encryption_csvExport`, `EmpiricalStressChallengeTest.kt`, `QrAuthHelper.kt`, `LocalGlucoseDatabaseTest`, `AlarmSerializationSyncTest.kt`, `DataQualityStatusCard`, `E2ETier4RealWorldScenariosTest.kt`, `MobileDualFloatingOrbs`, `DualFloatingOrbs`?**
  _High betweenness centrality (0.192) - this node is a cross-community bridge._
- **Why does `UserPreferencesRepository` connect `UserPreferencesRepository` to `QrScannerScreen.kt`, `wear/MainActivity.kt`, `WearBluetoothRfcommService.kt`, `MobileDashboardScreen`, `MobileLoginScreen.kt`, `GlucoseMonitorForegroundService.kt`, `WearDashboardScreen.kt`, `MobileGlucoseChart`, `AlarmSeverity`, `QrAuthHelper`, `GlucoseDashboardCarScreen.kt`, `dispatchers`, `LocalGlucoseDatabase`, `OpenGlucoProvider.kt`, `GlucoseTileService.kt`, `.purgeAllLocalData`, `ReportsHubScreen`, `.calculateAvailableDays`, `LocalGlucoseDatabase.kt`, `WearLoginViewModel.kt`, `MobileDashboardScreen.kt`, `ClinicalReportsCalculator.kt`, `GlucoseUnit`, `GlucoseAlarmWorker.kt`, `.decrypt`, `GlucoseWidgetUpdater.kt`, `WearQrLoginScreen.kt`, `UserPreferencesRepository.kt`, `EmpiricalStressChallengeTest.kt`, `first`, `MobileAppNavigation`, `WearSettingsScreen`?**
  _High betweenness centrality (0.111) - this node is a cross-community bridge._
- **Why does `GlucoseUnit` connect `GlucoseUnit` to `ConfigurationDiagnosticsDialog.kt`, `MobileDashboardScreen`, `WearDashboardScreen.kt`, `.generateCsv`, `E2ETier2BoundaryCornerCasesTest.kt`, `MobileGlucoseChart`, `ClinicalReportsCalculator`, `GlucoseDashboardCarScreen.kt`, `dispatchers`, `AlarmConfigSection.kt`, `mobile/ui/theme/Color.kt`, `WearSettingsAndDashboardContractTest.kt`, `GlucoseTileService.kt`, `ReportsHubScreen`, `MobileDashboardScreen.kt`, `GlucoseWidgetUpdater.kt`, `UserPreferencesRepository.kt`, `.testCombination_qrSync_encryption_csvExport`, `EmpiricalStressChallengeTest.kt`, `QrAuthHelper.kt`, `E2ETier4RealWorldScenariosTest.kt`, `MobileDualFloatingOrbs`, `DualFloatingOrbs`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Are the 23 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testSubsampleOneOfThree_eliminatesMicroOscillationSpikes()` and `.testSubsampleOneOfThree_preservesExactLiveMeasurementAtTheTip()`) actually correct?**
  _`GlucoseMeasurement` has 23 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `UserPreferencesRepository` (e.g. with `4. Persistencia y Datos (`core:data`)` and `4. Persistencia y Telemetria Historica (`core:data` & `core:model`)`) actually correct?**
  _`UserPreferencesRepository` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 12 inferred relationships involving `SensorInfo` (e.g. with `.testSensorExpirationDate_includesWarmupHour()` and `.testSensorLifecycleState_active_calculatesRemainingDays()`) actually correct?**
  _`SensorInfo` has 12 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _248 weakly-connected nodes found - possible documentation gaps or missing edges._