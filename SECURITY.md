# Politica de Seguridad: OpenGluco Ecosystem

La seguridad y la confidencialidad de los datos de salud son fundamentales en este proyecto. A continuacion se detallan las directrices de seguridad y el procedimiento para reportar vulnerabilidades.

---

## 1. Arquitectura de Seguridad y Privacidad

- **Cifrado en Reposo:**
  - Todas las credenciales sensibles (token de autenticacion, ID de usuario, correos electronicos, alarmas e historial de telemetria) se cifran mediante **AES-256-GCM** respaldado por hardware en el **Android KeyStore** (`opengluco_master_keystore_key_v1`).
  - Formato de almacenamiento: `ENC:` + Base64(IV + Ciphertext + Tag).
- **Cifrado en Transito:**
  - Forzado estricto de HTTPS / TLS mediante `network_security_config.xml` con `cleartextTrafficPermitted="false"`.
- **Proteccion de Respaldos:**
  - `android:allowBackup="false"` habilitado en todas las aplicaciones para prevenir la extraccion de credenciales via Android Backup o ADB backups no autorizados.
- **Cumplimiento RGPD (Art. 9 y 17) y Cero Intermediarios:**
  - La aplicacion se comunica directamente mediante TLS con los servidores oficiales de LibreView (Abbott Laboratories).
  - Los datos de glucosa no se retransmiten a servidores puente de terceros, nubes privadas ni analiticas externas.
  - Opcion de purga completa y permanente (`purgeAllLocalData`) en el menu de configuracion.

---

## 2. Reporte Responsable de Vulnerabilidades

Si identifica una posible vulnerabilidad de seguridad en OpenGluco Ecosystem:

1. **NO abra un issue publico en GitHub.**
2. Envie un reporte detallado a traves del canal de seguridad privado del repositorio en GitHub (**Security Advisories > Report a vulnerability**) o contacte directamente a los mantenedores.
3. Incluya en su reporte:
   - Descripcion detallada de la vulnerabilidad.
   - Pasos reproducibles o prueba de concepto (PoC).
   - Modulo afectado (`core`, `app-mobile`, `app-wear`, `app-auto`).
   - Impacto potencial estimado.

Nos comprometemos a revisar y mitigar cualquier vulnerabilidad confirmada a la mayor brevedad posible.
