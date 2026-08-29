@echo off
pwsh -ExecutionPolicy Bypass -File "%~dp0deploy_and_sync_all.ps1"
if %ERRORLEVEL% NEQ 0 (
    powershell -ExecutionPolicy Bypass -File "%~dp0deploy_and_sync_all.ps1"
)
pause
