package es.brujula.jitai.core.model

import kotlinx.serialization.Serializable

enum class ThemePreference(val label: String) {
    SYSTEM("Automático"),
    LIGHT("Claro marfil"),
    DARK("Noche petróleo")
}

enum class ProgramGoalType(val title: String, val summary: String) {
    HARM_REDUCTION(
        "Cuidar mis márgenes",
        "Ganar calma, proteger mi descanso y reducir momentos de desborde."
    ),
    TEMPORARY_PAUSE(
        "Tomarme una pausa",
        "Abrir espacio durante unas semanas a mi propio ritmo."
    ),
    ABSTINENCE(
        "Caminar sin consumo",
        "Sostener mis días con apoyo cercano, sin culpa si hay un tropiezo."
    )
}

/**
 * Representación visual no diagnóstica de la brújula según el último autoinforme o contexto.
 * Nunca etiqueta patología; refleja suavemente el rumbo percibido.
 */
enum class CompassBearingState(
    val shortTitle: String,
    val poeticReading: String,
    val targetAzimuthDeg: Float
) {
    SERENE_HORIZON(
        shortTitle = "Aguas tranquilas",
        poeticReading = "Hay espacio y calma para elegir tu ritmo.",
        targetAzimuthDeg = 16f
    ),
    VITAL_AUTONOMY(
        shortTitle = "Pulso vital",
        poeticReading = "Tu energía y tu deseo te pertenecen, en calma.",
        targetAzimuthDeg = 48f
    ),
    ATTENTIVE_BREEZE(
        shortTitle = "Brisa cambiante",
        poeticReading = "Bajar media marcha puede devolverte claridad.",
        targetAzimuthDeg = 320f
    ),
    INTENSE_SWELL(
        shortTitle = "Oleaje intenso",
        poeticReading = "Respira aquí un instante; no tienes que decidir deprisa.",
        targetAzimuthDeg = 286f
    )
}

enum class SubstanceContextTag(val label: String) {
    NONE("Sin especificar"),
    STIMULANTS("Estimulantes"),
    COMBINED("Combinado"),
    OTHER("Otro contexto")
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
        title = "Espacio refugio",
        shortBadge = "Refugio",
        clinicalMeaning = "Lugar que te aporta descanso, calma o buena compañía."
    ),
    YELLOW(
        title = "Zona de atención",
        shortBadge = "Atención",
        clinicalMeaning = "Un entorno donde viene bien parar un segundo y escucharte."
    ),
    RED(
        title = "Zona sensible",
        shortBadge = "Sensible",
        clinicalMeaning = "Solo te invita suavemente a ver cómo estás, sin dar nada por hecho."
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
    MORNING("Mañana", "09:00"),
    AFTERNOON("Tarde", "17:30"),
    NIGHT("Noche", "22:00"),
    LATE_NIGHT("Madrugada", "01:30")
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
data class CopingSessionLog(
    val id: Long = 0,
    val timestampMillis: Long,
    val toolId: String,
    val toolTitle: String,
    val durationSeconds: Int,
    val helpfulRating: Int? = null
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
    EMA_COMPLETED("Check-in"),
    TWENTY_MIN_REEVAL("Pausa 20 min"),
    ZONE_ENTRY("Zona cercana"),
    VULNERABLE_SLOT("Momento sensible"),
    MANUAL_REQUEST("Manual")
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
    val userResponse: String = "PENDIENTE"
)

@Serializable
data class NurseNavigatorProfile(
    val nurseName: String = "Elena Martín · Enfermera referente",
    val centerName: String = "Unidad de Salud Sexual y Acompañamiento",
    val phone: String = "+34 910 00 12 34",
    val contactChannelNote: String = "Contacto directo en horario asistencial",
    val scheduleLabel: String = "Lunes a viernes · 09:00 – 17:00",
    val responseSlaLabel: String = "Respuesta habitual en el día laborable"
)

@Serializable
data class JitaiExperimentalSettings(
    val cravingThreshold: Int = 7,
    val controlThreshold: Int = 3,
    val maxDailyNotifications: Int = 2,
    val quietHoursStartHour: Int = 23,
    val quietHoursEndHour: Int = 8,
    val rejectionPauseHours: Int = 4,
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
    val personalMotto: String = "Respiro primero; elijo mi siguiente paso.",
    val programGoal: ProgramGoalType = ProgramGoalType.HARM_REDUCTION,
    val currentProgramWeek: Int = 4,
    val activeZoneId: Long? = null,
    val ruleVersion: Int = 1,
    val ruleChangeHistory: List<String> = listOf(
        "v1 · Sensibilidad inicial: impulso ≥7/10 o margen ≤3/10, máx. 2 avisos/día, pausa 4 h."
    )
)

data class BreathingPattern(
    val id: String,
    val title: String,
    val shortTag: String,
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
    val configuredZonesCount: Int,
    val ruleVersion: Int,
    val thresholdsSummary: String,
    val milestoneReflections: List<MilestoneReflection>
)
