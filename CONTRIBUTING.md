# Directrices de Contribucion: OpenGluco Ecosystem

Agradecemos el interes en contribuir a OpenGluco Ecosystem. Para mantener la coherencia arquitectonica, la seguridad del paciente y el cumplimiento normativo del proyecto, solicitamos seguir estas directrices.

---

## 1. Reglas Innegociables del Proyecto

### A. Prohibicion Estricta de Emojis
- Esta terminantemente prohibido incluir caracteres emoji en el codigo fuente, comentarios, nombres de variables, cadenas de recursos (`strings.xml`), mensajes de log, commits o documentacion.
- Para indicadores de tendencia o estado, utilice exclusivamente simbolos vectoriales tipograficos limpios (`→`, `↑`, `↓`, `↗`, `↘`) o equivalentes ASCII (`->`, `^`, `v`).

### B. Invariante de Seguridad Clinica (MDDS)
- El proyecto es estrictamente un **visualizador pasivo secundario de telemetria**.
- No se aceptaran contribuciones que incorporen calculadoras de dosis de insulina, estimaciones de bolos, recomendaciones terapeuticas directas o automatizaciones de infusion.

### C. Tokens de Diseno y Formato
- **Fondo OLED:** `#000000` en todas las pantallas oscuras (Wear OS y modo oscuro movil).
- **Formato Horario:** Estrictamente 24 horas (`HH:mm`).
- **Tokens Clinicos:**
  - En Rango (70 - 180 mg/dL): `#4ADE80` (PrimaryMint)
  - Hipoglucemia / Bajo (56 - 69 mg/dL): `#F87171` (LowCoral)
  - Urgente Bajo (<= 55 mg/dL): `#EF4444` (UrgentCrimson)
  - Hiperglucemia / Alto (181 - 249 mg/dL): `#FBBF24` (HighAmber)
  - Muy Alto (>= 250 mg/dL): `#FB923C` (TangerineWarning)

### D. Mencion a Abbott Laboratories y Marcas
- Toda referencia a sensores o servicios de telemetria debe senalar a **Abbott Laboratories** (*FreeStyle, Libre, LibreLink, LibreLinkUp, LibreView*) bajo uso legitimo nominativo, manteniendo el descargo de no afiliacion.

---

## 2. Flujo de Trabajo para Contribuciones

1. **Fork del Repositorio:** Cree su propio fork en GitHub.
2. **Creacion de Rama:** Utilice una nomenclatura descriptiva:
   - `feature/nombre-funcionalidad`
   - `fix/descripcion-error`
   - `refactor/area-modificada`
3. **Desarrollo y Pruebas:**
   - Asegurese de que todo nuevo codigo este cubierto por pruebas unitarias.
   - Ejecute la suite completa de pruebas antes de enviar el cambio:
     ```bash
     ./gradlew testDebugUnitTest
     ```
4. **Envio de Pull Request:**
   - Describa detalladamente el proposito del cambio, modulo afectado y verificaciones realizadas.

---

## 3. Estructura de Modulos

- `:core:model`: Modelos puros serializables con `@Serializable` y `@SerialName`.
- `:core:network`: Cliente HTTP e interceptores.
- `:core:data`: Persistencia DataStore/JSON cifrado con Android Keystore (`AES-256-GCM`), evaluacion de alarmas y utilidades clinicas.
- `:app-mobile`: UI para Smartphones con Jetpack Compose y Material 3.
- `:app-wear`: UI para Wear OS con Wear Compose M3, Tiles y Complications.
- `:app-auto`: UI para Android Auto con Car App Library (categoria IoT).

---

## 4. Convenciones de Codigo

- **Kotlin Official Style Guide** obligatorio.
- Inyeccion de dependencias manual por constructor (sin frameworks de reflexion en tiempo de ejecucion).
- Manejo de corrutinas con Dispatchers explicitos (`Dispatchers.IO` para I/O y persistencia).
