package es.brujula.jitai.core.jitai

import es.brujula.jitai.core.model.CompassBearingState
import es.brujula.jitai.core.model.DayPlanItem
import es.brujula.jitai.core.model.EmaCheckIn
import es.brujula.jitai.core.model.JitaiActionKind
import es.brujula.jitai.core.model.JitaiDecisionRecord
import es.brujula.jitai.core.model.JitaiExperimentalSettings
import es.brujula.jitai.core.model.JitaiTriggerSource
import es.brujula.jitai.core.model.PersonalZone
import es.brujula.jitai.core.model.ZoneCategory
import java.time.Instant
import java.time.ZoneId

data class JitaiEvaluationOutcome(
    val decision: JitaiDecisionRecord,
    val shouldSendNotification: Boolean,
    val scheduleReevalIn20Min: Boolean
)

/**
 * Motor JITAI local, determinista y explicable.
 * Mensajes breves, serenos y libres de ruido cognitivo en momentos sensibles.
 */
object JitaiEngine {

    fun deriveCompassBearingState(
        latestCheckIn: EmaCheckIn?,
        activeZone: PersonalZone?,
        settings: JitaiExperimentalSettings
    ): CompassBearingState {
        if (latestCheckIn != null) {
            if (latestCheckIn.craving >= settings.cravingThreshold ||
                latestCheckIn.perceivedControl <= settings.controlThreshold
            ) {
                return CompassBearingState.INTENSE_SWELL
            }
            if (latestCheckIn.craving >= (settings.cravingThreshold - 2).coerceAtLeast(4) ||
                latestCheckIn.perceivedControl <= (settings.controlThreshold + 1) ||
                latestCheckIn.mood <= 3
            ) {
                return CompassBearingState.ATTENTIVE_BREEZE
            }
            if (latestCheckIn.sexualDesire >= 7 && latestCheckIn.craving < settings.cravingThreshold) {
                return CompassBearingState.VITAL_AUTONOMY
            }
        }
        if (activeZone != null && (activeZone.category == ZoneCategory.YELLOW || activeZone.category == ZoneCategory.RED)) {
            return CompassBearingState.ATTENTIVE_BREEZE
        }
        return CompassBearingState.SERENE_HORIZON
    }

    fun isWithinQuietHours(
        nowMillis: Long,
        startHour: Int,
        endHour: Int
    ): Boolean {
        if (startHour == endHour) return false
        val hour = Instant.ofEpochMilli(nowMillis)
            .atZone(ZoneId.systemDefault())
            .hour
        return if (startHour > endHour) {
            hour >= startHour || hour < endHour
        } else {
            hour in startHour until endHour
        }
    }

    fun canDispatchNotification(
        nowMillis: Long,
        settings: JitaiExperimentalSettings
    ): Pair<Boolean, String?> {
        if (!settings.notificationsEnabled) {
            return false to "Avisos desactivados"
        }
        if (nowMillis < settings.pausedUntilMillis) {
            val minsLeft = ((settings.pausedUntilMillis - nowMillis) / 60_000L).coerceAtLeast(1L)
            return false to "En pausa de 4 h (~${minsLeft} min)"
        }
        if (isWithinQuietHours(nowMillis, settings.quietHoursStartHour, settings.quietHoursEndHour)) {
            return false to "Horas de silencio"
        }
        if (settings.notificationsSentToday >= settings.maxDailyNotifications.coerceAtMost(2)) {
            return false to "Límite diario de 2 avisos alcanzado"
        }
        return true to null
    }

    fun evaluateCheckIn(
        checkIn: EmaCheckIn,
        settings: JitaiExperimentalSettings,
        plannedAlternative: DayPlanItem?,
        nowMillis: Long = System.currentTimeMillis()
    ): JitaiEvaluationOutcome {
        val highCraving = checkIn.craving >= settings.cravingThreshold
        val lowControl = checkIn.perceivedControl <= settings.controlThreshold
        val triggersExperimentalThreshold = highCraving || lowControl

        if (triggersExperimentalThreshold && (checkIn.isDriving || !checkIn.availableForSupport)) {
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = if (checkIn.isReevaluation) JitaiTriggerSource.TWENTY_MIN_REEVAL else JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.SUPPRESSED_BY_RULE,
                headline = "Respetamos tu espacio",
                explanation = if (checkIn.isDriving) {
                    "Conduce tranquilo; cuando pares tendrás tus recursos a mano."
                } else {
                    "Guardado en silencio. La pestaña Calma sigue abierta si la necesitas."
                },
                ruleTrace = "v${settings.ruleVersion} experimental: impulso ${checkIn.craving}/10, margen ${checkIn.perceivedControl}/10 · omitido por conducción o no disponibilidad."
            )
            return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
        }

