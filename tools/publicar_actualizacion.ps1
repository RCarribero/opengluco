# Asistente Guiado de Publicacion de Actualizaciones OpenGluco (100% Local - Coste Cero)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "    OPENGLUCO - ASISTENTE DE ACTUALIZACIONES (100% LOCAL)   " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Compilacion en tu propio ordenador (Sin costes ni Actions)" -ForegroundColor Gray
Write-Host ""

# 1. Obtener la version actual del proyecto
$buildGradle = Get-Content "app-mobile\build.gradle.kts" -Raw
$currentVersion = "1.0.0"
if ($buildGradle -match 'versionName\s*=\s*"([^"]+)"') {
    $currentVersion = $Matches[1]
}

Write-Host "Version actual del proyecto: " -NoNewline
Write-Host "v$currentVersion" -ForegroundColor Yellow
Write-Host ""

# 2. Solicitar el nuevo numero de version
$parts = $currentVersion.Split('.')
$suggestedVersion = if ($parts.Length -eq 3) {
    "$($parts[0]).$($parts[1]).$([int]$parts[2] + 1)"
} else {
    "1.1.0"
}

$versionInput = Read-Host "Introduce el nuevo numero de version (por defecto: $suggestedVersion)"
if ([string]::IsNullOrWhiteSpace($versionInput)) {
    $versionInput = $suggestedVersion
}
$versionTag = if ($versionInput.StartsWith("v")) { $versionInput } else { "v$versionInput" }
$versionClean = $versionTag.TrimStart("v")

# 3. Solicitar las novedades o changelog
Write-Host ""
Write-Host "Introduce las novedades o cambios de esta version:" -ForegroundColor Cyan
$notesInput = Read-Host "Notas (ej: Explicaciones clinicas y mejoras de estabilidad)"
if ([string]::IsNullOrWhiteSpace($notesInput)) {
    $notesInput = "Actualizacion con mejoras clinicas, estabilidad y optimizacion general."
}

# 4. Actualizar versionName en los build.gradle.kts de los 3 modulos
Write-Host ""
Write-Host "Actualizando archivos de configuracion a la version $versionClean..." -ForegroundColor Gray

(Get-Content "app-mobile\build.gradle.kts") -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$versionClean`"" | Set-Content "app-mobile\build.gradle.kts"
(Get-Content "app-wear\build.gradle.kts") -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$versionClean`"" | Set-Content "app-wear\build.gradle.kts"
(Get-Content "app-auto\build.gradle.kts") -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$versionClean`"" | Set-Content "app-auto\build.gradle.kts"

Write-Host "Version actualizada en app-mobile, app-wear y app-auto." -ForegroundColor Green
Write-Host ""

# 5. Compilar los APKs en local (coste 0€)
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " COMPILANDO APKS Y VERIFICANDO TESTS EN TU ORDENADOR...     " -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan

.\gradlew assembleDebug testDebugUnitTest

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error durante la compilacion. Abortando." -ForegroundColor Red
    exit 1
}

# 6. Crear carpeta de salida con nombres claros
$outputDir = "$PSScriptRoot\..\releases\$versionTag"
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
}

$apkMobile = "$outputDir\OpenGluco-Mobile-$versionTag.apk"
$apkWear = "$outputDir\OpenGluco-WearOS-$versionTag.apk"
$apkAuto = "$outputDir\OpenGluco-AndroidAuto-$versionTag.apk"

Copy-Item "app-mobile\build\outputs\apk\debug\app-mobile-debug.apk" $apkMobile -Force
Copy-Item "app-wear\build\outputs\apk\debug\app-wear-debug.apk" $apkWear -Force
Copy-Item "app-auto\build\outputs\apk\debug\app-auto-debug.apk" $apkAuto -Force

Write-Host ""
Write-Host "APKs generados con exito en:" -ForegroundColor Green
Write-Host " -> $apkMobile" -ForegroundColor White
Write-Host " -> $apkWear" -ForegroundColor White
Write-Host " -> $apkAuto" -ForegroundColor White
Write-Host ""

# 7. Git commit y tag local
Write-Host "Creando commit y etiqueta de Git ($versionTag)..." -ForegroundColor Cyan
git add .
git commit -m "Release $versionTag - $notesInput"
git tag -a "$versionTag" -m "OpenGluco $versionTag: $notesInput"

Write-Host "¿Deseas subir los cambios y la etiqueta a GitHub ahora? (s/n)" -ForegroundColor Cyan
$pushConfirm = Read-Host
if ($pushConfirm -eq "s" -or $pushConfirm -eq "S" -or [string]::IsNullOrWhiteSpace($pushConfirm)) {
    git push origin main --tags
    Write-Host "Etiqueta $versionTag subida a GitHub." -ForegroundColor Green
}

# 8. Opciones de publicacion de Release (100% gratis)
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "                  TODO LISTO PARA PUBLICAR                  " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "Para que los usuarios puedan actualizar automáticamente:" -ForegroundColor White
Write-Host ""
Write-Host "1) Abrir la carpeta de los APKs y la web de GitHub para arrastrar los archivos." -ForegroundColor Cyan
Write-Host "2) Finalizar" -ForegroundColor Gray
Write-Host ""
$finalOpt = Read-Host "Elige una opcion (1 o 2)"

if ($finalOpt -eq "1" -or [string]::IsNullOrWhiteSpace($finalOpt)) {
    # Abrir carpeta en explorador de Windows
    explorer.exe (Resolve-Path $outputDir).Path
    # Abrir web de nueva release en navegador
    $releaseUrl = "https://github.com/RCarribero/opengluco/releases/new?tag=$versionTag&title=OpenGluco+$versionTag"
    Start-Process $releaseUrl
    Write-Host ""
    Write-Host "Se ha abierto el navegador y la carpeta de tus APKs." -ForegroundColor Yellow
    Write-Host "Solo tienes que arrastrar el archivo 'OpenGluco-Mobile-$versionTag.apk' a la web y pulsar 'Publish release'." -ForegroundColor White
}

Write-Host ""
Write-Host "Proceso completado." -ForegroundColor Green
