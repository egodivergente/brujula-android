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
 * Motor JITAI local, determinista y explicable (Parte 2 · §2.3 y §3).
 *
 * Reglas fundamentales:
 * 1. Craving >= umbral (7/10) o control percibido <= umbral (3/10) -> ofrecer elección entre
 *    afrontamiento breve y apoyo humano, SOLO si la persona está disponible y NO está conduciendo.
 * 2. El deseo sexual se valora de forma independiente y NUNCA activa alertas por sí solo.
 * 3. Entrar en una zona amarilla o roja propone únicamente un check-in voluntario, sin inferir consumo.
 * 4. Comenzar una franja vulnerable ofrece comprobar cómo se siente y muestra su alternativa elegida.
 * 5. Máximo 2 notificaciones diarias, respeto al periodo de silencio nocturno y pausa de 4 h tras rechazo.
 * 6. El acceso manual a todas las herramientas permanece disponible siempre.
 * 7. Los umbrales son configuraciones experimentales revisables, no criterios clínicos validados.
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
            // Tramo nocturno que cruza medianoche (p. ej. 23:00 a 08:00)
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
            return false to "Notificaciones desactivadas por preferencia del usuario"
        }
        if (nowMillis < settings.pausedUntilMillis) {
            val minsLeft = ((settings.pausedUntilMillis - nowMillis) / 60_000L).coerceAtLeast(1L)
            return false to "Pausa activa de 4 h tras rechazo previo (restan ~${minsLeft} min)"
        }
        if (isWithinQuietHours(nowMillis, settings.quietHoursStartHour, settings.quietHoursEndHour)) {
            return false to "Periodo de silencio configurado (${"%02d".format(settings.quietHoursStartHour)}:00–${"%02d".format(settings.quietHoursEndHour)}:00)"
        }
        if (settings.notificationsSentToday >= settings.maxDailyNotifications.coerceAtMost(2)) {
            return false to "Cupo diario alcanzado (${settings.notificationsSentToday}/${settings.maxDailyNotifications.coerceAtMost(2)} avisos hoy)"
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

        // 1. Verificación de receptividad y seguridad vial
        if (triggersExperimentalThreshold && (checkIn.isDriving || !checkIn.availableForSupport)) {
            val reason = when {
                checkIn.isDriving -> "Se ha indicado conducción activa: por seguridad no se despliega intervención automática."
                else -> "Has indicado que ahora no estás disponible para recibir propuestas."
            }
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = if (checkIn.isReevaluation) JitaiTriggerSource.TWENTY_MIN_REEVAL else JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.SUPPRESSED_BY_RULE,
                headline = "Respetando tu contexto actual",
                explanation = "$reason Todas las herramientas de Apoyo permanecen accesibles manualmente cuando lo decidas.",
                ruleTrace = "Regla v${settings.ruleVersion} (experimental): umbral alcanzado (craving ${checkIn.craving}/10, control ${checkIn.perceivedControl}/10), pero bloqueado por filtro de receptividad/conducción."
            )
            return JitaiEvaluationOutcome(
                decision = decision,
                shouldSendNotification = false,
                scheduleReevalIn20Min = false
            )
        }

        // 2. Umbral experimental alcanzado con disponibilidad y sin conducción
        if (triggersExperimentalThreshold) {
            val triggerDetail = buildList {
                if (highCraving) add("craving ${checkIn.craving}/10 (umbral experimental ≥${settings.cravingThreshold})")
                if (lowControl) add("control percibido ${checkIn.perceivedControl}/10 (umbral experimental ≤${settings.controlThreshold})")
            }.joinToString(" y ")

            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = if (checkIn.isReevaluation) JitaiTriggerSource.TWENTY_MIN_REEVAL else JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.CHOOSE_COPING_OR_HUMAN,
                headline = "Acompañamiento disponible a tu medida",
                explanation = "Según tu valoración ($triggerDetail), puedes elegir entre un ejercicio breve de afrontamiento ahora mismo o contactar con tu enfermera navegadora. En 20 minutos podrás reevaluar voluntariamente cómo evoluciona el craving.",
                ruleTrace = "Regla v${settings.ruleVersion} (experimental, no diagnóstica): $triggerDetail + disponible + sin conducción → elección afrontamiento breve / apoyo humano + reevaluación voluntaria 20 min."
            )
            return JitaiEvaluationOutcome(
                decision = decision,
                shouldSendNotification = false, // Ya está dentro de la app viendo la tarjeta
                scheduleReevalIn20Min = !checkIn.isReevaluation
            )
        }

        // 3. Deseo sexual elevado sin craving alto -> validación explícita sin alertas
        if (checkIn.sexualDesire >= 7) {
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.NONE,
                headline = "Rumbo registrado · Autonomía respetada",
                explanation = "Tu deseo sexual (${checkIn.sexualDesire}/10) se recoge por separado del craving (${checkIn.craving}/10) y nunca activa avisos clínicos por sí mismo.",
                ruleTrace = "Regla v${settings.ruleVersion}: craving < ${settings.cravingThreshold} y control > ${settings.controlThreshold} · Deseo sexual independiente excluido de alertas."
            )
            return JitaiEvaluationOutcome(
                decision = decision,
                shouldSendNotification = false,
                scheduleReevalIn20Min = false
            )
        }

        // 4. Momento intermedio -> recordar alternativa planificada si existe
        if (checkIn.craving >= 4 || checkIn.mood <= 4) {
            val altText = plannedAlternative?.chosenAlternative
                ?: "Respiración fisiológica de 2 minutos o dar un paseo corto"
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = JitaiTriggerSource.EMA_COMPLETED,
                actionKind = JitaiActionKind.OFFER_PLANNED_ALTERNATIVE,
                headline = "Tu alternativa elegida está a mano",
                explanation = "No se superan los umbrales de alerta, pero si te apetece cuidarte un poco más hoy tienes anotada tu alternativa: «$altText».",
                ruleTrace = "Regla v${settings.ruleVersion}: bajo umbral de alerta (craving ${checkIn.craving}/10, control ${checkIn.perceivedControl}/10) → sugerencia suave de alternativa propia."
            )
            return JitaiEvaluationOutcome(
                decision = decision,
                shouldSendNotification = false,
                scheduleReevalIn20Min = false
            )
        }

        // 5. Rumbo sereno
        val decision = JitaiDecisionRecord(
            timestampMillis = nowMillis,
            triggerSource = JitaiTriggerSource.EMA_COMPLETED,
            actionKind = JitaiActionKind.NONE,
            headline = "Orientación tranquila registrada",
            explanation = "Ánimo ${checkIn.mood}/10 y control percibido ${checkIn.perceivedControl}/10. Sin avisos adicionales; tus herramientas siguen disponibles cuando quieras.",
            ruleTrace = "Regla v${settings.ruleVersion}: parámetros dentro de margen personal."
        )
        return JitaiEvaluationOutcome(
            decision = decision,
            shouldSendNotification = false,
            scheduleReevalIn20Min = false
        )
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
                headline = "En espacio verde: ${zone.name}",
                explanation = zone.anchorReminder.ifBlank {
                    "Zona configurada como entorno de apoyo y calma. No requiere ninguna alerta."
                },
                ruleTrace = "Regla v${settings.ruleVersion}: zona verde procesada en el dispositivo → refuerzo sereno sin notificación."
            )
            return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
        }

        val (canNotify, blockReason) = canDispatchNotification(nowMillis, settings)
        val zoneLabel = if (zone.category == ZoneCategory.RED) "zona roja" else "zona amarilla"

        if (!canNotify) {
            val decision = JitaiDecisionRecord(
                timestampMillis = nowMillis,
                triggerSource = JitaiTriggerSource.ZONE_ENTRY,
                actionKind = JitaiActionKind.SUPPRESSED_BY_RULE,
                headline = "Contexto detectado sin interrumpirte",
                explanation = "Entrada en $zoneLabel («${zone.name}»). No se envía aviso externo: $blockReason. Si te apetece, puedes abrir el Check-in manualmente.",
                ruleTrace = "Regla v${settings.ruleVersion}: entrada en $zoneLabel → bloqueada por salvaguarda ($blockReason). Sin inferir consumo."
            )
            return JitaiEvaluationOutcome(decision, shouldSendNotification = false, scheduleReevalIn20Min = false)
        }

        val reminderSuffix = if (zone.anchorReminder.isNotBlank()) " Tu nota personal: «${zone.anchorReminder}»." else ""
        val decision = JitaiDecisionRecord(
            timestampMillis = nowMillis,
            triggerSource = JitaiTriggerSource.ZONE_ENTRY,
            actionKind = JitaiActionKind.INVITE_NEUTRAL_CHECKIN,
            headline = "¿Comprobamos cómo te sientes ahora?",
            explanation = "Has indicado o entrado en «${zone.name}» ($zoneLabel). BRÚJULA nunca infiere consumo por ubicación; solo te invita a un check-in breve si estás disponible.$reminderSuffix",
            ruleTrace = "Regla v${settings.ruleVersion}: entrada en $zoneLabel → proponer únicamente check-in EMA sin inferencia diagnóstica (${settings.notificationsSentToday + 1}/${settings.maxDailyNotifications} avisos hoy).",
            wasNotified = true
        )
        return JitaiEvaluationOutcome(
            decision = decision,
            shouldSendNotification = true,
            scheduleReevalIn20Min = false
        )
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
            headline = "Franja señalada: ${planItem.title}",
            explanation = "Comienza un momento que marcaste en Mi día (${planItem.timeLabel}). Puedes comprobar cómo te sientes o apoyarte en tu alternativa elegida: «${planItem.chosenAlternative}».",
            ruleTrace = if (canNotify) {
                "Regla v${settings.ruleVersion}: inicio de franja vulnerable planificada → invitación suave a check-in y alternativa propia."
            } else {
                "Regla v${settings.ruleVersion}: inicio de franja vulnerable mostrado en pantalla sin notificación push ($blockReason)."
            },
            wasNotified = canNotify
        )
        return JitaiEvaluationOutcome(
            decision = decision,
            shouldSendNotification = canNotify,
            scheduleReevalIn20Min = false
        )
    }
}
