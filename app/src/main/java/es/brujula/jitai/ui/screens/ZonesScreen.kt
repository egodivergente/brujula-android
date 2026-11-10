package es.brujula.jitai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaStatusDot
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.CleanSans
import es.brujula.jitai.core.designsystem.TabularNumberStyle
import es.brujula.jitai.core.model.PersonalZone
import es.brujula.jitai.core.model.ZoneCategory
import es.brujula.jitai.ui.BrujulaUiState
import kotlin.math.hypot

@OptIn(ExperimentalLayoutApi::class)
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
        attachGps: Boolean
    ) -> Unit,
    onDeleteZone: (Long) -> Unit,
    onNavigateToCheckIn: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val textMeasurer = rememberTextMeasurer()

    var filterCategory by remember { mutableStateOf<ZoneCategory?>(null) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var draftX by remember { mutableFloatStateOf(0.0f) }
    var draftY by remember { mutableFloatStateOf(0.0f) }
    var draftName by remember { mutableStateOf("") }
    var draftCategory by remember { mutableStateOf(ZoneCategory.GREEN) }
    var draftRadius by remember { mutableIntStateOf(250) }
    var draftAnchor by remember { mutableStateOf("") }
    var attachGpsToNewZone by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onToggleOptionalGps(granted)
        if (granted) {
            onCheckLocalGps()
        }
    }

    val filteredZones = remember(state.zones, filterCategory) {
        if (filterCategory == null) state.zones else state.zones.filter { it.category == filterCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BrujulaEditorialHeader(
                eyebrow = "Cartografía personal privada",
                title = "Mis zonas",
                subtitle = "Configura espacios verdes de apoyo y contextos amarillos o rojos. Entrar en ellos nunca infiere consumo: solo ofrece un check-in voluntario."
            )
        }

        // Mapa cartográfico interactivo 100% local (sin APIs externas ni envío de datos)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceElevated)
                    .border(0.9.dp, colors.borderStrong, RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrujulaStatusDot(color = colors.zoneGreen, label = "Verde")
                        BrujulaStatusDot(color = colors.zoneAmber, label = "Amarilla")
                        BrujulaStatusDot(color = colors.zoneCoral, label = "Roja")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${"%.1f".format(zoom)}×",
                            style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                            color = colors.inkMuted
                        )
                        IconButton(
                            onClick = {
                                zoom = 1f
                                panOffset = Offset.Zero
                                haptics.tick()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CenterFocusStrong,
                                contentDescription = "Centrar mapa",
                                tint = colors.inkSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(248.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (colors.isDark) Color(0xFF0C1B24) else Color(0xFFF3EEE4))
                        .border(0.8.dp, colors.borderHairline, RoundedCornerShape(14.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    zoom = (zoom * gestureZoom).coerceIn(0.75f, 2.6f)
                                    val maxPan = size.width * 0.45f
                                    panOffset = Offset(
                                        x = (panOffset.x + pan.x).coerceIn(-maxPan, maxPan),
                                        y = (panOffset.y + pan.y).coerceIn(-maxPan, maxPan)
                                    )
                                }
                            }
                            .pointerInput(filteredZones, zoom, panOffset) {
                                detectTapGestures { tap ->
                                    val center = Offset(
                                        size.width / 2f + panOffset.x,
                                        size.height / 2f + panOffset.y
                                    )
                                    val scaleR = (minOf(size.width, size.height).toFloat() * 0.44f) * zoom

                                    // Comprobar si ha tocado alguna zona existente
                                    val tappedZone = filteredZones.firstOrNull { zone ->
                                        val zx = center.x + zone.relativeX * scaleR
                                        val zy = center.y + zone.relativeY * scaleR
                                        hypot(tap.x - zx, tap.y - zy) <= 28.dp.toPx()
                                    }

                                    if (tappedZone != null) {
                                        haptics.confirm()
                                        onSelectManualZone(tappedZone)
                                    } else {
                                        // Crear nueva zona en el punto tocado del mapa
                                        haptics.click()
                                        draftX = ((tap.x - center.x) / scaleR).coerceIn(-0.9f, 0.9f)
                                        draftY = ((tap.y - center.y) / scaleR).coerceIn(-0.9f, 0.9f)
                                        draftName = ""
                                        draftAnchor = ""
                                        showCreateDialog = true
                                    }
                                }
                            }
                    ) {
                        val origin = Offset(
                            size.width / 2f + panOffset.x,
                            size.height / 2f + panOffset.y
                        )
                        val maxRadius = (size.minDimension * 0.44f) * zoom
                        val gridColor = if (colors.isDark) {
                            Color(0xFF213C4B)
                        } else {
                            Color(0xFFDCD3C2)
                        }

                        // Retícula cartográfica suave
                        val gridStep = 34.dp.toPx() * zoom.coerceIn(0.8f, 1.5f)
                        var gx = origin.x % gridStep
                        while (gx < size.width) {
                            drawLine(
                                color = gridColor.copy(alpha = 0.45f),
                                start = Offset(gx, 0f),
                                end = Offset(gx, size.height),
                                strokeWidth = 0.7.dp.toPx()
                            )
                            gx += gridStep
                        }
                        var gy = origin.y % gridStep
                        while (gy < size.height) {
                            drawLine(
                                color = gridColor.copy(alpha = 0.45f),
                                start = Offset(0f, gy),
                                end = Offset(size.width, gy),
                                strokeWidth = 0.7.dp.toPx()
                            )
                            gy += gridStep
                        }

                        // Anillos concéntricos de distancia (500 m, 1 km, 1.5 km)
                        listOf(0.33f to "500 m", 0.66f to "1 km", 1.0f to "1,5 km").forEach { (ratio, label) ->
                            val r = maxRadius * ratio
                            drawCircle(
                                color = gridColor,
                                radius = r,
                                center = origin,
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                )
                            )
                            val labelStyle = TextStyle(
                                fontFamily = CleanSans,
                                fontSize = 9.5.sp,
                                color = colors.inkMuted
                            )
                            val measured = textMeasurer.measure(label, labelStyle)
                            drawText(
                                textLayoutResult = measured,
                                topLeft = Offset(origin.x + 4.dp.toPx(), origin.y - r + 2.dp.toPx())
                            )
                        }

                        // Ejes cardinales
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, origin.y),
                            end = Offset(size.width, origin.y),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = gridColor,
                            start = Offset(origin.x, 0f),
                            end = Offset(origin.x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Geocercas personales
                        filteredZones.forEach { zone ->
                            val zoneColor = when (zone.category) {
                                ZoneCategory.GREEN -> colors.zoneGreen
                                ZoneCategory.YELLOW -> colors.zoneAmber
                                ZoneCategory.RED -> colors.zoneCoral
                            }
                            val center = Offset(
                                origin.x + zone.relativeX * maxRadius,
                                origin.y + zone.relativeY * maxRadius
                            )
                            val visualRadius = ((zone.radiusMeters / 1500f) * maxRadius)
                                .coerceIn(18.dp.toPx(), 64.dp.toPx())
                            val isSelected = state.activeZone?.id == zone.id

                            drawCircle(
                                color = zoneColor.copy(alpha = if (isSelected) 0.30f else 0.17f),
                                radius = visualRadius,
                                center = center
                            )
                            drawCircle(
                                color = zoneColor,
                                radius = visualRadius,
                                center = center,
                                style = Stroke(width = if (isSelected) 2.2.dp.toPx() else 1.2.dp.toPx())
                            )
                            drawCircle(
                                color = zoneColor,
                                radius = 5.dp.toPx(),
                                center = center
                            )

                            val nameStyle = TextStyle(
                                fontFamily = CleanSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.5.sp,
                                color = colors.inkPrimary
                            )
                            val shortName = if (zone.name.length > 18) zone.name.take(17) + "…" else zone.name
                            val measured = textMeasurer.measure(shortName, nameStyle)
                            drawText(
                                textLayoutResult = measured,
                                topLeft = Offset(
                                    center.x - measured.size.width / 2f,
                                    center.y + 7.dp.toPx()
                                )
                            )
                        }

                        // Punto central de referencia personal
                        drawCircle(
                            color = colors.petroleumAccent,
                            radius = 4.5.dp.toPx(),
                            center = origin
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toca cualquier zona del mapa para simular/indicar que estás allí, o toca un espacio libre para crear una nueva zona personal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted
                )
            }
        }

        // Controles de Geolocalización opcional en dispositivo
        item {
            BrujulaSurface {
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
                            imageVector = Icons.Outlined.GpsFixed,
                            contentDescription = null,
                            tint = colors.jadePrimary
                        )
                        Column(modifier = Modifier.padding(start = 10.dp, end = 8.dp)) {
                            Text(
                                text = "Geolocalización opcional en el teléfono",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.inkPrimary
                            )
                            Text(
                                text = "Sin servidor externo. Puedes revocar el permiso en cualquier momento y usar BRÚJULA sin GPS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted
                            )
                        }
                    }
                    Switch(
                        checked = state.settings.optionalGpsEnabled,
                        onCheckedChange = { wantEnable ->
                            if (wantEnable) {
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
                    BrujulaGhostButton(
                        text = "Comprobar ubicación local ahora",
                        icon = Icons.Outlined.MyLocation,
                        onClick = onCheckLocalGps,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!state.gpsStatusMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = colors.jadePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.gpsStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                    }
                }
            }
        }

        // Filtros y botón añadir zona
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    BrujulaTagPill(
                        label = "Todas (${state.zones.size})",
                        selected = filterCategory == null,
                        onClick = { filterCategory = null }
                    )
                    BrujulaTagPill(
                        label = "Verdes",
                        selected = filterCategory == ZoneCategory.GREEN,
                        accentColor = colors.zoneGreen,
                        washColor = colors.zoneGreenWash,
                        onClick = { filterCategory = ZoneCategory.GREEN }
                    )
                    BrujulaTagPill(
                        label = "Amarillas",
                        selected = filterCategory == ZoneCategory.YELLOW,
                        accentColor = colors.zoneAmber,
                        washColor = colors.zoneAmberWash,
                        onClick = { filterCategory = ZoneCategory.YELLOW }
                    )
                    BrujulaTagPill(
                        label = "Rojas",
                        selected = filterCategory == ZoneCategory.RED,
                        accentColor = colors.zoneCoral,
                        washColor = colors.zoneCoralWash,
                        onClick = { filterCategory = ZoneCategory.RED }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                BrujulaPrimaryButton(
                    text = "Nueva",
                    icon = Icons.Outlined.AddLocationAlt,
                    onClick = {
                        draftX = 0.08f
                        draftY = -0.12f
                        draftName = ""
                        draftAnchor = ""
                        showCreateDialog = true
                    }
                )
            }
        }

        // Lista de zonas configuradas
        items(filteredZones, key = { it.id }) { zone ->
            val isActive = state.activeZone?.id == zone.id
            val accent = when (zone.category) {
                ZoneCategory.GREEN -> colors.zoneGreen
                ZoneCategory.YELLOW -> colors.zoneAmber
                ZoneCategory.RED -> colors.zoneCoral
            }
            val wash = when (zone.category) {
                ZoneCategory.GREEN -> colors.zoneGreenWash
                ZoneCategory.YELLOW -> colors.zoneAmberWash
                ZoneCategory.RED -> colors.zoneCoralWash
            }

            BrujulaSurface(
                containerColor = if (isActive) wash else colors.surfaceElevated,
                borderColor = if (isActive) accent else colors.borderHairline,
                onClick = { onSelectManualZone(if (isActive) null else zone) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BrujulaStatusDot(color = accent, label = zone.category.title)
                            Text(
                                text = "· ${zone.radiusMeters} m",
                                style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                                color = colors.inkMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                        if (zone.anchorReminder.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = zone.anchorReminder,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = { onDeleteZone(zone.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Eliminar zona",
                            tint = colors.inkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (isActive && (zone.category == ZoneCategory.YELLOW || zone.category == ZoneCategory.RED)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BrujulaPrimaryButton(
                            text = "Comprobar cómo me siento (Check-in)",
                            onClick = onNavigateToCheckIn,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onSelectManualZone(null) }) {
                            Text("Salir de zona")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Configurar zona personal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Nombre identificativo para ti") },
                        placeholder = { Text("Ej. Gimnasio, plaza, casa de amigo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Tipo de zona:",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ZoneCategory.entries.forEach { cat ->
                            BrujulaTagPill(
                                label = cat.shortBadge,
                                selected = draftCategory == cat,
                                onClick = { draftCategory = cat }
                            )
                        }
                    }

                    Text(
                        text = "Radio aproximado: $draftRadius m",
                        style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                        color = colors.inkSecondary
                    )
                    Slider(
                        value = draftRadius.toFloat(),
                        onValueChange = { draftRadius = it.toInt() },
                        valueRange = 100f..800f
                    )

                    OutlinedTextField(
                        value = draftAnchor,
                        onValueChange = { draftAnchor = it },
                        label = { Text("Recordatorio o alternativa al estar aquí") },
                        placeholder = { Text("Ej. Llamar antes de subir o dar un paseo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.settings.optionalGpsEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vincular mi ubicación GPS actual (local)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Switch(
                                checked = attachGpsToNewZone,
                                onCheckedChange = { attachGpsToNewZone = it }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveZone(
                            0L,
                            draftName,
                            draftCategory,
                            draftX,
                            draftY,
                            draftRadius,
                            draftAnchor,
                            attachGpsToNewZone
                        )
                        showCreateDialog = false
                    }
                ) {
                    Text("Guardar zona")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
