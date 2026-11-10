package es.brujula.jitai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.brujula.jitai.core.data.BrujulaRepository
import es.brujula.jitai.core.data.CuratedClinicalContent
import es.brujula.jitai.core.jitai.JitaiEngine
import es.brujula.jitai.core.location.LocalGpsSnapshot
import es.brujula.jitai.core.model.CompassBearingState
import es.brujula.jitai.core.model.CopingSessionLog
import es.brujula.jitai.core.model.CuratedGuidanceTopic
import es.brujula.jitai.core.model.DayPlanItem
import es.brujula.jitai.core.model.DayTimeBlock
import es.brujula.jitai.core.model.EmaCheckIn
import es.brujula.jitai.core.model.GhbVoluntaryLog
import es.brujula.jitai.core.model.JitaiDecisionRecord
import es.brujula.jitai.core.model.JitaiExperimentalSettings
import es.brujula.jitai.core.model.MilestoneReflection
import es.brujula.jitai.core.model.NurseNavigatorProfile
import es.brujula.jitai.core.model.PersonalZone
import es.brujula.jitai.core.model.PrivateMilestoneBadge
import es.brujula.jitai.core.model.ProgramGoalType
import es.brujula.jitai.core.model.SubstanceContextTag
import es.brujula.jitai.core.model.ThemePreference
import es.brujula.jitai.core.model.ZoneCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BrujulaUiState(
    val settings: JitaiExperimentalSettings = JitaiExperimentalSettings(),
    val nurseProfile: NurseNavigatorProfile = NurseNavigatorProfile(),
    val checkIns: List<EmaCheckIn> = emptyList(),
    val zones: List<PersonalZone> = emptyList(),
    val dayPlan: List<DayPlanItem> = emptyList(),
    val ghbLogs: List<GhbVoluntaryLog> = emptyList(),
    val copingSessions: List<CopingSessionLog> = emptyList(),
    val milestones: List<MilestoneReflection> = emptyList(),
    val jitaiDecisions: List<JitaiDecisionRecord> = emptyList(),
    val compassBearing: CompassBearingState = CompassBearingState.SERENE_HORIZON,
    val activeZone: PersonalZone? = null,
    val privateBadges: List<PrivateMilestoneBadge> = emptyList(),
    val adherenceLast7DaysPercent: Int = 0,
    val lastGpsSnapshot: LocalGpsSnapshot? = null,
    val gpsStatusMessage: String? = null,
    val exportedSummaryJson: String? = null,
    val statusBannerMessage: String? = null,
    val isReevaluationMode: Boolean = false
)

private data class CoreStreamsBundle(
    val settings: JitaiExperimentalSettings,
    val nurse: NurseNavigatorProfile,
    val checkIns: List<EmaCheckIn>,
    val zones: List<PersonalZone>,
    val dayPlan: List<DayPlanItem>
)

private data class SecondaryStreamsBundle(
    val ghbLogs: List<GhbVoluntaryLog>,
    val copingSessions: List<CopingSessionLog>,
    val milestones: List<MilestoneReflection>,
    val decisions: List<JitaiDecisionRecord>
)

private data class EphemeralUiFeedback(
    val gpsSnapshot: LocalGpsSnapshot? = null,
    val gpsMessage: String? = null,
    val exportedJson: String? = null,
    val bannerMessage: String? = null,
    val reevalMode: Boolean = false
)

