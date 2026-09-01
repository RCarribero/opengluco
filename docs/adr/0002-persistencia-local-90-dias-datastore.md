# [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
La API de LibreLinkUp proporciona lecturas historicas inmediatas limitadas en cada sondeo. Para generar metricas clinicas estandarizadas (AGP, GMI, Time in Range a 7d, 30d y 90d) y permitir auditorias clinicas completas en modo offline, se requiere un mecanismo de almacenamiento local ligero, no bloqueante y acumulativo.

---

## 2. Factores Decisivos (Decision Drivers)
- Persistencia asincrona no bloqueante con soporte reactivo (`Flow<T>`).
- Deduplicacion estricta por marca temporal (`epochMs`) en cada insercion.
- Mantenimiento estricto del limite temporal movil de 90 dias para controlar la memoria.
- Seguridad en reposo de credenciales mediante Android KeyStore y cifrado AES-GCM 256.

---

## 3. Opciones Consideradas
1. **Opcion A: Jetpack DataStore Preferences con Serializacion JSON y KeyStore**
   - *Ventajas:* Asincrono, seguro ante corrupciones de escritura transaccionales, API reactiva nativa en Kotlin Coroutines.
   - *Desventajas:* Operaciones complejas de agregacion deben realizarse en memoria con calculadores especializados.
2. **Opcion B: Base de datos relacional SQLite / Room**
   - *Ventajas:* Consultas SQL directas indexadas.
   - *Desventajas:* Mayor huella en APK, migraciones de esquema manuales mas complejas para Wear OS.

---

## 4. Decision Elegida
Se adopta la **Opcion A: Jetpack DataStore Preferences** complementada con `ClinicalReportsCalculator` y `KeystoreCryptoHelper`. La deduplicacion se realiza indexando las lecturas por `epochMs` y purgando mediciones con mas de 90 dias de antiguedad en cada sincronizacion.

---

## 5. Consecuencias y Compromisos (Trade-offs)
- Garantiza disponibilidad instantanea de graficas historicas incluso sin conexion a internet.
- Los calculos de TIR y GMI se ejecutan en background sin bloquear la interfaz de usuario.
