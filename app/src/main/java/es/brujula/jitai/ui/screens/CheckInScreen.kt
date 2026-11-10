package es.brujula.jitai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrecisionScale
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.model.JitaiActionKind
import es.brujula.jitai.core.model.JitaiDecisionRecord
import es.brujula.jitai.core.model.SubstanceContextTag
import es.brujula.jitai.ui.BrujulaUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInScreen(
    state: BrujulaUiState,
    onSubmitCheckIn: (
        mood: Int,
        craving: Int,
        sexualDesire: Int,
        perceivedControl: Int,
        availableForSupport: Boolean,
        isDriving: Boolean,
        substanceTag: SubstanceContextTag,
        personalNote: String,
        isReevaluation: Boolean,
        onEvaluated: (JitaiDecisionRecord) -> Unit
    ) -> Unit,
    onOpenCopingSupport: () -> Unit,
    onOpenNurseContact: () -> Unit,
    onRejectSuggestion4h: (Long) -> Unit,
    onDoneReturnHome: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val previous = state.checkIns.firstOrNull()

    var mood by remember { mutableIntStateOf(previous?.mood ?: 6) }
    var craving by remember { mutableIntStateOf(previous?.craving ?: 3) }
    var sexualDesire by remember { mutableIntStateOf(previous?.sexualDesire ?: 5) }
    var perceivedControl by remember { mutableIntStateOf(previous?.perceivedControl ?: 7) }
    var availableForSupport by remember { mutableStateOf(true) }
    var isDriving by remember { mutableStateOf(false) }
    var selectedSubstance by remember { mutableStateOf(SubstanceContextTag.NONE) }
    var personalNote by remember { mutableStateOf("") }
    var evaluatedDecision by remember { mutableStateOf<JitaiDecisionRecord?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BrujulaEditorialHeader(
                eyebrow = if (state.isReevaluationMode) {
                    "Reevaluación voluntaria · 20 min"
                } else {
                    "Evaluación Ecológica Momentánea (EMA)"
                },
                title = "¿Cómo sientes tu rumbo ahora?",
                subtitle = "Menos de 25 segundos. Tus respuestas quedan únicamente en este dispositivo y orientan herramientas explicables sin juzgarte."
            )
        }

        // Resultado inmediato explicable tras guardar
        item {
            AnimatedVisibility(visible = evaluatedDecision != null) {
                evaluatedDecision?.let { decision ->
                    val isIntense = decision.actionKind == JitaiActionKind.CHOOSE_COPING_OR_HUMAN
                    BrujulaSurface(
                        containerColor = if (isIntense) colors.coralWash else colors.jadeWash,
                        borderColor = if (isIntense) colors.coralAccent else colors.jadePrimary
                    ) {
                        Text(
                            text = "REGISTRO GUARDADO · MOTOR JITAI EXPLICABLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIntense) colors.onCoralWash else colors.onJadeWash
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = decision.headline,
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = decision.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = decision.ruleTrace,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isIntense) {
                                BrujulaPrimaryButton(
                                    text = "Afrontamiento breve",
                                    onClick = onOpenCopingSupport,
                                    modifier = Modifier.weight(1f)
                                )
                                BrujulaGhostButton(
                                    text = "Enfermería",
                                    onClick = onOpenNurseContact
                                )
                                TextButton(onClick = { onRejectSuggestion4h(decision.id) }) {
                                    Text("Pausar 4 h")
                                }
                            } else {
                                BrujulaPrimaryButton(
                                    text = "Volver a Mi brújula",
                                    onClick = onDoneReturnHome,
                                    modifier = Modifier.weight(1f)
                                )
                                BrujulaGhostButton(
                                    text = "Ir a Apoyo",
                                    onClick = onOpenCopingSupport
                                )
                            }
                        }
                    }
                }
            }
        }

        // Escalas táctiles 0-10
        item {
            BrujulaSurface {
                BrujulaPrecisionScale(
                    title = "1. Estado de ánimo",
                    subtitle = "Sensación emocional y energía general en este momento",
                    value = mood,
                    onValueChange = { mood = it },
                    minAnchorLabel = "0 · Muy bajo / agotado",
                    maxAnchorLabel = "10 · Sereno y vital",
                    accentColor = colors.jadePrimary
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = colors.borderHairline)
                Spacer(modifier = Modifier.height(16.dp))

                val cravingColor = when {
                    craving >= state.settings.cravingThreshold -> colors.coralAccent
                    craving >= 4 -> colors.amberAccent
                    else -> colors.jadePrimary
                }
                BrujulaPrecisionScale(
                    title = "2. Deseo de consumir (craving)",
                    subtitle = "Umbral experimental actual: ≥${state.settings.cravingThreshold}/10",
                    value = craving,
                    onValueChange = { craving = it },
                    minAnchorLabel = "0 · Ausente",
                    maxAnchorLabel = "10 · Ola muy intensa",
                    accentColor = cravingColor
                )

                if (!state.settings.discreetModeEnabled && craving >= 3) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sustancia asociada (opcional):",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        SubstanceContextTag.entries.forEach { tag ->
                            BrujulaTagPill(
                                label = tag.label,
                                selected = selectedSubstance == tag,
                                onClick = { selectedSubstance = tag }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = colors.borderHairline)
                Spacer(modifier = Modifier.height(16.dp))

                BrujulaPrecisionScale(
                    title = "3. Deseo sexual (independiente)",
                    subtitle = "Se valora por separado; el deseo sexual aislado nunca activa avisos",
                    value = sexualDesire,
                    onValueChange = { sexualDesire = it },
                    minAnchorLabel = "0 · Bajo ahora",
                    maxAnchorLabel = "10 · Intenso / presente",
                    accentColor = colors.petroleumAccent
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = colors.borderHairline)
                Spacer(modifier = Modifier.height(16.dp))

                val controlColor = when {
                    perceivedControl <= state.settings.controlThreshold -> colors.coralAccent
                    perceivedControl <= 5 -> colors.amberAccent
                    else -> colors.jadePrimary
                }
                BrujulaPrecisionScale(
                    title = "4. Control percibido sobre el consumo",
                    subtitle = "Umbral experimental actual: ≤${state.settings.controlThreshold}/10",
                    value = perceivedControl,
                    onValueChange = { perceivedControl = it },
                    minAnchorLabel = "0 · Poco margen hoy",
                    maxAnchorLabel = "10 · Con pleno timón",
                    accentColor = controlColor
                )
            }
        }

        // Disponibilidad y seguridad vial (receptividad JITAI §2.2 / §2.3)
        item {
            BrujulaSurface {
                Text(
                    text = "5. Receptividad y contexto seguro",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = colors.jadePrimary
                        )
                        Column(modifier = Modifier.padding(start = 10.dp, end = 8.dp)) {
                            Text(
                                text = "Disponible para acompañamiento breve",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkPrimary
                            )
                            Text(
                                text = "Si necesitas silencio ahora, no se desplegarán propuestas automáticas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted
                            )
                        }
                    }
                    Switch(
                        checked = availableForSupport,
                        onCheckedChange = { availableForSupport = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = colors.borderHairline)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = colors.amberAccent
                        )
                        Column(modifier = Modifier.padding(start = 10.dp, end = 8.dp)) {
                            Text(
                                text = "Estoy conduciendo en este momento",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkPrimary
                            )
                            Text(
                                text = "Por seguridad vial, conducir bloquea cualquier intervención activa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted
                            )
                        }
                    }
                    Switch(
                        checked = isDriving,
                        onCheckedChange = { isDriving = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = personalNote,
                    onValueChange = { personalNote = it },
                    label = { Text("Nota breve para ti (opcional)") },
                    placeholder = { Text("Ej. Salgo cansado de guardia; me apetece descansar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                BrujulaPrimaryButton(
                    text = if (state.isReevaluationMode) {
                        "Guardar reevaluación de 20 min"
                    } else {
                        "Guardar check-in y actualizar brújula"
                    },
                    icon = Icons.Outlined.Check,
                    onClick = {
                        onSubmitCheckIn(
                            mood,
                            craving,
                            sexualDesire,
                            perceivedControl,
                            availableForSupport,
                            isDriving,
                            selectedSubstance,
                            personalNote,
                            state.isReevaluationMode
                        ) { decision ->
                            evaluatedDecision = decision
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
