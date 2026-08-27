# Regla de Estilo y Comunicación: Prohibición Estricta de Emojis

Este documento establece una directriz obligatoria e incondicional para todo el ecosistema OpenGluco y para todas las interacciones, código, interfaces y documentación.

---

## 1. Prohibición Absoluta de Emojis
- **Queda estrictamente prohibido el uso de emojis** en:
  - Respuestas del asistente y mensajes al usuario.
  - Interfaces de usuario (Móvil, Wear OS, Android Auto, HTML, Compose, XML).
  - Títulos de diálogos, botones, notificaciones y descargos legales.
  - Documentación técnica, artefactos (`walkthrough.md`, `implementation_plan.md`, etc.), archivos de reglas y comentarios de código.
  - Strings de prueba y logs.

## 2. Alternativas Formales y Clínicas
- Para flechas de tendencia: Utilizar exclusivamente caracteres ASCII o símbolos de texto estándar (`->`, `<-`, `^`, `v`, o caracteres Unicode de flecha limpia como `→`, `↑`, `↓`, `↗`, `↘`).
- Para iconos de interfaz: Utilizar componentes vectoriales de Material Icons (`androidx.compose.material.icons.Icons`) en lugar de caracteres emoji en texto.
- Para alertas o estados de severidad: Utilizar texto formal (`[Alerta]`, `[Urgente]`, `[Aviso]`, `[Informacion]`) y colores clínicos (`PrimaryMint`, `LowCoral`, `UrgentCrimson`, `HighAmber`).
