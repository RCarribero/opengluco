# [ADR-0003] Sincronizacion Dual Bluetooth RFCOMM y Google Play Services DataLayer

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
Los usuarios de relojes inteligentes Wear OS requieren recibir telemetria y alertas clinicas en tiempo real tanto en relojes conectados mediante Google Play Services como en dispositivos independientes o sin soporte de los servicios de Google.

---

## 2. Factores Decisivos (Decision Drivers)
- Maxima compatibilidad entre marcas de relojes Wear OS (Galaxy Watch, Pixel Watch, TicWatch).
- Transmision de tokens de sesion y configuraciones de alarma de forma bidireccional.
- Consumo energetico ultra-bajo en Wear OS con caida a modo de suspension (Doze mode friendly).

---

## 3. Decision Elegida
Implementar un canal de enlace hibrido:
1. **Canal Primario:** Google Play Services Wearable DataLayer (`MessageClient` y `DataClient`) para sincronizacion instantanea en segundo plano.
2. **Canal Secundario / Fallback:** Servidor socket `WearBluetoothRfcommService` para conexion directa Bluetooth RFCOMM serie estandar.
