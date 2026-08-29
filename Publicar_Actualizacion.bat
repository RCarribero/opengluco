@echo off
chcp 65001 > nul
title OpenGluco - Asistente de Publicación de Actualizaciones
powershell -ExecutionPolicy Bypass -File "%~dp0tools\publicar_actualizacion.ps1"
pause
