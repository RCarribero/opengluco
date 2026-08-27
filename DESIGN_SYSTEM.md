# Sistema de Diseno: OpenGluco (Minimalista Clinico)

> **Documento Maestro de Diseno y Especificaciones UI para el Ecosistema OpenGluco (Mobile, Wear OS, Android Auto)**  
> *Generado bajo directrices de precision clinica, accesibilidad y eficiencia energetica OLED.*

---

## 1. Filosofia y Estilo Visual

- **Arquetipo:** *Minimalismo Clinico & Glassmorphism Sutil*
- **Objetivo:** Maxima legibilidad medica de un vistazo (glanceability), reduccion de distraccion cognitiva y consumo minimo de bateria OLED.
- **Forma y Geometria:** Formas esfericas y pildoras flotantes (`RoundedCornerShape`), adaptadas a la ergonomia de relojes circulares, salpicaderos de vehiculos y terminales moviles.
- **Invariante:** Cero caracteres emoji en toda la interfaz, codigo, textos y recursos.

---

## 2. Paleta de Colores Oficial

### Fondos y Superficies OLED
| Token | Codigo HEX | Uso / Descripcion |
| :--- | :--- | :--- |
| `Background` | `#000000` | Negro puro para pixeles apagados en pantalla AMOLED. |
| `SurfaceOrb` | `#1E232D` | Pizarra mate para las esferas flotantes. |
| `SurfaceCard` | `#161A22` | Superficie secundaria para tarjetas y graficos. |
| `SurfaceBorder` | `#2D3748` | Borde sutil de 1dp para delimitar elementos sin saturar. |
| `SurfaceActiveBorder` | `#4ADE80` *(30% opacidad)* | Anillo de enfoque cuando un elemento esta activo o seleccionado. |

### Estados Clinicos de Glucosa
| Estado | Rango (mg/dL) | Color HEX | Nombre Clinico |
| :--- | :--- | :--- | :--- |
| **En Rango (Normal)** | `70 - 180` | `#4ADE80` | *Sage Mint Green* |
| **Bajo (Hipoglucemia)** | `56 - 69` | `#F87171` | *Soft Coral Red* |
| **Urgente Bajo** | `<= 55` | `#EF4444` | *Crimson Alert* |
| **Alto (Hiperglucemia)** | `181 - 249` | `#FBBF24` | *Warm Amber* |
| **Muy Alto** | `>= 250` | `#FB923C` | *Tangerine Warning* |
| **Acento Secundario** | Info / Sincronizacion | `#38BDF8` | *Arctic Medical Cyan* |

### Tipografia y Contraste
| Token | Codigo HEX | Uso |
| :--- | :--- | :--- |
| `TextPrimary` | `#FFFFFF` | Numeros grandes de glucosa y titulos principales. |
| `TextSecondary` | `#94A3B8` | Etiquetas de unidades (`mg/dL`), subtitulos y fechas. |
| `TextMuted` | `#64748B` | Informacion complementaria y timestamps. |

---

## 3. Jerarquia y Distribucion Espacial en Wear OS

```text
+-----------------------------------------------+
|                   12:45                       |  <- TimeText (Curvo / Superior)
|                                               |
|       +--------------+      +--------------+  |
|       |   (  140  )  |      |   (  ->  )   |  |  <- Fila Superior: 2 Esferas Flotantes
|       |    mg/dL     |      |   Estable    |  |     (Nivel Glucosa  |  Tendencia)
|       +--------------+      +--------------+  |
|                                               |
|       +------------------------+  +--------+  |
|       | ~~~~~~/ \~~~~~~~~~~~~~ |  |  12d   |  |  <- Fila Inferior: Grafica Sparkline
|       | (Curva suave ultimas h)|  | Sensor |  |     + Mini Badge Dias de Sensor
|       +------------------------+  +--------+  |
|                                               |
|              [  Ajustes / QR ]                |  <- Boton de acceso rapido / Rotary Scroll
+-----------------------------------------------+
```

### Especificaciones de Componentes:

### A. Esferas Flotantes Superiores (`DualFloatingOrbs`)
- **Tamano:** `76.dp` de diametro cada esfera, ubicadas simetricamente lado a lado con un espacio de `10.dp`.
- **Esfera Izquierda (Nivel de Glucosa):**
  - Fondo: `#1E232D` con borde `#2D3748`.
  - Arco exterior sutil: Trazo de 2.5dp con el color del estado clinico (ej. Verde menta `#4ADE80`).
  - Numero de glucosa: Tamano `26.sp`, peso `FontWeight.Bold`, color `#FFFFFF`.
  - Etiqueta: `mg/dL` a `9.sp`, color `#94A3B8`.
- **Esfera Derecha (Tendencia):**
  - Fondo: `#1E232D` con borde `#2D3748`.
  - Flecha de tendencia grande: `24.sp` (simbolos: `->`, `^`, `v`, `->`, `^`, etc.).
  - Badge inferior: Texto de estado (ej. `Estable`, `Subiendo`) a `9.sp` con color a juego.

### B. Grafica Sparkline + Badge de Sensor
- **Grafica:**
  - Curva Bezier continua suave con trazo de `2.dp` en `#4ADE80`.
  - Relleno con degradado vertical suave hacia transparente.
  - Franja objetivo punteada sutil (70 y 180 mg/dL).
- **Badge Lateral de Sensor:**
  - Pildora compacta en gris pizarra con dias restantes (`12d`) y texto `Sensor`.

---

## 4. Interactividad y Feedback Haptico

1. **Toque en la Esfera de Glucosa:**
   - Emite una vibracion corta (`HapticFeedbackType.LongPress`).
   - Muestra un dialogo rapido con estadisticas del dia: Promedio, Minimo y Maximo.
2. **Toque en la Esfera de Tendencia:**
   - Muestra la dinamica y flecha de tendencia.
3. **Toque en el Badge de Sensor:**
   - Despliega tarjeta con los dias exactos restantes, tiempo de calentamiento y numero de serie del sensor FreeStyle Libre.
4. **Giro del Bisel (Rotary Knob):**
   - Desplaza la pantalla con curvatura suave para acceder a Ajustes, Selector de Paciente y Login QR.

---

## 5. Mapeo de Codigo Jetpack Compose

```kotlin
// Tokens de Color Centralizados
object WearClinicalTheme {
    val Background = Color(0xFF000000)
    val SurfaceOrb = Color(0xFF1E232D)
    val SurfaceBorder = Color(0xFF2D3748)
    val PrimaryMint = Color(0xFF4ADE80)
    val LowCoral = Color(0xFFF87171)
    val UrgentCrimson = Color(0xFFEF4444)
    val HighAmber = Color(0xFFFBBF24)
    val TangerineWarning = Color(0xFFFB923C)
    val ArcticCyan = Color(0xFF38BDF8)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}
```

---

## 6. Mencion a Abbott Laboratories y Marcas Registradas

- **Compatibilidad de Sensores:** El sistema de diseno esta concebido para visualizar la telemetria continua de los sensores **FreeStyle Libre (Libre 1, Libre 2, Libre 3)** fabricados por **Abbott Laboratories / Abbott Diabetes Care Inc.**
- **Titularidad Nominativa:** *FreeStyle, Libre, LibreLink, LibreLinkUp y LibreView* son marcas registradas de **Abbott Laboratories**.
- **Independencia y No Afiliacion:** OpenGluco es un proyecto de software libre completamente independiente y no cuenta con patrocinio, aprobacion ni vinculo comercial con Abbott Laboratories.
