package es.brujula.jitai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.model.DayPlanItem
import es.brujula.jitai.core.model.DayTimeBlock
import es.brujula.jitai.ui.BrujulaUiState
import kotlin.math.sin

private data class QuickAnchorSuggestion(
    val label: String,
    val block: DayTimeBlock,
    val hour: String,
    val alternative: String,
    val vulnerable: Boolean
)

private val QuickAnchors = listOf(
    QuickAnchorSuggestion(
        label = "Paseo sin pantallas",
        block = DayTimeBlock.AFTERNOON,
        hour = "19:30",
        alternative = "Dejar el móvil cargando 30 min y tomar aire fresco",
        vulnerable = false
    ),
    QuickAnchorSuggestion(
        label = "Noche tranquila",
        block = DayTimeBlock.NIGHT,
        hour = "23:00",
        alternative = "Cena cálida, ducha tibia y modo silencio",
        vulnerable = true
    ),
    QuickAnchorSuggestion(
        label = "Descanso protegido",
        block = DayTimeBlock.LATE_NIGHT,
        hour = "01:00",
        alternative = "Luz tenue, agua a mano y cerrar apps sociales",
        vulnerable = true
    )
)

@Composable
fun MyDayScreen(
    state: BrujulaUiState,
    onSavePlanItem: (
        id: Long,
        title: String,
        timeBlock: DayTimeBlock,
        timeLabel: String,
        isVulnerableWindow: Boolean,
        chosenAlternative: String
    ) -> Unit,
    onToggleCompleted: (DayPlanItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onTriggerVulnerableSlot: (DayPlanItem) -> Unit,
    onNavigateToCheckIn: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics

    var selectedBlock by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    val filteredItems = remember(state.dayPlan, selectedBlock) {
        if (selectedBlock == null) {
            state.dayPlan
        } else {
            state.dayPlan.filter { it.timeBlock.name == selectedBlock }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BrujulaEditorialHeader(
            title = "Mi día",
            subtitle = "Pequeñas anclas para acompañarte hoy.",
            trailingActionLabel = "+ Ancla",
            onTrailingAction = { showAddDialog = true }
        )

        // Arco solar visual del día (sin contadores ni cajas numéricas)
        DayHorizonArcCanvas(items = state.dayPlan)

        // Filtro por momento del día
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BrujulaTagPill(
                label = "Todo",
                selected = selectedBlock == null,
                onClick = { selectedBlock = null }
            )
            DayTimeBlock.entries.forEach { block ->
                BrujulaTagPill(
                    label = block.label,
                    selected = selectedBlock == block.name,
                    onClick = {
                        selectedBlock = if (selectedBlock == block.name) null else block.name
                    }
                )
            }
        }

        // Tarjetas serenas de la jornada
        if (filteredItems.isEmpty()) {
            BrujulaSurface(
                contentPadding = PaddingValues(24.dp),
                onClick = { showAddDialog = true }
            ) {
                Text(
                    text = "Un espacio en blanco para hoy",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toca aquí o elige una idea rápida abajo para colocar un gesto amable en tu día.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredItems.forEach { item ->
                    val accent = if (item.isVulnerableWindow) colors.amberAccent else colors.jadePrimary

                    BrujulaSurface(
                        borderColor = if (item.isVulnerableWindow && !item.completed) {
                            colors.amberAccent.copy(alpha = 0.38f)
                        } else {
                            colors.borderHairline.copy(alpha = 0.50f)
                        },
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Botón circular orgánico de completado
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.completed) colors.jadePrimary else colors.surfaceSubtle,
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (item.completed) colors.jadePrimary else accent.copy(alpha = 0.65f),
                                        CircleShape
                                    )
                                    .clickable {
                                        haptics.confirm()
                                        onToggleCompleted(item)
                                    }
                            ) {
                                if (item.completed) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Completado",
                                        tint = if (colors.isDark) colors.canvas else androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.timeLabel} · ${item.timeBlock.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (item.completed) colors.inkMuted else accent
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        textDecoration = if (item.completed) {
                                            TextDecoration.LineThrough
                                        } else {
                                            TextDecoration.None
                                        }
                                    ),
                                    color = if (item.completed) colors.inkMuted else colors.inkPrimary
                                )

                                if (item.chosenAlternative.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.chosenAlternative,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.inkSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (item.isVulnerableWindow && !item.completed) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        BrujulaTagPill(
                                            label = "Preparar plan B",
                                            selected = true,
                                            accentColor = colors.amberAccent,
                                            washColor = colors.amberWash,
                                            icon = Icons.Outlined.WbTwilight,
                                            onClick = { onTriggerVulnerableSlot(item) }
                                        )
                                        BrujulaTagPill(
                                            label = "Sentir",
                                            selected = false,
                                            onClick = onNavigateToCheckIn
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onDeleteItem(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Quitar",
                                    tint = colors.inkMuted.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inspiración de un toque (sin esfuerzo mental)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Ideas de un toque",
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkMuted,
                modifier = Modifier.padding(start = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAnchors.forEach { idea ->
                    BrujulaTagPill(
                        label = idea.label,
                        icon = Icons.Outlined.Add,
                        onClick = {
                            haptics.confirm()
                            onSavePlanItem(
                                0L,
                                idea.label,
                                idea.block,
                                idea.hour,
                                idea.vulnerable,
                                idea.alternative
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    if (showAddDialog) {
        AddPlanAnchorDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, block, hour, vulnerable, alternative ->
                onSavePlanItem(0L, title, block, hour, vulnerable, alternative)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DayHorizonArcCanvas(items: List<DayPlanItem>) {
    val colors = BrujulaDesign.colors

    BrujulaSurface(
        containerColor = colors.surfaceElevated,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
        ) {
            val padX = 16.dp.toPx()
            val usableW = (size.width - padX * 2f).coerceAtLeast(1f)
            val baseLineY = size.height * 0.78f
            val arcAmplitude = size.height * 0.52f

            // Curva suave del horizonte diario
            val arcPath = Path()
            val fillPath = Path()
            val steps = 48
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val x = padX + usableW * t
                val y = baseLineY - sin(t * Math.PI).toFloat() * arcAmplitude
                if (i == 0) {
                    arcPath.moveTo(x, y)
                    fillPath.moveTo(x, baseLineY)
                    fillPath.lineTo(x, y)
                } else {
                    arcPath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(padX + usableW, baseLineY)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.jadePrimary.copy(alpha = 0.16f),
                        colors.jadePrimary.copy(alpha = 0.0f)
                    )
                )
            )
            drawPath(
                path = arcPath,
                color = colors.jadePrimary.copy(alpha = 0.55f),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Puntos luminosos por cada ancla del día
            items.forEachIndexed { idx, item ->
                val baseT = when (item.timeBlock) {
                    DayTimeBlock.MORNING -> 0.18f
                    DayTimeBlock.AFTERNOON -> 0.44f
                    DayTimeBlock.NIGHT -> 0.72f
                    DayTimeBlock.LATE_NIGHT -> 0.90f
                }
                val jitter = ((idx % 3) - 1) * 0.035f
                val t = (baseT + jitter).coerceIn(0.06f, 0.94f)
                val px = padX + usableW * t
                val py = baseLineY - sin(t * Math.PI).toFloat() * arcAmplitude
                val dotColor = when {
                    item.completed -> colors.jadePrimary
                    item.isVulnerableWindow -> colors.amberAccent
                    else -> colors.petroleumAccent
                }

                drawCircle(
                    color = dotColor.copy(alpha = 0.24f),
                    radius = 10.dp.toPx(),
                    center = Offset(px, py)
                )
                drawCircle(
                    color = dotColor,
                    radius = 4.6.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }
    }
}

@Composable
private fun AddPlanAnchorDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        block: DayTimeBlock,
        hourLabel: String,
        vulnerable: Boolean,
        alternative: String
    ) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var block by rememberSaveable { mutableStateOf(DayTimeBlock.NIGHT) }
    var hourLabel by rememberSaveable { mutableStateOf(DayTimeBlock.NIGHT.hoursHint) }
    var vulnerable by rememberSaveable { mutableStateOf(false) }
    var alternative by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva ancla",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Qué momento quieres cuidar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DayTimeBlock.entries.forEach { b ->
                        BrujulaTagPill(
                            label = b.label,
                            selected = block == b,
                            onClick = {
                                block = b
                                hourLabel = b.hoursHint
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = hourLabel,
                    onValueChange = { hourLabel = it },
                    label = { Text("Hora aproximada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = alternative,
                    onValueChange = { alternative = it },
                    label = { Text("Alternativa amable (plan B)") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { vulnerable = !vulnerable }
                ) {
                    Checkbox(checked = vulnerable, onCheckedChange = { vulnerable = it })
                    Text(
                        text = "Es un momento más sensible para mí",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, block, hourLabel, vulnerable, alternative)
                }
            ) {
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
