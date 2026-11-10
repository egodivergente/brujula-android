package es.brujula.jitai.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import es.brujula.jitai.core.model.CopingSessionLog
import es.brujula.jitai.core.model.DayPlanItem
import es.brujula.jitai.core.model.DayTimeBlock
import es.brujula.jitai.core.model.EmaCheckIn
import es.brujula.jitai.core.model.JitaiActionKind
import es.brujula.jitai.core.model.JitaiDecisionRecord
import es.brujula.jitai.core.model.JitaiTriggerSource
import es.brujula.jitai.core.model.MilestoneReflection
import es.brujula.jitai.core.model.PersonalZone
import es.brujula.jitai.core.model.ZoneCategory

@Entity(tableName = "ema_checkins")
data class EmaCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val mood: Int,
    val craving: Int,
    val sexualDesire: Int,
    val perceivedControl: Int,
    val availableForSupport: Boolean,
    val isDriving: Boolean,
    val substanceTag: String,
    val personalNote: String,
    val isReevaluation: Boolean
) {
    fun toDomain() = EmaCheckIn(
        id = id,
        timestampMillis = timestampMillis,
        mood = mood,
        craving = craving,
        sexualDesire = sexualDesire,
        perceivedControl = perceivedControl,
        availableForSupport = availableForSupport,
        isDriving = isDriving,
        substanceTag = substanceTag,
        personalNote = personalNote,
        isReevaluation = isReevaluation
    )
}

fun EmaCheckIn.toEntity() = EmaCheckInEntity(
    id = id,
    timestampMillis = timestampMillis,
    mood = mood,
    craving = craving,
    sexualDesire = sexualDesire,
    perceivedControl = perceivedControl,
    availableForSupport = availableForSupport,
    isDriving = isDriving,
    substanceTag = substanceTag,
    personalNote = personalNote,
    isReevaluation = isReevaluation
)

@Entity(tableName = "personal_zones")
data class PersonalZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val relativeX: Float,
    val relativeY: Float,
    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Int,
    val anchorReminder: String,
    val enabled: Boolean
) {
    fun toDomain() = PersonalZone(
        id = id,
        name = name,
        category = runCatching { ZoneCategory.valueOf(category) }.getOrDefault(ZoneCategory.GREEN),
        relativeX = relativeX,
        relativeY = relativeY,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        anchorReminder = anchorReminder,
        enabled = enabled
    )
}

fun PersonalZone.toEntity() = PersonalZoneEntity(
    id = id,
    name = name,
    category = category.name,
    relativeX = relativeX,
    relativeY = relativeY,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    anchorReminder = anchorReminder,
    enabled = enabled
)

@Entity(tableName = "day_plan_items")
data class DayPlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timeBlock: String,
    val timeLabel: String,
    val isVulnerableWindow: Boolean,
    val chosenAlternative: String,
    val completed: Boolean
) {
    fun toDomain() = DayPlanItem(
        id = id,
        title = title,
        timeBlock = runCatching { DayTimeBlock.valueOf(timeBlock) }.getOrDefault(DayTimeBlock.NIGHT),
        timeLabel = timeLabel,
        isVulnerableWindow = isVulnerableWindow,
        chosenAlternative = chosenAlternative,
        completed = completed
    )
}

fun DayPlanItem.toEntity() = DayPlanItemEntity(
    id = id,
    title = title,
    timeBlock = timeBlock.name,
    timeLabel = timeLabel,
    isVulnerableWindow = isVulnerableWindow,
    chosenAlternative = chosenAlternative,
    completed = completed
)

@Entity(tableName = "coping_sessions")
data class CopingSessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val toolId: String,
    val toolTitle: String,
    val durationSeconds: Int,
    val helpfulRating: Int?
) {
    fun toDomain() = CopingSessionLog(
        id = id,
        timestampMillis = timestampMillis,
        toolId = toolId,
        toolTitle = toolTitle,
        durationSeconds = durationSeconds,
        helpfulRating = helpfulRating
    )
}

@Entity(tableName = "milestone_reflections")
data class MilestoneReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checkpointWeek: Int,
    val timestampMillis: Long,
    val daysUsedLastMonth: Int,
    val lossOfControlEpisodes: Int,
    val wellbeingScore: Int,
    val goalAttainmentScore: Int,
    val reflectionNote: String
) {
    fun toDomain() = MilestoneReflection(
        id = id,
        checkpointWeek = checkpointWeek,
        timestampMillis = timestampMillis,
        daysUsedLastMonth = daysUsedLastMonth,
        lossOfControlEpisodes = lossOfControlEpisodes,
        wellbeingScore = wellbeingScore,
        goalAttainmentScore = goalAttainmentScore,
        reflectionNote = reflectionNote
    )
}

@Entity(tableName = "jitai_decisions")
data class JitaiDecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val triggerSource: String,
    val actionKind: String,
    val headline: String,
    val explanation: String,
    val ruleTrace: String,
    val wasNotified: Boolean,
    val userResponse: String
) {
    fun toDomain() = JitaiDecisionRecord(
        id = id,
        timestampMillis = timestampMillis,
        triggerSource = runCatching { JitaiTriggerSource.valueOf(triggerSource) }
            .getOrDefault(JitaiTriggerSource.EMA_COMPLETED),
        actionKind = runCatching { JitaiActionKind.valueOf(actionKind) }
            .getOrDefault(JitaiActionKind.NONE),
        headline = headline,
        explanation = explanation,
        ruleTrace = ruleTrace,
        wasNotified = wasNotified,
        userResponse = userResponse
    )
}

fun JitaiDecisionRecord.toEntity() = JitaiDecisionEntity(
    id = id,
    timestampMillis = timestampMillis,
    triggerSource = triggerSource.name,
    actionKind = actionKind.name,
    headline = headline,
    explanation = explanation,
    ruleTrace = ruleTrace,
    wasNotified = wasNotified,
    userResponse = userResponse
)
