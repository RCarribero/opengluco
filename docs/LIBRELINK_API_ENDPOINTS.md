# Especificación Técnica de Endpoints: Ecosistema FreeStyle LibreLink / LibreView

> **Atribución y Uso Legítimo Nominativo:** Todas las referencias a marcas (*FreeStyle, Libre, LibreLink, LibreLinkUp, LibreView*) son marcas registradas de **Abbott Laboratories / Abbott Diabetes Care Inc.**
> **Aviso de Interoperabilidad:** Este documento recopila la estructura técnica de interfaces de red con fines exclusivos de interoperabilidad clínica y portabilidad de datos del paciente, al amparo del Artículo 100.3 del Real Decreto Legislativo 1/1996 (TRLPI) y el Reglamento (UE) 2023/2854 (Data Act). El proyecto OpenGluco no posee afiliación, patrocinio ni respaldo oficial de Abbott Laboratories.

---

## 1. Topología de Servidores y Puertas de Enlace (Gateways)

La aplicación oficial **FreeStyle LibreLink** (`com.freestylelibre.app.es`) se comunica con dos arquitecturas de backend diferenciadas según la función:

1. **Gateway Principal de Telemetría y Cuenta de Paciente (LSL - LibreView Server Layer):**
   - **Base URL Producción (España / UE):** `https://api-c-es.libreview.io/lsl/`
   - **Base URL Producción Global (UE):** `https://api-eu.libreview.io/lsl/`
   - **Base URL Estados Unidos:** `https://api-us.libreview.io/lsl/`
   - **Base URL Preproducción (Staging/Dev):** `https://api-c-es.preprod.adc-lv.io/lsl/`
   - **Identificador de Gateway (`gatewayType`):** `FSLibreLink.Android`
   - **Dominio (`domain`):** `Libreview`

2. **Servicio de Cuentas Conectadas y Compartición (Sharing Gateway):**
   - **Base URL:** `https://api.libreview.io/sharing/legacytoken`
   - **Función:** Generación e intercambio de tokens para vinculación con LibreLinkUp y cuidadores.

3. **Plataforma de Distribución de Contenidos y Configuración Regulatoria (CDN):**
   - **Base URL:** `https://fsll.freestyleserver.com/Payloads/Mobile/`
   - **Función:** Descarga dinámica de paquetes de etiquetado (UDI), manuales de usuario (IFU), acuerdos legales y configuración por país.

4. **Investigación de Reclamaciones y Diagnóstico de Sensores (CIG OneStep):**
   - **Base URL:** `https://cig.freestyleserver.com/fsll`
   - **Audience / Issuer:** `ComplaintInvestigationDataCloud` / `FreeStyleLibreLink`
   - **Función:** Telemetría de fallos de inserción, errores de comunicación NFC/Bluetooth y códigos de error de sensor (ER3, ER4).

5. **Infraestructura de Telemetría Operativa y Notificaciones:**
   - **Segment (Telemetría de Uso):** `https://events.eu1.segmentapis.com/v1`
   - **Sentry (Monitorización de Fallos UE):** `https://o4507528335982592.ingest.de.sentry.io/4507856007462992`
   - **Braze (Mensajería y Push en Servidores Europeos):** `https://sdk.fra-02.braze.eu`
   - **SmartPen (Plumas de Insulina Conectadas NovoPen):** `https://www.smartpendeveloper.com/`

---

## 2. Endpoints del Gateway Clínico Principal (`NumeraWebApi` en `/lsl/`)

Estos endpoints constituyen el núcleo del cliente REST (`com.librelink.app.network.NumeraWebApi`) implementado sobre Retrofit y RxJava3.

### 2.1. Autenticación y Gestión de Sesión

#### `POST api/nisperson/getauthentication`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/nisperson/getauthentication`
- **Propósito:** Inicia sesión con la cuenta del paciente y devuelve los tokens de autorización y perfil.
- **Cuerpo de Solicitud (`AuthenticateParameters`):**
  ```json
  {
    "domain": "Libreview",
    "gatewayType": "FSLibreLink.Android",
    "userName": "usuario@ejemplo.com",
    "password": "PasswordSegura123",
    "applicationId": "com.freestylelibre.app.es",
    "appCultureCode": "es-ES",
    "setDevice": true
  }
  ```
- **Respuesta (`NumeraResponse<AuthenticationResult>`):**
  ```json
  {
    "status": 0,
    "result": {
      "accountId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
      "userToken": "JWT_OR_SESSION_TOKEN_STRING",
      "firstName": "Nombre",
      "lastName": "Apellidos",
      "email": "usuario@ejemplo.com",
      "dateOfBirth": "1985-05-15",
      "country": "ES",
      "culture": "es-ES",
      "domainData": "{}",
      "consents": { ... }
    },
    "reason": null
  }
  ```

