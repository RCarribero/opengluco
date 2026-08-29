@echo off
pwsh -ExecutionPolicy Bypass -File "%~dp0sync_gem_knowledge.ps1"
if %ERRORLEVEL% NEQ 0 (
    powershell -ExecutionPolicy Bypass -File "%~dp0sync_gem_knowledge.ps1"
)
pause
