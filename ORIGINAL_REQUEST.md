# Original User Request

## Initial Request — 2026-08-27T10:10:46Z

Garantizar la paridad de funcionalidades y la preparación legal completa para distribución pública en las tres aplicaciones del ecosistema OpenGluco (Android Móvil, Wear OS Galaxy Watch y Android Auto), incorporando términos legales obligatorios (descargo médico MDR/FDA, aviso de marcas y no afiliación con Abbott) y garantizando el pleno cumplimiento del Reglamento General de Protección de Datos (RGPD Art. 9, 17 y 20).

Working directory: C:\Users\RBX\Desktop\opengluco-ecosystem
Integrity mode: development

## Requirements

### R1. Paridad de Configuración Legal y Avisos Normativos en Todas las Vistas
- **Aplicación Móvil (app-mobile):** Mantener el menú de Ajustes completo con selector de tema OLED/Claro, selector de unidades mg/dL vs mmol/L, botón de vinculación QR, exportación CSV, borrado total de datos y diálogos modales de:
  - Descargo médico (MDR UE 2017/745 / FDA MDDS: uso informativo secundario, prohibición de dosificación de insulina).
  - Aviso de marcas de terceros y no afiliación con Abbott Laboratories / FreeStyle Libre.
  - Aviso de tratamiento y privacidad de datos de salud (RGPD Art. 9).
- **Aplicación para Reloj (app-wear):** Incorporar en WearSettingsScreen.kt el acceso a la lectura de descargos legales esenciales (Aviso Médico y Marcas/No Afiliación) y opción de borrado local de datos, adaptados para la interacción táctil en pantallas circulares de reloj.
- **Android Auto (app-auto):** Añadir en la pantalla del vehículo (GlucoseDashboardCarScreen.kt / Template) la fila de información legal y descargo de responsabilidad pasivo, garantizando legibilidad segura sin distracciones al volante.

### R2. Privacidad y Gestión de Datos de Salud (RGPD Art. 9, 17 y 20)
- Garantizar que las tres plataformas almacenen las credenciales y la telemetría histórica exclusivamente en almacenamiento local seguro cifrado con **Android Keystore (AES-256-GCM)**, con cero intermediarios en la nube (*local-first*).
- Proveer en las interfaces aplicables las opciones de:
  - **Portabilidad (Art. 20 RGPD):** Exportación estructurada a formato CSV.
  - **Derecho al Olvido (Art. 17 RGPD):** Borrado total e irreversible del historial y credenciales locales.

### R3. Invariantes de Interfaz y Sistema de Diseño Clínico
- Mantener en todas las vistas los tokens oficiales de diseño:
  - Fondo negro puro #000000 en pantallas OLED.
  - Estados de glucosa: #4ADE80 (en rango), #F87171 (bajo), #EF4444 (urgente bajo), #FBBF24 (alto), #FB923C (muy alto).
  - Formato de horas en 24 horas (HH:mm).
  - Símbolos de tendencia ASCII/vectoriales (→, ↑, ↑↑, ↓, ↓↓).

## Acceptance Criteria

### Paridad de Interfaces y Configuración Legal
- [ ] La pantalla de ajustes de app-mobile, app-wear y app-auto ofrece acceso a los descargos legales obligatorios (Descargo Médico y Aviso de Marcas).
- [ ] Todas las vistas exhiben de forma permanente o accesible el pie de página legal: *"Visualizador secundario pasivo. No es un dispositivo médico y no sustituye al lector oficial ni a decisiones clínicas profesionales."*
- [ ] Ninguna de las aplicaciones contiene calculadoras de bolos ni herramientas que violen la exención MDDS.

### Privacidad y Control de Datos
- [ ] El almacenamiento de tokens JWT y datos de glucosa en reposo está cifrado mediante Android Keystore.
- [ ] La función de exportación CSV y purga total de datos locales funciona correctamente.
- [ ] Los manifiestos de los tres módulos tienen android:allowBackup="false" y configuración de seguridad de red restrictiva.

### Verificación Técnica y Compilación
- [ ] ./gradlew assembleDebug compila limpiamente con código 0 en todos los módulos (:app-mobile, :app-wear, :app-auto, :core:model, :core:network, :core:data).
