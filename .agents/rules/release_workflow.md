# Protocolo de Publicación y Actualizaciones OTA (Bajo Demanda)

Este documento es una guía de procedimiento estricta para el Asistente de IA (Antigravity). **Solo debe ejecutarse cuando el usuario lo solicite explícitamente** (por ejemplo: *"publica una nueva versión"*, *"crea la release 1.1.0"*, *"prepara la actualización"*).

---

## Principios Obligatorios:
- **Ejecución Bajo Demanda Exclusiva:** No disparar compilaciones ni publicaciones automáticas en segundo plano sin orden del usuario.
- **Coste Cero:** No utilizar GitHub Actions ni servicios de pago. Toda la compilación se realiza localmente en el entorno del proyecto.
- **Invariantes del Proyecto:** Conservar formato 24h, tokens clínicos, cero emojis y blindaje legal MDDS.

---

## Procedimiento Paso a Paso:

### 1. Confirmación de Versión y Changelog
- Determinar el nuevo `versionName` (ejemplo: si el actual es `1.0.0`, el siguiente será `1.1.0` o `1.0.1` según el alcance).
- Redactar un resumen claro y en español de los cambios y mejoras clínicas introducidas.

### 2. Sincronización de Versiones en Gradle
- Actualizar `versionName` y aumentar `versionCode` en:
  - `app-mobile/build.gradle.kts`
  - `app-wear/build.gradle.kts`
  - `app-auto/build.gradle.kts`

### 3. Validación y Pruebas Unitarias
- Ejecutar la suite de pruebas:
  ```powershell
  .\gradlew testDebugUnitTest
  ```
- Verificar que el 100% de los tests pasen con éxito.

### 4. Compilación Local de APKs
- Compilar los paquetes de los 3 módulos:
  ```powershell
  .\gradlew assembleDebug
  ```

### 5. Empaquetado y Organización de Artefactos
- Crear el directorio `releases/vX.Y.Z/` y copiar los APKs con nombres estandarizados:
  - `app-mobile/build/outputs/apk/debug/app-mobile-debug.apk` -> `releases/vX.Y.Z/OpenGluco-Mobile-vX.Y.Z.apk`
  - `app-wear/build/outputs/apk/debug/app-wear-debug.apk` -> `releases/vX.Y.Z/OpenGluco-WearOS-vX.Y.Z.apk`
  - `app-auto/build/outputs/apk/debug/app-auto-debug.apk` -> `releases/vX.Y.Z/OpenGluco-AndroidAuto-vX.Y.Z.apk`

### 6. Versionado en Git
- Crear commit y etiqueta anotada en Git:
  ```powershell
  git add .
  git commit -m "Release vX.Y.Z - [Notas de la versión]"
  git tag -a "vX.Y.Z" -m "OpenGluco vX.Y.Z: [Notas de la versión]"
  ```

### 7. Publicación de la Release
- Publicar la release en GitHub con los APKs adjuntos mediante la API pública o proporcionar la URL directa con los archivos preparados en local.
- Sincronizar el grafo de conocimiento: `python -m graphify.cli update .`.