class BrujulaViewModel(
    private val repository: BrujulaRepository
) : ViewModel() {

    private val ephemeralState = MutableStateFlow(EphemeralUiFeedback())

    private val coreBundleFlow = combine(
        repository.settingsFlow,
        repository.nurseProfileFlow,
        repository.checkInsFlow,
        repository.zonesFlow,
        repository.dayPlanFlow
    ) { settings, nurse, checkIns, zones, dayPlan ->
        CoreStreamsBundle(settings, nurse, checkIns, zones, dayPlan)
    }

    private val secondaryBundleFlow = combine(
        repository.ghbLogsFlow,
        repository.copingSessionsFlow,
        repository.milestonesFlow,
        repository.jitaiDecisionsFlow
    ) { ghb, coping, milestones, decisions ->
        SecondaryStreamsBundle(ghb, coping, milestones, decisions)
    }

    val uiState: StateFlow<BrujulaUiState> = combine(
        coreBundleFlow,
        secondaryBundleFlow,
        ephemeralState
    ) { core, secondary, ephemeral ->
        val activeZone = core.zones.firstOrNull { it.id == core.settings.activeZoneId }
        val bearing = JitaiEngine.deriveCompassBearingState(
            latestCheckIn = core.checkIns.firstOrNull(),
            activeZone = activeZone,
            settings = core.settings
        )
        val badges = repository.computePrivateMilestones(
            checkIns = core.checkIns,
            zones = core.zones,
            planItems = core.dayPlan,
            copingSessions = secondary.copingSessions,
            settings = core.settings
        )
        val adherence = repository.calculateLast7DaysAdherencePercent(core.checkIns)

        BrujulaUiState(
            settings = core.settings,
            nurseProfile = core.nurse,
            checkIns = core.checkIns,
            zones = core.zones,
            dayPlan = core.dayPlan,
            ghbLogs = secondary.ghbLogs,
            copingSessions = secondary.copingSessions,
            milestones = secondary.milestones,
            jitaiDecisions = secondary.decisions,
            compassBearing = bearing,
            activeZone = activeZone,
            privateBadges = badges,
            adherenceLast7DaysPercent = adherence,
            lastGpsSnapshot = ephemeral.gpsSnapshot,
            gpsStatusMessage = ephemeral.gpsMessage,
            exportedSummaryJson = ephemeral.exportedJson,
            statusBannerMessage = ephemeral.bannerMessage,
            isReevaluationMode = ephemeral.reevalMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BrujulaUiState()
    )

    fun enterReevaluationMode(enabled: Boolean) {
        ephemeralState.value = ephemeralState.value.copy(reevalMode = enabled)
    }

    fun clearBannerMessage() {
        ephemeralState.value = ephemeralState.value.copy(bannerMessage = null)
    }

    fun clearExportedJson() {
        ephemeralState.value = ephemeralState.value.copy(exportedJson = null)
    }

    fun submitEmaCheckIn(
        mood: Int,
        craving: Int,
        sexualDesire: Int,
        perceivedControl: Int,
        availableForSupport: Boolean,
        isDriving: Boolean,
        substanceTag: SubstanceContextTag,
        personalNote: String,
        isReevaluation: Boolean,
        onEvaluated: (JitaiDecisionRecord) -> Unit = {}
    ) {
        viewModelScope.launch {
            val checkIn = EmaCheckIn(
                timestampMillis = System.currentTimeMillis(),
                mood = mood,
                craving = craving,
                sexualDesire = sexualDesire,
                perceivedControl = perceivedControl,
                availableForSupport = availableForSupport,
                isDriving = isDriving,
                substanceTag = substanceTag.name,
                personalNote = personalNote.trim(),
                isReevaluation = isReevaluation
            )
            val decision = repository.submitCheckIn(checkIn)
            ephemeralState.value = ephemeralState.value.copy(
                reevalMode = false,
                bannerMessage = decision.headline
            )
            onEvaluated(decision)
        }
    }

    fun rejectCurrentSuggestionWithPause(decisionId: Long) {
        viewModelScope.launch {
            repository.rejectJitaiSuggestionWithFourHourPause(decisionId)
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Avisos automáticos en pausa durante 4 horas. Acceso manual siempre abierto."
            )
        }
    }

    fun acceptCurrentSuggestion(decisionId: Long) {
        viewModelScope.launch {
            repository.acknowledgeJitaiSuggestion(decisionId)
        }
    }

    fun resumeAutomaticNotificationsNow() {
        viewModelScope.launch {
            repository.clearFourHourPause()
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Pausa de 4 horas finalizada manualmente."
            )
        }
    }

    fun dismissTwentyMinReeval() {
        viewModelScope.launch {
            repository.dismissTwentyMinuteReevaluation()
        }
    }

    // Zonas
    fun savePersonalZone(
        id: Long = 0L,
        name: String,
        category: ZoneCategory,
        relativeX: Float,
        relativeY: Float,
        radiusMeters: Int,
        anchorReminder: String,
        attachCurrentGps: Boolean
    ) {
        viewModelScope.launch {
            val gps = if (attachCurrentGps && repository.hasLocationPermission()) {
                repository.scanCurrentLocationLocally().first
            } else {
                null
            }
            val zone = PersonalZone(
                id = id,
                name = name.trim().ifBlank { "Zona personal" },
                category = category,
                relativeX = relativeX.coerceIn(-0.92f, 0.92f),
                relativeY = relativeY.coerceIn(-0.92f, 0.92f),
                latitude = gps?.latitude,
                longitude = gps?.longitude,
                radiusMeters = radiusMeters.coerceIn(100, 1000),
                anchorReminder = anchorReminder.trim(),
                enabled = true
            )
            repository.saveZone(zone)
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Zona «${zone.name}» guardada en este dispositivo."
            )
        }
    }

    fun deletePersonalZone(zoneId: Long) {
        viewModelScope.launch {
            repository.deleteZone(zoneId)
        }
    }

    fun selectManualZoneContext(zone: PersonalZone?) {
        viewModelScope.launch {
            val decision = repository.registerZoneContext(zone)
            ephemeralState.value = ephemeralState.value.copy(
                gpsMessage = if (zone != null) {
                    "Contexto activo manual: ${zone.name} (${zone.category.shortBadge})"
                } else {
                    "Sin zona contextual seleccionada"
                },
                bannerMessage = decision?.headline
            )
        }
    }

    fun checkLocalGpsAgainstZones() {
        viewModelScope.launch {
            if (!repository.hasLocationPermission()) {
                ephemeralState.value = ephemeralState.value.copy(
                    gpsMessage = "Permiso de ubicación no concedido. Puedes operar 100% en modo manual."
                )
                return@launch
            }
            val (snapshot, matchedZone) = repository.scanCurrentLocationLocally()
            if (snapshot == null) {
                ephemeralState.value = ephemeralState.value.copy(
                    gpsMessage = "Ubicación del sistema no disponible ahora; tus zonas siguen operativas en modo manual."
                )
            } else if (matchedZone != null) {
                ephemeralState.value = ephemeralState.value.copy(
                    gpsSnapshot = snapshot,
                    gpsMessage = "Comprobación local (±${snapshot.accuracyMeters.toInt()} m): dentro de «${matchedZone.name}».",
                    bannerMessage = "Detectada ${matchedZone.category.title.lowercase()} en el dispositivo."
                )
            } else {
                ephemeralState.value = ephemeralState.value.copy(
                    gpsSnapshot = snapshot,
                    gpsMessage = "Comprobación local (±${snapshot.accuracyMeters.toInt()} m): fuera de zonas amarillas o rojas configuradas."
                )
            }
        }
    }

    // Mi día
    fun savePlanItem(
        id: Long = 0L,
        title: String,
        timeBlock: DayTimeBlock,
        timeLabel: String,
        isVulnerableWindow: Boolean,
        chosenAlternative: String
    ) {
        viewModelScope.launch {
            repository.saveDayPlanItem(
                DayPlanItem(
                    id = id,
                    title = title.trim().ifBlank { "Actividad personal" },
                    timeBlock = timeBlock,
                    timeLabel = timeLabel.trim().ifBlank { timeBlock.hoursHint },
                    isVulnerableWindow = isVulnerableWindow,
                    chosenAlternative = chosenAlternative.trim(),
                    completed = false
                )
            )
        }
    }

    fun togglePlanItemDone(item: DayPlanItem) {
        viewModelScope.launch {
            repository.toggleDayPlanCompleted(item)
        }
    }

    fun deletePlanItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteDayPlanItem(itemId)
        }
    }

    fun triggerVulnerableSlotSupport(item: DayPlanItem) {
        viewModelScope.launch {
            val decision = repository.activateVulnerableWindowPrompt(item)
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = decision.headline
            )
        }
    }

    // Registro voluntario GHB/GBL
    fun addVoluntaryGhbLog(note: String) {
        viewModelScope.launch {
            repository.logVoluntaryGhbEntry(note)
        }
    }

    fun removeVoluntaryGhbLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteGhbEntry(logId)
        }
    }

    fun clearAllVoluntaryGhbLogs() {
        viewModelScope.launch {
            repository.clearGhbHistory()
        }
    }

    // Práctica de apoyo y guía revisada
    fun logCompletedCopingSession(toolId: String, toolTitle: String, durationSeconds: Int) {
        viewModelScope.launch {
            repository.recordCopingPractice(toolId, toolTitle, durationSeconds)
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Práctica registrada en tu progreso privado: $toolTitle."
            )
        }
    }

    fun searchCuratedGuidance(query: String): Pair<Boolean, List<CuratedGuidanceTopic>> {
        return CuratedClinicalContent.queryReviewedGuidance(query)
    }

    // Mi progreso
    fun saveMilestone(
        checkpointWeek: Int,
        daysUsed: Int,
        lossOfControlEpisodes: Int,
        wellbeing: Int,
        goalAttainment: Int,
        note: String
    ) {
        viewModelScope.launch {
            repository.saveMilestoneReflection(
                MilestoneReflection(
                    checkpointWeek = checkpointWeek,
                    timestampMillis = System.currentTimeMillis(),
                    daysUsedLastMonth = daysUsed.coerceIn(0, 31),
                    lossOfControlEpisodes = lossOfControlEpisodes.coerceIn(0, 31),
                    wellbeingScore = wellbeing.coerceIn(0, 10),
                    goalAttainmentScore = goalAttainment.coerceIn(0, 10),
                    reflectionNote = note
                )
            )
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Autoevaluación de Semana $checkpointWeek guardada localmente."
            )
        }
    }

    fun saveOrientationSettings(motto: String, goal: ProgramGoalType, week: Int) {
        viewModelScope.launch {
            repository.updatePersonalOrientation(motto, goal, week)
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Rumbo y meta personal actualizados."
            )
        }
    }

    // Configuración, reglas y privacidad
    fun saveExperimentalRules(
        cravingThreshold: Int,
        controlThreshold: Int,
        maxDailyNotifications: Int,
        quietStartHour: Int,
        quietEndHour: Int
    ) {
        viewModelScope.launch {
            repository.updateThresholds(
                cravingThreshold = cravingThreshold,
                controlThreshold = controlThreshold,
                maxDailyNotifications = maxDailyNotifications,
                quietStartHour = quietStartHour,
                quietEndHour = quietEndHour
            )
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Reglas experimentales actualizadas y versionadas."
            )
        }
    }

    fun setOptionalGps(enabled: Boolean) {
        viewModelScope.launch { repository.setOptionalGpsEnabled(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setDiscreetMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDiscreetModeEnabled(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun setLargeText(enabled: Boolean) {
        viewModelScope.launch { repository.setLargeTextEnabled(enabled) }
    }

    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch { repository.setThemePreference(theme) }
    }

    fun saveNurseNavigatorProfile(profile: NurseNavigatorProfile) {
        viewModelScope.launch {
            repository.updateNurseProfile(profile)
            ephemeralState.value = ephemeralState.value.copy(
                bannerMessage = "Contacto de enfermería actualizado."
            )
        }
    }

    fun triggerTestNeutralNotification() {
        val sent = repository.sendTestNeutralNotification(uiState.value.settings.discreetModeEnabled)
        ephemeralState.value = ephemeralState.value.copy(
            bannerMessage = if (sent) {
                "Notificación neutra enviada para verificar discreción en pantalla."
            } else {
                "Concede permiso de notificaciones del sistema para recibir avisos."
            }
        )
    }

    fun generateAgreedSummaryExport() {
        viewModelScope.launch {
            val json = repository.exportAgreedSummaryJson()
            ephemeralState.value = ephemeralState.value.copy(exportedJson = json)
        }
    }

    fun deleteAllDataFromDevice() {
        viewModelScope.launch {
            repository.wipeAllLocalData()
            ephemeralState.value = EphemeralUiFeedback(
                bannerMessage = "Todos los registros locales se han eliminado del dispositivo."
            )
        }
    }

    class Factory(private val repository: BrujulaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BrujulaViewModel(repository) as T
        }
    }
}
