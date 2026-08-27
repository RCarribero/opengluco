# Project: OpenGluco Ecosystem Legal Readiness & Feature Parity

## Architecture
The OpenGluco Ecosystem is a local-first multi-platform suite (Android Mobile, Wear OS Galaxy Watch, and Android Auto) for passive secondary monitoring of continuous glucose telemetry from OpenGluco / Abbott Cloud:
- `:core:model`: Clinical domain models (`GlucoseMeasurement`, `GlucoseColorRange`, `TrendArrow`), units (`mg/dL`, `mmol/L`), and clinical design tokens.
- `:core:network`: Direct TLS communication with official OpenGluco REST APIs with zero intermediate servers.
- `:core:data`: Local-first persistence. Android Keystore AES-256-GCM authenticated encryption for JWT tokens and 90-day historical telemetry (`glucose_history.json`), GDPR Art. 17 data purge (`purgeAllLocalData`), and GDPR Art. 20 CSV export (`HealthDataExporter`).
- `:app-mobile`: Android phone client with full settings, CameraX/ZXing QR scanner, encrypted local auth transfer, modal legal notices, and passive legal footer.
- `:app-wear`: Wear OS smartwatch client with rotary touch UI, 76dp dual floating orbs, Bézier sparkline, complications, tiles, legal disclaimers, local wipe, and passive legal footer.
- `:app-auto`: Android Auto Car App Library dashboard with glanceable non-distracting UI, passive secondary disclaimer row, and restrictive network security.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | MDR/FDA Medical Disclaimer (Mobile) | Modal dialog in Settings with passive secondary display notice and insulin dosing prohibition | M3 | ORIGINAL_REQUEST §R1 |
| 2 | Trademark & Non-Affiliation Notice (Mobile) | Modal dialog stating Abbott / FreeStyle Libre nominative fair use & non-affiliation | M3 | ORIGINAL_REQUEST §R1 |
| 3 | GDPR Art. 9 Health Data Privacy (Mobile) | Modal notice explaining local-first Keystore encryption and health data rights | M3 | ORIGINAL_REQUEST §R1 |
| 4 | GDPR Art. 20 CSV Export (Mobile) | Export structured CSV via FileProvider with timestamp, glucose, trend, clinical status | M3 | ORIGINAL_REQUEST §R2 |
| 5 | GDPR Art. 17 Total Local Purge (Mobile) | Destructive purge modal wiping credentials, history file, and cache | M3 | ORIGINAL_REQUEST §R2 |
| 6 | Passive Secondary Display Footer (Mobile) | Permanent legal footer on dashboard and settings views | M3 | ORIGINAL_REQUEST §R1 |
| 7 | Medical & Trademark Disclaimers (Wear OS) | Scrollable legal dialogs in WearSettingsScreen adapted for circular touch watch screens | M1 | ORIGINAL_REQUEST §R1 |
| 8 | GDPR Art. 17 Local Data Purge (Wear OS) | Destructive confirmation dialog and local wipe invoking `purgeAllLocalData()` | M1 | ORIGINAL_REQUEST §R1, §R2 |
| 9 | Passive Secondary Display Footer (Wear OS) | Regulatory footer in WearDashboardScreen and WearSettingsScreen | M1 | ORIGINAL_REQUEST §R1 |
| 10 | Passive Legal Disclaimer Row (Android Auto) | 4th row in PaneTemplate with passive secondary non-medical warning | M2 | ORIGINAL_REQUEST §R1 |
| 11 | Network Security Config (Android Auto) | `network_security_config.xml` restricting cleartext traffic matching mobile/wear | M2 | ORIGINAL_REQUEST §Acceptance Criteria |
| 12 | Keystore AES-256-GCM Encryption (Core) | Transparent hardware-backed encryption for JWT tokens, user IDs, and 90-day history file | M3 | ORIGINAL_REQUEST §R2 |
| 13 | Local-First Architecture Guarantee | Direct official API communication with zero intermediary cloud servers | M3 | ORIGINAL_REQUEST §R2 |
| 14 | Clinical Design Invariants (All Platforms) | OLED #000000, clinical ranges (#4ADE80, #F87171, #EF4444, #FBBF24, #FB923C), 24h format, trend glyphs | M1, M2, M3 | ORIGINAL_REQUEST §R3 |
| 15 | Exemption MDDS Guarantee | Complete absence of insulin dosing or bolus calculation algorithms | M1, M2, M3 | ORIGINAL_REQUEST §Acceptance Criteria |
| 16 | Manifest `android:allowBackup="false"` | Enforced across all module manifests to prevent cleartext backup extraction | M1, M2, M3 | ORIGINAL_REQUEST §Acceptance Criteria |
| 17 | E2E Opaque-Box Test Suite (Tiers 1-4) | Comprehensive requirement-driven test suite with >=11*N test cases | M4 | Project Architecture |
| 18 | Final E2E Pass, Adversarial Hardening (Tier 5) & Global Build | 100% test pass, adversarial test coverage, clean `./gradlew assembleDebug` exit code 0 | M5 | ORIGINAL_REQUEST §Acceptance Criteria |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Wear OS Legal Parity & Local Purge (`:app-wear`) | Add legal disclaimers dialogs/screens, local data purge with confirmation, and passive footer in `:app-wear` | none | DONE |
| 2 | Android Auto Legal Disclaimer & Network Security (`:app-auto`) | Add 4th row passive disclaimer in `GlucoseDashboardCarScreen.kt`, add `network_security_config.xml` & manifest config | none | DONE |
| 3 | Core & Mobile Verification & Shared Contracts (`:core:*`, `:app-mobile`) | Verify Keystore encryption, CSV exporter, purge logic, and mobile UI contracts | none | DONE |
| 4 | E2E Testing Track (`TEST_INFRA.md` & `TEST_READY.md`) | Build requirement-driven opaque-box test suite across Tiers 1-4 covering all 18 features | none (parallel) | DONE |
| 5 | Final E2E Test Pass, Adversarial Hardening & Build Verification | Pass 100% E2E tests (Tiers 1-4), execute Tier 5 adversarial hardening, verify clean `./gradlew assembleDebug` (exit code 0) | M1, M2, M3, M4 | DONE |