        if (triggersExperimentalThreshold) {
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = if (checkIn.isReevaluation) JitaiTriggerSource.TWENTY_MIN_REEVAL else JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.CHOOSE_COPING_OR_HUMAN,
                headline = "No tienes que decidir deprisa",
                explanation = "¿Te apetece una pausa guiada de dos minutos o hablar con tu enfermera?",
                ruleTrace = "v${settings.ruleVersion} experimental: impulso ${checkIn.craving}/10 (≥${settings.cravingThreshold}) o margen ${checkIn.perceivedControl}/10 (≤${settings.controlThreshold}) · disponible y sin conducir."
            )
            return JitaiEvaluationOutcome(
                decision = decision,
                shouldSendNotification = false,
                scheduleReevalIn20Min = !checkIn.isReevaluation
            )
        }

        if (checkIn.sexualDesire >= 7) {
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.NONE,
                headline = "Tu pulso vital está en calma",
                explanation = "El deseo es natural y no activa alertas.",
                ruleTrace = "v${settings.ruleVersion}: deseo sexual valorado de forma independiente sin activar avisos."
            )
            return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
        }

        if (checkIn.craving >= 4 || checkIn.mood <= 4) {
            val altText = plannedAlternative?.chosenAlternative
                ?: "Dos minutos de respiración suave o cambiar de aire"
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.OFFER_PLANNED_ALTERNATIVE,
                headline = "Un gesto amable para ahora",
                explanation = altText,
                ruleTrace = "v${settings.ruleVersion}: momento intermedio → recordatorio suave de tu alternativa propia."
            )
            return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
        }

        val decision = JitaiDecisionRecord(
            timestampMillis = nowMillis,
            triggerSource = JitaiTriggerSource.EMA_COMPLETED,
            actionKind = JitaiActionKind.NONE,
            headline = "Rumbo en calma",
            explanation = "Guardado suavemente en tu brújula.",
            ruleTrace = "v${settings.ruleVersion}: dentro de tu margen habitual."
        )
        return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
    }

    fun evaluateZoneEntry(
        zone: PersonalZone,
        settings: JitaiExperimentalSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): JitaiEvaluationOutcome {
        if (zone.category == ZoneCategory.GREEN) {
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = JitaiTriggerSource.ZONE_ENTRY,
                actionKind = JitaiActionKind.NONE,
                headline = "En ${zone.name}",
                explanation = zone.anchorReminder.ifBlank { "Un espacio que te sienta bien." },
                ruleTrace = "v${settings.ruleVersion}: espacio refugio · sin aviso."
            )
            return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
        }

        val (canNotify, blockReason) = canDispatchNotification(nowMillis, settings)
        if (!canNotify) {
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = JitaiTriggerSource.ZONE_ENTRY,
                actionKind = JitaiActionKind.SUPPRESSED_BY_RULE,
                headline = "Cerca de ${zone.name}",
                explanation = "Sin avisos externos ahora ($blockReason).",
                ruleTrace = "v${settings.ruleVersion}: zona ${zone.category.shortBadge.lowercase()} · aviso omitido ($blockReason) · sin inferir consumo."
            )
            return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
        }

        val decision = JitaiDecisionRecord(
            timestampMillis = nowMillis,
            triggerSource = JitaiTriggerSource.ZONE_ENTRY,
            actionKind = JitaiActionKind.INVITE_NEUTRAL_CHECKIN,
            headline = "Un segundo para escucharte",
            explanation = zone.anchorReminder.ifBlank { "¿Cómo te encuentras en este momento?" },
            ruleTrace = "v${settings.ruleVersion}: zona ${zone.category.shortBadge.lowercase()} → invitación neutra a check-in sin inferir consumo.",
            wasNotified = true
        )
        return JitaiEvaluationOutcome(decision, shouldSendNotification = true, scheduleReevalIn20Min = false)
    }

    fun evaluateVulnerableWindowStart(
        planItem: DayPlanItem,
        settings: JitaiExperimentalSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): JitaiEvaluationOutcome {
        val (canNotify, blockReason) = canDispatchNotification(nowMillis, settings)
        val decision = JitaiDecisionRecord(
            timestampMillis = nowMillis,
            triggerSource = JitaiTriggerSource.VULNERABLE_SLOT,
            actionKind = JitaiActionKind.OFFER_PLANNED_ALTERNATIVE,
            headline = planItem.title,
            explanation = planItem.chosenAlternative.ifBlank { "Respira suave y elige con calma." },
            ruleTrace = if (canNotify) {
                "v${settings.ruleVersion}: momento sensible planificado → recordatorio suave."
            } else {
                "v${settings.ruleVersion}: mostrado sin push ($blockReason)."
            },
            wasNotified = canNotify
        )
        return JitaiEvaluationOutcome(decision, shouldSendNotification = canNotify, scheduleReevalIn20Min = false)
    }
}