#### `POST api/nisperson/register`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/nisperson/register`
- **Propósito:** Registro de una nueva cuenta de paciente en la nube de LibreView.
- **Cuerpo de Solicitud (`RegisterParameters`):**
  - Parámetros base (`domain`, `gatewayType`).
  - Datos demográficos (`firstName`, `lastName`, `email`, `password`, `dateOfBirth`, `country`, `culture`).
  - Aceptación de términos y política de privacidad.

#### `POST api/nisperson/signout`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/nisperson/signout`
- **Propósito:** Invalida la sesión activa y revoca el token en el servidor.
- **Cuerpo de Solicitud (`SignOutParameters`):**
  - `userToken`, `domain`, `gatewayType`.

#### `POST api/nisperson/deletepatientaccount`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/nisperson/deletepatientaccount`
- **Propósito:** Eliminación irrevocable de la cuenta del paciente en cumplimiento del RGPD / GDPR (Derecho al Olvido).

---

### 2.2. Subida de Datos de Glucosa y Telemetría del Sensor (`POST api/measurements`)

Este es el endpoint central a través del cual FreeStyle LibreLink transfiere el historial completo de lecturas al servidor de Abbott.

#### `POST api/measurements`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/measurements`
- **Propósito:** Subida periódica o inmediata por lotes de lecturas automáticas del sensor, escaneos NFC, dosis de insulina, carbohidratos y estados del sensor.
- **Cuerpo de Solicitud (`UploadData$Parameters`):**
  ```json
  {
    "domain": "Libreview",
    "gatewayType": "FSLibreLink.Android",
    "userToken": "TOKEN_DE_SESION_DEL_PACIENTE",
    "deviceData": {
      "header": {
        "device": {
          "hardwareDescriptor": "Samsung Galaxy S21",
          "osType": "Android",
          "osVersion": "14",
          "modelName": "SM-G991B",
          "serialNumber": "UUID_DEL_DISPOSITIVO"
        }
      },
      "measurementLog": {
        "capabilities": [ ... ],
        "scheduledContinuousGlucoseEntries": [
          {
            "timestamp": "2026-09-22T14:30:00.000Z",
            "valueInMgPerDl": 115.0,
            "trendArrow": 3,
            "sensorSerialNumber": "0M001A8934"
          }
        ],
        "unscheduledContinuousGlucoseEntries": [
          {
            "timestamp": "2026-09-22T14:45:12.000Z",
            "valueInMgPerDl": 122.0,
            "trendArrow": 4,
            "sensorSerialNumber": "0M001A8934"
          }
        ],
        "currentGlucoseEntries": [
          {
            "timestamp": "2026-09-22T15:55:00.000Z",
            "valueInMgPerDl": 118.0,
            "trendArrow": 3
          }
        ],
        "bloodGlucoseEntries": [ ... ],
        "insulinEntries": [
          {
            "timestamp": "2026-09-22T14:00:00.000Z",
            "units": 4.5,
            "insulinType": "RapidActing",
            "medication": "Fiasp"
          }
        ],
        "foodEntries": [
          {
            "timestamp": "2026-09-22T14:05:00.000Z",
            "gramsCarbs": 45.0
          }
        ],
        "ketoneEntries": [ ... ],
        "genericEntries": [ ... ]
      },
      "deviceSettings": {
        "targetLow": 70,
        "targetHigh": 180,
        "alarmSettings": { ... }
      },
      "connectedDevicesEntry": { ... },
      "forceUpload": false
    }
  }
  ```
- **Respuesta (`UploadData$Result`):**
  ```json
  {
    "status": 0,
    "result": {
      "itemCount": 48,
      "status": 1,
      "uploadId": "upload-xxxxxxxx-xxxx"
    },
    "reason": null
  }
  ```

---

### 2.3. Perfil, Consentimientos y Reglas Regulatorias

#### `GET api/rules/CheckMinor`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/rules/CheckMinor?GatewayType=FSLibreLink.Android&Country=ES&DateOfBirth=YYYY-MM-DD`
- **Propósito:** Evalúa si el usuario es menor de edad según las leyes del país seleccionado para requerir consentimiento parental.
- **Respuesta:** `{"status": 0, "result": false, "reason": null}`

