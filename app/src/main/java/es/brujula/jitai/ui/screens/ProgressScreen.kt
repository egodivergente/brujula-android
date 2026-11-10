package es.brujula.jitai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSectionTitle
import es.brujula.jitai.core.designsystem.BrujulaStatusDot
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.EditorialSerif
import es.brujula.jitai.core.model.EmaCheckIn
import es.brujula.jitai.core.model.MilestoneReflection
import es.brujula.jitai.core.model.ProgramGoalType
import es.brujula.jitai.ui.BrujulaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    state: BrujulaUiState,
    onSaveOrientation: (motto: String, goal: ProgramGoalType, week: Int) -> Unit,
    onSaveMilestone: (
        checkpointWeek: Int,
        daysUsed: Int,
        lossOfControlEpisodes: Int,
        wellbeing: Int,
        goalAttainment: Int,
        note: String
    ) -> Unit,
    onNavigateToCheckIn: () -> Unit
) {
    val colors = BrujulaDesign.colors

    var showGoalEditor by rememberSaveable { mutableStateOf(false) }
    var editingCheckpointWeek by rememberSaveable { mutableStateOf<Int?>(null) }
    var showImpulseOverlay by rememberSaveable { mutableStateOf(false) }

    val chronological = remember(state.checkIns) {
        state.checkIns.take(12).reversed()
    }
    var selectedPointIdx by remember(chronological.size) {
        mutableIntStateOf((chronological.size - 1).coerceAtLeast(0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BrujulaEditorialHeader(
            title = "Mi camino",
            subtitle = state.settings.programGoal.summary,
            trailingActionLabel = "Mi meta",
            onTrailingAction = { showGoalEditor = true }
        )

        // 1. Horizonte visual de 12 etapas (puntos de luz, sin cajas de porcentajes)
        TwelveStepHorizonCard(
            goalTitle = state.settings.programGoal.title,
            currentWeek = state.settings.currentProgramWeek,
            onClick = { showGoalEditor = true }
        )

        // 2. Onda orgánica de evolución cualitativa
        BrujulaSurface(contentPadding = PaddingValues(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paisaje interior",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
                BrujulaTagPill(
                    label = "Impulso",
                    selected = showImpulseOverlay,
                    accentColor = colors.coralAccent,
                    washColor = colors.coralWash,
                    onClick = { showImpulseOverlay = !showImpulseOverlay }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BrujulaStatusDot(color = colors.jadePrimary, label = "Margen")
                BrujulaStatusDot(color = colors.petroleumAccent, label = "Ánimo")
                if (showImpulseOverlay) {
                    BrujulaStatusDot(color = colors.coralAccent, label = "Impulso")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (chronological.size < 2) {
                Text(
                    text = "Con un par de registros aparecerá aquí la onda suave de tus días.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                BrujulaPrimaryButton(
                    text = "Sentir ahora",
                    onClick = onNavigateToCheckIn
                )
            } else {
                FlowingHorizonWaveCanvas(
                    points = chronological,
                    selectedIndex = selectedPointIdx,
                    showImpulse = showImpulseOverlay,
                    onSelectIndex = { selectedPointIdx = it }
                )

                val inspected = chronological.getOrNull(selectedPointIdx)
                if (inspected != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val dayStr = remember(inspected.timestampMillis) {
                        SimpleDateFormat("EEE d MMM · HH:mm", Locale("es", "ES"))
                            .format(Date(inspected.timestampMillis))
                    }
                    Text(
                        text = dayStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${qualitativeMood(inspected.mood)} · ${qualitativeMargin(inspected.perceivedControl)} · ${qualitativeImpulse(inspected.craving)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkPrimary
                    )
                    if (inspected.personalNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "«${inspected.personalNote}»",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = colors.inkSecondary
                        )
                    }
                }
            }
        }

        // 3. Tres momentos de reflexión personal sin penalizar recaídas
        BrujulaSectionTitle(title = "Pausas de perspectiva")

        val checkpoints = listOf(
            0 to "Punto de partida",
            6 to "Mitad de camino",
            12 to "Mirada amplia"
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            checkpoints.forEach { (weekKey, label) ->
                val saved = state.milestones.firstOrNull { it.checkpointWeek == weekKey }
                BrujulaSurface(
                    contentPadding = PaddingValues(18.dp),
                    onClick = { editingCheckpointWeek = weekKey }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                        Text(
                            text = if (saved != null) "Editar" else "Escribir",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.jadePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = saved?.reflectionNote?.ifBlank { "Reflexión guardada" }
                            ?: "Un espacio tranquilo para anotar cómo te sientes contigo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (saved != null) colors.inkSecondary else colors.inkMuted
                    )
                }
            }
        }

        // 4. Constelación discreta de logros privados (sin rachas punitivas)
        BrujulaSectionTitle(title = "Huellas propias")

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.privateBadges.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { badge ->
                        val accent = if (badge.unlocked) colors.jadePrimary else colors.inkMuted
                        BrujulaSurface(
                            modifier = Modifier.weight(1f),
                            borderColor = if (badge.unlocked) {
                                colors.jadePrimary.copy(alpha = 0.35f)
                            } else {
                                colors.borderHairline.copy(alpha = 0.45f)
                            },
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Canvas(modifier = Modifier.size(26.dp)) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(
                                    color = accent.copy(alpha = 0.18f),
                                    radius = size.minDimension / 2f,
                                    center = c
                                )
                                drawCircle(
                                    color = accent,
                                    radius = if (badge.unlocked) 5.dp.toPx() else 3.dp.toPx(),
                                    center = c
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inkPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    if (showGoalEditor) {
        GoalOrientationDialog(
            initialMotto = state.settings.personalMotto,
            initialGoal = state.settings.programGoal,
            initialWeek = state.settings.currentProgramWeek,
            onDismiss = { showGoalEditor = false },
            onSave = { motto, goal, week ->
                onSaveOrientation(motto, goal, week)
                showGoalEditor = false
            }
        )
    }

    if (editingCheckpointWeek != null) {
        val wk = editingCheckpointWeek ?: 0
        val existing = state.milestones.firstOrNull { it.checkpointWeek == wk }
        MilestoneCalmDialog(
            checkpointWeek = wk,
            existing = existing,
            onDismiss = { editingCheckpointWeek = null },
            onSave = { wellbeing, goalScore, note ->
                onSaveMilestone(
                    wk,
                    existing?.daysUsedLastMonth ?: 0,
                    existing?.lossOfControlEpisodes ?: 0,
                    wellbeing,
                    goalScore,
                    note
                )
                editingCheckpointWeek = null
            }
        )
    }
}

@Composable
private fun TwelveStepHorizonCard(
    goalTitle: String,
    currentWeek: Int,
    onClick: () -> Unit
) {
    val colors = BrujulaDesign.colors

    BrujulaSurface(
        contentPadding = PaddingValues(20.dp),
        onClick = onClick
    ) {
        Text(
            text = goalTitle,
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = EditorialSerif),
            color = colors.inkPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            val total = 12
            val pad = 8.dp.toPx()
            val stepX = (size.width - pad * 2f) / (total - 1)
            val cy = size.height / 2f

            drawLine(
                color = colors.borderHairline,
                start = Offset(pad, cy),
                end = Offset(size.width - pad, cy),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )

            val activeEnd = pad + stepX * (currentWeek.coerceIn(1, 12) - 1)
            drawLine(
                color = colors.jadePrimary,
                start = Offset(pad, cy),
                end = Offset(activeEnd, cy),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )

            for (i in 0 until total) {
                val x = pad + stepX * i
                val weekNum = i + 1
                val reached = weekNum <= currentWeek
                val isCurrent = weekNum == currentWeek
                if (isCurrent) {
                    drawCircle(
                        color = colors.jadePrimary.copy(alpha = 0.24f),
                        radius = 11.dp.toPx(),
                        center = Offset(x, cy)
                    )
                }
                drawCircle(
                    color = if (reached) colors.jadePrimary else colors.borderStrong.copy(alpha = 0.55f),
                    radius = if (isCurrent) 5.5.dp.toPx() else 3.6.dp.toPx(),
                    center = Offset(x, cy)
                )
            }
        }
    }
}

@Composable
private fun FlowingHorizonWaveCanvas(
    points: List<EmaCheckIn>,
    selectedIndex: Int,
    showImpulse: Boolean,
    onSelectIndex: (Int) -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .pointerInput(points.size) {
                detectTapGestures { offset ->
                    val pad = 12.dp.toPx()
                    val usableW = (size.width - pad * 2f).coerceAtLeast(1f)
                    val step = usableW / (points.size - 1).coerceAtLeast(1)
                    val idx = ((offset.x - pad) / step).roundToInt()
                        .coerceIn(0, points.lastIndex)
                    haptics.tick()
                    onSelectIndex(idx)
                }
            }
    ) {
        val padX = 12.dp.toPx()
        val padY = 14.dp.toPx()
        val usableW = (size.width - padX * 2f).coerceAtLeast(1f)
        val usableH = (size.height - padY * 2f).coerceAtLeast(1f)
        val stepX = usableW / (points.size - 1).coerceAtLeast(1)

        fun mapPoint(index: Int, score: Int): Offset {
            val x = padX + stepX * index
            val norm = (score.coerceIn(0, 10) / 10f)
            val y = padY + usableH * (1f - norm)
            return Offset(x, y)
        }

        fun buildSmoothCurve(extractor: (EmaCheckIn) -> Int): Pair<Path, Path> {
            val strokePath = Path()
            val fillPath = Path()
            val offsets = points.mapIndexed { idx, item -> mapPoint(idx, extractor(item)) }
            offsets.forEachIndexed { idx, pt ->
                if (idx == 0) {
                    strokePath.moveTo(pt.x, pt.y)
                    fillPath.moveTo(pt.x, size.height - padY)
                    fillPath.lineTo(pt.x, pt.y)
                } else {
                    val prev = offsets[idx - 1]
                    val midX = (prev.x + pt.x) / 2f
                    strokePath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                    fillPath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                }
            }
            val last = offsets.last()
            fillPath.lineTo(last.x, size.height - padY)
            fillPath.close()
            return strokePath to fillPath
        }

        // Onda de Margen (Jade)
        val (controlStroke, controlFill) = buildSmoothCurve { it.perceivedControl }
        drawPath(
            path = controlFill,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.jadePrimary.copy(alpha = 0.24f),
                    Color.Transparent
                )
            )
        )
        drawPath(
            path = controlStroke,
            color = colors.jadePrimary,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Onda de Ánimo (Petróleo)
        val (moodStroke, _) = buildSmoothCurve { it.mood }
        drawPath(
            path = moodStroke,
            color = colors.petroleumAccent.copy(alpha = 0.80f),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Onda opcional de Impulso (Coral suave)
        if (showImpulse) {
            val (cravingStroke, _) = buildSmoothCurve { it.craving }
            drawPath(
                path = cravingStroke,
                color = colors.coralAccent.copy(alpha = 0.82f),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Indicador luminoso del punto seleccionado
        val safeIdx = selectedIndex.coerceIn(0, points.lastIndex)
        val focusPt = mapPoint(safeIdx, points[safeIdx].perceivedControl)
        drawLine(
            color = colors.borderStrong.copy(alpha = 0.45f),
            start = Offset(focusPt.x, padY),
            end = Offset(focusPt.x, size.height - padY),
            strokeWidth = 0.9.dp.toPx()
        )
        drawCircle(
            color = colors.jadePrimary.copy(alpha = 0.25f),
            radius = 10.dp.toPx(),
            center = focusPt
        )
        drawCircle(
            color = colors.jadePrimary,
            radius = 4.8.dp.toPx(),
            center = focusPt
        )
    }
}

private fun qualitativeMood(v: Int): String = when (v) {
    0, 1, 2 -> "Ánimo bajo"
    3, 4, 5, 6 -> "Ánimo sereno"
    else -> "Ánimo luminoso"
}

private fun qualitativeMargin(v: Int): String = when (v) {
    0, 1, 2, 3 -> "Poco margen"
    4, 5, 6 -> "Margen estable"
    else -> "Con buen aire"
}

private fun qualitativeImpulse(v: Int): String = when (v) {
    0, 1, 2 -> "Impulso en calma"
    3, 4, 5, 6 -> "Impulso moderado"
    else -> "Impulso intenso"
}

@Composable
private fun GoalOrientationDialog(
    initialMotto: String,
    initialGoal: ProgramGoalType,
    initialWeek: Int,
    onDismiss: () -> Unit,
    onSave: (String, ProgramGoalType, Int) -> Unit
) {
    val colors = BrujulaDesign.colors
    var motto by rememberSaveable { mutableStateOf(initialMotto) }
    var goal by rememberSaveable { mutableStateOf(initialGoal) }
    var week by rememberSaveable { mutableIntStateOf(initialWeek) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tu rumbo personal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgramGoalType.entries.forEach { option ->
                    BrujulaSurface(
                        borderColor = if (goal == option) colors.jadePrimary else colors.borderHairline,
                        contentPadding = PaddingValues(14.dp),
                        onClick = { goal = option }
                    ) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.inkPrimary
                        )
                        Text(
                            text = option.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted
                        )
                    }
                }

                OutlinedTextField(
                    value = motto,
                    onValueChange = { motto = it },
                    label = { Text("Frase personal") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "Etapa actual · Semana $week",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkSecondary
                    )
                    Slider(
                        value = week.toFloat(),
                        onValueChange = { week = it.roundToInt().coerceIn(1, 12) },
                        valueRange = 1f..12f,
                        steps = 10
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(motto, goal, week) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun MilestoneCalmDialog(
    checkpointWeek: Int,
    existing: MilestoneReflection?,
    onDismiss: () -> Unit,
    onSave: (wellbeing: Int, goalScore: Int, note: String) -> Unit
) {
    val colors = BrujulaDesign.colors
    var wellbeing by rememberSaveable { mutableIntStateOf(existing?.wellbeingScore ?: 7) }
    var goalScore by rememberSaveable { mutableIntStateOf(existing?.goalAttainmentScore ?: 7) }
    var note by rememberSaveable { mutableStateOf(existing?.reflectionNote ?: "") }

    val title = when (checkpointWeek) {
        0 -> "Punto de partida"
        6 -> "Mitad de camino"
        else -> "Mirada amplia"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Sensación general de bienestar",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkSecondary
                )
                Slider(
                    value = wellbeing.toFloat(),
                    onValueChange = { wellbeing = it.roundToInt() },
                    valueRange = 0f..10f
                )

                Text(
                    text = "Cercanía a lo que buscas cuidar",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkSecondary
                )
                Slider(
                    value = goalScore.toFloat(),
                    onValueChange = { goalScore = it.roundToInt() },
                    valueRange = 0f..10f
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota para ti") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(wellbeing, goalScore, note) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
