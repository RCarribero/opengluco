const fs = require('fs');
const path = require('path');

const adr1 = `# [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
El ecosistema OpenGluco proporciona monitorizacion continua de glucosa en tiempo real a traves de tres factores de forma distintos: telefonos moviles (\`app-mobile\`), relojes inteligentes Wear OS (\`app-wear\`) y pantallas de vehiculos (\`app-auto\`). Se requiere compartir la logica de dominio, modelos de telemetria, conexion de red con LibreView (Abbott) y persistencia sin duplicar codigo ni acoplar las interfaces de usuario entre si.

---

## 2. Factores Decisivos (Decision Drivers)
- Reutilizacion de codigo de calculo clinico y conexion API entre Mobile, Wear OS y Android Auto.
- Tiempos de compilacion incrementales rapidos e independencia de ciclo de vida en cada factor de forma.
- Aislamiento absoluto del modelo de dominio frente a frameworks de UI (Compose, Car App Library, Glance).
- Preparacion para potencial soporte KMP (Kotlin Multiplatform).

---

## 3. Opciones Consideradas
1. **Opcion A: Monorepo Multi-Modulo Gradle en Capas (\`:core:model\`, \`:core:network\`, \`:core:data\`)**
   - *Ventajas:* Desacoplamiento limpio, flujo unidireccional de dependencias, compilacion incremental optima.
   - *Desventajas:* Configuracion inicial de modulos y publicacion de dependencias locales.
2. **Opcion B: Monolito de aplicacion unica con subpaquetes internos**
   - *Ventajas:* Menos configuracion de Gradle.
   - *Desventajas:* Alto riesgo de acoplamiento circular y compilaciones completas lentas.

---

## 4. Decision Elegida
Se elige la **Opcion A: Monorepo Multi-Modulo Gradle en Capas**. El nucleo se divide en tres modulos compartidos:
1. \`:core:model\`: Modelos puros Kotlin inmutables, serializables (\`@Serializable\`) y algoritmos matematicos/clinicos (\`CgmCurveSmoother\`).
2. \`:core:network\`: Cliente Retrofit/OkHttp, interceptores y DTOs de red.
3. \`:core:data\`: Repositorios, persistencia DataStore, evaluadores de alarma y calculadores clinicos.

Las aplicaciones (\`app-mobile\`, \`app-wear\`, \`app-auto\`) consumen \`:core:data\` manteniendo su capa de presentacion y servicios estrictamente desacoplados.

---

## 5. Consecuencias y Compromisos (Trade-offs)

### Consecuencias Positivas:
- Cero duplicacion de algoritmos clinicos y calculos de AGP/TIR.
- Las pruebas unitarias de \`:core:model\` y \`:core:data\` se ejecutan en milisegundos en la JVM sin emulador.
- Cada modulo de aplicacion (\`app-mobile\`, \`app-wear\`, \`app-auto\`) evoluciona de manera independiente.

### Consecuencias Negativas / Riesgos Asumidos:
- Requiere mantener la consistencia de dependencias y versiones de Kotlin/Android Gradle Plugin en el root \`settings.gradle.kts\` y \`build.gradle.kts\`.

---

## 6. Reglas de Validacion y Cumplimiento (Enforcement)
- Registrado en \`AGENTS.md\` y \`settings.gradle.kts\`.
- Ningun modulo \`:core\` puede depender de \`:app-mobile\`, \`:app-wear\` o \`:app-auto\`.
- \`:core:model\` no debe incluir dependencias del framework Android UI.
`;

