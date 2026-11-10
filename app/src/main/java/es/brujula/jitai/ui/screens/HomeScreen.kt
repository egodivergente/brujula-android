package es.brujula.jitai.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSectionTitle
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.TabularNumberStyle
import es.brujula.jitai.core.model.JitaiActionKind
import es.brujula.jitai.core.model.ZoneCategory
import es.brujula.jitai.ui.BrujulaUiState
import es.brujula.jitai.ui.components.GhbVoluntaryTrackerCard
import es.brujula.jitai.ui.components.LivingCompassCanvas
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    state: BrujulaUiState,
    onNavigateToCheckIn: (reeval: Boolean) -> Unit,
    onNavigateToZones: () -> Unit,
    onNavigateToMyDay: () -> Unit,
    onNavigateToSupport: (initialSection: String?) -> Unit,
    onNavigateToProgress: () -> Unit,
    onTogglePlanCompleted: (es.brujula.jitai.core.model.DayPlanItem) -> Unit,
    onRejectSuggestion4h: (Long) -> Unit,
    onAcceptSuggestion: (Long) -> Unit,
    onResumeNotifications: () -> Unit,
    onDismissReeval: () -> Unit,
    onUpdateMotto: (String) -> Unit,
    onLogGhb: (String) -> Unit,
    onDeleteGhb: (Long) -> Unit,
    onClearGhb: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    var showMottoDialog by remember { mutableStateOf(false) }
    var draftMotto by remember(state.settings.personalMotto) {
        mutableStateOf(state.settings.personalMotto)
    }
    var showRuleExplanation by remember { mutableStateOf(false) }

    val latestCheckIn = state.checkIns.firstOrNull()
    val latestDecision = state.jitaiDecisions.firstOrNull()
    val nextPlanned = state.dayPlan.firstOrNull { !it.completed }
    val isPaused4h = now < state.settings.pausedUntilMillis
    val reevalActive = state.settings.reevalDueAtMillis > 0L

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Cabecera Protagonista: La Brújula Viva
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surfaceHero)
                    .border(0.9.dp, Color(0xFF284D5F), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFF48C4AC), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "SEMANA ${state.settings.currentProgramWeek} DE 12",
                            style = MaterialTheme.typography.labelSmall.merge(TabularNumberStyle),
                            color = Color(0xFF9EC7D4)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (state.settings.discreetModeEnabled) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = "Modo discreto activo",
                                tint = Color(0xFF9EC7D4),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        val zoneBadgeText = state.activeZone?.let {
                            "Zona ${it.category.shortBadge.lowercase()}"
                        } ?: if (state.settings.optionalGpsEnabled) "GPS local" else "Modo sin GPS"
                        Text(
                            text = zoneBadgeText,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFD6E5EA),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF183847))
                                .clickable { onNavigateToZones() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LivingCompassCanvas(
                    bearingState = state.compassBearing,
                    diameter = 196.dp,
                    onCompassTapped = { onNavigateToCheckIn(false) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = state.compassBearing.shortTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurfaceHero,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = state.compassBearing.poeticReading,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC4D6DD),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF234454))
                Spacer(modifier = Modifier.height(10.dp))

                // Lema de orientación autónoma editable
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showMottoDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NORTE PERSONAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7FA6B5)
                        )
                        Text(
                            text = "«${state.settings.personalMotto}»",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFEDE7DA)
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar norte personal",
                        tint = Color(0xFF8FB4C2),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resumen del último estado reportado + botón rápido de Check-in
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (latestCheckIn != null) {
                        val stamp = Instant.ofEpochMilli(latestCheckIn.timestampMillis)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Último check-in ($stamp)",
                                style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle),
                                color = Color(0xFF92B3C0)
                            )
                            Text(
                                text = "Ánimo ${latestCheckIn.mood} · Control ${latestCheckIn.perceivedControl} · Craving ${latestCheckIn.craving}",
                                style = MaterialTheme.typography.labelLarge.merge(TabularNumberStyle),
                                color = Color(0xFFF3EEE5)
                            )
                        }
                    } else {
                        Text(
                            text = "Registra cómo te encuentras en 20 s",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB5CAD2),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BrujulaPrimaryButton(
                        text = "Check-in EMA",
                        icon = Icons.Outlined.CompassCalibration,
                        containerColor = Color(0xFF33AE96),
                        contentColor = Color(0xFF06251F),
                        onClick = { onNavigateToCheckIn(false) }
                    )
                }
            }
        }

        // 2. Estado de Pausa de 4 h o Reevaluación voluntaria a los 20 min
        if (isPaused4h) {
            item {
                val minsLeft = ((state.settings.pausedUntilMillis - now) / 60_000L).coerceAtLeast(1L)
                BrujulaSurface(
                    containerColor = colors.surfaceSubtle,
                    borderColor = colors.borderStrong
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PauseCircleOutline,
                                contentDescription = null,
                                tint = colors.inkSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pausa de 4 h activa (${minsLeft / 60}h ${"%02d".format(minsLeft % 60)}m restantes)",
                                    style = MaterialTheme.typography.titleSmall.merge(TabularNumberStyle),
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = "No recibirás avisos automáticos; todas las herramientas siguen abiertas manualmente.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkSecondary
                                )
                            }
                        }
                        TextButton(onClick = onResumeNotifications) {
                            Text("Reanudar")
                        }
                    }
                }
            }
        }

        if (reevalActive) {
            item {
                val remainingMillis = state.settings.reevalDueAtMillis - now
                val isReady = remainingMillis <= 0L
                val mins = (remainingMillis.coerceAtLeast(0L) / 60_000L)
                BrujulaSurface(
                    containerColor = colors.jadeWash,
                    borderColor = colors.jadePrimary.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (isReady) {
                            "REEVALUACIÓN VOLUNTARIA DISPONIBLE"
                        } else {
                            "REEVALUACIÓN VOLUNTARIA EN ~${mins + 1} MIN"
                        },
                        style = MaterialTheme.typography.labelSmall.merge(TabularNumberStyle),
                        color = colors.onJadeWash
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¿Cómo evoluciona la ola de craving tras unos minutos?",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BrujulaPrimaryButton(
                            text = "Reevaluar ahora",
                            onClick = { onNavigateToCheckIn(true) }
                        )
                        BrujulaGhostButton(
                            text = "Omitir sin alerta",
                            onClick = onDismissReeval
                        )
                    }
                }
            }
        }

        // 3. Tarjeta JITAI explicable cuando hay una sugerencia activa pendiente
        if (latestDecision != null &&
            latestDecision.actionKind != JitaiActionKind.NONE &&
            latestDecision.userResponse == "PENDIENTE"
        ) {
            item {
                val isIntense = latestDecision.actionKind == JitaiActionKind.CHOOSE_COPING_OR_HUMAN
                BrujulaSurface(
                    containerColor = if (isIntense) colors.coralWash else colors.amberWash,
                    borderColor = if (isIntense) colors.coralAccent.copy(alpha = 0.55f) else colors.amberAccent.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORIENTACIÓN ADAPTATIVA · ${latestDecision.triggerSource.label.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIntense) colors.onCoralWash else colors.onAmberWash
                        )
                        Text(
                            text = if (showRuleExplanation) "Ocultar regla" else "¿Por qué?",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isIntense) colors.onCoralWash else colors.onAmberWash,
                            modifier = Modifier.clickable { showRuleExplanation = !showRuleExplanation }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = latestDecision.headline,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = latestDecision.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkSecondary
                    )

                    AnimatedVisibility(visible = showRuleExplanation) {
                        Text(
                            text = "${latestDecision.ruleTrace} (Umbral experimental no clínico).",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (latestDecision.actionKind) {
                            JitaiActionKind.CHOOSE_COPING_OR_HUMAN -> {
                                BrujulaPrimaryButton(
                                    text = "Afrontamiento (3 min)",
                                    onClick = {
                                        onAcceptSuggestion(latestDecision.id)
                                        onNavigateToSupport("coping")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                BrujulaGhostButton(
                                    text = "Enfermería",
                                    onClick = {
                                        onAcceptSuggestion(latestDecision.id)
                                        onNavigateToSupport("nurse")
                                    }
                                )
                            }
                            JitaiActionKind.INVITE_NEUTRAL_CHECKIN -> {
                                BrujulaPrimaryButton(
                                    text = "Hacer check-in",
                                    onClick = {
                                        onAcceptSuggestion(latestDecision.id)
                                        onNavigateToCheckIn(false)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            else -> {
                                BrujulaPrimaryButton(
                                    text = "Ver apoyo",
                                    onClick = {
                                        onAcceptSuggestion(latestDecision.id)
                                        onNavigateToSupport(null)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        TextButton(
                            onClick = { onRejectSuggestion4h(latestDecision.id) }
                        ) {
                            Text(
                                text = "Pausar 4 h",
                                color = colors.inkSecondary
                            )
                        }
                    }
                }
            }
        }

        // 4. Acceso inmediato al apoyo (permanente, 1 toque)
        item {
            BrujulaSectionTitle(
                title = "Apoyo inmediato",
                meta = "Siempre abierto manualmente",
                actionLabel = "Todo el apoyo",
                onAction = { onNavigateToSupport(null) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                QuickSupportTile(
                    icon = Icons.Outlined.Air,
                    title = "Respirar",
                    subtitle = "Pausa 4·2·6",
                    accent = colors.jadePrimary,
                    wash = colors.jadeWash,
                    onClick = { onNavigateToSupport("breathing") },
                    modifier = Modifier.weight(1f)
                )
                QuickSupportTile(
                    icon = Icons.Outlined.PsychologyAlt,
                    title = "Guía IA",
                    subtitle = "Revisada",
                    accent = colors.petroleumAccent,
                    wash = colors.surfaceSubtle,
                    onClick = { onNavigateToSupport("guide") },
                    modifier = Modifier.weight(1f)
                )
                QuickSupportTile(
                    icon = Icons.Outlined.MedicalServices,
                    title = "Enfermera",
                    subtitle = "9–17 h",
                    accent = colors.amberAccent,
                    wash = colors.amberWash,
                    onClick = { onNavigateToSupport("nurse") },
                    modifier = Modifier.weight(1f)
                )
                QuickSupportTile(
                    icon = Icons.Outlined.Emergency,
                    title = "112",
                    subtitle = "Urgencia",
                    accent = colors.coralAccent,
                    wash = colors.coralWash,
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Siguiente actividad planificada / ventana vulnerable de Mi día
        item {
            BrujulaSectionTitle(
                title = "Siguiente paso en tu día",
                actionLabel = "Planificar Mi día",
                onAction = onNavigateToMyDay
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (nextPlanned != null) {
                BrujulaSurface(onClick = onNavigateToMyDay) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { onTogglePlanCompleted(nextPlanned) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (nextPlanned.completed) {
                                    Icons.Outlined.CheckCircle
                                } else {
                                    Icons.Outlined.RadioButtonUnchecked
                                },
                                contentDescription = "Marcar completada",
                                tint = colors.jadePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${nextPlanned.timeBlock.label} · ${nextPlanned.timeLabel}",
                                    style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                                    color = if (nextPlanned.isVulnerableWindow) colors.amberAccent else colors.jadePrimary
                                )
                                if (nextPlanned.isVulnerableWindow) {
                                    BrujulaTagPill(
                                        label = "Franja sensible",
                                        selected = true,
                                        accentColor = colors.amberAccent,
                                        washColor = colors.amberWash
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = nextPlanned.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.inkPrimary
                            )
                            if (nextPlanned.chosenAlternative.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Alternativa propia: ${nextPlanned.chosenAlternative}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                BrujulaSurface(onClick = onNavigateToMyDay) {
                    Text(
                        text = "Has completado tus actividades previstas de hoy. Toca para añadir un descanso o alternativa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkSecondary
                    )
                }
            }
        }

        // 6. Registro voluntario de GHB/GBL (cronómetro transcurrido sin recomendar intervalos seguros)
        item {
            GhbVoluntaryTrackerCard(
                logs = state.ghbLogs,
                discreetMode = state.settings.discreetModeEnabled,
                onLogNewEntry = onLogGhb,
                onDeleteEntry = onDeleteGhb,
                onClearAll = onClearGhb
            )
        }

        // 7. Tarjeta compacta de conexión con Zonas y Mi Progreso
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BrujulaSurface(
                    onClick = onNavigateToZones,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = colors.jadePrimary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mis zonas",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.inkPrimary
                    )
                    val greenCount = state.zones.count { it.category == ZoneCategory.GREEN }
                    Text(
                        text = "${state.zones.size} activas ($greenCount verdes)",
                        style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle),
                        color = colors.inkMuted
                    )
                }

                BrujulaSurface(
                    onClick = onNavigateToProgress,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Insights,
                        contentDescription = null,
                        tint = colors.petroleumAccent,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mi progreso",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.inkPrimary
                    )
                    val unlocked = state.privateBadges.count { it.unlocked }
                    Text(
                        text = "${state.adherenceLast7DaysPercent}% 7d · $unlocked hitos",
                        style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle),
                        color = colors.inkMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showMottoDialog) {
        AlertDialog(
            onDismissRequest = { showMottoDialog = false },
            title = { Text("Tu frase de orientación") },
            text = {
                OutlinedTextField(
                    value = draftMotto,
                    onValueChange = { draftMotto = it },
                    label = { Text("Frase personal") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateMotto(draftMotto)
                        showMottoDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMottoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun QuickSupportTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    wash: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .border(0.85.dp, colors.borderHairline, RoundedCornerShape(14.dp))
            .clickable {
                haptics.click()
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 11.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(wash)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.inkPrimary,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
            maxLines = 1
        )
    }
}
