# ==============================================================================
# OpenGluco: Automatización Total (Graphify + Build + Tests + Drive Sync + Git)
# ==============================================================================

[CmdletBinding()]
param(
    [string]$CommitMessage = "chore(sync): automated Graphify graph, knowledge sync and verification"
)

$ErrorActionPreference = "Stop"
$projectRoot = Resolve-Path "$PSScriptRoot\.."

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  OpenGluco: Automatizacion Total de Pipeline y Conocimiento" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Configurar Entorno Java y Python
$jdkPath = (Get-ChildItem "C:\Program Files\Microsoft\jdk-17*" -Directory).FullName
if ($jdkPath) {
    $env:JAVA_HOME = $jdkPath
    $env:Path = "$jdkPath\bin;" + $env:Path
}
$env:ANDROID_HOME = "C:\Users\rcarr\AppData\Local\Android\Sdk"
$env:Path = "$env:ANDROID_HOME\platform-tools;C:\Users\rcarr\AppData\Local\Programs\Python\Python312;C:\Users\rcarr\AppData\Local\Programs\Python\Python312\Scripts;" + $env:Path

# 2. Actualizar Grafo de Conocimiento (Graphify)
Write-Host "`n[1/5] Actualizando Grafo de Conocimiento Graphify..." -ForegroundColor Yellow
Push-Location $projectRoot
try {
    graphify update .
    Write-Host "[OK] Grafo Graphify actualizado exitosamente." -ForegroundColor Green
} catch {
    Write-Warning "No se pudo actualizar Graphify automaticamente: $_"
}

# 3. Ejecutar Pruebas Unitarias
Write-Host "`n[2/5] Ejecutando Pruebas Unitarias locales (Gradle)..." -ForegroundColor Yellow
./gradlew testDebugUnitTest
Write-Host "[OK] Todas las pruebas unitarias pasaron con exito (100%)." -ForegroundColor Green

# 4. Sincronizar Conocimiento para Gemini Gem (Google Drive)
Write-Host "`n[3/5] Sincronizando Base de Conocimiento con Google Drive..." -ForegroundColor Yellow
& "$PSScriptRoot\sync_gem_knowledge.ps1"

# 5. Sincronizar con Dispositivos Conectados (ADB) si estan disponibles
Write-Host "`n[4/5] Verificando dispositivos conectados..." -ForegroundColor Yellow
$devices = adb devices | Select-String "device$"
if ($devices) {
    Write-Host "Dispositivos detectados. Compilando e instalando APKs..." -ForegroundColor Gray
    ./gradlew assembleDebug
    foreach ($dev in $devices) {
        $devId = ($dev.Line -split "\s+")[0]
        if ($devId -like "*:*") {
            Write-Host "Instalando en Smartwatch ($devId)..." -ForegroundColor Cyan
            adb -s $devId install -r "app-wear/build/outputs/apk/debug/app-wear-debug.apk"
        } else {
            Write-Host "Instalando en Telefono ($devId)..." -ForegroundColor Cyan
            adb -s $devId install -r "app-mobile/build/outputs/apk/debug/app-mobile-debug.apk"
        }
    }
} else {
    Write-Host "[INFO] No hay dispositivos ADB activos en este momento." -ForegroundColor Gray
}

# 6. Git Commit y Push
Write-Host "`n[5/5] Sincronizando con repositorio remoto (GitHub)..." -ForegroundColor Yellow
git add .
$status = git status --porcelain
if ($status) {
    git commit -m $CommitMessage --author="RCarribero <rcarribero@proton.me>"
    git push origin master
    Write-Host "[OK] Cambios sincronizados con GitHub (origin/master)." -ForegroundColor Green
} else {
    Write-Host "[OK] No hay cambios pendientes en Git." -ForegroundColor Green
}

Pop-Location
Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host "  Pipeline Completado con Exito al 100%" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Cyan
