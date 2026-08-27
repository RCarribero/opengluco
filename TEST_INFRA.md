# Test Infrastructure & Specification Matrix: OpenGluco Ecosystem

**Project**: OpenGluco Ecosystem (Android Mobile, Wear OS Galaxy Watch, Android Auto)  
**Document Version**: 1.0.0  
**Verification Date**: 2026-08-27  

---

## 1. Test Architecture Overview

The OpenGluco Ecosystem test infrastructure is designed for high-assurance medical telemetry and regulatory compliance. It follows a multi-tier opaque-box and contract-driven verification model across all 6 submodules:
- `:core:model` (Clinical models, units, trends, design tokens, serialization)
- `:core:network` (Official REST API communication, interceptors, auth headers, TLS constraints)
- `:core:data` (Keystore AES-256-GCM authenticated crypto, DataStore persistence, GDPR Art. 17 purge, Art. 20 CSV export, QR pairing)
- `:app-mobile` (Mobile settings, legal dialogs, CSV sharing, passive legal footers, manifest backup policy)
- `:app-wear` (Wear OS smartwatch rotary settings, legal notices, local purge confirmation, glanceable UI tokens)
- `:app-auto` (Android Auto glanceable dashboard, 4th-row non-distracting passive disclaimer, network security)

---

## 2. Test Tiers & Methodology

### Tier 1: Feature Coverage (>=5 Test Cases per Feature across all 18 Features)
Every feature specified in `PROJECT.md` is tested with at least 5 targeted test cases exercising primary behaviors, contracts, and interfaces.

### Tier 2: Boundary & Corner Cases (>=5 Test Cases per Feature)
Rigorous stress-testing of:
- Empty states (empty history, null timestamps, missing sensor data, empty credentials).
- Extreme numerical thresholds (hypoglycemia `<= 55`, low `56..69`, normal `70..180`, high `181..249`, very high `>= 250`).
- Corrupt payloads (corrupt Base64 in Keystore crypto, invalid GCM authentication tags, malformed JSON).
- International formatting & unit conversion (`mg/dL` integer vs `mmol/L` US decimal with 18.0182 divisor).
- Multi-format timezone normalization (ISO-8601 UTC with 'Z', 12h AM/PM, 24h format).

### Tier 3: Cross-Feature Combinations (Pairwise & Integration Workflows)
- QR Session Pairing + Keystore AES-256-GCM Storage + CSV Export Portability.
- Unit Conversion (mg/dL <-> mmol/L) + Sparkline Scaling + CSV Header / Row Formatting.
- GDPR Art. 17 Atomic Purge + Session Token Invalidation + Disk File Removal + StateFlow Reset.
- Network Re-authentication (403 Terms Acceptance / Token Refresh) + Historical Cache Merging.

### Tier 4: Real-World Application Scenarios
- **Scenario A (Daily Diabetes Tracking)**: Continuous telemetry polling, deduplication of readings, 90-day retention pruning, clinical alert status calculation.
- **Scenario B (Automotive In-Car Glanceability)**: Distraction-free passive display, safe warning status text, passive non-medical footer row, TLS security enforcement.
- **Scenario C (Smartwatch Ambient & Offline Resilience)**: Local cached history playback when network drops, rotary settings legal compliance, zero bolus calculator invariant.

---

## 3. Detailed Feature-to-Test Mapping Matrix

| Feature ID | Feature Name | Primary Module | Target Test Suite | Test Count (Tiers 1-4) |
|---|---|---|---|:---:|
| **F01** | MDR/FDA Medical Disclaimer (Mobile) | `:app-mobile` | `MobileLegalComplianceTest` | 6 |
| **F02** | Trademark & Non-Affiliation Notice (Mobile) | `:app-mobile` | `MobileLegalComplianceTest` | 6 |
| **F03** | GDPR Art. 9 Health Data Privacy (Mobile) | `:app-mobile` | `MobileLegalComplianceTest` | 6 |
| **F04** | GDPR Art. 20 CSV Export (Mobile & Core) | `:core:data`, `:app-mobile` | `HealthDataExporterTest`, `MobileExportTest` | 8 |
| **F05** | GDPR Art. 17 Total Local Purge (Mobile & Core) | `:core:data`, `:app-mobile` | `UserPreferencesPurgeTest`, `MobilePurgeTest` | 8 |
| **F06** | Passive Secondary Display Footer (Mobile) | `:app-mobile` | `MobileLegalComplianceTest` | 6 |
| **F07** | Medical & Trademark Disclaimers (Wear OS) | `:app-wear` | `WearLegalComplianceTest` | 6 |
| **F08** | GDPR Art. 17 Local Data Purge (Wear OS) | `:app-wear`, `:core:data` | `WearLegalComplianceTest` | 6 |
| **F09** | Passive Secondary Display Footer (Wear OS) | `:app-wear` | `WearLegalComplianceTest` | 6 |
| **F10** | Passive Legal Disclaimer Row (Android Auto) | `:app-auto` | `AutoLegalComplianceTest` | 6 |
| **F11** | Network Security Config (Android Auto & All) | `:app-auto`, `:app-mobile`, `:app-wear` | `NetworkSecurityConfigTest` | 6 |
| **F12** | Keystore AES-256-GCM Encryption (Core) | `:core:data` | `KeystoreCryptoHelperTest` | 10 |
| **F13** | Local-First Architecture Guarantee | `:core:network`, `:core:data` | `LocalFirstArchitectureTest` | 8 |
| **F14** | Clinical Design Invariants (All Platforms) | `:core:model`, `:app-mobile`, `:app-wear` | `ClinicalDesignInvariantsTest` | 10 |
| **F15** | Exemption MDDS Guarantee (Zero Bolus Calc) | All Modules | `MddsRegulatoryExemptionTest` | 6 |
| **F16** | Manifest `android:allowBackup="false"` | `:app-mobile`, `:app-wear`, `:app-auto` | `ManifestSecurityPolicyTest` | 6 |
| **F17** | E2E Opaque-Box Test Suite (Tiers 1-4) | All Modules | Cross-Module Integration Harness | 12 |
| **F18** | Final E2E Pass & Global Build Verification | All Modules | `GlobalBuildVerificationTest` | 8 |

---

## 4. Test Execution & Verification

### Global Test Command
```bash
./gradlew test
```

### Module-Specific Unit Test Tasks
```bash
./gradlew :core:model:testDebugUnitTest
./gradlew :core:data:testDebugUnitTest
./gradlew :core:network:testDebugUnitTest
./gradlew :app-mobile:testDebugUnitTest
./gradlew :app-wear:testDebugUnitTest
./gradlew :app-auto:testDebugUnitTest
```

### Verification Criteria
- 100% test pass rate across all suites.
- Zero flaky or order-dependent test executions.
- Zero mock leaks or disk state leakage between test runs.
- Strict enforcement of medical disclaimers, trademark notices, and cryptographic integrity.
