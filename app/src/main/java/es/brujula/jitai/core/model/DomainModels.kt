package es.brujula.jitai.core.model

import kotlinx.serialization.Serializable

enum class ThemePreference(val label: String) {
    SYSTEM("Automático del sistema"),
    LIGHT("Claro · Marfil cálido"),
    DARK("Oscuro · Petróleo nocturno")
}

enum class ProgramGoalType(val title: String, val summary: String) {
    HARM_REDUCTION(
        "Reducción de daños y mayor margen",
        "Disminuir episodios de pérdida de control, cuidar descansos y proteger mi bienestar integral."
    ),
    TEMPORARY_PAUSE(
        "Pausa temporal pactada",
        "Tomar distancia durante las próximas semanas revisando herramientas con enfermería."
    ),
    ABSTINENCE(
        "Abstinencia acompañada",
        "Sostener días sin consumo con alternativas elegidas, sin culpabilizar si ocurre un tropiezo."
    )
}

/**
 * Representación visual no diagnóstica de la brújula según el último autoinforme o contexto.
 * Nunca etiqueta patología; refleja el rumbo percibido por la propia persona.
 */
enum class CompassBearingState(
    val shortTitle: String,
    val poeticReading: String,
    val targetAzimuthDeg: Float
) {
    SERENE_HORIZON(
        shortTitle = "Rumbo sereno",
        poeticReading = "Cuentas con margen amplio para elegir tu ritmo hoy.",
        targetAzimuthDeg = 18f
    ),
    VITAL_AUTONOMY(
        shortTitle = "Energía y deseo autónomo",
        poeticReading = "El deseo sexual forma parte de tu vitalidad y no activa alertas clínicas.",
        targetAzimuthDeg = 52f
    ),
    ATTENTIVE_BREEZE(
        shortTitle = "Brisa cambiante",
        poeticReading = "Momento sensible: bajar media marcha puede darte más claridad.",
        targetAzimuthDeg = 318f
    ),
    INTENSE_SWELL(
        shortTitle = "Oleaje intenso",
        poeticReading = "Tu registro indica intensidad; aquí tienes anclaje breve o apoyo humano si lo deseas.",
        targetAzimuthDeg = 285f
    )
}

enum class SubstanceContextTag(val label: String) {
    NONE("Prefiero no indicar"),
    METHAMPHETAMINE("Metanfetamina"),
    MEPHEDRONE("Mefedrona"),
    GHB_GBL("GHB / GBL"),
    COMBINED("Contexto combinado")
}

@Serializable
data class EmaCheckIn(
    val id: Long = 0,
    val timestampMillis: Long,
    val mood: Int,                    // 0..10
    val craving: Int,                 // 0..10
    val sexualDesire: Int,            // 0..10 (independiente, nunca dispara alerta aislado)
    val perceivedControl: Int,        // 0..10
    val availableForSupport: Boolean, // receptividad
    val isDriving: Boolean,           // seguridad: ausencia de conducción requerida
    val substanceTag: String = SubstanceContextTag.NONE.name,
    val personalNote: String = "",
    val isReevaluation: Boolean = false
)

enum class ZoneCategory(
    val title: String,
    val shortBadge: String,
    val clinicalMeaning: String
) {
    GREEN(
        title = "Zona verde · Apoyo",
        shortBadge = "Verde",
        clinicalMeaning = "Espacio protector, descanso, red afectiva o actividad saludable."
    ),
    YELLOW(
        title = "Zona amarilla · Vulnerabilidad",
        shortBadge = "Amarilla",
        clinicalMeaning = "Contexto que puede invitar a parar un segundo y comprobar cómo estás."
    ),
    RED(
        title = "Zona roja · Desencadenante",
        shortBadge = "Roja",
        clinicalMeaning = "Entorno relevante configurado por ti; solo sugiere un check-in voluntario sin inferir consumo."
    )
}

@Serializable
data class PersonalZone(
    val id: Long = 0,
    val name: String,
    val category: ZoneCategory,
    val relativeX: Float,             // Coordenada métrica local en plano (-1f..1f = ±1.5 km)
    val relativeY: Float,
    val latitude: Double? = null,     // Opcional, solo procesado en el teléfono
    val longitude: Double? = null,
    val radiusMeters: Int = 250,
    val anchorReminder: String = "",
    val enabled: Boolean = true
)

enum class DayTimeBlock(val label: String, val hoursHint: String) {
    MORNING("Mañana", "08:00 – 14:00"),
    AFTERNOON("Tarde", "14:00 – 20:00"),
    NIGHT("Noche", "20:00 – 01:00"),
    LATE_NIGHT("Madrugada", "01:00 – 08:00")
}

@Serializable
data class DayPlanItem(
    val id: Long = 0,
    val title: String,
    val timeBlock: DayTimeBlock,
    val timeLabel: String,
    val isVulnerableWindow: Boolean,
    val chosenAlternative: String,
    val completed: Boolean = false
)

@Serializable
data class GhbVoluntaryLog(
    val id: Long = 0,
    val timestampMillis: Long,
    val contextNote: String = ""
)

