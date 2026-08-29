# ==============================================================================
# OpenGluco: Sincronizador Automatico de Conocimiento para Gemini Gems (Google Drive)
# ==============================================================================

[CmdletBinding()]
param(
    [string]$CustomDrivePath = ""
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  OpenGluco: Sincronizacion de Conocimiento para Gemini Gem" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$projectRoot = Resolve-Path "$PSScriptRoot\.."
$agentsFile = Join-Path $projectRoot "AGENTS.md"
$graphReport = Join-Path $projectRoot "graphify-out\GRAPH_REPORT.md"
$designSystem = Join-Path $projectRoot "DESIGN_SYSTEM.md"

if (-not (Test-Path $agentsFile)) {
    Write-Error "No se encontro AGENTS.md en $projectRoot"
    exit 1
}

# 1. Detectar la ruta de Google Drive
$drivePath = $null

if ($CustomDrivePath -and (Test-Path $CustomDrivePath)) {
    $drivePath = $CustomDrivePath
} elseif ($env:GOOGLE_DRIVE_PATH -and (Test-Path $env:GOOGLE_DRIVE_PATH)) {
    $drivePath = $env:GOOGLE_DRIVE_PATH
} else {
    # Buscar unidades montadas por Google Drive para escritorio (G:\, H:\, etc.)
    $possibleRoots = @("G:\Mi unidad", "G:\My Drive", "H:\Mi unidad", "H:\My Drive", "I:\Mi unidad", "I:\My Drive", "F:\Mi unidad", "F:\My Drive")
    foreach ($candidate in $possibleRoots) {
        if (Test-Path $candidate) {
            $drivePath = $candidate
            break
        }
    }

    if (-not $drivePath) {
        # Buscar en perfil de usuario
        $userCandidates = @(
            "$env:USERPROFILE\Google Drive",
            "$env:USERPROFILE\GoogleDrive",
            "$env:USERPROFILE\My Drive"
        )
        foreach ($candidate in $userCandidates) {
            if (Test-Path $candidate) {
                $drivePath = $candidate
                break
            }
        }
    }
}

# 2. Generar un archivo consolidado para carga directa o respaldo
$stagingDir = Join-Path $projectRoot "tools\gem_knowledge_export"
if (-not (Test-Path $stagingDir)) {
    New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null
}

$consolidatedPath = Join-Path $stagingDir "OPENGLUCO_GEM_KNOWLEDGE.md"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

$header = @"
# OpenGluco: Base de Conocimiento Consolidada para Gemini Gem
*Generado automaticamente el: $timestamp*

"@

$agentsContent = Get-Content $agentsFile -Raw -Encoding UTF8
$graphContent = if (Test-Path $graphReport) { Get-Content $graphReport -Raw -Encoding UTF8 } else { "Grafo no generado aun. Ejecuta 'graphify update .'" }

$consolidatedContent = $header + "`n`n" + $agentsContent + "`n`n---`n`n" + $graphContent
[System.IO.File]::WriteAllText($consolidatedPath, $consolidatedContent, [System.Text.Encoding]::UTF8)

# Copiar archivos individuales a la carpeta de staging
Copy-Item $agentsFile (Join-Path $stagingDir "AGENTS.md") -Force
if (Test-Path $graphReport) {
    Copy-Item $graphReport (Join-Path $stagingDir "GRAPH_REPORT.md") -Force
}
if (Test-Path $designSystem) {
    Copy-Item $designSystem (Join-Path $stagingDir "DESIGN_SYSTEM.md") -Force
}

# 3. Sincronizar con Google Drive si esta disponible
if ($drivePath) {
    $targetDir = Join-Path $drivePath "OpenGluco_Gem_Knowledge"
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }

    Copy-Item $agentsFile (Join-Path $targetDir "AGENTS.md") -Force
    if (Test-Path $graphReport) {
        Copy-Item $graphReport (Join-Path $targetDir "GRAPH_REPORT.md") -Force
    }
    Copy-Item $consolidatedPath (Join-Path $targetDir "OPENGLUCO_GEM_KNOWLEDGE.md") -Force

    Write-Host "[OK] Conocimiento sincronizado exitosamente con Google Drive:" -ForegroundColor Green
    Write-Host "     Destino: $targetDir" -ForegroundColor Green
    Write-Host "     - AGENTS.md" -ForegroundColor Gray
    Write-Host "     - GRAPH_REPORT.md" -ForegroundColor Gray
    Write-Host "     - OPENGLUCO_GEM_KNOWLEDGE.md" -ForegroundColor Gray
} else {
    Write-Host "[INFO] Google Drive para escritorio no detectado en las rutas estandar." -ForegroundColor Yellow
    Write-Host "       Se han generado los archivos de conocimiento en staging local:" -ForegroundColor Yellow
    Write-Host "       Ruta: $stagingDir" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "       Para sincronizar automaticamente con tu unidad de Drive personalizada:" -ForegroundColor Gray
    Write-Host "       `$env:GOOGLE_DRIVE_PATH = 'D:\Tu_Carpeta_Drive\Mi unidad'" -ForegroundColor Gray
    Write-Host "       .\tools\sync_gem_knowledge.ps1" -ForegroundColor Gray
}

Write-Host "==========================================================" -ForegroundColor Cyan
