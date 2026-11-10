# BRÚJULA · Intervención Adaptativa Justo a Tiempo (JITAI) en Salud Digital

**BRÚJULA** es una aplicación Android nativa escrita en **Kotlin** y **Jetpack Compose** que materializa un programa ambulatorio híbrido de **intervención adaptativa justo a tiempo (*Just-In-Time Adaptive Intervention*, JITAI)** para el acompañamiento, la autorregulación y la reducción de daños en contextos de *chemsex*.

> **Principio rector:** Orientación, serenidad visual, autonomía y acompañamiento clínico cálido; nunca vigilancia, sobrecarga cognitiva, diagnóstico automático ni culpabilización. BRÚJULA no equipara *chemsex* con adicción ni patologiza el deseo sexual.

---

## 1. Diseño Minimalista y Refugio Emocional (`v1.1.0`)

Cuando existe impulso intenso o ruido mental, enfrentarse a cifras, tablas o textos densos embota la mente. BRÚJULA prioriza una **interfaz contemplativa, visual y silenciosa**:

- **La Brújula Escultórica (`LivingCompassCanvas`):** Esfera mineral en Azul Petróleo profundo con rosa de los vientos en filigrana, doble halo respiratorio y aguja magnética facetada con física de muelle suave. Refleja el estado autoinformado (*Aguas tranquilas*, *Pulso vital*, *Brisa cambiante*, *Oleaje intenso*) sin emitir etiquetas diagnósticas.
- **Cero ruido numérico a primera vista:** Las escalas táctiles muestran palabras humanas cualitativas (*En calma*, *Sereno*, *Con aire*, *Luminoso*) y revelan los detalles paso a paso solo al tocar (*divulgación progresiva*).
- **Paleta cromática orgánica:**
  - **Verde Jade** (`#1B7463` / `#48C4AC`): orientación, autonomía y calma vital.
  - **Marfil Cálido** (`#FAF7F1` / `#FFFDF9`): lienzo luminoso alejado de la frialdad hospitalaria.
  - **Azul Petróleo** (`#0E2530` / `#0A161D`): profundidad serena y modo oscuro nocturno.
  - **Ámbar Cálido** (`#D98A29`) y **Coral Terracota** (`#D8634C`): atención cercana sin semántica de alarma.
- **Navegación flotante y háptica sutil:** Cabecera editorial limpia, muelle inferior flotante redondeado, microvibraciones suaves desactivables, modo claro/oscuro/automático, modo discreto y ampliación tipográfica (`+12%`). Optimizada para **Samsung Galaxy S25 Ultra**.

---

## 2. Pantallas Conectadas

1. **Brújula (`HomeScreen`):**
   - Escenario atmosférico sin contadores, rosa náutica interactiva, frase personal editable, tarjetas ilustradas en `Canvas` (*Calma* y *Espacios*), siguiente ancla del día y acceso discreto a enfermería y `112`.
2. **Sentir (`CheckInScreen`):**
   - Valoración rápida con deslizadores orgánicos cualitativos para **ánimo**, **impulso de consumir**, **deseo sexual independiente** (aislado del motor de alertas) y **sensación de margen**, píldoras ligeras de disponibilidad/conducción y respuesta acogedora tras guardar.
3. **Zonas (`ZonesScreen`):**
   - Radar cartográfico luminoso con constelación de espacios **refugio** (verdes), **atención** (ámbar) y **sensibles** (coral), creación con un toque en el lienzo y geolocalización local opcional procesada exclusivamente en el teléfono.
4. **Mi día (`MyDayScreen`):**
   - Arco solar visual de la jornada, planificación flexible por franjas (*Mañana*, *Tarde*, *Noche*, *Madrugada*), preparación de alternativas amables (*plan B*) e ideas rápidas de un toque.
5. **Calma (`SupportScreen`):**
   - Loto/aurora respiratoria multicapa animada (*Suspiro suave 4·2·6*, *Equilibrio 4·4·4·4*, *Descanso nocturno 4·0·7*), ejercicios breves desplegables sin muros de texto, guía clínica revisada de reducción de daños y contacto directo configurable con **enfermería referente**.
6. **Mi camino (`ProgressScreen`):**
   - Horizonte visual de 12 etapas, onda orgánica con relleno degradado (*Margen*, *Ánimo*, *Impulso*), pausas de reflexión personal (*Punto de partida*, *Mitad de camino*, *Mirada amplia*) y huellas propias sin rachas punitivas.
7. **Ajustes (`SettingsScreen`):**
   - Aspecto, modo discreto, permisos revocables, sensibilidad experimental JITAI plegable para evitar saturación visual, exportación local de resumen pactado en JSON y borrado total en el dispositivo.

---

## 3. Motor JITAI Local y Explicable (`JitaiEngine`)

- **Impulso intenso o poco margen:** Si `impulso ≥ 7/10` o `margen ≤ 3/10`, la persona está **disponible** y **no está conduciendo**, BRÚJULA sugiere suavemente respirar 3 minutos o contactar con enfermería, habilitando una reevaluación opcional a los 20 minutos.
- **Independencia del deseo sexual:** Un deseo sexual alto con impulso bajo y margen conservado **nunca dispara alertas**.
- **Espacios de atención o sensibles:** Propone únicamente parar un segundo a sentir cómo estás, sin inferir consumo ni mostrar nombres de lugares en notificaciones.
- **Protección frente a saturación:** Máximo **2 avisos automáticos diarios**, **horas de silencio** configurables (`23:00 – 08:00`) y **pausa automática de 4 horas** tras posponer una sugerencia.
- **Acceso manual permanente:** Todas las herramientas de respiración, calma, guía y urgencias (`112`) están siempre disponibles.

---

## 4. Arquitectura Técnica y Privacidad

```text
app/src/main/java/es/brujula/jitai/
├── core/
│   ├── data/           # BrujulaRepository + CuratedClinicalContent
│   ├── database/       # Room SQLite local v2 (entidades, DAO y semilla inicial)
│   ├── datastore/      # DataStore Preferences (sensibilidad JITAI, privacidad y enfermería)
│   ├── designsystem/   # Paleta Jade/Marfil/Petróleo, tipografía editorial, háptica y controles orgánicos
│   ├── jitai/          # JitaiEngine (reglas explicables) + BrujulaNotifier
│   ├── location/       # LocalZoneMonitor (geolocalización 100% en dispositivo)
│   └── model/          # Modelos de dominio serializables
├── ui/
│   ├── components/     # LivingCompassCanvas (rosa náutica escultórica)
│   ├── navigation/     # BrujulaAppShell (cabecera limpia + muelle flotante)
│   ├── screens/        # Home, CheckIn, Zones, MyDay, Support, Progress y Settings
│   └── BrujulaViewModel.kt
├── BrujulaApp.kt
└── MainActivity.kt
```

- **Requisitos:** Android `minSdk 28` (Android 9+) · `targetSdk 35` · JDK 17 · Gradle 8.9 · Kotlin 2.0.21 · Compose BOM `2024.10.00`.
- **Coste operativo:** `0 €` (100 % local en el dispositivo, sin servidores ni APIs de pago).

---

## 5. Compilación e Instalación

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n es.brujula.jitai/.MainActivity
```

---

## 6. Nota Ética y Sanitaria

BRÚJULA es una herramienta tecnológica de apoyo a la autogestión y reducción de daños diseñada en el marco de un proyecto universitario de innovación en salud digital. **No sustituye la valoración médica o enfermera presencial ni constituye un servicio de urgencias 24 horas.** Ante cualquier emergencia vital, llama inmediatamente al **112**.
