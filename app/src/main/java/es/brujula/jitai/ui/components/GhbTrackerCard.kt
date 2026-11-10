package es.brujula.jitai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.TabularNumberStyle
import es.brujula.jitai.core.model.GhbVoluntaryLog
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Registro voluntario de GHB/GBL (Parte 2 · §2.4 y CAME F1→M1).
 * Muestra exclusivamente el tiempo transcurrido desde el registro anterior en tipografía tabular neutra.
 * Deliberadamente NO utiliza semáforos verde/rojo ni recomienda dosis o intervalos considerados seguros.
 */
@Composable
fun GhbVoluntaryTrackerCard(
    logs: List<GhbVoluntaryLog>,
    discreetMode: Boolean,
    onLogNewEntry: (String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var optionalNote by remember { mutableStateOf("") }

    LaunchedEffect(logs.firstOrNull()?.timestampMillis) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val latest = logs.firstOrNull()
    val previous = logs.getOrNull(1)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("dd/MM · HH:mm") }

    BrujulaSurface(
        modifier = modifier.fillMaxWidth(),
        containerColor = colors.surfaceElevated,
        borderColor = colors.borderStrong.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = colors.petroleumAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (discreetMode) "Cronómetro personal voluntario" else "Registro voluntario GHB / GBL",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
            }
            if (logs.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showHistory = !showHistory }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${logs.size} reg.",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkSecondary
                    )
                    Icon(
                        imageVector = if (showHistory) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = "Mostrar historial",
                        tint = colors.inkSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (latest != null) {
            val elapsedSec = ((nowMillis - latest.timestampMillis).coerceAtLeast(0L)) / 1000L
            val formattedElapsed = formatElapsedHhMmSs(elapsedSec)
            val formattedStamp = Instant.ofEpochMilli(latest.timestampMillis)
                .atZone(ZoneId.systemDefault())
                .format(timeFormatter)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceSubtle)
                    .border(0.8.dp, colors.borderHairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TIEMPO DESDE EL ÚLTIMO REGISTRO",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedElapsed,
                            style = MaterialTheme.typography.displaySmall.merge(TabularNumberStyle),
                            fontWeight = FontWeight.SemiBold,
                            color = colors.inkPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Última marca",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted
                        )
                        Text(
                            text = formattedStamp,
                            style = MaterialTheme.typography.labelLarge.merge(TabularNumberStyle),
                            color = colors.inkSecondary
                        )
                        if (previous != null) {
                            val prevDiffMin = ((latest.timestampMillis - previous.timestampMillis).coerceAtLeast(0L)) / 60_000L
                            Text(
                                text = "Intervalo previo: ${prevDiffMin / 60}h ${"%02d".format(prevDiffMin % 60)}m",
                                style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle),
                                color = colors.inkMuted
                            )
                        }
                    }
                }
            }
            if (latest.contextNote.isNotBlank() && !discreetMode) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nota: ${latest.contextNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary
                )
            }
        } else {
            Text(
                text = "Sin marcas activas. Si decides anotar un registro voluntario, verás el tiempo exacto transcurrido desde la marca anterior.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Salvaguarda clínica obligatoria (Parte 2 · §2.4 y CAME F1→M1)
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.amberWash.copy(alpha = 0.65f))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = colors.onAmberWash,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Información meramente temporal: esta herramienta no recomienda dosis ni indica intervalos considerados seguros. Evita combinar con alcohol u otros depresores; ante somnolencia profunda o dificultad respiratoria, llama al 112.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onAmberWash
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BrujulaPrimaryButton(
                text = "Anotar marca ahora",
                icon = Icons.Outlined.Add,
                containerColor = colors.petroleumAccent,
                contentColor = if (colors.isDark) colors.canvas else androidx.compose.ui.graphics.Color.White,
                onClick = { showConfirmDialog = true },
                modifier = Modifier.weight(1f)
            )
            if (logs.isNotEmpty()) {
                BrujulaGhostButton(
                    text = "Limpiar",
                    onClick = { showClearDialog = true }
                )
            }
        }

        AnimatedVisibility(visible = showHistory && logs.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.borderHairline)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Marcas recientes registradas en este teléfono",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                logs.take(6).forEachIndexed { index, entry ->
                    val stamp = Instant.ofEpochMilli(entry.timestampMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(timeFormatter)
                    val older = logs.getOrNull(index + 1)
                    val deltaText = older?.let {
                        val mins = ((entry.timestampMillis - it.timestampMillis).coerceAtLeast(0L)) / 60_000L
                        " (+${mins / 60}h ${"%02d".format(mins % 60)}m respecto a la anterior)"
                    } ?: " (primera marca de la serie)"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$stamp$deltaText",
                                style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle),
                                color = colors.inkPrimary
                            )
                            if (entry.contextNote.isNotBlank() && !discreetMode) {
                                Text(
                                    text = entry.contextNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkMuted
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeleteEntry(entry.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Eliminar marca",
                                tint = colors.inkMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Confirmar nueva marca temporal",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Se guardará la hora exacta actual en tu dispositivo para mostrarte el tiempo transcurrido. Recuerda que BRÚJULA no calcula intervalos seguros.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = optionalNote,
                        onValueChange = { optionalNote = it },
                        label = { Text("Nota personal opcional") },
                        placeholder = { Text("Ej. En casa acompañado, sin alcohol") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogNewEntry(optionalNote)
                        optionalNote = ""
                        showConfirmDialog = false
                    }
                ) {
                    Text("Guardar hora actual")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("¿Vaciar marcas temporales?") },
            text = {
                Text("Se eliminarán del dispositivo todas las marcas horarias del registro voluntario GHB/GBL.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    }
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Conservar")
                }
            }
        )
    }
}

fun formatElapsedHhMmSs(totalSeconds: Long): String {
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
