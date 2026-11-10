package es.brujula.jitai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaStatusDot
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.CleanSans
import es.brujula.jitai.core.model.PersonalZone
import es.brujula.jitai.core.model.ZoneCategory
import es.brujula.jitai.ui.BrujulaUiState
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun ZonesScreen(
    state: BrujulaUiState,
    onToggleOptionalGps: (Boolean) -> Unit,
    onCheckLocalGps: () -> Unit,
    onSelectManualZone: (PersonalZone?) -> Unit,
    onSaveZone: (
        id: Long,
        name: String,
        category: ZoneCategory,
        relativeX: Float,
        relativeY: Float,
        radiusMeters: Int,
        anchorReminder: String,
        attachCurrentGps: Boolean
    ) -> Unit,
    onDeleteZone: (Long) -> Unit,
    onNavigateToCheckIn: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics

    var selectedCategoryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var draftX by rememberSaveable { mutableFloatStateOf(0.18f) }
    var draftY by rememberSaveable { mutableFloatStateOf(-0.22f) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onToggleOptionalGps(granted)
        if (granted) {
            onCheckLocalGps()
        }
    }

    val visibleZones = remember(state.zones, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            state.zones
        } else {
            state.zones.filter { it.category.name == selectedCategoryFilter }
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
            title = "Mis espacios",
            subtitle = "Lugares de calma y zonas donde ir despacio.",
            trailingActionLabel = "+ Espacio",
            onTrailingAction = {
                draftX = 0f
                draftY = -0.25f
                showCreateDialog = true
            }
        )

        // Mapa radar atmosférico y luminoso
        AtmosphericZonesRadar(
            zones = state.zones,
            activeZoneId = state.activeZone?.id,
            onZoneTapped = { tapped ->
                haptics.confirm()
                if (state.activeZone?.id == tapped.id) {
                    onSelectManualZone(null)
                } else {
                    onSelectManualZone(tapped)
                }
            },
            onEmptyCanvasTapped = { relX, relY ->
                haptics.tick()
                draftX = relX
                draftY = relY
                showCreateDialog = true
            }
        )

        // Leyenda cromática minimalista
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrujulaStatusDot(color = Color(0xFF4AD2B8), label = "Refugio")
            BrujulaStatusDot(color = Color(0xFFF2B35A), label = "Atención")
            BrujulaStatusDot(color = Color(0xFFEF8771), label = "Sensible")
        }

        // Tarjeta suave de espacio activo
        val active = state.activeZone
        AnimatedVisibility(visible = active != null) {
            if (active != null) {
                val accent = when (active.category) {
                    ZoneCategory.GREEN -> colors.zoneGreen
                    ZoneCategory.YELLOW -> colors.zoneAmber
                    ZoneCategory.RED -> colors.zoneCoral
                }
                BrujulaSurface(
                    borderColor = accent.copy(alpha = 0.40f),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(accent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = active.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.inkPrimary
                            )
                        }
                        Text(
                            text = "Salir",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectManualZone(null) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    val note = active.anchorReminder.ifBlank { active.category.clinicalMeaning }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkSecondary
                    )

                    if (active.category != ZoneCategory.GREEN) {
                        Spacer(modifier = Modifier.height(12.dp))
                        BrujulaPrimaryButton(
                            text = "Sentir cómo estoy",
                            onClick = onNavigateToCheckIn,
                            containerColor = accent,
                            contentColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Filtro por tipo de espacio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BrujulaTagPill(
                label = "Todos",
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null }
            )
            ZoneCategory.entries.forEach { cat ->
                val catColor = when (cat) {
                    ZoneCategory.GREEN -> colors.zoneGreen
                    ZoneCategory.YELLOW -> colors.zoneAmber
                    ZoneCategory.RED -> colors.zoneCoral
                }
                val catWash = when (cat) {
                    ZoneCategory.GREEN -> colors.zoneGreenWash
                    ZoneCategory.YELLOW -> colors.zoneAmberWash
                    ZoneCategory.RED -> colors.zoneCoralWash
                }
                BrujulaTagPill(
                    label = cat.shortBadge,
                    selected = selectedCategoryFilter == cat.name,
                    accentColor = catColor,
                    washColor = catWash,
                    onClick = {
                        selectedCategoryFilter =
                            if (selectedCategoryFilter == cat.name) null else cat.name
                    }
                )
            }
        }

        // Lista limpia de espacios personales
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            visibleZones.forEach { zone ->
                val accent = when (zone.category) {
                    ZoneCategory.GREEN -> colors.zoneGreen
                    ZoneCategory.YELLOW -> colors.zoneAmber
                    ZoneCategory.RED -> colors.zoneCoral
                }
                val isSelected = state.activeZone?.id == zone.id

                BrujulaSurface(
                    borderColor = if (isSelected) accent.copy(alpha = 0.55f) else colors.borderHairline.copy(alpha = 0.5f),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    onClick = {
                        if (isSelected) onSelectManualZone(null) else onSelectManualZone(zone)
                    }
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
                                    .size(28.dp)
                                    .background(accent.copy(alpha = 0.16f), CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(accent, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = zone.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.inkPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (zone.anchorReminder.isNotBlank()) {
                                    Text(
                                        text = zone.anchorReminder,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.inkMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDeleteZone(zone.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Eliminar espacio",
                                tint = colors.inkMuted.copy(alpha = 0.65f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Ubicación opcional discreta en una sola tarjeta tranquila
        BrujulaSurface(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ubicación opcional",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.inkPrimary
                    )
                    Text(
                        text = state.gpsStatusMessage ?: "Solo en tu teléfono, nunca se envía fuera.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted
                    )
                }
                Switch(
                    checked = state.settings.optionalGpsEnabled,
                    onCheckedChange = { wantEnabled ->
                        if (wantEnabled) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            onToggleOptionalGps(false)
                        }
                    }
                )
            }
            if (state.settings.optionalGpsEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                BrujulaTagPill(
                    label = "Comprobar dónde estoy",
                    selected = true,
                    icon = Icons.Outlined.MyLocation,
                    onClick = onCheckLocalGps
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    if (showCreateDialog) {
        CreateZoneMinimalDialog(
            initialX = draftX,
            initialY = draftY,
            gpsEnabled = state.settings.optionalGpsEnabled,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, category, radius, reminder, attachGps ->
                onSaveZone(0L, name, category, draftX, draftY, radius, reminder, attachGps)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun AtmosphericZonesRadar(
    zones: List<PersonalZone>,
    activeZoneId: Long?,
    onZoneTapped: (PersonalZone) -> Unit,
    onEmptyCanvasTapped: (Float, Float) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val activeHaloScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activeHalo"
    )

    val radarShape = RoundedCornerShape(28.dp)
    val labelStyle = TextStyle(
        fontFamily = CleanSans,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFF3EEE5)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(296.dp)
            .clip(radarShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF153544),
                        Color(0xFF0C202A),
                        Color(0xFF08151C)
                    )
                ),
                shape = radarShape
            )
            .border(0.8.dp, Color(0xFFF4EFE6).copy(alpha = 0.12f), radarShape)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(zones) {
                    detectTapGestures { tapOffset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val minDim = minOf(size.width, size.height).toFloat()
                        val usableR = minDim * 0.42f

                        val hit = zones.firstOrNull { zone ->
                            val zx = cx + zone.relativeX * usableR
                            val zy = cy + zone.relativeY * usableR
                            hypot(tapOffset.x - zx, tapOffset.y - zy) <= 28.dp.toPx()
                        }
                        if (hit != null) {
                            onZoneTapped(hit)
                        } else {
                            val relX = ((tapOffset.x - cx) / usableR).coerceIn(-0.88f, 0.88f)
                            val relY = ((tapOffset.y - cy) / usableR).coerceIn(-0.88f, 0.88f)
                            onEmptyCanvasTapped(relX, relY)
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension * 0.42f

            // Anillos concéntricos suaves de horizonte
            listOf(0.35f, 0.68f, 1.0f).forEachIndexed { idx, ratio ->
                drawCircle(
                    color = Color(0xFFF4EFE6).copy(alpha = if (idx == 2) 0.14f else 0.08f),
                    radius = maxR * ratio,
                    center = center,
                    style = Stroke(
                        width = 0.9.dp.toPx(),
                        pathEffect = if (idx == 1) {
                            PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        } else {
                            null
                        }
                    )
                )
            }

            // Ejes sutiles
            drawLine(
                color = Color(0xFFF4EFE6).copy(alpha = 0.06f),
                start = Offset(center.x - maxR * 1.08f, center.y),
                end = Offset(center.x + maxR * 1.08f, center.y),
                strokeWidth = 0.8.dp.toPx()
            )
            drawLine(
                color = Color(0xFFF4EFE6).copy(alpha = 0.06f),
                start = Offset(center.x, center.y - maxR * 1.08f),
                end = Offset(center.x, center.y + maxR * 1.08f),
                strokeWidth = 0.8.dp.toPx()
            )

            // Punto central sereno ("Tú")
            drawCircle(
                color = Color(0xFFF4EFE6).copy(alpha = 0.25f),
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0xFFF4EFE6),
                radius = 3.dp.toPx(),
                center = center
            )

            // Constelación de espacios personales
            zones.forEach { zone ->
                val pos = Offset(
                    x = center.x + zone.relativeX * maxR,
                    y = center.y + zone.relativeY * maxR
                )
                val orbColor = when (zone.category) {
                    ZoneCategory.GREEN -> Color(0xFF4AD2B8)
                    ZoneCategory.YELLOW -> Color(0xFFF2B35A)
                    ZoneCategory.RED -> Color(0xFFEF8771)
                }
                val isSelected = zone.id == activeZoneId
                val haloR = (zone.radiusMeters.coerceIn(120, 600) / 600f) * 38.dp.toPx() + 18.dp.toPx()

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orbColor.copy(alpha = if (isSelected) 0.42f else 0.24f),
                            Color.Transparent
                        ),
                        center = pos,
                        radius = if (isSelected) haloR * activeHaloScale else haloR
                    ),
                    radius = if (isSelected) haloR * activeHaloScale else haloR,
                    center = pos
                )

                if (isSelected) {
                    drawCircle(
                        color = orbColor.copy(alpha = 0.70f),
                        radius = 16.dp.toPx() * activeHaloScale,
                        center = pos,
                        style = Stroke(width = 1.3.dp.toPx())
                    )
                }

                drawCircle(
                    color = orbColor,
                    radius = if (isSelected) 8.5.dp.toPx() else 6.8.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF0A1D26),
                    radius = 2.4.dp.toPx(),
                    center = pos
                )

                val shortTitle = if (zone.name.length > 16) zone.name.take(15) + "…" else zone.name
                val measured = textMeasurer.measure(shortTitle, labelStyle)
                val tx = (pos.x - measured.size.width / 2f)
                    .coerceIn(10.dp.toPx(), size.width - measured.size.width - 10.dp.toPx())
                val ty = (pos.y + 11.dp.toPx())
                    .coerceIn(8.dp.toPx(), size.height - measured.size.height - 8.dp.toPx())

                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(tx, ty)
                )
            }
        }

        Text(
            text = "Toca un punto para situarte · toca el lienzo para añadir",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFA7BDC7).copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )
    }
}

@Composable
private fun CreateZoneMinimalDialog(
    initialX: Float,
    initialY: Float,
    gpsEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        category: ZoneCategory,
        radiusMeters: Int,
        anchorReminder: String,
        attachGps: Boolean
    ) -> Unit
) {
    val colors = BrujulaDesign.colors
    var name by rememberSaveable(initialX, initialY) { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(ZoneCategory.GREEN) }
    var radius by rememberSaveable { mutableIntStateOf(250) }
    var reminder by rememberSaveable { mutableStateOf("") }
    var attachGps by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nuevo espacio",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Casa, Parque, Zona centro)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ZoneCategory.entries.forEach { cat ->
                        val accent = when (cat) {
                            ZoneCategory.GREEN -> colors.zoneGreen
                            ZoneCategory.YELLOW -> colors.zoneAmber
                            ZoneCategory.RED -> colors.zoneCoral
                        }
                        val wash = when (cat) {
                            ZoneCategory.GREEN -> colors.zoneGreenWash
                            ZoneCategory.YELLOW -> colors.zoneAmberWash
                            ZoneCategory.RED -> colors.zoneCoralWash
                        }
                        BrujulaTagPill(
                            label = cat.shortBadge,
                            selected = category == cat,
                            accentColor = accent,
                            washColor = wash,
                            onClick = { category = cat }
                        )
                    }
                }

                OutlinedTextField(
                    value = reminder,
                    onValueChange = { reminder = it },
                    label = { Text("Recordatorio amable (opcional)") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "Alcance aproximado",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkSecondary
                    )
                    Slider(
                        value = radius.toFloat(),
                        onValueChange = { radius = it.roundToInt() },
                        valueRange = 120f..600f
                    )
                }

                if (gpsEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { attachGps = !attachGps }
                    ) {
                        Checkbox(checked = attachGps, onCheckedChange = { attachGps = it })
                        Text(
                            text = "Vincular mi ubicación actual",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name, category, radius, reminder, attachGps)
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
