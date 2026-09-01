# [ADR-0001] Adopcion de Arquitectura Multi-Modulo con Nucleo Limpio Compartido

- **Fecha:** 2026-09-01
- **Estado:** Aceptado
- **Autores / Decisores:** Equipo de Arquitectura OpenGluco
- **Harness / Contexto:** Antigravity Universal Suite

---

## 1. Contexto y Declaracion del Problema
El ecosistema OpenGluco proporciona monitorizacion continua de glucosa en tiempo real a traves de tres factores de forma distintos: telefonos moviles (`app-mobile`), relojes inteligentes Wear OS (`app-wear`) y pantallas de vehiculos (`app-auto`). Se requiere compartir la logica de dominio, modelos de telemetria, conexion de red con LibreView (Abbott) y persistencia sin duplicar codigo ni acoplar las interfaces de usuario entre si.

---

## 2. Factores Decisivos (Decision Drivers)
- Reutilizacion de codigo de calculo clinico y conexion API entre Mobile, Wear OS y Android Auto.
- Tiempos de compilacion incrementales rapidos e independencia de ciclo de vida en cada factor de forma.
- Aislamiento absoluto del modelo de dominio frente a frameworks de UI (Compose, Car App Library, Glance).
- Preparacion para potencial soporte KMP (Kotlin Multiplatform).

---

## 3. Opciones Consideradas
1. **Opcion A: Monorepo Multi-Modulo Gradle en Capas (`:core:model`, `:core:network`, `:core:data`)**
   - *Ventajas:* Desacoplamiento limpio, flujo unidireccional de dependencias, compilacion incremental optima.
   - *Desventajas:* Configuracion inicial de modulos y publicacion de dependencias locales.
2. **Opcion B: Monolito de aplicacion unica con subpaquetes internos**
   - *Ventajas:* Menos configuracion de Gradle.
   - *Desventajas:* Alto riesgo de acoplamiento circular y compilaciones completas lentas.

---

## 4. Decision Elegida
Se elige la **Opcion A: Monorepo Multi-Modulo Gradle en Capas**. El nucleo se divide en tres modulos compartidos:
1. `:core:model`: Modelos puros Kotlin inmutables, serializables (`@Serializable`) y algoritmos matematicos/clinicos (`CgmCurveSmoother`).
2. `:core:network`: Cliente Retrofit/OkHttp, interceptores y DTOs de red.
3. `:core:data`: Repositorios, persistencia DataStore, evaluadores de alarma y calculadores clinicos.

Las aplicaciones (`app-mobile`, `app-wear`, `app-auto`) consumen `:core:data` manteniendo su capa de presentacion y servicios estrictamente desacoplados.

---

## 5. Consecuencias y Compromisos (Trade-offs)

### Consecuencias Positivas:
- Cero duplicacion de algoritmos clinicos y calculos de AGP/TIR.
- Las pruebas unitarias de `:core:model` y `:core:data` se ejecutan en milisegundos en la JVM sin emulador.
- Cada modulo de aplicacion (`app-mobile`, `app-wear`, `app-auto`) evoluciona de manera independiente.

### Consecuencias Negativas / Riesgos Asumidos:
- Requiere mantener la consistencia de dependencias y versiones de Kotlin/Android Gradle Plugin en el root `settings.gradle.kts` y `build.gradle.kts`.

---

## 6. Reglas de Validacion y Cumplimiento (Enforcement)
- Registrado en `AGENTS.md` y `settings.gradle.kts`.
- Ningun modulo `:core` puede depender de `:app-mobile`, `:app-wear` o `:app-auto`.
- `:core:model` no debe incluir dependencias del framework Android UI.
