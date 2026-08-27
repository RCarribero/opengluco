# Reglas de Interfaz y Estandares Clinicos: OpenGluco Ecosystem

Este documento establece las pautas maestras y restricciones obligatorias para el desarrollo en todos los modulos (`app-mobile`, `app-wear`, `app-auto`, `core`).

---

## 1. Paleta de Colores y Tokens Clinicos Oficiales

| Estado Clinico | Rango de Glucosa | Codigo HEX | Nombre del Token |
| :--- | :--- | :--- | :--- |
| **En Rango (Normal)** | `70 - 180 mg/dL` | `#4ADE80` | `PrimaryMint` |
| **Bajo (Hipoglucemia)** | `56 - 69 mg/dL` | `#F87171` | `LowCoral` |
| **Urgente Bajo** | `<= 55 mg/dL` | `#EF4444` | `UrgentCrimson` |
| **Alto (Hiperglucemia)** | `181 - 249 mg/dL` | `#FBBF24` | `HighAmber` |
| **Muy Alto** | `>= 250 mg/dL` | `#FB923C` | `TangerineWarning` |
| **Acento Secundario** | Info / Enlaces / Sync | `#38BDF8` | `ArcticCyan` |

### Superficies OLED
- **Fondo Wear OS y Modo Oscuro Movil:** Negro puro `#000000` (ahorro de bateria AMOLED).
- **Superficie de Esferas / Orbs:** `#1E232D` con borde `#2D3748`.
- **Superficie de Tarjetas:** `#161A22` con borde `#2D3748`.

---

## 2. Directrices de Interfaz Movil (`app-mobile`)

1. **TopAppBar Limpia:**
   - La barra superior solo debe contener la informacion del paciente, el boton de refresco y el boton de engranaje (Ajustes).
   - Los botones de **"Vincular"** y **"Cerrar Sesion"** deben ubicarse exclusivamente dentro del dialogo de **Configuracion (`SettingsDialog`)**, nunca sueltos en la vista principal ni en botones flotantes inferiores.
2. **Grafica Continua con Scrubber:**
   - Debe incluir una bolita indicadora en la curva (por defecto en el punto mas reciente con halo verde menta `#4ADE80`).
   - Al tocar o arrastrar el dedo horizontalmente (`pointerInput` / `detectDragGestures`), debe trazar una linea vertical discontinua y desplegar un **pop-up flotante** con la hora exacta (`HH:mm`), el valor de glucosa y su etiqueta clinica.
3. **Formato de Horas:**
   - Siempre formatear horas en **formato 24h (`HH:mm`)** evitando desfases con la hora real de la medicion.
4. **Flechas de Tendencia:**
   - Usar caracteres ASCII o iconos vectoriales estandar (`->`, `^`, `v` o `→`, `↑`, `↓`, `↗`, `↘`).
5. **Prohibicion de Emojis:**
   - Queda estrictamente prohibido el uso de emojis en interfaces, textos, botones, avisos y documentacion.
6. **Autenticacion (Login):**
   - El campo de contrasena debe contar siempre con boton de alternancia para ver/ocultar contrasena (icono de ojo interactivo).

---

## 3. Directrices Wear OS (`app-wear`)

1. **Jerarquia Visual:**
   - Fila superior: **Esferas Flotantes Duales** (`DualFloatingOrbs`) de `74.dp` (Izquierda: Glucosa con arco clinico; Derecha: Tendencia y velocidad).
   - Fila inferior: **Sparkline continua** junto al badge compacto de **dias restantes del sensor** (`Sensor: Xd`).
2. **Interactividad Haptica:**
   - Toque en la esfera de glucosa: Abre dialogo modal con estadisticas del dia (Promedio, Minimo, Maximo, TIR %).
   - Toque en la esfera de tendencia: Muestra la velocidad estimada de cambio (ej. `+1.2 mg/dL/min`).
3. **Integracion con el Sistema:**
   - Mantener soporte para Complicaciones (`GlucoseComplicationService`), Mosaicos/Tiles (`GlucoseTileService`) y sincronizacion por Bluetooth / Wearable DataLayer (`/opengluco_auth_sync`).

---

## 4. Persistencia y Telemetria Historica (`core:data` & `core:model`)

1. **Ventana de Retencion de 90 Dias:**
   - Dado que la API de Abbott solo devuelve las ultimas 24h por peticion, `UserPreferencesRepository` debe persistir y acumular todas las lecturas recibidas durante los ultimos **90 dias**.
2. **Calculo de Metricas (`Dia`, `Semana`, `Mes`, `3 Meses`):**
   - El Maximo, Minimo, Media y Porcentaje en Rango (TIR %) deben calcularse sobre el conjunto total de datos acumulados localmente, garantizando el registro fidedigno de picos pasados.
3. **Sincronizacion en Tiempo Real:**
   - La ultima medicion en vivo debe unificarse automaticamente al final de los bloques historicos de la grafica.

---

## 5. Invariantes de Telemetria y Formateo

1. **Deduplicacion Estricta:**
   - Toda nueva lectura recibida por polling o refresco manual debe insertarse con deduplicacion por timestamp (`epochMs`), evitando lecturas duplicadas en la persistencia local.
2. **Coincidencia Exacta de Horas:**
   - La hora mostrada en la bolita (scrubber), en el pop-up flotante y en el badge de ultima actualizacion debe coincidir exactamente con el timestamp emitido por el sensor en formato 24h (`HH:mm`).
