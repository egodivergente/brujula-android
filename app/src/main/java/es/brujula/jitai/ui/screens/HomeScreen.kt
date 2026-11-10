package es.brujula.jitai.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.EditorialSerif
import es.brujula.jitai.core.model.CompassBearingState
import es.brujula.jitai.core.model.DayPlanItem
import es.brujula.jitai.core.model.JitaiActionKind
import es.brujula.jitai.core.model.JitaiDecisionRecord
import es.brujula.jitai.core.model.ZoneCategory
import es.brujula.jitai.ui.BrujulaUiState
import es.brujula.jitai.ui.components.LivingCompassCanvas

@Composable
fun HomeScreen(
    state: BrujulaUiState,
    onNavigateToCheckIn: (reevaluation: Boolean) -> Unit,
    onNavigateToZones: () -> Unit,
    onNavigateToMyDay: () -> Unit,
    onNavigateToSupport: (section: String?) -> Unit,
    onNavigateToProgress: () -> Unit,
    onTogglePlanCompleted: (DayPlanItem) -> Unit,
    onRejectSuggestion4h: (Long) -> Unit,
    onAcceptSuggestion: (Long) -> Unit,
    onResumeNotifications: () -> Unit,
    onDismissReeval: () -> Unit,
    onUpdateMotto: (String) -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    var showMottoEditor by rememberSaveable { mutableStateOf(false) }
    var draftMotto by rememberSaveable(state.settings.personalMotto) {
        mutableStateOf(state.settings.personalMotto)
    }

    val latestPendingDecision = state.jitaiDecisions.firstOrNull {
        it.userResponse == "PENDIENTE" &&
            it.actionKind != JitaiActionKind.NONE &&
            it.actionKind != JitaiActionKind.SUPPRESSED_BY_RULE
    }
    val isPaused4h = state.settings.pausedUntilMillis > now
    val reevalActive = state.settings.reevalDueAtMillis in 1..now
    val nextPlanned = state.dayPlan.firstOrNull { !it.completed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Escenario atmosférico protagonista: Brújula viva y lectura serena sin cifras
        SanctuaryCompassStage(
            state = state,
            onNavigateToCheckIn = { onNavigateToCheckIn(false) },
            onNavigateToBreathing = { onNavigateToSupport("breathing") },
            onEditMotto = {
                draftMotto = state.settings.personalMotto
                showMottoEditor = true
            },
            onOpenZones = onNavigateToZones
        )

        // 2. Susurro contextual JITAI (solo si hay una invitación activa o pausa)
        if (reevalActive) {
            BrujulaSurface(
                containerColor = colors.jadeWash.copy(alpha = 0.75f),
                borderColor = colors.jadePrimary.copy(alpha = 0.30f),
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "Han pasado unos minutos",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onJadeWash
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¿Cómo se siente el cuerpo ahora?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrujulaPrimaryButton(
                        text = "Volver a sentir",
                        onClick = { onNavigateToCheckIn(true) },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismissReeval) {
                        Text("Ahora no", color = colors.inkMuted)
                    }
                }
            }
        } else if (latestPendingDecision != null) {
            MinimalJitaiWhisperCard(
                decision = latestPendingDecision,
                onPrimaryAction = {
                    onAcceptSuggestion(latestPendingDecision.id)
                    when (latestPendingDecision.actionKind) {
                        JitaiActionKind.CHOOSE_COPING_OR_HUMAN -> onNavigateToSupport("breathing")
                        JitaiActionKind.OFFER_PLANNED_ALTERNATIVE -> onNavigateToMyDay()
                        JitaiActionKind.INVITE_NEUTRAL_CHECKIN -> onNavigateToCheckIn(false)
                        JitaiActionKind.INVITE_CRAVING_REEVALUATION -> onNavigateToCheckIn(true)
                        else -> onNavigateToSupport(null)
                    }
                },
                onSecondaryNurseAction = if (latestPendingDecision.actionKind == JitaiActionKind.CHOOSE_COPING_OR_HUMAN) {
                    {
                        onAcceptSuggestion(latestPendingDecision.id)
                        onNavigateToSupport("nurse")
                    }
                } else {
                    null
                },
                onReject4h = { onRejectSuggestion4h(latestPendingDecision.id) }
            )
        } else if (isPaused4h) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(colors.surfaceElevated)
                    .border(0.6.dp, colors.borderHairline.copy(alpha = 0.55f), RoundedCornerShape(50))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avisos en silencio temporal",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted
                )
                Text(
                    text = "Reactivar",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.jadePrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            haptics.click()
                            onResumeNotifications()
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // 3. Dos tarjetas visuales orgánicas (Cero métricas, pura calma visual)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VisualSanctuaryCard(
                title = "Calma",
                subtitle = "Aliento guiado",
                accentColor = colors.jadePrimary,
                artworkKind = SanctuaryArtworkKind.WAVES,
                onClick = { onNavigateToSupport("breathing") },
                modifier = Modifier.weight(1f)
            )
            VisualSanctuaryCard(
                title = "Espacios",
                subtitle = state.activeZone?.name ?: "Mapa sereno",
                accentColor = when (state.activeZone?.category) {
                    ZoneCategory.YELLOW -> colors.zoneAmber
                    ZoneCategory.RED -> colors.zoneCoral
                    else -> colors.petroleumAccent
                },
                artworkKind = SanctuaryArtworkKind.CONSTELLATION,
                onClick = onNavigateToZones,
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Siguiente ancla suave del día (sin listas abrumadoras)
        BrujulaSurface(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            onClick = onNavigateToMyDay
        ) {
            if (nextPlanned != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Próxima ancla · ${nextPlanned.timeLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (nextPlanned.isVulnerableWindow) colors.amberAccent else colors.jadePrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = nextPlanned.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (nextPlanned.chosenAlternative.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = nextPlanned.chosenAlternative,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.jadeWash, CircleShape)
                            .clickable {
                                haptics.confirm()
                                onTogglePlanCompleted(nextPlanned)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Completar paso",
                            tint = colors.jadePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mi día está despejado",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                        Text(
                            text = "Toca para elegir un pequeño gesto de cuidado",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted
                        )
                    }
                }
            }
        }

        // 5. Acceso humano discreto y siempre a mano
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiscreetFooterPill(
                icon = Icons.Outlined.SupportAgent,
                label = "Enfermería",
                tint = colors.inkSecondary,
                onClick = { onNavigateToSupport("nurse") }
            )
            DiscreetFooterPill(
                icon = Icons.Outlined.Emergency,
                label = "Urgencias 112",
                tint = colors.coralAccent,
                onClick = {
                    haptics.gentlePulse()
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showMottoEditor) {
        AlertDialog(
            onDismissRequest = { showMottoEditor = false },
            title = {
                Text(
                    text = "Tu frase de calma",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                OutlinedTextField(
                    value = draftMotto,
                    onValueChange = { draftMotto = it },
                    label = { Text("Qué te ayuda recordar") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateMotto(draftMotto)
                        showMottoEditor = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMottoEditor = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SanctuaryCompassStage(
    state: BrujulaUiState,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToBreathing: () -> Unit,
    onEditMotto: () -> Unit,
    onOpenZones: () -> Unit
) {
    val haptics = BrujulaDesign.haptics
    val stageShape = RoundedCornerShape(30.dp)

    val ambientGlowColor = when (state.compassBearing) {
        CompassBearingState.SERENE_HORIZON -> Color(0xFF2EA992)
        CompassBearingState.VITAL_AUTONOMY -> Color(0xFF56D8C0)
        CompassBearingState.ATTENTIVE_BREEZE -> Color(0xFFE0973B)
        CompassBearingState.INTENSE_SWELL -> Color(0xFFDF6C55)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(stageShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E2530),
                        Color(0xFF0A1B24),
                        Color(0xFF08151C)
                    )
                ),
                shape = stageShape
            )
            .border(
                width = 0.8.dp,
                color = Color(0xFFF4EFE6).copy(alpha = 0.12f),
                shape = stageShape
            )
    ) {
        // Luz atmosférica radial de fondo
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ambientGlowColor.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height * 0.36f),
                    radius = size.minDimension * 0.72f
                ),
                radius = size.minDimension * 0.72f,
                center = Offset(size.width / 2f, size.height * 0.36f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val activeZone = state.activeZone
            if (activeZone != null) {
                val dotColor = when (activeZone.category) {
                    ZoneCategory.GREEN -> Color(0xFF57D5BC)
                    ZoneCategory.YELLOW -> Color(0xFFF2B35A)
                    ZoneCategory.RED -> Color(0xFFEF8771)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.07f))
                        .clickable {
                            haptics.click()
                            onOpenZones()
                        }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(dotColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = activeZone.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEAE3D5)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            LivingCompassCanvas(
                bearingState = state.compassBearing,
                diameter = 252.dp,
                onCompassTapped = onNavigateToCheckIn
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = state.compassBearing.shortTitle,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFFF6F2EA),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.compassBearing.poeticReading,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC7D6DE),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "«${state.settings.personalMotto}»",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = Color(0xFF8EB0BE),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        haptics.click()
                        onEditMotto()
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        haptics.confirm()
                        onNavigateToCheckIn()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38B89F),
                        contentColor = Color(0xFF06261F)
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Sentir ahora",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = {
                        haptics.click()
                        onNavigateToBreathing()
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFFF6F2EA).copy(alpha = 0.24f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF6F2EA)
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Air,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Respirar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

private enum class SanctuaryArtworkKind {
    WAVES,
    CONSTELLATION
}

@Composable
private fun VisualSanctuaryCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    artworkKind: SanctuaryArtworkKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    val infiniteTransition = rememberInfiniteTransition(label = "miniArtwork")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "miniPulse"
    )

    BrujulaSurface(
        modifier = modifier,
        contentPadding = PaddingValues(18.dp),
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Canvas(modifier = Modifier.size(58.dp)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val maxR = size.minDimension / 2f
                when (artworkKind) {
                    SanctuaryArtworkKind.WAVES -> {
                        drawCircle(
                            color = accentColor.copy(alpha = 0.12f),
                            radius = maxR * pulse,
                            center = c
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = 0.30f),
                            radius = maxR * 0.68f * pulse,
                            center = c,
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        drawCircle(
                            color = accentColor,
                            radius = maxR * 0.28f,
                            center = c
                        )
                    }

                    SanctuaryArtworkKind.CONSTELLATION -> {
                        drawCircle(
                            color = accentColor.copy(alpha = 0.10f),
                            radius = maxR * 0.96f,
                            center = c
                        )
                        drawCircle(
                            color = colors.borderStrong.copy(alpha = 0.45f),
                            radius = maxR * 0.72f,
                            center = c,
                            style = Stroke(width = 0.9.dp.toPx())
                        )
                        drawCircle(
                            color = accentColor,
                            radius = 5.dp.toPx(),
                            center = Offset(c.x + maxR * 0.26f, c.y - maxR * 0.20f)
                        )
                        drawCircle(
                            color = colors.jadePrimary.copy(alpha = 0.75f),
                            radius = 3.6.dp.toPx(),
                            center = Offset(c.x - maxR * 0.30f, c.y + maxR * 0.22f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.inkPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MinimalJitaiWhisperCard(
    decision: JitaiDecisionRecord,
    onPrimaryAction: () -> Unit,
    onSecondaryNurseAction: (() -> Unit)?,
    onReject4h: () -> Unit
) {
    val colors = BrujulaDesign.colors
    var showTrace by rememberSaveable(decision.id) { mutableStateOf(false) }

    val primaryLabel = when (decision.actionKind) {
        JitaiActionKind.CHOOSE_COPING_OR_HUMAN -> "Respirar 3 min"
        JitaiActionKind.OFFER_PLANNED_ALTERNATIVE -> "Ver mi plan"
        JitaiActionKind.INVITE_NEUTRAL_CHECKIN -> "Sentir ahora"
        JitaiActionKind.INVITE_CRAVING_REEVALUATION -> "Volver a sentir"
        else -> "Abrir calma"
    }

    BrujulaSurface(
        containerColor = colors.surfaceElevated,
        borderColor = colors.amberAccent.copy(alpha = 0.35f),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = decision.headline,
                style = MaterialTheme.typography.titleMedium,
                color = colors.inkPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Por qué aparece esto",
                tint = colors.inkMuted.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable { showTrace = !showTrace }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = decision.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkSecondary
        )

        AnimatedVisibility(visible = showTrace) {
            Text(
                text = decision.ruleTrace,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BrujulaPrimaryButton(
                text = primaryLabel,
                onClick = onPrimaryAction,
                modifier = Modifier.weight(1f)
            )
            if (onSecondaryNurseAction != null) {
                BrujulaGhostButton(
                    text = "Enfermería",
                    onClick = onSecondaryNurseAction,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Ahora no · pausar avisos 4 h",
            style = MaterialTheme.typography.labelMedium,
            color = colors.inkMuted,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onReject4h)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DiscreetFooterPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.surfaceElevated)
            .border(0.6.dp, colors.borderHairline.copy(alpha = 0.6f), RoundedCornerShape(50))
            .clickable {
                haptics.click()
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
    }
}
