# Reglas de Proyecto: OpenGluco Ecosystem

Guia maestra de desarrollo para el ecosistema OpenGluco (`app-mobile`, `app-wear`, `app-auto`, `core`).

## 1. Directrices de Interfaz y Tokens Clinicos
- **Fondo OLED:** `#000000` en Wear OS y modo oscuro de la app movil.
- **Tokens de Glucosa:**
  - En Rango (70 - 180 mg/dL): `#4ADE80` (PrimaryMint)
  - Hipoglucemia / Bajo (56 - 69 mg/dL): `#F87171` (LowCoral)
  - Urgente Bajo (<= 55 mg/dL): `#EF4444` (UrgentCrimson)
  - Hiperglucemia / Alto (181 - 249 mg/dL): `#FBBF24` (HighAmber)
  - Muy Alto (>= 250 mg/dL): `#FB923C` (TangerineWarning)
- **Horas:** Siempre en formato 24 horas (`HH:mm`).
- **Flechas:** Usar siempre simbolos de texto estandar (`->`, `^`, `v` o caracteres vectoriales limpios `→`, `↑`, `↓`, `↗`, `↘`).
- **Prohibicion de Emojis:** Esta estrictamente prohibido el uso de emojis en cualquier parte del codigo, interfaz, mensajes o documentacion.

## 2. App Movil (`app-mobile`)
- La barra superior (`TopAppBar`) solo debe incluir paciente, refresco y engranaje de Ajustes.
- Los botones "Vincular" y "Cerrar Sesion" residen exclusivamente dentro de `SettingsDialog`.
- La grafica cuenta con bolita interactiva (scrubber), linea guia vertical y pop-up flotante clinico con hora y valor.

## 3. App Reloj (`app-wear`)
- Esferas flotantes duales (`DualFloatingOrbs`) de `74.dp` con feedback tactil.
- Sparkline continua y badge de dias restantes de sensor (`Sensor: Xd`).
- Soporte para Tiles (`GlucoseTileService`), Complicaciones (`GlucoseComplicationService`) y sincronizacion Bluetooth por DataLayer.

## 4. Persistencia y Datos (`core:data`)
- Almacenamiento local persistente acumulativo de 90 dias en `UserPreferencesRepository`.
- Las metricas de periodos (`1d`, `7d`, `30d`, `90d`) se calculan sobre todo el historico guardado para reflejar picos reales.
- Sincronizacion continua de la ultima medicion en vivo con la grafica historica.

## 5. Invariantes de Telemetria y Formato
- Deduplicacion obligatoria por timestamp (`epochMs`) en cada insercion local.
- Sincronizacion exacta en 24h (`HH:mm`) entre la hora del sensor, la bolita (scrubber) y el badge de ultima actualizacion.

## 6. Mencion a Abbott Laboratories y Blindaje Legal
- **Uso Legítimo Nominativo:** Todas las referencias a marcas (*FreeStyle, Libre, LibreLink, LibreLinkUp, LibreView*) deben atribuirse formalmente como marcas registradas de **Abbott Laboratories / Abbott Diabetes Care Inc.**
- **No Afiliacion:** Debe declararse explícitamente la ausencia de afiliación, patrocinio o respaldo oficial de Abbott Laboratories.
- **Exención Médica (MDR/MDDS):** Prohibición absoluta de funciones de cálculo de bolos o dosificación de insulina.
- **Respaldo Legal:** Las comunicaciones con el backend de LibreView están respaldadas por el Art. 100.3 TRLPI (interoperabilidad) y el Reglamento UE 2023/2854 (Data Act).

## 7. Arquitectura y Grafo de Conocimiento (Graphify)
- **Consulta Obligatoria al Añadir Módulos o Funcionalidades:** Antes de incorporar nuevos módulos, pantallas, servicios o flujos en el ecosistema, se debe consultar primero el grafo de conocimiento (`graphify query "<feature/modulo>"`, `graphify explain "<concepto>"` o `graphify path "<origen>" "<destino>"`).
- **Objetivo:** Identificar dependencias existentes, modelos y repositorios compartidos (`core:model`, `core:data`, `core:network`), y asegurar la consistencia entre `app-mobile`, `app-wear` y `app-auto` sin introducir duplicidades ni código acoplado.
- **Actualización Continua:** Tras finalizar modificaciones de código, ejecutar `graphify update .` para mantener el grafo sincronizado.