#### `GET api/version`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/version?Domain=Libreview&GatewayType=FSLibreLink.Android`
- **Propósito:** Valida si la versión de la aplicación está soportada o si se requiere actualización forzosa del cliente.

#### `POST api/nisperson/getAccountInfo`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/nisperson/getAccountInfo`
- **Propósito:** Obtiene la información actualizada del perfil del paciente y estado de consentimiento.

#### `PUT api/nisperson`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/nisperson`
- **Propósito:** Actualiza ajustes del perfil, unidades de glucosa (`mg/dL` vs `mmol/L`) y metadatos de usuario.

#### `POST api/nisperson/consent`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/nisperson/consent`
- **Propósito:** Registro y actualización de firmas de consentimiento informado y tratamiento de datos médicos.

#### `GET api/passwordreset`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/passwordreset?Domain=Libreview&GatewayType=FSLibreLink.Android&Email=...`
- **Propósito:** Solicita el envío del correo de restablecimiento de contraseña.

#### `POST api/passwordreset/updateKnownPassword`
- **URL Completa:** `https://api-c-es.libreview.io/lsl/api/passwordreset/updateKnownPassword`
- **Propósito:** Cambio de contraseña para usuarios autenticados.

---

## 3. Endpoints del CDN de Distribución (`fsll.freestyleserver.com`)

Base URL: `https://fsll.freestyleserver.com/Payloads/Mobile/`

| Método | Ruta Relativa | Propósito |
|---|---|---|
| `GET` | `Config/{app_name}_Android_UDI_{country}_{version}_config.json` | Configuración de Identificador Único de Dispositivo Médico (UDI/MDR). |
| `GET` | `Config/{app_name}_Android_{version}_{country}_config.json` | Matriz de compatibilidad y límites clínicos específicos del país. |
| `GET` | `Config/{region}.json` | Configuración regional de enrutamiento cloud. |
| `GET` | `Labeling/{app_name}_Android_{version}_{country}_{labelingType}.zip` | Paquetes de etiquetado médico regulatorio comprimidos. |
| `GET` | `Labeling/{app_name}_Android_{version}_{country}_{labelingType}_version.txt` | Verificación de versión del paquete de etiquetado. |
| `GET` | `Manual/{app_name}_Android_{version}_{country}_{uom}_manual.zip` | Manual de instrucciones de uso (IFU) en formato comprimido. |
| `GET` | `Manual/{app_name}_Android_{version}_{country}_{uom}_manual_version.txt` | Verificación de versión del manual de instrucciones. |
| `GET` | `{type}_Android_{locale}_{agreement}.zip` | Paquete de términos legales y condiciones de servicio por idioma. |
| `GET` | `{type}_Android_{locale}_{agreement}_version.txt` | Control de versión de términos de servicio. |

---

## 4. Endpoints de Consulta y Lectura (Ecosistema LibreLinkUp `/llu/`)

Mientras que **LibreLink** actúa primariamente como el nodo **emisor/subidor** (`/lsl/api/measurements`), el ecosistema de consulta para cuidadores y aplicaciones de interoperabilidad (como **OpenGluco**) interactúa con la puerta de enlace **LibreLinkUp (`/llu/`)**:

| Método | Endpoint | Propósito Clínico |
|---|---|---|
| `POST` | `llu/auth/login` | Autenticación de cuenta seguidora / paciente con redirección de región. |
| `POST` | `llu/auth/terms/accept` | Confirmación de términos legales de la plataforma. |
| `GET` | `llu/connections` | Lista de pacientes y sensores vinculados a la cuenta. |
| `GET` | `llu/connections/{patientId}/graph` | Telemetría en tiempo real: última medición, flecha, estado del sensor y ventana de 12 horas de lecturas históricas (intervalos de 15 min). |
| `GET` | `llu/connections/{patientId}/logbook` | Historial acumulado de eventos de escaneo, alarmas y registros manuales hasta 30 días. |
| `GET` | `llu/connections/{patientId}` | Detalles de configuración y umbrales de rango objetivo del paciente. |

---

## 5. Cuadro Resumen de Cabeceras Requeridas

Las peticiones HTTP salientes desde el cliente móvil oficial hacia la infraestructura de Abbott incorporan las siguientes cabeceras estándar:

```http
Content-Type: application/json
Accept: application/json
product: llu.android (o FSLibreLink.Android)
version: 2.13.0 (versión de LibreLink) o 4.16.0 (versión de LibreLinkUp)
User-Agent: Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36
Authorization: Bearer <JWT_SESSION_TOKEN>
account-id: <SHA256_HASH_ACCOUNT_ID>
```
