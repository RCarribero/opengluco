# PROMPT PARA RETOMAR EL PROYECTO CON AGENTES (ANTIGRAVITY / TEAMWORK)

Copia y pega el siguiente bloque completo en Antigravity en tu nuevo ordenador para reanudar el equipo multi-agente en el punto exacto:

```text
/teamwork-preview Retomar la orquestacion del ecosistema LibreLinkUp (Hito 3 y Hito 4).

Working directory: .
Integrity mode: development

## Contexto y Estado del Proyecto
- Hito 1 (Core Unification) y Hito 2 (Mobile App Reactivity) estan 100% completados y testeados en Git.
- Todos los modelos base y calculadoras clinicas estan en :core:model y :core:data.
- Fuente unica de la verdad: UserPreferencesRepository.userSettingsFlow (targetLow, targetHigh, unit mg/dL vs mmol/L).

## Objetivos Inmediatos (Hito 3 y Hito 4)

### R1. Unificacion de Reactividad en Wear OS (app-wear)
- Asegurar que WearDashboardScreen, DualFloatingOrbs, WearGlucoseGauge, Tiles (GlucoseTileService) y Complicaciones de esfera (GlucoseComplicationService) consuman dinamicamente el rango objetivo (targetLow / targetHigh) y la unidad seleccionada (mg/dL vs mmol/L) desde UserPreferencesRepository.
- Eliminar cualquier valor hardcodeado o estatico fuera del fallback oficial 70-180 mg/dL.
- Purgar codigo muerto o funciones redundantes en el modulo Wear OS.

### R2. Unificacion de Reactividad en Android Auto (app-auto)
- Conectar GlucoseDashboardCarScreen y PaneTemplate con UserPreferencesRepository para reflejar los rangos clinicos y unidades reales en la pantalla del vehiculo.
- Mantener la fila de aviso legal y descargo de responsabilidad pasivo.

### R3. Preservacion Estricta de Invariantes del Proyecto
- CERO EMOJIS: Prohibido cualquier emoji en codigo Kotlin, XML, strings, comentarios o logs.
- Paleta Clinica Oficial: Fondo negro puro OLED #000000, #4ADE80 (en rango), #F87171 (bajo), #EF4444 (urgente bajo), #FBBF24 (alto), #FB923C (muy alto).
- Cifrado Local Keystore: Almacenamiento seguro AES-256-GCM (local-first, cero servidores externos).

## Acceptance Criteria
- [ ] `./gradlew testDebugUnitTest` compila y pasa el 100% de las pruebas en todos los modulos (:app-mobile, :app-wear, :app-auto, :core:data, :core:model, :core:network).
- [ ] `./gradlew assembleDebug` genera los APKs de depuracion con codigo 0.
- [ ] Se verifica con script de auditoria CERO emojis en todo el repositorio.
```

---

## Instrucciones Rapidas para el Nuevo Ordenador

1. **Clonar el proyecto:**
   ```bash
   git clone https://github.com/RCarribero/opengluco.git librelinkup-ecosystem
   cd librelinkup-ecosystem
   ```
2. **Abrir la carpeta en Antigravity.**
3. **Pegar el prompt anterior en el chat de Antigravity.**
