package es.brujula.jitai.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.model.ThemePreference
import es.brujula.jitai.ui.BrujulaUiState
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    state: BrujulaUiState,
    onSaveExperimentalRules: (
        cravingThreshold: Int,
        controlThreshold: Int,
        maxDailyNotifications: Int,
        quietStartHour: Int,
        quietEndHour: Int
    ) -> Unit,
    onToggleDiscreetMode: (Boolean) -> Unit,
    onToggleOptionalGps: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    onSelectTheme: (ThemePreference) -> Unit,
    onTestNeutralNotification: () -> Unit,
    onResumeFourHourPause: () -> Unit,
    onGenerateSummaryExport: () -> Unit,
    onDismissSummaryExport: () -> Unit,
    onWipeAllLocalData: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val context = LocalContext.current

    var expandSensitivity by rememberSaveable { mutableStateOf(false) }
    var confirmWipeDialog by rememberSaveable { mutableStateOf(false) }

    var cravingThreshold by rememberSaveable(state.settings.cravingThreshold) {
        mutableIntStateOf(state.settings.cravingThreshold)
    }
    var controlThreshold by rememberSaveable(state.settings.controlThreshold) {
        mutableIntStateOf(state.settings.controlThreshold)
    }
    var maxDaily by rememberSaveable(state.settings.maxDailyNotifications) {
        mutableIntStateOf(state.settings.maxDailyNotifications)
    }
    var quietStart by rememberSaveable(state.settings.quietHoursStartHour) {
        mutableIntStateOf(state.settings.quietHoursStartHour)
    }
    var quietEnd by rememberSaveable(state.settings.quietHoursEndHour) {
        mutableIntStateOf(state.settings.quietHoursEndHour)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onToggleNotifications(granted)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onToggleOptionalGps(granted)
    }

    val isPaused = state.settings.pausedUntilMillis > System.currentTimeMillis()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BrujulaEditorialHeader(
            title = "Ajustes",
            subtitle = "Todo permanece únicamente en tu teléfono."
        )

        // 1. Aspecto y comodidad visual
        BrujulaSurface(contentPadding = PaddingValues(20.dp)) {
            Text(
                text = "Aspecto",
                style = MaterialTheme.typography.titleMedium,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePreference.entries.forEach { pref ->
                    BrujulaTagPill(
                        label = pref.label,
                        selected = state.settings.themePreference == pref,
                        onClick = { onSelectTheme(pref) }
                    )
                }
            }

            QuietSettingRow(
                title = "Modo discreto",
                subtitle = "Textos neutros en pantalla y avisos",
                checked = state.settings.discreetModeEnabled,
                onCheckedChange = onToggleDiscreetMode
            )

            QuietSettingRow(
                title = "Respuesta háptica sutil",
                subtitle = "Vibración suave al deslizar o respirar",
                checked = state.settings.hapticsEnabled,
                onCheckedChange = onToggleHaptics
            )

            QuietSettingRow(
                title = "Tipografía más amplia",
                subtitle = "Mayor cuerpo de lectura",
                checked = state.settings.largeTextEnabled,
                onCheckedChange = onToggleLargeText,
                showTopDivider = true
            )
        }

        // 2. Privacidad y permisos opcionales
        BrujulaSurface(contentPadding = PaddingValues(20.dp)) {
            Text(
                text = "Avisos y privacidad",
                style = MaterialTheme.typography.titleMedium,
                color = colors.inkPrimary
            )

            QuietSettingRow(
                title = "Avisos suaves",
                subtitle = "Máximo ${state.settings.maxDailyNotifications} al día, sin insistencia",
                checked = state.settings.notificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onToggleNotifications(enabled)
                    }
                },
                showTopDivider = false
            )

            QuietSettingRow(
                title = "Ubicación local opcional",
                subtitle = "Solo para tus espacios configurados",
                checked = state.settings.optionalGpsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BrujulaTagPill(
                    label = "Probar aviso discreto",
                    icon = Icons.Outlined.NotificationsNone,
                    onClick = onTestNeutralNotification
                )
                if (isPaused) {
                    BrujulaTagPill(
                        label = "Quitar pausa de 4 h",
                        selected = true,
                        onClick = onResumeFourHourPause
                    )
                }
            }
        }

        // 3. Sensibilidad del motor JITAI (plegada por defecto para evitar ruido)
        BrujulaSurface(
            contentPadding = PaddingValues(20.dp),
            onClick = { expandSensitivity = !expandSensitivity }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sensibilidad del acompañamiento",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                    Text(
                        text = "Umbrales experimentales y horas de silencio",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted
                    )
                }
                Icon(
                    imageVector = if (expandSensitivity) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = colors.inkMuted
                )
            }

            AnimatedVisibility(visible = expandSensitivity) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Estos valores son configuraciones personales y experimentales, no criterios diagnósticos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted
                    )

                    Column {
                        Text(
                            text = "Ofrecer apoyo a partir de impulso: $cravingThreshold / 10",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkPrimary
                        )
                        Slider(
                            value = cravingThreshold.toFloat(),
                            onValueChange = { cravingThreshold = it.roundToInt().coerceIn(5, 9) },
                            valueRange = 5f..9f,
                            steps = 3
                        )
                    }

                    Column {
                        Text(
                            text = "Ofrecer apoyo si el margen baja a: $controlThreshold / 10",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkPrimary
                        )
                        Slider(
                            value = controlThreshold.toFloat(),
                            onValueChange = { controlThreshold = it.roundToInt().coerceIn(1, 5) },
                            valueRange = 1f..5f,
                            steps = 3
                        )
                    }

                    Column {
                        Text(
                            text = "Tope diario de avisos: $maxDaily",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkPrimary
                        )
                        Slider(
                            value = maxDaily.toFloat(),
                            onValueChange = { maxDaily = it.roundToInt().coerceIn(0, 2) },
                            valueRange = 0f..2f,
                            steps = 1
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Silencio desde ${"%02d:00".format(quietStart)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Slider(
                                value = quietStart.toFloat(),
                                onValueChange = { quietStart = it.roundToInt().coerceIn(0, 23) },
                                valueRange = 0f..23f
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hasta ${"%02d:00".format(quietEnd)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkSecondary
                            )
                            Slider(
                                value = quietEnd.toFloat(),
                                onValueChange = { quietEnd = it.roundToInt().coerceIn(0, 23) },
                                valueRange = 0f..23f
                            )
                        }
                    }

                    BrujulaPrimaryButton(
                        text = "Guardar sensibilidad",
                        onClick = {
                            onSaveExperimentalRules(
                                cravingThreshold,
                                controlThreshold,
                                maxDaily,
                                quietStart,
                                quietEnd
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. Datos locales y borrado completo
        BrujulaSurface(contentPadding = PaddingValues(20.dp)) {
            Text(
                text = "Tus datos",
                style = MaterialTheme.typography.titleMedium,
                color = colors.inkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sin cuentas ni nube. Puedes generar un resumen si deseas compartirlo o vaciar la aplicación en cualquier momento.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted
            )
            Spacer(modifier = Modifier.height(14.dp))

            BrujulaGhostButton(
                text = "Generar resumen para compartir",
                icon = Icons.Outlined.Share,
                onClick = onGenerateSummaryExport,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            BrujulaGhostButton(
                text = "Borrar todos mis datos",
                icon = Icons.Outlined.DeleteOutline,
                accentColor = colors.coralAccent,
                onClick = { confirmWipeDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    if (!state.exportedSummaryJson.isNullOrBlank()) {
        val json = state.exportedSummaryJson
        AlertDialog(
            onDismissRequest = onDismissSummaryExport,
            title = { Text("Resumen personal") },
            text = {
                Text(
                    text = json,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(share, "Compartir resumen"))
                    }
                ) {
                    Text("Compartir")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissSummaryExport) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (confirmWipeDialog) {
        AlertDialog(
            onDismissRequest = { confirmWipeDialog = false },
            title = { Text("¿Borrar todos los datos?") },
            text = {
                Text(
                    text = "Se eliminarán tus registros, espacios, plan del día, reflexiones y preferencias guardadas en este teléfono.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onWipeAllLocalData()
                        confirmWipeDialog = false
                    }
                ) {
                    Text("Borrar todo", color = colors.coralAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmWipeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun QuietSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showTopDivider: Boolean = true
) {
    val colors = BrujulaDesign.colors
    if (showTopDivider) {
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = colors.borderHairline.copy(alpha = 0.45f), thickness = 0.6.dp)
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.inkPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
