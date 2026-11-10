package es.brujula.jitai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrecisionScale
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSectionTitle
import es.brujula.jitai.core.designsystem.BrujulaStatusDot
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.TabularNumberStyle
import es.brujula.jitai.core.model.EmaCheckIn
import es.brujula.jitai.core.model.MilestoneReflection
import es.brujula.jitai.core.model.ProgramGoalType
import es.brujula.jitai.ui.BrujulaUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
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
    val haptics = BrujulaDesign.haptics

    var showGoalEditor by remember { mutableStateOf(false) }
    var editingCheckpointWeek by remember { mutableStateOf<Int?>(null) }

    val chronologicalCheckIns = remember(state.checkIns) {
        state.checkIns.take(12).reversed()
    }
    var selectedChartIndex by remember(chronologicalCheckIns.size) {
        mutableIntStateOf((chronologicalCheckIns.size - 1).coerceAtLeast(0))
    }

    val avgControl = remember(state.checkIns) {
        if (state.checkIns.isEmpty()) 0f
        else state.checkIns.map { it.perceivedControl }.average().toFloat()
    }
    val avgMood = remember(state.checkIns) {
        if (state.checkIns.isEmpty()) 0f
        else state.checkIns.map { it.mood }.average().toFloat()
    }

    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM · HH:mm")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BrujulaEditorialHeader(
                eyebrow = "Recorrido personal · 12 semanas",
                title = "Mi progreso",
                subtitle = "Perspectiva longitudinal sin rachas punitivas ni penalización por tropiezos.",
                trailingActionLabel = "Ajustar meta",
                onTrailingAction = { showGoalEditor = true }
            )
        }

        // 1. Horizonte de 12 semanas y meta elegida (no exige abstinencia para continuar)
        item {
            BrujulaSurface(
                containerColor = colors.surfaceElevated,
                borderColor = colors.borderStrong
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrujulaTagPill(
                        label = "Semana ${state.settings.currentProgramWeek} de 12",
                        selected = true,
                        accentColor = colors.jadePrimary,
                        washColor = colors.jadeWash,
                        icon = Icons.Outlined.Flag,
                        onClick = { showGoalEditor = true }
                    )
                    Text(
                        text = "Cambiar horizonte",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.jadePrimary,
                        modifier = Modifier.clickable {
                            haptics.click()
                            showGoalEditor = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = state.settings.programGoal.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.settings.programGoal.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Barra segmentada de 12 semanas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (week in 1..12) {
                        val reached = week <= state.settings.currentProgramWeek
                        val isCurrent = week == state.settings.currentProgramWeek
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCurrent) 8.dp else 6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isCurrent -> colors.jadePrimary
                                        reached -> colors.jadePrimary.copy(alpha = 0.45f)
                                        else -> colors.surfaceSubtle
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Principio clínico: los tropiezos o consumos no reinician tu proceso ni bloquean ninguna herramienta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted
                )
            }
        }

        // 2. Resumen compacto de ritmo y autopercepción
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "Ritmo 7 días",
                    value = "${state.adherenceLast7DaysPercent}%",
                    caption = "Ref. exploratoria 70%",
                    accent = colors.jadePrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Control medio",
                    value = if (state.checkIns.isEmpty()) "—" else String.format("%.1f", avgControl),
                    caption = "${state.checkIns.size} check-ins",
                    accent = colors.petroleumAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Prácticas",
                    value = "${state.copingSessions.size}",
                    caption = if (state.checkIns.isEmpty()) "Ánimo —" else String.format("Ánimo %.1f", avgMood),
                    accent = colors.amberAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Gráfica interactiva de evolución EMA
        item {
            BrujulaSurface {
                BrujulaSectionTitle(
                    title = "Curva de autoinformes",
                    meta = if (chronologicalCheckIns.isEmpty()) "Sin registros" else "Últimos ${chronologicalCheckIns.size}",
                    actionLabel = "Nuevo check-in",
                    onAction = onNavigateToCheckIn
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BrujulaStatusDot(color = colors.jadePrimary, label = "Control percibido")
                    BrujulaStatusDot(color = colors.petroleumAccent, label = "Ánimo")
                    BrujulaStatusDot(color = colors.amberAccent, label = "Craving")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (chronologicalCheckIns.isEmpty()) {
                    Text(
                        text = "Cuando completes tu primer check-in EMA verás aquí cómo dialogan ánimo, control percibido y craving a lo largo de los días.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkSecondary
                    )
                } else {
                    val safeIndex = selectedChartIndex.coerceIn(0, chronologicalCheckIns.lastIndex)
                    EvolutionTrendCanvas(
                        entries = chronologicalCheckIns,
                        selectedIndex = safeIndex,
                        onSelectIndex = { idx ->
                            if (idx != selectedChartIndex) {
                                selectedChartIndex = idx
                                haptics.tick()
                            }
                        },
                        controlColor = colors.jadePrimary,
                        moodColor = if (colors.isDark) Color(0xFF7EC4DF) else colors.petroleumAccent,
                        cravingColor = colors.amberAccent,
                        gridColor = colors.borderHairline,
                        surfaceColor = colors.surfaceElevated
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val inspected = chronologicalCheckIns[safeIndex]
                    val formattedTime = remember(inspected.timestampMillis) {
                        Instant.ofEpochMilli(inspected.timestampMillis)
                            .atZone(ZoneId.systemDefault())
                            .format(dateTimeFormatter)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceSubtle,
                        border = BorderStroke(0.8.dp, colors.borderHairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.labelLarge.merge(TabularNumberStyle),
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = if (inspected.isReevaluation) "Reevaluación 20m" else "Toca la gráfica para explorar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Control ${inspected.perceivedControl}/10 · Ánimo ${inspected.mood}/10 · Craving ${inspected.craving}/10 · Deseo sexual ${inspected.sexualDesire}/10",
                                style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle),
                                color = colors.inkSecondary
                            )
                            if (inspected.personalNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "«${inspected.personalNote}»",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Autoevaluaciones clínicas longitudinales: Inicio (0), Semana 6 y Semana 12
        item {
            BrujulaSectionTitle(
                title = "Revisiones de horizonte",
                meta = "Inicio · Sem. 6 · Sem. 12"
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tres paradas tranquilas para valorar bienestar, margen de control y objetivos propios junto a enfermería si lo deseas.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            val checkpoints = listOf(
                0 to "Inicio · Punto de partida",
                6 to "Semana 6 · Mitad de camino",
                12 to "Semana 12 · Cierre y balance"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                checkpoints.forEach { (week, title) ->
                    val existing = state.milestones.firstOrNull { it.checkpointWeek == week }
                    MilestoneCheckpointCard(
                        title = title,
                        reflection = existing,
                        onEdit = { editingCheckpointWeek = week }
                    )
                }
            }
        }

        // 5. Logros privados discretos (sin gamificación infantil ni rachas punitivas)
        item {
            BrujulaSectionTitle(
                title = "Hitos discretos de autonomía",
                meta = "Solo visibles en tu teléfono"
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.privateBadges.forEach { badge ->
                    BrujulaSurface(
                        containerColor = if (badge.unlocked) colors.jadeWash.copy(alpha = 0.55f) else colors.surfaceElevated,
                        borderColor = if (badge.unlocked) colors.jadePrimary.copy(alpha = 0.45f) else colors.borderHairline,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            if (badge.unlocked) colors.jadePrimary.copy(alpha = 0.16f) else colors.surfaceSubtle,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (badge.unlocked) Icons.Outlined.CheckCircle else Icons.Outlined.CompassCalibration,
                                        contentDescription = null,
                                        tint = if (badge.unlocked) colors.jadePrimary else colors.inkMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.inkPrimary
                                    )
                                    Text(
                                        text = badge.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.inkSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = badge.progressLabel,
                                style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                                color = if (badge.unlocked) colors.jadePrimary else colors.inkMuted
                            )
                        }
                    }
                }
            }
        }

        // 6. Historial breve de herramientas de afrontamiento utilizadas
        if (state.copingSessions.isNotEmpty()) {
            item {
                BrujulaSurface {
                    BrujulaSectionTitle(
                        title = "Prácticas recientes",
                        meta = "Para comentar qué te sirve"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    state.copingSessions.take(5).forEachIndexed { idx, session ->
                        if (idx > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = colors.borderHairline
                            )
                        }
                        val formatted = remember(session.timestampMillis) {
                            Instant.ofEpochMilli(session.timestampMillis)
                                .atZone(ZoneId.systemDefault())
                                .format(dateTimeFormatter)
                        }
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
                                    imageVector = Icons.Outlined.SelfImprovement,
                                    contentDescription = null,
                                    tint = colors.jadePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = session.toolTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.inkPrimary
                                    )
                                    Text(
                                        text = formatted,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.inkMuted
                                    )
                                }
                            }
                            Text(
                                text = "${(session.durationSeconds / 60).coerceAtLeast(1)} min",
                                style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                                color = colors.inkSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = colors.inkMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Toda tu evolución permanece almacenada localmente en este dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted
                )
            }
        }
    }

    if (showGoalEditor) {
        GoalAndWeekDialog(
            initialMotto = state.settings.personalMotto,
            initialGoal = state.settings.programGoal,
            initialWeek = state.settings.currentProgramWeek,
            onDismiss = { showGoalEditor = false },
            onConfirm = { motto, goal, week ->
                onSaveOrientation(motto, goal, week)
                showGoalEditor = false
            }
        )
    }

    editingCheckpointWeek?.let { targetWeek ->
        val current = state.milestones.firstOrNull { it.checkpointWeek == targetWeek }
        MilestoneEditorDialog(
            checkpointWeek = targetWeek,
            existing = current,
            onDismiss = { editingCheckpointWeek = null },
            onSave = { days, episodes, wellbeing, attainment, note ->
                onSaveMilestone(targetWeek, days, episodes, wellbeing, attainment, note)
                editingCheckpointWeek = null
            }
        )
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    caption: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    BrujulaSurface(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.merge(TabularNumberStyle),
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun EvolutionTrendCanvas(
    entries: List<EmaCheckIn>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    controlColor: Color,
    moodColor: Color,
    cravingColor: Color,
    gridColor: Color,
    surfaceColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .pointerInput(entries.size) {
                detectTapGestures { offset ->
                    if (entries.isEmpty()) return@detectTapGestures
                    val padH = 14.dp.toPx()
                    val usableW = (size.width - padH * 2).coerceAtLeast(1f)
                    val idx = if (entries.size == 1) 0 else {
                        (((offset.x - padH) / usableW) * (entries.size - 1))
                            .roundToInt()
                            .coerceIn(0, entries.lastIndex)
                    }
                    onSelectIndex(idx)
                }
            }
            .pointerInput(entries.size) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    if (entries.isEmpty()) return@detectHorizontalDragGestures
                    val padH = 14.dp.toPx()
                    val usableW = (size.width - padH * 2).coerceAtLeast(1f)
                    val idx = if (entries.size == 1) 0 else {
                        (((change.position.x - padH) / usableW) * (entries.size - 1))
                            .roundToInt()
                            .coerceIn(0, entries.lastIndex)
                    }
                    onSelectIndex(idx)
                }
            }
    ) {
        val padH = 14.dp.toPx()
        val padV = 14.dp.toPx()
        val plotW = (size.width - padH * 2).coerceAtLeast(1f)
        val plotH = (size.height - padV * 2).coerceAtLeast(1f)

        fun pointFor(index: Int, score: Int): Offset {
            val x = if (entries.size <= 1) size.width / 2f
            else padH + (plotW / (entries.size - 1).toFloat()) * index
            val normalized = (score.coerceIn(0, 10) / 10f)
            val y = padV + plotH * (1f - normalized)
            return Offset(x, y)
        }

        // Líneas guía horizontales (0, 5 y 10)
        listOf(0f, 0.5f, 1f).forEach { level ->
            val y = padV + plotH * level
            drawLine(
                color = gridColor,
                start = Offset(padH, y),
                end = Offset(size.width - padH, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (level == 0.5f) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
            )
        }

        // Cursor vertical de inspección
        val selectedX = pointFor(selectedIndex, 5).x
        drawLine(
            color = controlColor.copy(alpha = 0.28f),
            start = Offset(selectedX, padV / 2f),
            end = Offset(selectedX, size.height - padV / 2f),
            strokeWidth = 1.5.dp.toPx()
        )

        fun drawSeries(selector: (EmaCheckIn) -> Int, color: Color, strokeDp: Float) {
            if (entries.size > 1) {
                val path = Path()
                entries.forEachIndexed { index, item ->
                    val pt = pointFor(index, selector(item))
                    if (index == 0) {
                        path.moveTo(pt.x, pt.y)
                    } else {
                        val prev = pointFor(index - 1, selector(entries[index - 1]))
                        val controlX = (prev.x + pt.x) / 2f
                        path.cubicTo(controlX, prev.y, controlX, pt.y, pt.x, pt.y)
                    }
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = strokeDp.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            entries.forEachIndexed { index, item ->
                val pt = pointFor(index, selector(item))
                val isSelected = index == selectedIndex
                drawCircle(
                    color = color,
                    radius = if (isSelected) 5.2.dp.toPx() else 3.2.dp.toPx(),
                    center = pt
                )
                if (isSelected) {
                    drawCircle(
                        color = surfaceColor,
                        radius = 2.2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        drawSeries({ it.craving }, cravingColor, 2.0f)
        drawSeries({ it.mood }, moodColor, 2.2f)
        drawSeries({ it.perceivedControl }, controlColor, 2.8f)
    }
}

@Composable
private fun MilestoneCheckpointCard(
    title: String,
    reflection: MilestoneReflection?,
    onEdit: () -> Unit
) {
    val colors = BrujulaDesign.colors
    BrujulaSurface(
        onClick = onEdit,
        containerColor = colors.surfaceElevated,
        borderColor = if (reflection != null) colors.jadePrimary.copy(alpha = 0.45f) else colors.borderHairline
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (reflection != null) Icons.Outlined.Timeline else Icons.Outlined.EditNote,
                    contentDescription = null,
                    tint = if (reflection != null) colors.jadePrimary else colors.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
            }
            BrujulaTagPill(
                label = if (reflection != null) "Actualizar" else "Completar",
                selected = reflection != null,
                onClick = onEdit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (reflection == null) {
            Text(
                text = "Pendiente de registrar cuando llegues a este momento del programa.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted
            )
        } else {
            Text(
                text = "Bienestar ${reflection.wellbeingScore}/10 · Meta personal ${reflection.goalAttainmentScore}/10 · Días consumo (30d): ${reflection.daysUsedLastMonth} · Pérdida control: ${reflection.lossOfControlEpisodes}",
                style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle),
                color = colors.inkSecondary
            )
            AnimatedVisibility(visible = reflection.reflectionNote.isNotBlank()) {
                Text(
                    text = "«${reflection.reflectionNote}»",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalAndWeekDialog(
    initialMotto: String,
    initialGoal: ProgramGoalType,
    initialWeek: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, ProgramGoalType, Int) -> Unit
) {
    val colors = BrujulaDesign.colors
    var motto by remember { mutableStateOf(initialMotto) }
    var selectedGoal by remember { mutableStateOf(initialGoal) }
    var week by remember { mutableIntStateOf(initialWeek.coerceIn(1, 12)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Meta personal y semana",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Elige el enfoque que mejor te represente ahora. Puedes modificarlo en cualquier momento:",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary
                )

                ProgramGoalType.entries.forEach { goal ->
                    val isSelected = goal == selectedGoal
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) colors.jadeWash else colors.surfaceSubtle,
                        border = BorderStroke(
                            0.9.dp,
                            if (isSelected) colors.jadePrimary else colors.borderHairline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedGoal = goal }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inkPrimary
                            )
                            Text(
                                text = goal.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary
                            )
                        }
                    }
                }

                BrujulaPrecisionScale(
                    title = "Semana actual del recorrido",
                    subtitle = "Horizonte de 12 semanas",
                    value = week,
                    onValueChange = { week = it },
                    minAnchorLabel = "Sem. 1",
                    maxAnchorLabel = "Sem. 12",
                    accentColor = colors.jadePrimary,
                    valueRange = 1..12
                )

                OutlinedTextField(
                    value = motto,
                    onValueChange = { motto = it },
                    label = { Text("Frase de orientación personal") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            BrujulaPrimaryButton(
                text = "Guardar",
                onClick = { onConfirm(motto, selectedGoal, week) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun MilestoneEditorDialog(
    checkpointWeek: Int,
    existing: MilestoneReflection?,
    onDismiss: () -> Unit,
    onSave: (
        daysUsed: Int,
        lossOfControlEpisodes: Int,
        wellbeing: Int,
        goalAttainment: Int,
        note: String
    ) -> Unit
) {
    val colors = BrujulaDesign.colors
    var daysUsed by remember { mutableIntStateOf(existing?.daysUsedLastMonth ?: 2) }
    var episodes by remember { mutableIntStateOf(existing?.lossOfControlEpisodes ?: 1) }
    var wellbeing by remember { mutableIntStateOf(existing?.wellbeingScore ?: 6) }
    var attainment by remember { mutableIntStateOf(existing?.goalAttainmentScore ?: 6) }
    var note by remember { mutableStateOf(existing?.reflectionNote ?: "") }

    val titleLabel = when (checkpointWeek) {
        0 -> "Inicio · Punto de partida"
        6 -> "Revisión de Semana 6"
        else -> "Balance de Semana 12"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = titleLabel, style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StepperCounterRow(
                    label = "Días de consumo (últimos 30 días)",
                    value = daysUsed,
                    onChange = { daysUsed = it.coerceIn(0, 30) }
                )
                StepperCounterRow(
                    label = "Episodios de pérdida de control",
                    value = episodes,
                    onChange = { episodes = it.coerceIn(0, 30) }
                )
                BrujulaPrecisionScale(
                    title = "Bienestar percibido",
                    subtitle = "Valoración global actual",
                    value = wellbeing,
                    onValueChange = { wellbeing = it },
                    minAnchorLabel = "Bajo",
                    maxAnchorLabel = "Pleno",
                    accentColor = colors.jadePrimary
                )
                BrujulaPrecisionScale(
                    title = "Alineación con tu meta",
                    subtitle = "Grado en que sientes que avanzas hacia lo pactado",
                    value = attainment,
                    onValueChange = { attainment = it },
                    minAnchorLabel = "Lejos",
                    maxAnchorLabel = "Alineado",
                    accentColor = colors.petroleumAccent
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Qué te ha ayudado o qué ajustarías") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            BrujulaPrimaryButton(
                text = "Guardar revisión",
                onClick = { onSave(daysUsed, episodes, wellbeing, attainment, note) }
            )
        },
        dismissButton = {
            BrujulaGhostButton(text = "Cerrar", onClick = onDismiss)
        }
    )
}

@Composable
private fun StepperCounterRow(
    label: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkPrimary,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = colors.surfaceSubtle,
                border = BorderStroke(0.8.dp, colors.borderStrong),
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        haptics.tick()
                        onChange(value - 1)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("−", style = MaterialTheme.typography.titleMedium, color = colors.inkPrimary)
                }
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                color = colors.inkPrimary,
                modifier = Modifier.width(28.dp)
            )
            Surface(
                shape = CircleShape,
                color = colors.surfaceSubtle,
                border = BorderStroke(0.8.dp, colors.borderStrong),
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        haptics.tick()
                        onChange(value + 1)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", style = MaterialTheme.typography.titleMedium, color = colors.inkPrimary)
                }
            }
        }
    }
}
