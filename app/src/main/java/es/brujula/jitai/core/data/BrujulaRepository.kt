package es.brujula.jitai.core.data

import es.brujula.jitai.core.database.BrujulaDao
import es.brujula.jitai.core.database.CopingSessionLogEntity
import es.brujula.jitai.core.database.GhbVoluntaryLogEntity
import es.brujula.jitai.core.database.MilestoneReflectionEntity
import es.brujula.jitai.core.database.toEntity
import es.brujula.jitai.core.datastore.PreferencesRepository
import es.brujula.jitai.core.jitai.BrujulaNotifier
import es.brujula.jitai.core.jitai.JitaiEngine
import es.brujula.jitai.core.location.LocalGpsSnapshot
import es.brujula.jitai.core.location.LocalZoneMonitor
import es.brujula.jitai.core.model.AgreedClinicalSummaryExport
import es.brujula.jitai.core.model.CopingSessionLog
import es.brujula.jitai.core.model.DayPlanItem
import es.brujula.jitai.core.model.EmaCheckIn
import es.brujula.jitai.core.model.GhbVoluntaryLog
import es.brujula.jitai.core.model.JitaiDecisionRecord
import es.brujula.jitai.core.model.JitaiExperimentalSettings
import es.brujula.jitai.core.model.MilestoneReflection
import es.brujula.jitai.core.model.NurseNavigatorProfile
import es.brujula.jitai.core.model.PersonalZone
import es.brujula.jitai.core.model.PrivateMilestoneBadge
import es.brujula.jitai.core.model.ProgramGoalType
import es.brujula.jitai.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

