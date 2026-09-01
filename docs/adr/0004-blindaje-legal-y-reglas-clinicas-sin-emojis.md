# [ADR-0004] Blindaje Legal, Conformidad MDR/MDDS y Prohibicion Estricta de Emojis

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
OpenGluco es un software de salud de monitorizacion continua de glucosa para pacientes con diabetes y cuidadores. Para garantizar la seguridad clinica del paciente, el rigor medico y el estricto cumplimiento regulatorio (MDR UE 2017/745 y FDA MDDS):
- No se deben proporcionar sugerencias de dosificacion de insulina ni calculo de bolos.
- La interfaz debe mantener sobriedad clinica sin ambiguedades visuales ni emojis informales.
- Toda referencia a LibreLink, LibreLinkUp, LibreView o FreeStyle debe atribuirse a Abbott Laboratories bajo uso legitimo nominativo.

---

## 2. Reglas Deterministas Forzadas
1. **Tokens Clinicos Estandar:** En rango (`#4ADE80`), Bajo (`#F87171`), Urgente Bajo (`#EF4444`), Alto (`#FBBF24`), Muy Alto (`#FB923C`).
2. **Horas en Formato 24h:** Siempre formato militar internacional `HH:mm`.
3. **Flechas de Tendencia:** Exclusivamente caracteres vectoriales limpios (`→`, `↑`, `↓`, `↗`, `↘`) o texto (`->`, `^`, `v`).
4. **Cero Emojis:** Prohibicion estricta en codigo fuente, UI, strings.xml, logs y documentacion, auditada en pruebas unitarias automatizadas.