## Interface Contracts

### Legal & Regulatory String Constants
- **Medical Badge**: `"MDR UE 2017/745 / FDA MDDS"`
- **Medical Content**: Explicit notice of passive secondary convenience viewer, strict prohibition of insulin bolus calculation or therapeutic adjustment, mandatory blood glucose verification on symptom mismatch.
- **Trademark Notice**: Explicit fair use nominative statement that FreeStyle, Libre, LibreLink, OpenGluco are registered trademarks of Abbott Laboratories / Abbott Diabetes Care Inc., and this app is an independent client not affiliated with Abbott.
- **GDPR Art. 9**: Health data local-first treatment notice.
- **Passive Footer**: `"Visualizador secundario pasivo. No es un dispositivo médico y no sustituye al lector oficial ni a decisiones clínicas profesionales."`

### Cryptographic Contract (`KeystoreCryptoHelper`)
- Alias: `opengluco_master_keystore_key_v1`
- Cipher: `AES/GCM/NoPadding` (256-bit key, 12-byte IV, 128-bit tag)
- Format: `"ENC:" + Base64(IV + Ciphertext + Tag)`

### Data Portability CSV Contract (`HealthDataExporter`)
- Format: `Timestamp,Glucosa (<unit>),Tendencia,Estado Clinico\n`
- Encoding: UTF-8, LF line endings.

## Code Layout
- `core/model/src/main/java/com/example/opengluco/core/model/` — Clinical domain models & tokens
- `core/data/src/main/java/com/example/opengluco/core/data/` — Keystore encryption, DataStore repo, CSV exporter
- `core/network/src/main/java/com/example/opengluco/core/network/` — OpenGluco API client
- `app-mobile/src/main/java/com/example/opengluco/mobile/` — Android mobile UI, components, settings
- `app-wear/src/main/java/com/example/opengluco/wear/` — Wear OS UI, settings, tiles, complications
- `app-auto/src/main/java/com/example/opengluco/auto/` — Android Auto Car App Service & screens
