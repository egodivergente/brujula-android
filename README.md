# BRÚJULA · Intervención Adaptativa Justo a Tiempo (JITAI) en Salud Digital

**BRÚJULA** es una aplicación Android nativa escrita en **Kotlin** y **Jetpack Compose** que materializa un programa ambulatorio híbrido de **intervención adaptativa justo a tiempo (*Just-In-Time Adaptive Intervention*, JITAI)** para el acompañamiento, la autorregulación y la reducción de daños asociados al consumo problemático de sustancias en contextos de *chemsex*.

> **Principio rector:** Orientación, autonomía y acompañamiento clínico cálido; nunca vigilancia, diagnóstico automático ni culpabilización. BRÚJULA no equipara *chemsex* con adicción ni patologiza el deseo sexual.

---

## 1. Identidad Visual y Experiencia Emocional

- **Metáfora central — La Brújula Viva:** Representación vectorial interactiva en `Canvas` con física de muelle amortiguado, 72 marcas acimutales, halo respiratorio sutil y respuesta táctil al giro. Refleja suavemente el rumbo autoinformado (*Rumbo sereno*, *Energía y deseo autónomo*, *Brisa cambiante*, *Oleaje intenso*) sin emitir etiquetas diagnósticas.
- **Paleta cromática:**
  - **Verde Jade** (`#1B7463` / `#48C4AC`): orientación, autonomía y calma vital.
  - **Marfil Cálido** (`#FAF7F1` / `#FFFDF9`): lienzo luminoso alejado de la frialdad hospitalaria.
  - **Azul Petróleo** (`#0E2530` / `#0A161D`): contraste legible y modo oscuro nocturno profundo.
  - **Ámbar Cálido** (`#D98A29`) y **Coral Terracota** (`#D8634C`): atención contextual cercana sin semántica punitiva.
- **Tipografía, Háptica y Accesibilidad:** Jerarquía editorial serif/sans, numeración tabular (`tnum`) en cronómetros y escalas `0–10` de precisión táctil, microvibraciones sutiles desactivables, modo claro/oscuro/automático, modo discreto reforzado y ampliación tipográfica (`+12%`). Optimizada para pantallas de alta densidad como **Samsung Galaxy S25 Ultra**.

---

## 2. Pantallas y Funcionalidades Conectadas

1. **Inicio / Mi brújula (`HomeScreen`):**
   - Brújula interactiva en tiempo real, lema personal editable, lectura contextual explicable, siguiente actividad planificada de *Mi día*, accesos de un toque al apoyo y registro voluntario de GHB/GBL.
2. **Check-in EMA (`CheckInScreen`):**
   - Evaluación ecológica momentánea rápida (`< 45 s`) con escalas táctiles `0–10` para **ánimo**, **craving**, **deseo sexual independiente** (aislado del motor de alertas para no patologizar la sexualidad) y **control percibido**, más interruptores de disponibilidad y conducción segura, y modo de reevaluación voluntaria a los 20 minutos.
3. **Mis zonas (`ZonesScreen`):**
   - Cartografía personal interactiva (`Canvas` con zoom, desplazamiento y pulsación prolongada para crear zonas) con **zonas verdes** (apoyo), **amarillas** (vulnerabilidad) y **rojas** (desencadenantes), radio configurable y **geolocalización local opcional** procesada exclusivamente dentro del teléfono (`LocationManager`).
4. **Mi día (`MyDayScreen`):**
   - Planificación flexible por franjas (*Mañana*, *Tarde*, *Noche*, *Madrugada*), identificación anticipada de momentos sensibles y elección de actividades alternativas protectoras.
5. **Apoyo (`SupportScreen`):**
   - Guía respiratoria animada con cadencia háptica (*Suspiro fisiológico*, *Coherencia 4-6*, *Caja 4-4-4-4*), ejercicios breves de afrontamiento de 3 minutos (*Surfear la ola*, *Anclaje sensorial 5-4-3-2-1*, *Pausa de margen*), **guía conversacional acotada a contenido clínico revisado** (reducción de daños, hidratación/descanso, PrEP/PEP/ITS, señales de alarma con derivación directa al `112`), registro voluntario de GHB/GBL y contacto configurable con **enfermería navegadora**.
6. **Mi progreso (`ProgressScreen`):**
   - Horizonte flexible de 12 semanas compatible con metas de reducción de daños, pausa temporal o abstinencia; curva interactiva multi-serie (*Control percibido*, *Ánimo*, *Craving*); autoevaluaciones de **Inicio**, **Semana 6** y **Semana 12**; e hitos privados discretos **sin rachas que se rompan ni penalización por recaídas**.