@Serializable
data class CopingSessionLog(
    val id: Long = 0,
    val timestampMillis: Long,
    val toolId: String,
    val toolTitle: String,
    val durationSeconds: Int,
    val helpfulRating: Int? = null // 1..5 opcional para revisión semanal CAME D1->C1
)

@Serializable
data class MilestoneReflection(
    val id: Long = 0,
    val checkpointWeek: Int,         // 0 (Inicio), 6 (Semana 6), 12 (Semana 12)
    val timestampMillis: Long,
    val daysUsedLastMonth: Int,
    val lossOfControlEpisodes: Int,
    val wellbeingScore: Int,         // 0..10
    val goalAttainmentScore: Int,    // 0..10
    val reflectionNote: String = ""
)

enum class JitaiTriggerSource(val label: String) {
    EMA_COMPLETED("Tras check-in EMA"),
    TWENTY_MIN_REEVAL("Reevaluación voluntaria (20 min)"),
    ZONE_ENTRY("Entrada en zona contextual"),
    VULNERABLE_SLOT("Inicio de franja sensible"),
    MANUAL_REQUEST("Consulta manual")
}

enum class JitaiActionKind {
    NONE,
    CHOOSE_COPING_OR_HUMAN,
    OFFER_PLANNED_ALTERNATIVE,
    INVITE_NEUTRAL_CHECKIN,
    INVITE_CRAVING_REEVALUATION,
    SUPPRESSED_BY_RULE
}

@Serializable
data class JitaiDecisionRecord(
    val id: Long = 0,
    val timestampMillis: Long,
    val triggerSource: JitaiTriggerSource,
    val actionKind: JitaiActionKind,
    val headline: String,
    val explanation: String,
    val ruleTrace: String,
    val wasNotified: Boolean = false,
    val userResponse: String = "PENDIENTE" // ACEPTADO, POSPUESTO_4H, REVISADO
)

@Serializable
data class NurseNavigatorProfile(
    val nurseName: String = "Elena Martín · Enfermera navegadora",
    val centerName: String = "CAD / Unidad Ambulatoria de Salud Sexual y Adicciones",
    val phone: String = "+34 910 00 12 34",
    val contactChannelNote: String = "Mensajería clínica asíncrona y cita telefónica pactada",
    val scheduleLabel: String = "Lunes a viernes · 09:00 – 17:00 h",
    val responseSlaLabel: String = "Respuesta habitual en ≤ 1 día laborable (sin vigilancia 24 h)"
)

@Serializable
data class JitaiExperimentalSettings(
    val cravingThreshold: Int = 7,           // Hipótesis experimental: >= 7/10
    val controlThreshold: Int = 3,           // Hipótesis experimental: <= 3/10
    val maxDailyNotifications: Int = 2,      // Tope estricto diario: máx. 2
    val quietHoursStartHour: Int = 23,       // 23:00
    val quietHoursEndHour: Int = 8,          // 08:00
    val rejectionPauseHours: Int = 4,        // 4 horas tras rechazo
    val pausedUntilMillis: Long = 0L,
    val reevalDueAtMillis: Long = 0L,
    val notificationsSentToday: Int = 0,
    val notificationsEpochDay: Long = 0L,
    val optionalGpsEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val discreetModeEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val largeTextEnabled: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val personalMotto: String = "Orientarme sin juzgarme; elegir mi próximo paso.",
    val programGoal: ProgramGoalType = ProgramGoalType.HARM_REDUCTION,
    val currentProgramWeek: Int = 4,
    val activeZoneId: Long? = null,
    val ruleVersion: Int = 1,
    val ruleChangeHistory: List<String> = listOf(
        "v1 · Reglas iniciales pactadas: craving ≥7/10 o control ≤3/10, máx. 2 avisos/día, pausa 4 h."
    )
)

data class BreathingPattern(
    val id: String,
    val title: String,
    val subtitle: String,
    val inhaleSec: Int,
    val holdAfterInhaleSec: Int,
    val exhaleSec: Int,
    val holdAfterExhaleSec: Int,
    val recommendedCycles: Int
)

data class CopingExerciseCard(
    val id: String,
    val title: String,
    val durationLabel: String,
    val category: String,
    val summary: String,
    val steps: List<String>
)

data class CuratedGuidanceTopic(
    val id: String,
    val title: String,
    val category: String,
    val keywords: List<String>,
    val reviewedResponse: String,
    val keyTakeaways: List<String>,
    val showEmergency112Banner: Boolean = false
)

data class PrivateMilestoneBadge(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val progressLabel: String
)

@Serializable
data class AgreedClinicalSummaryExport(
    val generatedAtIso: String,
    val privacyNotice: String,
    val programWeek: Int,
    val goal: String,
    val totalCheckInsCompleted: Int,
    val last7DaysAdherencePercent: Int,
    val averageMood: Float,
    val averageCraving: Float,
    val averagePerceivedControl: Float,
    val copingSessionsCompleted: Int,
    val configuredZonesCount: Int, // Coordenadas omitidas deliberadamente por privacidad CAME F3->M3
    val ruleVersion: Int,
    val thresholdsSummary: String,
    val milestoneReflections: List<MilestoneReflection>
)
