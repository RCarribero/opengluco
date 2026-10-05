# Graph Report - librelinkup-ecosystem-master  (2026-10-05)

## Corpus Check
- 150 files · ~115,140 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 40 file(s) not represented in the graph (top: .xml 26, .bat 4, (none) 3)

## Summary
- 1793 nodes · 4753 edges · 120 communities (57 shown, 63 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 89 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5fb2c840`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- intent
- OpenGlucoInterceptorTest
- MobileAlarmNotificationHelper
- UserPreferencesRepository
- WearDashboardScreen.kt
- AlarmConfigSection.kt
- WearBluetoothRfcommService.kt
- MobileDashboardScreen
- ConfigurationDiagnosticsDialog.kt
- SensorInfo
- AlarmRepository
- mobile/ui/theme/Color.kt
- OpenGluco Ecosystem
- E2ETier1FeatureCoverageTest
- WearQrLoginScreen.kt
- EmpiricalStressChallengeTest
- HealthDataExporter
- GlucoseMeasurement
- MobileLegalComplianceTest
- WearAuthMessageListenerService.kt
- WearBluetoothSecurityTest
- GlucoseChartWidgetProvider.kt
- GlucoseCompactWidgetProvider.kt
- SensorDurationDialog
- Test
- GlucoseComplicationService.kt
- 2.3. Perfil, Consentimientos y Reglas Regulatorias
- AlarmSeverity
- WearClinicalDesignAndSafetyTest
- Sistema de Diseno: OpenGluco (Minimalista Clinico)
- 4.2 Invariantes de Seguridad Implementadas en OpenGluco:
- AutoManifestAndSecurityTest.kt
- UserPreferencesRepository.kt
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
- GlucoseUnit
- AlarmSoundType
- Procedimiento Paso a Paso:
- DualFloatingOrbs
- GlucoseWidgetUpdater.kt
- bug_report.md
- feature_request.md
- Codigo de Conducta del Contribuyente
- ConfigurationDiagnosticsDialog
- GlucoseAlarmWorker.kt
- ModelSanityTest.kt
- PULL_REQUEST_TEMPLATE.md
- TEST_READY.md
- WearStatDetailModal
- no_emojis.md
- PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)
- rules/graphify.md
- workflows/graphify.md
- DetailModalType
- ClinicalErrorType
- wear/MainActivity.kt
- GlucoseAlarm
- first
- QrLoginCarScreen.kt
- ClinicalReportsCalculator.kt
- GlucoseDashboardCarScreen.kt
- sp
- MetricPeriod
- E2ETier4RealWorldScenariosTest.kt
- dispatchers
- AlarmSerializationSyncTest
- .decrypt
- LocalGlucoseDatabaseTest
- OpenGlucoApiServiceContractTest.kt
- ConnectionItem
- MobileDualFloatingOrbs
- EmpiricalStressChallengeTest.kt
- MobileWearableMessageListenerService.kt
- OpenGlucoInterceptor
- OpenGlucoRepository
- SensorLifecycleState
- AlarmCreationDialog
- Test
- file
- KeystoreCryptoHelper
- AutoTtsAlertManager
- WearLegalNoticeType
- .login
- PatientListCarScreen.kt
- WearQrLoginScreen
- InteractiveMedical3DScene
- AlarmSerializationSyncTest.kt
- DataQualityStatusCard
- [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido
- OpenGluco | Landing Page Interactiva 3D con Conmutador de Tema
- [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion
- [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis
- ResponsiveLayout.kt
- write_adrs.js

## God Nodes (most connected - your core abstractions)
1. `GlucoseMeasurement` - 135 edges
2. `UserPreferencesRepository` - 87 edges
3. `GlucoseAlarm` - 58 edges
4. `GlucoseUnit` - 47 edges
5. `SensorInfo` - 46 edges
6. `AlarmRepository` - 44 edges
7. `ConnectionItem` - 39 edges
8. `OpenGlucoRepository` - 37 edges
9. `ClinicalModelsTest` - 37 edges
10. `MobileDashboardScreen()` - 35 edges

## Surprising Connections (you probably didn't know these)
- `Data Portability CSV Contract (`HealthDataExporter`)` --references--> `HealthDataExporter`  [INFERRED]
  PROJECT.md → core/data/src/main/java/com/example/opengluco/core/data/HealthDataExporter.kt
- `Cryptographic Contract (`KeystoreCryptoHelper`)` --references--> `KeystoreCryptoHelper`  [INFERRED]
  PROJECT.md → core/data/src/main/java/com/example/opengluco/core/data/KeystoreCryptoHelper.kt
- `4. Persistencia y Datos (`core:data`)` --references--> `UserPreferencesRepository`  [INFERRED]
  AGENTS.md → core/data/src/main/java/com/example/opengluco/core/data/UserPreferencesRepository.kt
- `4. Persistencia y Telemetria Historica (`core:data` & `core:model`)` --references--> `UserPreferencesRepository`  [INFERRED]
  .agents/rules/clinical_design.md → core/data/src/main/java/com/example/opengluco/core/data/UserPreferencesRepository.kt
- `4. Decision Elegida` --references--> `CgmCurveSmoother`  [INFERRED]
  docs/adr/0001-arquitectura-modular-compartida.md → core/model/src/main/java/com/example/opengluco/core/model/CgmCurveSmoother.kt

## Import Cycles
- None detected.

## Communities (120 total, 63 thin omitted)

### Community 5 - "WearDashboardScreen.kt"
Cohesion: 0.15
Nodes (6): PatientSelectorChip(), WearDashboardScreen(), WearLegalNoticeDialog(), WearLegalSectionBox(), WearLegalTexts, WearPassiveLegalFooter()

### Community 7 - "WearBluetoothRfcommService.kt"
Cohesion: 0.07
Nodes (7): BootReceiver, GlucoseMonitorForegroundService, WearBluetoothRfcommService, 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Decision Elegida, [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer

### Community 8 - "MobileDashboardScreen"
Cohesion: 0.19
Nodes (15): DailyStatItem(), DashboardChartCard(), DashboardClinicalErrorBanner(), DashboardHeroSection(), DashboardSensorCard(), DashboardStatsCard(), formatChartTime(), MobileDashboardScreen() (+7 more)

### Community 13 - "mobile/ui/theme/Color.kt"
Cohesion: 0.20
Nodes (6): PatientHeaderChip(), PatientSelectorModal(), ClinicalColorScheme, getClinicalStatusColor(), getGlucoseStatusColor(), getGlucoseValueColor()

### Community 14 - "OpenGluco Ecosystem"
Cohesion: 0.06
Nodes (30): 1. Resumen Ejecutivo del Dictamen Legal, 2.1 Permisos del Sistema Declarados en Manifiesto, 2.2 Auditoría de Almacenamiento y Cero Telemetría de Terceros, 2. Auditoría Técnica de Permisos, Accesos y Datos Registrados, 3.1 Derecho de Interoperabilidad e Ingeniería Inversa, 3.2 Titularidad del Paciente sobre sus Datos Biológicos y de Salud, 3.3 Reglamento Europeo de Datos (Data Act - Reglamento UE 2023/2854), 3.4 Derecho a la Portabilidad y Exención Doméstica (RGPD) (+22 more)

### Community 18 - "HealthDataExporter"
Cohesion: 0.22
Nodes (3): HealthDataExporter, E2ETier2BoundaryCornerCasesTest, Architecture

### Community 19 - "GlucoseMeasurement"
Cohesion: 0.15
Nodes (6): WidgetChartRenderer, Success, CgmCurveSmoother, CubicBezierSegment, GlucoseMeasurement, CgmCurveSmootherTest

### Community 21 - "WearAuthMessageListenerService.kt"
Cohesion: 0.18
Nodes (3): WearAlarmNotificationHelper, WearAuthMessageListenerService, WearGlucoseSyncWorker

### Community 25 - "SensorDurationDialog"
Cohesion: 0.83
Nodes (3): DurationOption, DurationOptionRow(), SensorDurationDialog()

### Community 28 - "2.3. Perfil, Consentimientos y Reglas Regulatorias"
Cohesion: 0.09
Nodes (21): 1. Topología de Servidores y Puertas de Enlace (Gateways), 2.1. Autenticación y Gestión de Sesión, 2.2. Subida de Datos de Glucosa y Telemetría del Sensor (`POST api/measurements`), 2.3. Perfil, Consentimientos y Reglas Regulatorias, 2. Endpoints del Gateway Clínico Principal (`NumeraWebApi` en `/lsl/`), 3. Endpoints del CDN de Distribución (`fsll.freestyleserver.com`), 4. Endpoints de Consulta y Lectura (Ecosistema LibreLinkUp `/llu/`), 5. Cuadro Resumen de Cabeceras Requeridas (+13 more)

### Community 29 - "AlarmSeverity"
Cohesion: 0.11
Nodes (8): AlarmDismissReceiver, AlarmSeverity, ALERT, INFORMATIVE, URGENT, AlarmType, HIGH, LOW

### Community 31 - "Sistema de Diseno: OpenGluco (Minimalista Clinico)"
Cohesion: 0.20
Nodes (9): 1. Filosofia y Estilo Visual, 2. Paleta de Colores Oficial, 4. Interactividad y Feedback Haptico, 5. Mapeo de Codigo Jetpack Compose, 6. Mencion a Abbott Laboratories y Marcas Registradas, Estados Clinicos de Glucosa, Fondos y Superficies OLED, Sistema de Diseno: OpenGluco (Minimalista Clinico) (+1 more)

### Community 32 - "4.2 Invariantes de Seguridad Implementadas en OpenGluco:"
Cohesion: 0.32
Nodes (4): AutoClinicalSafetyTest, 4.1 Análisis bajo la Guía MDCG 2019-11 (Regla 11 del Anexo VIII MDR), 4.2 Invariantes de Seguridad Implementadas en OpenGluco:, 4. Clasificación Regulatoria de Dispositivos Médicos (MDR UE 2017/745 y FDA MDDS)

### Community 34 - "UserPreferencesRepository.kt"
Cohesion: 0.05
Nodes (16): DialogHeader(), EmptyNotesState(), eventColor(), eventIcon(), EventNoteCard(), GlucoseEventEditorDialog(), GlucoseEventListDialog(), LocalGlucoseDatabase (+8 more)

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

### Community 57 - "GlucoseUnit"
Cohesion: 0.21
Nodes (7): AlarmCard(), AlarmConfigSection(), AlarmSubsection(), ClinicalRangeVisualCard(), GlucoseUnit, MGDL, MMOL

### Community 58 - "AlarmSoundType"
Cohesion: 0.11
Nodes (19): AlarmCooldown, MIN_1, MIN_10, MIN_15, MIN_2, MIN_3, MIN_30, MIN_4 (+11 more)

### Community 59 - "Procedimiento Paso a Paso:"
Cohesion: 0.18
Nodes (10): 1. Confirmación de Versión y Changelog, 2. Sincronización de Versiones en Gradle, 3. Validación y Pruebas Unitarias, 4. Compilación Local de APKs, 5. Empaquetado y Organización de Artefactos, 6. Versionado en Git, 7. Publicación de la Release, Principios Obligatorios: (+2 more)

### Community 60 - "DualFloatingOrbs"
Cohesion: 0.13
Nodes (12): 3. App Reloj (`app-wear`), 1. Paleta de Colores y Tokens Clinicos Oficiales, 2. Directrices de Interfaz Movil (`app-mobile`), 3. Directrices Wear OS (`app-wear`), 4. Persistencia y Telemetria Historica (`core:data` & `core:model`), 5. Invariantes de Telemetria y Formateo, Superficies OLED, DualFloatingOrbs() (+4 more)

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
Cohesion: 0.22
Nodes (9): AuthExpired, ClinicalErrorType, Generic, NetworkError, None, NoPatients, NoSensor, SensorExpired (+1 more)

### Community 77 - "wear/MainActivity.kt"
Cohesion: 0.06
Nodes (18): MainActivity, WearAppNavigation(), WearLoginScreen(), Error, Idle, Loading, Success, WearLoginUiState (+10 more)

### Community 78 - "GlucoseAlarm"
Cohesion: 0.25
Nodes (4): AlarmEvaluator, AlarmEvaluatorTest, AlarmEvaluationResult, GlucoseAlarm

### Community 85 - "ClinicalReportsCalculator.kt"
Cohesion: 0.05
Nodes (31): ClinicalExplanationBox(), MetricColumn(), ReportsHubScreen(), TirCategoryRow(), DataCoherenceAndThemeTest, ClinicalReportsCalculator, UserSettings, ClinicalReportsCalculatorTest (+23 more)

### Community 88 - "MetricPeriod"
Cohesion: 0.40
Nodes (5): MetricPeriod, DAY, MONTH, THREE_MONTHS, WEEK

### Community 90 - "dispatchers"
Cohesion: 0.18
Nodes (5): MobilePairingHelper, QrEncryptedPayload, QrPairingPayload, QrSessionExchange, QrAuthModelsTest

### Community 95 - "ConnectionItem"
Cohesion: 0.24
Nodes (11): ActiveSensorEntry, AuthTicket, BaseResponse, ConnectionItem, DeviceInfo, GraphData, LoginData, LoginRequest (+3 more)

### Community 96 - "MobileDualFloatingOrbs"
Cohesion: 0.29
Nodes (5): MobileDualFloatingOrbs(), 1. App Movil (`app-mobile`), 2. App Reloj (`app-wear`), 3. App Coche (`app-auto`), Caracteristicas Principales

### Community 97 - "EmpiricalStressChallengeTest.kt"
Cohesion: 0.11
Nodes (3): QrDeviceType, ANDROID_AUTO, WEAR_OS

### Community 98 - "MobileWearableMessageListenerService.kt"
Cohesion: 0.15
Nodes (3): MobileAlarmSyncHelper, MobileWearableMessageListenerService, WearAlarmDismissReceiver

### Community 100 - "OpenGlucoRepository"
Cohesion: 0.14
Nodes (8): OpenGlucoRegion, AP, DE, EU, FR, JP, US, OpenGlucoRepository

### Community 101 - "SensorLifecycleState"
Cohesion: 0.43
Nodes (5): Active, Expired, lifecycleState(), SensorLifecycleState, WarmingUp

### Community 102 - "AlarmCreationDialog"
Cohesion: 0.53
Nodes (3): AlarmCreationDialog(), copyCustomAudioToInternalStorage(), getFileNameFromUri()

### Community 107 - "WearLegalNoticeType"
Cohesion: 0.33
Nodes (6): WearLegalNoticeType, DELETE_CONFIRMATION, MEDICAL_DISCLAIMER, NONE, PRIVACY_GDPR, TRADEMARKS

### Community 111 - "InteractiveMedical3DScene"
Cohesion: 0.16
Nodes (8): GLUCOSE_STATES, InteractiveMedical3DScene, renderTelemetryGraph(), setRangeState(), setTheme(), telemetry24h, toggleTheme(), updateGraphPoint()

### Community 141 - "[ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido"
Cohesion: 0.20
Nodes (9): 1. Contexto y Declaracion del Problema, 2. Factores Decisivos (Decision Drivers), 3. Opciones Consideradas, 4. Decision Elegida, 5. Consecuencias y Compromisos (Trade-offs), 6. Reglas de Validacion y Cumplimiento (Enforcement), [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido, Consecuencias Negativas / Riesgos Asumidos: (+1 more)

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
Cohesion: 0.05
Nodes (21): MainActivity, MobileAppNavigation(), MobileLoginScreen(), CameraPreview(), decodeQrFromImage(), QrScannerScreen(), rotateYUV420Degree180(), rotateYUV420Degree270() (+13 more)

### Community 174 - "write_adrs.js"
Cohesion: 0.33
Nodes (3): docsDir, fs, path

## Knowledge Gaps
- **250 isolated node(s):** `H24`, `H12`, `H6`, `H2`, `H1` (+245 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 496 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **63 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GlucoseMeasurement` connect `GlucoseMeasurement` to `UserPreferencesRepository`, `WearBluetoothRfcommService.kt`, `MobileDashboardScreen`, `SensorInfo`, `E2ETier1FeatureCoverageTest`, `WearQrLoginScreen.kt`, `EmpiricalStressChallengeTest`, `HealthDataExporter`, `WearBluetoothSecurityTest`, `UserPreferencesRepository.kt`, `.generateCsv`, `WearSettingsAndDashboardContractTest.kt`, `DualFloatingOrbs`, `GlucoseWidgetUpdater.kt`, `ModelSanityTest.kt`, `wear/MainActivity.kt`, `MobileDashboardScreen.kt`, `ClinicalReportsCalculator.kt`, `GlucoseDashboardCarScreen.kt`, `E2ETier4RealWorldScenariosTest.kt`, `LocalGlucoseDatabaseTest`, `ConnectionItem`, `MobileDualFloatingOrbs`, `EmpiricalStressChallengeTest.kt`, `AlarmSerializationSyncTest.kt`, `DataQualityStatusCard`?**
  _High betweenness centrality (0.169) - this node is a cross-community bridge._
- **Why does `UserPreferencesRepository` connect `UserPreferencesRepository` to `MobileAlarmNotificationHelper`, `QrScannerScreen.kt`, `WearDashboardScreen.kt`, `WearBluetoothRfcommService.kt`, `MobileDashboardScreen`, `MobileLoginScreen.kt`, `WearQrLoginScreen.kt`, `WearAuthMessageListenerService.kt`, `GlucoseComplicationService.kt`, `UserPreferencesRepository.kt`, `ResponsiveLayout.kt`, `GlucoseTileService.kt`, `.purgeAllLocalData`, `Reglas de Proyecto: OpenGluco Ecosystem`, `GlucoseUnit`, `DualFloatingOrbs`, `GlucoseWidgetUpdater.kt`, `GlucoseAlarmWorker.kt`, `wear/MainActivity.kt`, `first`, `MobileDashboardScreen.kt`, `ClinicalReportsCalculator.kt`, `GlucoseDashboardCarScreen.kt`, `.decrypt`, `MobileWearableMessageListenerService.kt`, `KeystoreCryptoHelper`, `WearQrLoginScreen`?**
  _High betweenness centrality (0.156) - this node is a cross-community bridge._
- **Why does `OpenGlucoRepository` connect `OpenGlucoRepository` to `GlucoseAlarmWorker.kt`, `MobileAlarmNotificationHelper`, `QrScannerScreen.kt`, `OpenGlucoInterceptor`, `WearBluetoothRfcommService.kt`, `MobileDashboardScreen`, `ResponsiveLayout.kt`, `Test`, `MobileLoginScreen.kt`, `.login`, `wear/MainActivity.kt`, `MobileDashboardScreen.kt`, `WearAuthMessageListenerService.kt`, `GlucoseDashboardCarScreen.kt`, `ConnectionItem`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Are the 23 inferred relationships involving `GlucoseMeasurement` (e.g. with `.testSubsampleOneOfThree_eliminatesMicroOscillationSpikes()` and `.testSubsampleOneOfThree_preservesExactLiveMeasurementAtTheTip()`) actually correct?**
  _`GlucoseMeasurement` has 23 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `UserPreferencesRepository` (e.g. with `4. Persistencia y Datos (`core:data`)` and `4. Persistencia y Telemetria Historica (`core:data` & `core:model`)`) actually correct?**
  _`UserPreferencesRepository` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 12 inferred relationships involving `SensorInfo` (e.g. with `.testSensorExpirationDate_includesWarmupHour()` and `.testSensorLifecycleState_active_calculatesRemainingDays()`) actually correct?**
  _`SensorInfo` has 12 INFERRED edges - model-reasoned connections that need verification._
- **What connects `H24`, `H12`, `H6` to the rest of the system?**
  _250 weakly-connected nodes found - possible documentation gaps or missing edges._