7. **Configuración (`SettingsScreen`):**
   - Privacidad desde el diseño, modo discreto, permisos revocables en cualquier momento, edición y **versionado auditable** de umbrales experimentales JITAI, trazas explicables de decisiones, prueba de notificación neutra, exportación local de **resumen clínico pactado en JSON** (omitiendo coordenadas) y borrado total irreversible en el dispositivo.

### Salvaguarda específica en el registro voluntario de GHB/GBL
El componente `GhbVoluntaryTrackerCard` muestra en tiempo real (`HH:MM:SS`) únicamente el **tiempo transcurrido desde el registro voluntario anterior**, acompañado de advertencia expresa: **no calcula ni recomienda dosis, mililitros ni intervalos considerados «seguros»**, y ofrece marcación directa al `112` ante somnolencia intensa o depresión respiratoria.

---

## 3. Motor JITAI Local y Explicable (`JitaiEngine`)

Todas las reglas se evalúan localmente en milisegundos y pueden auditarse desde la interfaz (`¿Por qué veo esto?`):

- **Craving elevado o bajo control percibido:** Si `craving ≥ 7/10` o `control percibido ≤ 3/10`, la persona está **disponible** y **no está conduciendo**, BRÚJULA ofrece elegir entre un ejercicio breve de afrontamiento (3 min) o contactar con enfermería navegadora, y habilita una reevaluación voluntaria a los 20 minutos.
- ** Independencia del deseo sexual:** Un deseo sexual alto (`8–10/10`) con craving bajo y control conservado **nunca dispara alertas**.
- **Entrada en zona amarilla o roja:** Propone únicamente un **check-in voluntario y neutro**, sin inferir consumo ni exponer el nombre del lugar en notificaciones.
- **Protección frente a fatiga de alertas:** Máximo **2 notificaciones automáticas diarias**, respeto estricto del **periodo de silencio nocturno** configurable (por defecto `23:00 – 08:00`) y **pausa automática de 4 horas** tras posponer o rechazar una sugerencia.
- **Acceso manual permanente:** Todas las herramientas de respiración, afrontamiento, reducción de daños y urgencias (`112`) permanecen abiertas las 24 horas con independencia de límites o pausas automáticas.
- **Naturaleza experimental:** Los puntos de corte iniciales son hipótesis operativas configurables y versionadas (`v1`, `v2`, …), no umbrales diagnósticos clínicos validados.

---

## 4. Arquitectura Técnica y Privacidad

```text
app/src/main/java/es/brujula/jitai/
├── core/
│   ├── data/           # BrujulaRepository + CuratedClinicalContent (guía revisada sin alucinación libre)
│   ├── database/       # Room SQLite local (entidades, DAO y semilla realista inicial)
│   ├── datastore/      # DataStore Preferences (reglas JITAI versionadas, privacidad y perfil enfermero)
│   ├── designsystem/   # Paleta Jade/Marfil/Petróleo, tipografía tabular, háptica y escalas táctiles
│   ├── jitai/          # JitaiEngine (reglas explicables) + BrujulaNotifier (avisos neutros)
│   ├── location/       # LocalZoneMonitor (geolocalización 100% en dispositivo con LocationManager)
│   └── model/          # Modelos de dominio serializables
├── ui/
│   ├── components/     # LivingCompassCanvas y GhbVoluntaryTrackerCard
│   ├── navigation/     # BrujulaAppShell (navegación conectada entre las 7 pantallas)
│   ├── screens/        # Home, CheckIn, Zones, MyDay, Support, Progress y Settings
│   └── BrujulaViewModel.kt
├── BrujulaApp.kt
└── MainActivity.kt
```

- **Requisitos:** Android `minSdk 28` (Android 9+) · `targetSdk 35` · JDK 17 · Gradle 8.9 · Kotlin 2.0.21 · Compose BOM `2024.10.00`.
- **Coste operativo:** `0 €` (persistencia 100 % local en Room + DataStore, cartografía vectorial propia en Compose `Canvas` y geolocalización nativa del sistema sin APIs externas ni telemetría de terceros).

---

## 5. Compilación e Instalación

### Compilar APK de depuración instalable

```bash
./gradlew assembleDebug
```

La APK generada se ubicará en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Instalar y abrir mediante ADB

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n es.brujula.jitai/.MainActivity
```

---

## 6. Nota Ética y Sanitaria

BRÚJULA es una herramienta tecnológica de apoyo a la autogestión y reducción de daños diseñada en el marco de un proyecto universitario de innovación en salud digital. **No sustituye la valoración médica o enfermera presencial ni constituye un servicio de urgencias 24 horas.** Ante cualquier emergencia vital o sospecha de sobredosis, llama inmediatamente al **112**.