const adr2 = `# [ADR-0002] Persistencia Local Acumulativa de 90 Dias con DataStore y Deduplicacion

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
La API de LibreLinkUp proporciona lecturas historicas inmediatas limitadas en cada sondeo. Para generar metricas clinicas estandarizadas (AGP, GMI, Time in Range a 7d, 30d y 90d) y permitir auditorias clinicas completas en modo offline, se requiere un mecanismo de almacenamiento local ligero, no bloqueante y acumulativo.

---

## 2. Factores Decisivos (Decision Drivers)
- Persistencia asincrona no bloqueante con soporte reactivo (\`Flow<T>\`).
- Deduplicacion estricta por marca temporal (\`epochMs\`) en cada insercion.
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
Se adopta la **Opcion A: Jetpack DataStore Preferences** complementada con \`ClinicalReportsCalculator\` y \`KeystoreCryptoHelper\`. La deduplicacion se realiza indexando las lecturas por \`epochMs\` y purgando mediciones con mas de 90 dias de antiguedad en cada sincronizacion.

---

## 5. Consecuencias y Compromisos (Trade-offs)
- Garantiza disponibilidad instantanea de graficas historicas incluso sin conexion a internet.
- Los calculos de TIR y GMI se ejecutan en background sin bloquear la interfaz de usuario.
`;

const adr3 = `# [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
Los usuarios de relojes inteligentes Wear OS requieren recibir telemetria y alertas clinicas en tiempo real tanto en relojes conectados mediante Google Play Services como en dispositivos independientes o sin soporte de los servicios de Google.

---

## 2. Factores Decisivos (Decision Drivers)
- Maxima compatibilidad entre marcas de relojes Wear OS (Galaxy Watch, Pixel Watch, TicWatch).
- Transmision de tokens de sesion y configuraciones de alarma de forma bidireccional.
- Consumo energetico ultra-bajo en Wear OS con caida a modo de suspension (Doze mode friendly).

---

## 3. Decision Elegida
Implementar un canal de enlace hibrido:
1. **Canal Primario:** Google Play Services Wearable DataLayer (\`MessageClient\` y \`DataClient\`) para sincronizacion instantanea en segundo plano.
2. **Canal Secundario / Fallback:** Servidor socket \`WearBluetoothRfcommService\` para conexion directa Bluetooth RFCOMM serie estandar.
`;

const adr4 = `# [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
OpenGluco es un software de salud de monitorizacion continua de glucosa para pacientes con diabetes y cuidadores. Para garantizar la seguridad clinica del paciente, el rigor medico y el estricto cumplimiento regulatorio (MDR UE 2017/745 y FDA MDDS):
- No se deben proporcionar sugerencias de dosificacion de insulina ni calculo de bolos.
- La interfaz debe mantener sobriedad clinica sin ambiguedades visuales ni emojis informales.
- Toda referencia a LibreLink, LibreLinkUp, LibreView o FreeStyle debe atribuirse a Abbott Laboratories bajo uso legitimo nominativo.

---

## 2. Reglas Deterministas Forzadas
1. **Tokens Clinicos Estandar:** En rango (\`#4ADE80\`), Bajo (\`#F87171\`), Urgente Bajo (\`#EF4444\`), Alto (\`#FBBF24\`), Muy Alto (\`#FB923C\`).
2. **Horas en Formato 24h:** Siempre formato militar internacional \`HH:mm\`.
3. **Flechas de Tendencia:** Exclusivamente caracteres vectoriales limpios (\`→\`, \`↑\`, \`↓\`, \`↗\`, \`↘\`) o texto (\`->\`, \`^\`, \`v\`).
4. **Cero Emojis:** Prohibicion estricta en codigo fuente, UI, strings.xml, logs y documentacion, auditada en pruebas unitarias automatizadas.
`;

const docsDir = path.join(process.cwd(), 'docs', 'adr');
fs.mkdirSync(docsDir, { recursive: true });
fs.writeFileSync(path.join(docsDir, '0001-arquitectura-modular-compartida.md'), adr1, 'utf8');
fs.writeFileSync(path.join(docsDir, '0002-persistencia-local-90-dias-datastore.md'), adr2, 'utf8');
fs.writeFileSync(path.join(docsDir, '0003-sincronizacion-dual-bluetooth-datalayer.md'), adr3, 'utf8');
fs.writeFileSync(path.join(docsDir, '0004-blindaje-legal-y-reglas-clinicas-sin-emojis.md'), adr4, 'utf8');

console.log('ADRs successfully written to docs/adr/');
