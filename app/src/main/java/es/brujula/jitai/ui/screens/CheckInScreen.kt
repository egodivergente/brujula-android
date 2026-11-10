package es.brujula.jitai.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrecisionScale
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.EditorialSerif
import es.brujula.jitai.core.model.JitaiActionKind
import es.brujula.jitai.core.model.JitaiDecisionRecord
import es.brujula.jitai.core.model.SubstanceContextTag
import es.brujula.jitai.ui.BrujulaUiState

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
    val latest = state.checkIns.firstOrNull()

    var mood by rememberSaveable { mutableIntStateOf(latest?.mood ?: 6) }
    var craving by rememberSaveable { mutableIntStateOf(latest?.craving ?: 2) }
    var sexualDesire by rememberSaveable { mutableIntStateOf(latest?.sexualDesire ?: 5) }
    var perceivedControl by rememberSaveable { mutableIntStateOf(latest?.perceivedControl ?: 7) }
    var availableForSupport by rememberSaveable { mutableStateOf(true) }
    var isDriving by rememberSaveable { mutableStateOf(false) }
    var showNoteSection by rememberSaveable { mutableStateOf(false) }
    var substanceTagName by rememberSaveable { mutableStateOf(SubstanceContextTag.NONE.name) }
    var personalNote by rememberSaveable { mutableStateOf("") }
    var evaluatedDecision by remember { mutableStateOf<JitaiDecisionRecord?>(null) }

    val highIntensity = craving >= state.settings.cravingThreshold ||
        perceivedControl <= state.settings.controlThreshold

    val moodAuraColor by animateColorAsState(
        targetValue = when {
            craving >= state.settings.cravingThreshold -> colors.coralAccent
            perceivedControl <= state.settings.controlThreshold -> colors.amberAccent
            else -> colors.jadePrimary
        },
        animationSpec = tween(650),
        label = "checkinAura"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedContent(
            targetState = evaluatedDecision,
            label = "checkInTransition"
        ) { decision ->
            if (decision == null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BrujulaEditorialHeader(
                            title = if (state.isReevaluationMode) "¿Cómo sigue todo?" else "¿Cómo estás ahora?",
                            subtitle = "Desliza suave, sin pensarlo demasiado.",
                            modifier = Modifier.weight(1f)
                        )
                        // Pequeño orbe cromático vivo que refleja el pulso sin números
                        Canvas(modifier = Modifier.size(44.dp)) {
                            val c = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(moodAuraColor.copy(alpha = 0.35f), Color.Transparent),
                                    center = c,
                                    radius = size.minDimension / 2f
                                ),
                                radius = size.minDimension / 2f,
                                center = c
                            )
                            drawCircle(
                                color = moodAuraColor,
                                radius = 7.dp.toPx(),
                                center = c
                            )
                        }
                    }

                    // Superficie única con los 4 deslizadores cualitativos (cero cifras frías)
                    BrujulaSurface(contentPadding = PaddingValues(22.dp)) {
                        BrujulaPrecisionScale(
                            title = "Ánimo",
                            value = mood,
                            onValueChange = { mood = it },
                            accentColor = colors.jadePrimary,
                            qualitativeLabel = { v ->
                                when (v) {
                                    0, 1 -> "Apagado"
                                    2, 3 -> "Bajo"
                                    4, 5, 6 -> "Sereno"
                                    7, 8 -> "Luminoso"
                                    else -> "Pleno"
                                }
                            }
                        )

                        SoftScaleDivider()

                        BrujulaPrecisionScale(
                            title = "Impulso de consumir",
                            value = craving,
                            onValueChange = { craving = it },
                            accentColor = if (craving >= state.settings.cravingThreshold) {
                                colors.coralAccent
                            } else {
                                colors.amberAccent
                            },
                            qualitativeLabel = { v ->
                                when (v) {
                                    0, 1 -> "En calma"
                                    2, 3 -> "Leve"
                                    4, 5, 6 -> "Presente"
                                    7, 8 -> "Fuerte"
                                    else -> "Muy intenso"
                                }
                            }
                        )

                        SoftScaleDivider()

                        BrujulaPrecisionScale(
                            title = "Deseo sexual",
                            subtitle = "Natural e independiente",
                            value = sexualDesire,
                            onValueChange = { sexualDesire = it },
                            accentColor = colors.petroleumAccent,
                            qualitativeLabel = { v ->
                                when (v) {
                                    0, 1 -> "Tranquilo"
                                    2, 3 -> "Suave"
                                    4, 5, 6 -> "Despierto"
                                    7, 8 -> "Vivo"
                                    else -> "Muy vivo"
                                }
                            }
                        )

                        SoftScaleDivider()

                        BrujulaPrecisionScale(
                            title = "Sensación de margen",
                            value = perceivedControl,
                            onValueChange = { perceivedControl = it },
                            accentColor = if (perceivedControl <= state.settings.controlThreshold) {
                                colors.coralAccent
                            } else {
                                colors.jadePrimary
                            },
                            qualitativeLabel = { v ->
                                when (v) {
                                    0, 1 -> "Desbordado"
                                    2, 3 -> "Justo"
                                    4, 5, 6 -> "Estable"
                                    7, 8 -> "Con aire"
                                    else -> "Con timón"
                                }
                            }
                        )
                    }

                    // Píldoras horizontales ligeras para disponibilidad, conducción y nota opcional
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrujulaTagPill(
                            label = if (availableForSupport) "Disponible ahora" else "Prefiero silencio",
                            selected = availableForSupport,
                            icon = Icons.Outlined.FavoriteBorder,
                            onClick = { availableForSupport = !availableForSupport }
                        )
                        BrujulaTagPill(
                            label = "Conduciendo",
                            selected = isDriving,
                            accentColor = colors.amberAccent,
                            washColor = colors.amberWash,
                            icon = Icons.Outlined.DirectionsCar,
                            onClick = { isDriving = !isDriving }
                        )
                        BrujulaTagPill(
                            label = "Nota breve",
                            selected = showNoteSection || personalNote.isNotBlank(),
                            icon = Icons.Outlined.EditNote,
                            onClick = { showNoteSection = !showNoteSection }
                        )
                    }

                    AnimatedVisibility(visible = showNoteSection) {
                        BrujulaSurface(contentPadding = PaddingValues(18.dp)) {
                            OutlinedTextField(
                                value = personalNote,
                                onValueChange = { personalNote = it },
                                label = { Text("Algo que quieras anotar para ti (opcional)") },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SubstanceContextTag.entries.forEach { tag ->
                                    BrujulaTagPill(
                                        label = tag.label,
                                        selected = substanceTagName == tag.name,
                                        onClick = { substanceTagName = tag.name }
                                    )
                                }
                            }
                        }
                    }

                    BrujulaPrimaryButton(
                        text = "Guardar en mi brújula",
                        icon = Icons.Outlined.Check,
                        containerColor = if (highIntensity) colors.coralAccent else colors.jadePrimary,
                        contentColor = Color.White,
                        onClick = {
                            val selectedTag = runCatching {
                                SubstanceContextTag.valueOf(substanceTagName)
                            }.getOrDefault(SubstanceContextTag.NONE)

                            onSubmitCheckIn(
                                mood,
                                craving,
                                sexualDesire,
                                perceivedControl,
                                availableForSupport,
                                isDriving,
                                selectedTag,
                                personalNote,
                                state.isReevaluationMode
                            ) { evaluated ->
                                evaluatedDecision = evaluated
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Tarjeta de acogida serena tras guardar el check-in
                CalmCheckInResponseView(
                    decision = decision,
                    accentColor = moodAuraColor,
                    onOpenCopingSupport = onOpenCopingSupport,
                    onOpenNurseContact = onOpenNurseContact,
                    onRejectSuggestion4h = {
                        onRejectSuggestion4h(decision.id)
                        onDoneReturnHome()
                    },
                    onDoneReturnHome = onDoneReturnHome,
                    onEditAgain = { evaluatedDecision = null }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun SoftScaleDivider() {
    val colors = BrujulaDesign.colors
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = colors.borderHairline.copy(alpha = 0.45f), thickness = 0.6.dp)
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun CalmCheckInResponseView(
    decision: JitaiDecisionRecord,
    accentColor: Color,
    onOpenCopingSupport: () -> Unit,
    onOpenNurseContact: () -> Unit,
    onRejectSuggestion4h: () -> Unit,
    onDoneReturnHome: () -> Unit,
    onEditAgain: () -> Unit
) {
    val colors = BrujulaDesign.colors

    BrujulaSurface(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(92.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.26f), Color.Transparent),
                            center = c,
                            radius = r
                        ),
                        radius = r,
                        center = c
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.45f),
                        radius = r * 0.64f,
                        center = c,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 10.dp.toPx(),
                        center = c
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = decision.headline,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = EditorialSerif),
                color = colors.inkPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = decision.explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (decision.actionKind == JitaiActionKind.CHOOSE_COPING_OR_HUMAN) {
                BrujulaPrimaryButton(
                    text = "Respirar un momento",
                    icon = Icons.Outlined.Air,
                    onClick = onOpenCopingSupport,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                BrujulaGhostButton(
                    text = "Contactar con enfermería",
                    icon = Icons.Outlined.SupportAgent,
                    onClick = onOpenNurseContact,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ahora no · pausar avisos 4 h",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onRejectSuggestion4h)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            } else {
                BrujulaPrimaryButton(
                    text = "Volver a mi brújula",
                    onClick = onDoneReturnHome,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                BrujulaGhostButton(
                    text = "Hacer una respiración suave",
                    icon = Icons.Outlined.Air,
                    onClick = onOpenCopingSupport,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ajustar mi respuesta",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onEditAgain)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
