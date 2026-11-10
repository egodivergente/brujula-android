package es.brujula.jitai.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SwapCalls
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSectionTitle
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.TabularNumberStyle
import es.brujula.jitai.core.model.DayPlanItem
import es.brujula.jitai.core.model.DayTimeBlock
import es.brujula.jitai.ui.BrujulaUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyDayScreen(
    state: BrujulaUiState,
    onSavePlanItem: (
        id: Long,
        title: String,
        timeBlock: DayTimeBlock,
        timeLabel: String,
        isVulnerable: Boolean,
        chosenAlternative: String
    ) -> Unit,
    onToggleCompleted: (DayPlanItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onTriggerVulnerableSlot: (DayPlanItem) -> Unit,
    onNavigateToCheckIn: () -> Unit
) {
    val colors = BrujulaDesign.colors

    var selectedBlock by remember { mutableStateOf<DayTimeBlock?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var draftTitle by remember { mutableStateOf("") }
    var draftBlock by remember { mutableStateOf(DayTimeBlock.NIGHT) }
    var draftTimeLabel by remember { mutableStateOf("22:00 h") }
    var draftVulnerable by remember { mutableStateOf(true) }
    var draftAlternative by remember { mutableStateOf("") }

    val filteredItems = remember(state.dayPlan, selectedBlock) {
        if (selectedBlock == null) state.dayPlan else state.dayPlan.filter { it.timeBlock == selectedBlock }
    }
    val vulnerableCount = state.dayPlan.count { it.isVulnerableWindow }
    val completedCount = state.dayPlan.count { it.completed }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BrujulaEditorialHeader(
                eyebrow = "Planificación flexible · Parte 2 §2.2",
                title = "Mi día",
                subtitle = "Anticipa momentos vulnerables sin rigidez y deja preparada tu propia actividad alternativa antes de que llegue el cansancio."
            )
        }

        // Resumen compacto y acción de añadir
        item {
            BrujulaSurface(
                containerColor = colors.surfaceSubtle,
                borderColor = colors.borderStrong.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$completedCount de ${state.dayPlan.size} pasos realizados · $vulnerableCount franjas sensibles con Plan B",
                            style = MaterialTheme.typography.titleSmall.merge(TabularNumberStyle),
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Saltarse un plan nunca penaliza tu progreso; puedes reajustar la jornada cuando quieras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    BrujulaPrimaryButton(
                        text = "Añadir",
                        icon = Icons.Outlined.Add,
                        onClick = {
                            draftTitle = ""
                            draftAlternative = ""
                            showAddDialog = true
                        }
                    )
                }
            }
        }

        // Filtro por tramo horario
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                BrujulaTagPill(
                    label = "Todo el día",
                    selected = selectedBlock == null,
                    onClick = { selectedBlock = null }
                )
                DayTimeBlock.entries.forEach { block ->
                    BrujulaTagPill(
                        label = block.label,
                        selected = selectedBlock == block,
                        onClick = { selectedBlock = block }
                    )
                }
            }
        }

        // Bloques de planificación
        items(filteredItems, key = { it.id }) { item ->
            val accent = if (item.isVulnerableWindow) colors.amberAccent else colors.jadePrimary
            val wash = if (item.isVulnerableWindow) colors.amberWash else colors.jadeWash

            BrujulaSurface(
                borderColor = if (item.isVulnerableWindow && !item.completed) {
                    colors.amberAccent.copy(alpha = 0.55f)
                } else {
                    colors.borderHairline
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(
                        onClick = { onToggleCompleted(item) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (item.completed) {
                                Icons.Outlined.CheckCircle
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = "Cambiar estado completado",
                            tint = if (item.completed) colors.jadePrimary else colors.inkMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${item.timeBlock.label} · ${item.timeLabel}",
                                style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                                color = accent
                            )
                            if (item.isVulnerableWindow) {
                                BrujulaTagPill(
                                    label = "Momento vulnerable",
                                    selected = true,
                                    accentColor = accent,
                                    washColor = wash
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (item.completed) TextDecoration.LineThrough else null,
                            color = if (item.completed) colors.inkMuted else colors.inkPrimary
                        )

                        if (item.chosenAlternative.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SwapCalls,
                                    contentDescription = null,
                                    tint = colors.jadePrimary,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Alternativa elegida: ${item.chosenAlternative}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.inkSecondary
                                )
                            }
                        }

                        if (item.isVulnerableWindow && !item.completed) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BrujulaGhostButton(
                                    text = "Activar apoyo de franja",
                                    icon = Icons.Outlined.NotificationsActive,
                                    accentColor = colors.amberAccent,
                                    onClick = { onTriggerVulnerableSlot(item) }
                                )
                                TextButton(onClick = onNavigateToCheckIn) {
                                    Text("Check-in")
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { onDeleteItem(item.id) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Eliminar elemento",
                            tint = colors.inkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Sugerencias rápidas de alternativas protectoras para añadir en 1 toque
        item {
            BrujulaSectionTitle(
                title = "Ideas rápidas de alternativa",
                meta = "Toca para añadir a tu día"
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickPresets = listOf(
                    Triple("Viernes noche con soledad", DayTimeBlock.NIGHT, "Cenar fuera con amigo + dejar móvil cargando en otra habitación"),
                    Triple("Domingo tarde de bajón", DayTimeBlock.AFTERNOON, "Cine tranquilo, hidratación isotónica y acostarme a las 22:30"),
                    Triple("Impulso de abrir apps cansado", DayTimeBlock.LATE_NIGHT, "Pausa de 20 minutos + respiración 4·2·6 + ducha caliente")
                )
                quickPresets.forEach { (title, block, alt) ->
                    BrujulaTagPill(
                        label = "+ $title",
                        icon = Icons.Outlined.Add,
                        onClick = {
                            onSavePlanItem(
                                0L,
                                title,
                                block,
                                block.hoursHint,
                                true,
                                alt
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nuevo bloque en Mi día") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { draftTitle = it },
                        label = { Text("Situación o actividad") },
                        placeholder = { Text("Ej. Salida de turno nocturno") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Tramo del día:",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DayTimeBlock.entries.forEach { block ->
                            BrujulaTagPill(
                                label = block.label,
                                selected = draftBlock == block,
                                onClick = {
                                    draftBlock = block
                                    draftTimeLabel = block.hoursHint
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draftTimeLabel,
                        onValueChange = { draftTimeLabel = it },
                        label = { Text("Horario orientativo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Señalar como momento vulnerable",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = draftVulnerable,
                            onCheckedChange = { draftVulnerable = it }
                        )
                    }

                    OutlinedTextField(
                        value = draftAlternative,
                        onValueChange = { draftAlternative = it },
                        label = { Text("Actividad alternativa elegida por ti") },
                        placeholder = { Text("Ej. Caminar 20 min y llamar a mi hermano") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSavePlanItem(
                            0L,
                            draftTitle,
                            draftBlock,
                            draftTimeLabel,
                            draftVulnerable,
                            draftAlternative
                        )
                        showAddDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
