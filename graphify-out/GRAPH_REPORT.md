# Graph Report - librelinkup-ecosystem-master  (2026-10-05)

## Corpus Check
- 150 files · ~122,530 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 37 file(s) not represented in the graph (top: .xml 26, .bat 4, (none) 3)

## Summary
- 1824 nodes · 4871 edges · 124 communities (60 shown, 64 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 91 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8cefe1d6`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OpenGlucoRepository.kt
- OpenGlucoInterceptor
- MobileAlarmNotificationHelper
- UserPreferencesRepository
- wear/MainActivity.kt
- WearBluetoothRfcommService.kt
- MobileDashboardScreen
- ConfigurationDiagnosticsDialog.kt
- GlucoseMeasurement
- AlarmRepository
- ClinicalReportsCalculatorTest
- OpenGluco Ecosystem
- GlucoseUnit
- WearDashboardScreen.kt
- EmpiricalStressChallengeTest
- E2ETier2BoundaryCornerCasesTest
- .renderSparkline
- MobileLegalComplianceTest
- WearAuthMessageListenerService.kt
- GlucoseEventMarker
- mobile/ui/theme/Color.kt
- QrAuthHelper.kt
- SensorDurationDialog
- Test
- GlucoseComplicationService.kt
- 2.3. Perfil, Consentimientos y Reglas Regulatorias
- MobileAlarmSyncHelper.kt
- WearClinicalDesignAndSafetyTest
- Sistema de Diseno: OpenGluco (Minimalista Clinico)
- 4.2 Invariantes de Seguridad Implementadas en OpenGluco:
- AutoManifestAndSecurityTest.kt
- LocalGlucoseDatabase
- DashboardTimeframe
- AppUpdateRepository
- LegalNoticeType
- .generateCsv
- 2. Test Tiers & Methodology
- GlucoseTileService.kt
- Acceptance Criteria
- 1. Reglas Innegociables del Proyecto
- Acceptance Criteria
- QrAuthHelper
- .purgeAllLocalData
- Reglas de Proyecto: OpenGluco Ecosystem
- BRIEFING.md
- WearSettingsAndDashboardContractTest.kt
- E2ETier1FeatureCoverageTest
- AlarmSeverity
- Procedimiento Paso a Paso:
- clinical_design.md
- locale
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
- ConfigurationDiagnosticsDialog
- WearQrLoginScreen.kt
- QrPairingPayload
- PULL_REQUEST_TEMPLATE.md
- TEST_READY.md
- WearStatDetailModal
- no_emojis.md
- PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)
- rules/graphify.md
- workflows/graphify.md
- DetailModalType
- ClinicalErrorType
- launch
- GlucoseAlarm
- OpenGlucoRepository
- wear/ui/theme/Theme.kt
- ClinicalReportsCalculator.kt
- GlucoseDashboardCarScreen.kt
- GlucoseAlarmWorker.kt
- MetricPeriod
- E2ETier4RealWorldScenariosTest.kt
- MobilePairingHelper.kt
- AlarmSerializationSyncTest
- .decrypt
- LocalGlucoseDatabaseTest
- .login
- ConnectionItem
- UserSettings
- GlucoseWidgetUpdater.kt
- WearBluetoothSecurityTest
- WearDashboardViewModel
- WearLegalNoticeType
- ReportsHubScreen
- LocalGlucoseDatabase.kt
- AppUpdateInstaller.kt
- EmpiricalStressChallengeTest.kt
- E2ETier3CrossFeatureCombinationsTest.kt
- WearSettingsScreen
- AlarmDismissReceiver.kt
- first
- MainActivity
- app.js
- AlarmSerializationSyncTest.kt
- DataQualityStatusCard
- DualFloatingOrbs
- Test
- TirCategory
- [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido
- OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema
- [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion
- [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis
- ResponsiveLayout.kt
- write_adrs.js

## God Nodes (most connected - your core abstractions)
1. `GlucoseMeasurement` - 140 edges
2. `UserPreferencesRepository` - 92 edges
3. `GlucoseAlarm` - 58 edges
4. `SensorInfo` - 48 edges
5. `GlucoseUnit` - 47 edges
6. `AlarmRepository` - 44 edges
7. `ConnectionItem` - 40 edges
8. `OpenGlucoRepository` - 39 edges
9. `ClinicalModelsTest` - 37 edges
10. `MobileDashboardScreen()` - 33 edges

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

## Communities (124 total, 64 thin omitted)

### Community 5 - "wear/MainActivity.kt"
Cohesion: 0.18
Nodes (3): WearAppNavigation(), WearLoginScreen(), WearDashboardScreen()

### Community 7 - "WearBluetoothRfcommService.kt"
Cohesion: 0.11
Nodes (5): WearBluetoothRfcommService, 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Decision Elegida, [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer

### Community 8 - "MobileDashboardScreen"
Cohesion: 0.17
Nodes (16): DailyStatItem(), DashboardChartCard(), DashboardClinicalErrorBanner(), DashboardHeroSection(), DashboardSensorCard(), DashboardStatsCard(), formatChartTime(), MobileDashboardScreen() (+8 more)

### Community 10 - "GlucoseMeasurement"
Cohesion: 0.10
Nodes (5): ClinicalSparklineWithSensor(), Success, GlucoseMeasurement, SensorInfo, ClinicalModelsTest

### Community 14 - "OpenGluco Ecosystem"
Cohesion: 0.06
Nodes (34): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+26 more)

### Community 15 - "GlucoseUnit"
Cohesion: 0.16
Nodes (10): AlarmCard(), AlarmConfigSection(), AlarmCreationDialog(), AlarmSubsection(), ClinicalRangeVisualCard(), copyCustomAudioToInternalStorage(), getFileNameFromUri(), GlucoseUnit (+2 more)

### Community 16 - "WearDashboardScreen.kt"
Cohesion: 0.15
Nodes (3): PatientSelectorChip(), WearSparklineChart(), WearLegalTexts

### Community 19 - ".renderSparkline"
Cohesion: 0.17
Nodes (4): WidgetChartRenderer, CgmCurveSmoother, CubicBezierSegment, CgmCurveSmootherTest

### Community 21 - "WearAuthMessageListenerService.kt"
Cohesion: 0.19
Nodes (3): WearAlarmNotificationHelper, WearAuthMessageListenerService, WearGlucoseSyncWorker

### Community 22 - "GlucoseEventMarker"
Cohesion: 0.17
Nodes (14): DialogHeader(), EmptyNotesState(), eventColor(), eventIcon(), EventNoteCard(), GlucoseEventEditorDialog(), GlucoseEventListDialog(), GlucoseEventNotesMenuButton() (+6 more)

### Community 23 - "mobile/ui/theme/Color.kt"
Cohesion: 0.16
Nodes (7): MobileDualFloatingOrbs(), PatientHeaderChip(), PatientSelectorModal(), ClinicalColorScheme, getClinicalStatusColor(), getGlucoseStatusColor(), getGlucoseValueColor()

### Community 25 - "SensorDurationDialog"
Cohesion: 0.83
Nodes (3): DurationOption, DurationOptionRow(), SensorDurationDialog()

### Community 28 - "2.3. Perfil, Consentimientos y Reglas Regulatorias"
Cohesion: 0.09
Nodes (21): 1. Topología de Servidores y Puertas de Enlace (Gateways), 2.1. Autenticación y Gestión de Sesión, 2.2. Subida de Datos de Glucosa y Telemetría del Sensor (`POST api/measurements`), 2.3. Perfil, Consentimientos y Reglas Regulatorias, 2. Endpoints del Gateway Clínico Principal (`NumeraWebApi` en `/lsl/`), 3. Endpoints del CDN de Distribución (`fsll.freestyleserver.com`), 4. Endpoints de Consulta y Lectura (Ecosistema LibreLinkUp `/llu/`), 5. Cuadro Resumen de Cabeceras Requeridas (+13 more)

### Community 31 - "Sistema de Diseno: OpenGluco (Minimalista Clinico)"
Cohesion: 0.20
Nodes (9): 1. Filosofia y Estilo Visual, 2. Paleta de Colores Oficial, 4. Interactividad y Feedback Haptico, 5. Mapeo de Codigo Jetpack Compose, 6. Mencion a Abbott Laboratories y Marcas Registradas, Estados Clinicos de Glucosa, Fondos y Superficies OLED, Sistema de Diseno: OpenGluco (Minimalista Clinico) (+1 more)

### Community 32 - "4.2 Invariantes de Seguridad Implementadas en OpenGluco:"
Cohesion: 0.32
Nodes (4): AutoClinicalSafetyTest, 4.1 Análisis bajo la Guía MDCG 2019-11 (Regla 11 del Anexo VIII MDR), 4.2 Invariantes de Seguridad Implementadas en OpenGluco:, 4. Clasificación Regulatoria de Dispositivos Médicos (MDR UE 2017/745 y FDA MDDS)

### Community 35 - "DashboardTimeframe"
Cohesion: 0.29
Nodes (6): DashboardTimeframe, H1, H12, H2, H24, H6

### Community 45 - "LegalNoticeType"
Cohesion: 0.25
Nodes (8): LegalNoticeDialog(), LegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS, LegalSectionBox()

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

### Community 53 - ".purgeAllLocalData"
Cohesion: 0.15
Nodes (10): Code Layout, Cryptographic Contract (`KeystoreCryptoHelper`), Data Portability CSV Contract (`HealthDataExporter`), Feature Inventory, Interface Contracts, Legal & Regulatory String Constants, Milestones, 1. Arquitectura de Seguridad y Privacidad (+2 more)

### Community 54 - "Reglas de Proyecto: OpenGluco Ecosystem"
Cohesion: 0.22
Nodes (8): 1. Directrices de Interfaz y Tokens Clinicos, 2. App Movil (`app-mobile`), 4. Persistencia y Datos (`core:data`), 5. Invariantes de Telemetria y Formato, 6. Mencion a Abbott Laboratories y Blindaje Legal, 7. Arquitectura y Grafo de Conocimiento (Graphify), 8. Protocolo de Publicación y Actualizaciones OTA (Bajo Demanda), Reglas de Proyecto: OpenGluco Ecosystem

### Community 55 - "BRIEFING.md"
Cohesion: 0.25
Nodes (7): Artifact Index, Key Constraints, Mission, My Identity, Project Status, User Context, Victory Audit Status

### Community 56 - "WearSettingsAndDashboardContractTest.kt"
Cohesion: 0.21
Nodes (4): WearGlucoseGauge(), getClinicalStatusColor(), getGlucoseStatusColor(), WearSettingsAndDashboardContractTest

### Community 58 - "AlarmSeverity"
Cohesion: 0.06
Nodes (26): AlarmCooldown, MIN_1, MIN_10, MIN_15, MIN_2, MIN_3, MIN_30, MIN_4 (+18 more)

### Community 59 - "Procedimiento Paso a Paso:"
Cohesion: 0.18
Nodes (10): 1. Confirmación de Versión y Changelog, 2. Sincronización de Versiones en Gradle, 3. Validación y Pruebas Unitarias, 4. Compilación Local de APKs, 5. Empaquetado y Organización de Artefactos, 6. Versionado en Git, 7. Publicación de la Release, Principios Obligatorios: (+2 more)

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

### Community 65 - "ConfigurationDiagnosticsDialog"
Cohesion: 0.31
Nodes (4): ConfigurationDiagnosticsDialog(), DiagnosticItemCard(), SystemDiagnosticsHelper, SystemDiagnosticsState

### Community 67 - "QrPairingPayload"
Cohesion: 0.24
Nodes (7): QrDeviceType, ANDROID_AUTO, WEAR_OS, QrEncryptedPayload, QrPairingPayload, QrSessionExchange, QrAuthModelsTest

### Community 68 - "PULL_REQUEST_TEMPLATE.md"
Cohesion: 0.40
Nodes (4): Descripcion del Cambio, Lista de Verificacion de Calidad y Cumplimiento, Modulos Modificados, Tipo de Cambio

### Community 69 - "TEST_READY.md"
Cohesion: 0.40
Nodes (4): 1. Executive Summary, 2. Test Suite Breakdown by Module, 3. Tier Coverage Matrix, 4. How to Run the Tests

### Community 70 - "WearStatDetailModal"
Cohesion: 0.22
Nodes (7): DetailModalType, GLUCOSE_STATS, NONE, SENSOR_INFO, TREND_INFO, StatRow(), WearStatDetailModal()

### Community 75 - "DetailModalType"
Cohesion: 0.25
Nodes (7): DetailModalType, GLUCOSE_STATS, NONE, SENSOR_INFO, TREND_INFO, MobileStatDetailModal(), StatRow()

### Community 76 - "ClinicalErrorType"
Cohesion: 0.15
Nodes (14): Active, AuthExpired, ClinicalErrorType, Expired, Generic, lifecycleState(), NetworkError, None (+6 more)

### Community 77 - "launch"
Cohesion: 0.16
Nodes (6): Error, Idle, Loading, Success, WearLoginUiState, WearLoginViewModel

### Community 78 - "GlucoseAlarm"
Cohesion: 0.25
Nodes (4): AlarmEvaluator, AlarmEvaluatorTest, AlarmEvaluationResult, GlucoseAlarm

### Community 79 - "OpenGlucoRepository"
Cohesion: 0.16
Nodes (8): OpenGlucoRegion, AP, DE, EU, FR, JP, US, OpenGlucoRepository

### Community 85 - "ClinicalReportsCalculator.kt"
Cohesion: 0.14
Nodes (16): AverageGlucoseReport, BlockMetric, DailyGraphDaySummary, DailyGraphReport, DailyPatternsReport, DataQualityReport, EstimatedA1cReport, HourlyPercentile (+8 more)

### Community 86 - "GlucoseDashboardCarScreen.kt"
Cohesion: 0.06
Nodes (6): AutoTtsAlertManager, GlucoseCarSession, GlucoseDashboardCarScreen, onDestroy(), PatientListCarScreen, QrLoginCarScreen

### Community 88 - "MetricPeriod"
Cohesion: 0.40
Nodes (5): MetricPeriod, DAY, MONTH, THREE_MONTHS, WEEK

### Community 95 - "ConnectionItem"
Cohesion: 0.24
Nodes (11): ActiveSensorEntry, AuthTicket, BaseResponse, ConnectionItem, DeviceInfo, GraphData, LoginData, LoginRequest (+3 more)

### Community 97 - "GlucoseWidgetUpdater.kt"
Cohesion: 0.09
Nodes (4): GlucoseCarAppService, GlucoseChartWidgetProvider, GlucoseCompactWidgetProvider, GlucoseWidgetUpdater

### Community 99 - "WearDashboardViewModel"
Cohesion: 0.27
Nodes (5): Error, Loading, NeedsLogin, WearDashboardUiState, WearDashboardViewModel

### Community 101 - "WearLegalNoticeType"
Cohesion: 0.33
Nodes (6): WearLegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS

### Community 102 - "ReportsHubScreen"
Cohesion: 0.21
Nodes (7): ClinicalExplanationBox(), MetricColumn(), ReportsHubScreen(), TirCategoryRow(), ClinicalReportsCalculator, TimeInRangeReport, TirBucket

### Community 107 - "WearSettingsScreen"
Cohesion: 0.33
Nodes (6): WearLegalNoticeDialog(), WearLegalSectionBox(), WearPassiveLegalFooter(), CompactActionRow(), CompactSettingsRow(), WearSettingsScreen()

### Community 111 - "app.js"
Cohesion: 0.17
Nodes (35): bindAutoEvents(), bindDashboardEvents(), bindLoginEvents(), bindQrEvents(), bindReportsEvents(), bindSettingsEvents(), bindWearEvents(), closeModal() (+27 more)

### Community 112 - "AlarmSerializationSyncTest.kt"
Cohesion: 0.14
Nodes (3): GlucoseDashboardLegalTest, AppUpdateRepositoryTest, ModelSanityTest

### Community 114 - "DualFloatingOrbs"
Cohesion: 0.29
Nodes (5): DualFloatingOrbs(), 3. Jerarquia y Distribucion Espacial en Wear OS, A. Esferas Flotantes Superiores (`DualFloatingOrbs`), B. Grafica Sparkline + Badge de Sensor, Especificaciones de Componentes:

### Community 117 - "TirCategory"
Cohesion: 0.33
Nodes (6): TirCategory, HIGH, IN_RANGE, LOW, VERY_HIGH, VERY_LOW

### Community 141 - "[ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido"
Cohesion: 0.20
Nodes (9): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), 6. Reglas de Validacion y Cumplimiento (Enforcement), [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido, Consecuencias Negativas / Riesgos Asumidos: (+1 more)

### Community 148 - "OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema"
Cohesion: 0.40
Nodes (4): Archivos, Novedades Principales, OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema, Visualización

### Community 159 - "[ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion"
Cohesion: 0.33
Nodes (5): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 5. Consecuencias y Compromisos (Trade-offs), [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion

### Community 168 - "[ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis"
Cohesion: 0.50
Nodes (3): 1. Contexto y Declaracion del Problema, 2. Reglas Deterministas Forzadas, [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis

### Community 169 - "ResponsiveLayout.kt"
Cohesion: 0.05
Nodes (21): MainActivity, MobileAppNavigation(), MobileLoginScreen(), CameraPreview(), decodeQrFromImage(), QrScannerScreen(), rotateYUV420Degree180(), rotateYUV420Degree270() (+13 more)

### Community 174 - "write_adrs.js"
Cohesion: 0.33
Nodes (3): docsDir, fs, path

## Knowledge Gaps
- **248 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+243 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 497 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **64 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GlucoseMeasurement` connect `GlucoseMeasurement` to `OpenGlucoRepository.kt`, `UserPreferencesRepository`, `QrScannerScreen.kt`, `WearBluetoothRfcommService.kt`, `MobileDashboardScreen`, `AlarmRepository`, `ClinicalReportsCalculatorTest`, `WearDashboardScreen.kt`, `EmpiricalStressChallengeTest`, `E2ETier2BoundaryCornerCasesTest`, `.renderSparkline`, `mobile/ui/theme/Color.kt`, `LocalGlucoseDatabase`, `.generateCsv`, `QrAuthHelper`, `WearSettingsAndDashboardContractTest.kt`, `E2ETier1FeatureCoverageTest`, `locale`, `launch`, `MobileDashboardScreen.kt`, `ClinicalReportsCalculator.kt`, `GlucoseDashboardCarScreen.kt`, `E2ETier4RealWorldScenariosTest.kt`, `LocalGlucoseDatabaseTest`, `.login`, `ConnectionItem`, `UserSettings`, `GlucoseWidgetUpdater.kt`, `WearBluetoothSecurityTest`, `UserPreferencesRepository.kt`, `ReportsHubScreen`, `LocalGlucoseDatabase.kt`, `EmpiricalStressChallengeTest.kt`, `E2ETier3CrossFeatureCombinationsTest.kt`, `AlarmSerializationSyncTest.kt`, `DataQualityStatusCard`, `DualFloatingOrbs`, `.getHistoricalReadingsList`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `UserPreferencesRepository` connect `UserPreferencesRepository` to `wear/MainActivity.kt`, `WearBluetoothRfcommService.kt`, `MobileDashboardScreen`, `MobileLoginScreen.kt`, `AlarmRepository`, `GlucoseUnit`, `WearDashboardScreen.kt`, `WearAuthMessageListenerService.kt`, `GlucoseEventMarker`, `QrAuthHelper.kt`, `GlucoseComplicationService.kt`, `LocalGlucoseDatabase`, `ResponsiveLayout.kt`, `GlucoseTileService.kt`, `.purgeAllLocalData`, `Reglas de Proyecto: OpenGluco Ecosystem`, `clinical_design.md`, `WearQrLoginScreen.kt`, `launch`, `MobileDashboardScreen.kt`, `ClinicalReportsCalculator.kt`, `GlucoseDashboardCarScreen.kt`, `GlucoseAlarmWorker.kt`, `.decrypt`, `UserSettings`, `GlucoseWidgetUpdater.kt`, `WearDashboardViewModel`, `UserPreferencesRepository.kt`, `ReportsHubScreen`, `LocalGlucoseDatabase.kt`, `WearSettingsScreen`, `first`, `.getHistoricalReadingsList`?**
  _High betweenness centrality (0.134) - this node is a cross-community bridge._
- **Why does `OpenGlucoRepository` connect `OpenGlucoRepository` to `OpenGlucoRepository.kt`, `OpenGlucoInterceptor`, `WearDashboardViewModel`, `wear/MainActivity.kt`, `MobileDashboardScreen`, `ResponsiveLayout.kt`, `MobileLoginScreen.kt`, `AlarmRepository`, `launch`, `MobileDashboardScreen.kt`, `Test`, `WearAuthMessageListenerService.kt`, `GlucoseDashboardCarScreen.kt`, `GlucoseAlarmWorker.kt`, `.login`, `ConnectionItem`?**
  _High betweenness centrality (0.036) - this node is a cross-community bridge._
- **Are the 23 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testSubsampleOneOfThree_eliminatesMicroOscillationSpikes()` and `.testSubsampleOneOfThree_preservesExactLiveMeasurementAtTheTip()`) actually correct?**
  _`GlucoseMeasurement` has 23 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `UserPreferencesRepository` (e.g. with `4. Persistencia y Datos (`core:data`)` and `4. Persistencia y Telemetria Historica (`core:data` & `core:model`)`) actually correct?**
  _`UserPreferencesRepository` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 12 inferred relationships involving `SensorInfo` (e.g. with `.testSensorExpirationDate_includesWarmupHour()` and `.testSensorLifecycleState_active_calculatesRemainingDays()`) actually correct?**
  _`SensorInfo` has 12 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _248 weakly-connected nodes found - possible documentation gaps or missing edges._