class BrujulaRepository(
    private val dao: BrujulaDao,
    private val prefs: PreferencesRepository,
    private val notifier: BrujulaNotifier,
    private val zoneMonitor: LocalZoneMonitor
) {
    private val exportJson = Json { prettyPrint = true }

    val settingsFlow: Flow<JitaiExperimentalSettings> = prefs.settingsFlow
    val nurseProfileFlow: Flow<NurseNavigatorProfile> = prefs.nurseProfileFlow

    val checkInsFlow: Flow<List<EmaCheckIn>> = dao.observeCheckIns()
        .map { list -> list.map { it.toDomain() } }

    val zonesFlow: Flow<List<PersonalZone>> = dao.observeZones()
        .map { list -> list.map { it.toDomain() } }

    val dayPlanFlow: Flow<List<DayPlanItem>> = dao.observeDayPlan()
        .map { list -> list.map { it.toDomain() } }

    val ghbLogsFlow: Flow<List<GhbVoluntaryLog>> = dao.observeGhbLogs()
        .map { list -> list.map { it.toDomain() } }

    val copingSessionsFlow: Flow<List<CopingSessionLog>> = dao.observeCopingSessions()
        .map { list -> list.map { it.toDomain() } }

    val milestonesFlow: Flow<List<MilestoneReflection>> = dao.observeMilestoneReflections()
        .map { list -> list.map { it.toDomain() } }

    val jitaiDecisionsFlow: Flow<List<JitaiDecisionRecord>> = dao.observeJitaiDecisions()
        .map { list -> list.map { it.toDomain() } }

    suspend fun submitCheckIn(checkIn: EmaCheckIn): JitaiDecisionRecord {
        dao.insertCheckIn(checkIn.toEntity())
        val settings = settingsFlow.first()
        val planItems = dayPlanFlow.first()
        val chosenAlt = planItems.firstOrNull { it.isVulnerableWindow && !it.completed }
            ?: planItems.firstOrNull { !it.completed }

        val outcome = JitaiEngine.evaluateCheckIn(
            checkIn = checkIn,
            settings = settings,
            plannedAlternative = chosenAlt
        )

        if (outcome.scheduleReevalIn20Min) {
            prefs.scheduleTwentyMinuteReevaluation(checkIn.timestampMillis)
        } else if (checkIn.isReevaluation) {
            prefs.clearTwentyMinuteReevaluation()
        }

        val id = dao.insertJitaiDecision(outcome.decision.toEntity())
        return outcome.decision.copy(id = id)
    }

    suspend fun registerZoneContext(zone: PersonalZone?): JitaiDecisionRecord? {
        prefs.setActiveZoneId(zone?.id)
        if (zone == null) return null

        val settings = settingsFlow.first()
        val outcome = JitaiEngine.evaluateZoneEntry(zone, settings)
        var sentPush = false
        if (outcome.shouldSendNotification) {
            sentPush = notifier.sendNeutralOrientationPrompt(settings.discreetModeEnabled)
            if (sentPush) {
                prefs.recordNotificationSent()
            }
        }
        val finalDecision = outcome.decision.copy(wasNotified = sentPush)
        val id = dao.insertJitaiDecision(finalDecision.toEntity())
        return finalDecision.copy(id = id)
    }

    suspend fun activateVulnerableWindowPrompt(item: DayPlanItem): JitaiDecisionRecord {
        val settings = settingsFlow.first()
        val outcome = JitaiEngine.evaluateVulnerableWindowStart(item, settings)
        var sentPush = false
        if (outcome.shouldSendNotification) {
            sentPush = notifier.sendNeutralOrientationPrompt(
                discreetMode = settings.discreetModeEnabled,
                customNeutralSubtitle = "Franja planificada activa · Tu alternativa está disponible."
            )
            if (sentPush) {
                prefs.recordNotificationSent()
            }
        }
        val finalDecision = outcome.decision.copy(wasNotified = sentPush)
        val id = dao.insertJitaiDecision(finalDecision.toEntity())
        return finalDecision.copy(id = id)
    }

    suspend fun rejectJitaiSuggestionWithFourHourPause(decisionId: Long) {
        if (decisionId > 0L) {
            dao.updateDecisionResponse(decisionId, "POSPUESTO_4H")
        }
        prefs.activateFourHourRejectionPause()
    }

    suspend fun acknowledgeJitaiSuggestion(decisionId: Long) {
        if (decisionId > 0L) {
            dao.updateDecisionResponse(decisionId, "ACEPTADO")
        }
    }

    suspend fun clearFourHourPause() {
        prefs.clearRejectionPause()
    }

    suspend fun dismissTwentyMinuteReevaluation() {
        prefs.clearTwentyMinuteReevaluation()
    }

    // Zonas y ubicación opcional en dispositivo
    suspend fun saveZone(zone: PersonalZone) {
        dao.upsertZone(zone.toEntity())
    }

    suspend fun deleteZone(zoneId: Long) {
        val settings = settingsFlow.first()
        if (settings.activeZoneId == zoneId) {
            prefs.setActiveZoneId(null)
        }
        dao.deleteZone(zoneId)
    }

    fun hasLocationPermission(): Boolean = zoneMonitor.hasLocationPermission()

    fun hasNotificationPermission(): Boolean = notifier.hasNotificationPermission()

    suspend fun scanCurrentLocationLocally(): Pair<LocalGpsSnapshot?, PersonalZone?> {
        val snapshot = zoneMonitor.captureCurrentLocationOnDevice() ?: return null to null
        val zones = zonesFlow.first()
        val match = zoneMonitor.findMatchingZone(snapshot, zones)?.first
        if (match != null) {
            registerZoneContext(match)
        }
        return snapshot to match
    }

    // Planificación Mi día
    suspend fun saveDayPlanItem(item: DayPlanItem) {
        dao.upsertDayPlanItem(item.toEntity())
    }

    suspend fun toggleDayPlanCompleted(item: DayPlanItem) {
        dao.updateDayPlanItem(item.copy(completed = !item.completed).toEntity())
    }

    suspend fun deleteDayPlanItem(itemId: Long) {
        dao.deleteDayPlanItem(itemId)
    }

    // Registro voluntario GHB/GBL (sin recomendar dosis ni intervalos seguros)
    suspend fun logVoluntaryGhbEntry(note: String = "", timestampMillis: Long = System.currentTimeMillis()) {
        dao.insertGhbLog(
            GhbVoluntaryLogEntity(
                timestampMillis = timestampMillis,
                contextNote = note.trim()
            )
        )
    }

    suspend fun deleteGhbEntry(logId: Long) {
        dao.deleteGhbLog(logId)
    }

    suspend fun clearGhbHistory() {
        dao.clearAllGhbLogs()
    }

    // Sesiones de respiración y afrontamiento
    suspend fun recordCopingPractice(
        toolId: String,
        toolTitle: String,
        durationSeconds: Int,
        helpfulRating: Int? = 5
    ) {
        dao.insertCopingSession(
            CopingSessionLogEntity(
                timestampMillis = System.currentTimeMillis(),
                toolId = toolId,
                toolTitle = toolTitle,
                durationSeconds = durationSeconds,
                helpfulRating = helpfulRating
            )
        )
    }

    // Hitos semanas 0, 6 y 12
    suspend fun saveMilestoneReflection(reflection: MilestoneReflection) {
        val existing = milestonesFlow.first().firstOrNull { it.checkpointWeek == reflection.checkpointWeek }
        dao.upsertMilestoneReflection(
            MilestoneReflectionEntity(
                id = existing?.id ?: 0L,
                checkpointWeek = reflection.checkpointWeek,
                timestampMillis = System.currentTimeMillis(),
                daysUsedLastMonth = reflection.daysUsedLastMonth,
                lossOfControlEpisodes = reflection.lossOfControlEpisodes,
                wellbeingScore = reflection.wellbeingScore,
                goalAttainmentScore = reflection.goalAttainmentScore,
                reflectionNote = reflection.reflectionNote.trim()
            )
        )
    }

    // Configuración y privacidad
    suspend fun updateThresholds(
        cravingThreshold: Int,
        controlThreshold: Int,
        maxDailyNotifications: Int,
        quietStartHour: Int,
        quietEndHour: Int
    ) {
        prefs.updateExperimentalThresholds(
            cravingThreshold = cravingThreshold,
            controlThreshold = controlThreshold,
            maxDailyNotifications = maxDailyNotifications,
            quietStartHour = quietStartHour,
            quietEndHour = quietEndHour
        )
    }

    suspend fun setOptionalGpsEnabled(enabled: Boolean) = prefs.setOptionalGpsEnabled(enabled)
    suspend fun setNotificationsEnabled(enabled: Boolean) = prefs.setNotificationsEnabled(enabled)
    suspend fun setDiscreetModeEnabled(enabled: Boolean) = prefs.setDiscreetModeEnabled(enabled)
    suspend fun setHapticsEnabled(enabled: Boolean) = prefs.setHapticsEnabled(enabled)
    suspend fun setLargeTextEnabled(enabled: Boolean) = prefs.setLargeTextEnabled(enabled)
    suspend fun setThemePreference(theme: ThemePreference) = prefs.setThemePreference(theme)

    suspend fun updatePersonalOrientation(motto: String, goal: ProgramGoalType, week: Int) {
        prefs.updatePersonalOrientation(motto, goal, week)
    }

    suspend fun updateNurseProfile(profile: NurseNavigatorProfile) {
        prefs.updateNurseProfile(profile)
    }

    fun sendTestNeutralNotification(discreetMode: Boolean): Boolean {
        return notifier.sendNeutralOrientationPrompt(discreetMode)
    }

    fun computePrivateMilestones(
        checkIns: List<EmaCheckIn>,
        zones: List<PersonalZone>,
        planItems: List<DayPlanItem>,
        copingSessions: List<CopingSessionLog>,
        settings: JitaiExperimentalSettings
    ): List<PrivateMilestoneBadge> {
        val vulnerableWithAlt = planItems.count { it.isVulnerableWindow && it.chosenAlternative.isNotBlank() }
        return listOf(
            PrivateMilestoneBadge(
                id = "first_compass",
                title = "Cartografía propia",
                description = "Configurar tus espacios verdes y contextos sensibles para anticiparte sin juicios.",
                unlocked = zones.size >= 2,
                progressLabel = "${zones.size} zonas definidas"
            ),
            PrivateMilestoneBadge(
                id = "flexible_alternatives",
                title = "Plan B elegido por ti",
                description = "Tener alternativas propias preparadas para momentos vulnerables de la semana.",
                unlocked = vulnerableWithAlt >= 1,
                progressLabel = "$vulnerableWithAlt alternativas listas"
            ),
            PrivateMilestoneBadge(
                id = "calm_practice",
                title = "Anclaje en práctica",
                description = "Practicar ejercicios breves de respiración o afrontamiento (sin rachas obligatorias).",
                unlocked = copingSessions.isNotEmpty(),
                progressLabel = "${copingSessions.size} prácticas realizadas"
            ),
            PrivateMilestoneBadge(
                id = "self_observation",
                title = "Escucha honesta",
                description = "Completar registros EMA para conocer mejor tu ánimo, deseo y margen de control.",
                unlocked = checkIns.size >= 3,
                progressLabel = "${checkIns.size} registros acumulados"
            ),
            PrivateMilestoneBadge(
                id = "co_designed_rules",
                title = "Reglas a tu medida",
                description = "Revisar o personalizar tus umbrales experimentales y horarios de silencio.",
                unlocked = settings.ruleVersion >= 2,
                progressLabel = "Versión v${settings.ruleVersion}"
            )
        )
    }

    fun calculateLast7DaysAdherencePercent(checkIns: List<EmaCheckIn>): Int {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val sevenDaysAgo = today.minusDays(6)
        val distinctDays = checkIns
            .map { Instant.ofEpochMilli(it.timestampMillis).atZone(zoneId).toLocalDate() }
            .filter { !it.isBefore(sevenDaysAgo) && !it.isAfter(today) }
            .toSet()
            .size
        return ((distinctDays / 7f) * 100f).roundToInt().coerceIn(0, 100)
    }

    suspend fun exportAgreedSummaryJson(): String {
        val settings = settingsFlow.first()
        val checkIns = checkInsFlow.first()
        val zones = zonesFlow.first()
        val coping = copingSessionsFlow.first()
        val milestones = milestonesFlow.first()

        val avgMood = if (checkIns.isNotEmpty()) checkIns.map { it.mood }.average().toFloat() else 0f
        val avgCraving = if (checkIns.isNotEmpty()) checkIns.map { it.craving }.average().toFloat() else 0f
        val avgControl = if (checkIns.isNotEmpty()) checkIns.map { it.perceivedControl }.average().toFloat() else 0f

        val summary = AgreedClinicalSummaryExport(
            generatedAtIso = Instant.now().toString(),
            privacyNotice = "Resumen voluntario generado en el dispositivo (CAME F3→M3). Excluye coordenadas GPS, nombres de zonas exactas y notas privadas no seleccionadas.",
            programWeek = settings.currentProgramWeek,
            goal = settings.programGoal.title,
            totalCheckInsCompleted = checkIns.size,
            last7DaysAdherencePercent = calculateLast7DaysAdherencePercent(checkIns),
            averageMood = (avgMood * 10).roundToInt() / 10f,
            averageCraving = (avgCraving * 10).roundToInt() / 10f,
            averagePerceivedControl = (avgControl * 10).roundToInt() / 10f,
            copingSessionsCompleted = coping.size,
            configuredZonesCount = zones.size,
            ruleVersion = settings.ruleVersion,
            thresholdsSummary = "Craving ≥${settings.cravingThreshold}/10 · Control ≤${settings.controlThreshold}/10 · Máx ${settings.maxDailyNotifications}/día",
            milestoneReflections = milestones
        )
        return exportJson.encodeToString(summary)
    }

    suspend fun wipeAllLocalData() {
        dao.clearCheckIns()
        dao.clearZones()
        dao.clearDayPlan()
        dao.clearAllGhbLogs()
        dao.clearCopingSessions()
        dao.clearMilestones()
        dao.clearDecisions()
        prefs.resetAllPreferences()
    }
}
