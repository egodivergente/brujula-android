package es.brujula.jitai.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Rule
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrecisionScale
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSectionTitle
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.TabularNumberStyle
import es.brujula.jitai.core.model.ThemePreference
import es.brujula.jitai.ui.BrujulaUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
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
    val haptics = BrujulaDesign.haptics
    val context = LocalContext.current
    val settings = state.settings
    val now = System.currentTimeMillis()
    val isPaused4h = now < settings.pausedUntilMillis

    var draftCraving by remember(settings.cravingThreshold) {
        mutableIntStateOf(settings.cravingThreshold)
    }
    var draftControl by remember(settings.controlThreshold) {
        mutableIntStateOf(settings.controlThreshold)
    }
    var draftMaxDaily by remember(settings.maxDailyNotifications) {
        mutableIntStateOf(settings.maxDailyNotifications)
    }
    var draftQuietStart by remember(settings.quietHoursStartHour) {
        mutableIntStateOf(settings.quietHoursStartHour)
    }
    var draftQuietEnd by remember(settings.quietHoursEndHour) {
        mutableIntStateOf(settings.quietHoursEndHour)
    }

    var showRuleHistory by remember { mutableStateOf(false) }
    var showAuditTrace by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var copiedFeedback by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onToggleNotifications(granted)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val anyGranted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onToggleOptionalGps(anyGranted)
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("dd MMM HH:mm") }

    val rulesChanged = draftCraving != settings.cravingThreshold ||
        draftControl != settings.controlThreshold ||
        draftMaxDaily != settings.maxDailyNotifications ||
        draftQuietStart != settings.quietHoursStartHour ||
        draftQuietEnd != settings.quietHoursEndHour

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BrujulaEditorialHeader(
                eyebrow = "Soberanía, permisos y umbrales · Parte 2 §2.3–2.5",
                title = "Configuración",
                subtitle = "Privacidad desde el diseño: todo se ejecuta en tu dispositivo, sin cuentas ni servidores externos."
            )
        }

        // 1. Apariencia y Accesibilidad (Modo Claro / Oscuro / Sistema + Tipografía + Háptica)
        item {
            BrujulaSurface {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = colors.jadePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apariencia y accesibilidad",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Paleta visual",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemePreference.entries.forEach { pref ->
                        BrujulaTagPill(
                            label = pref.label,
                            selected = settings.themePreference == pref,
                            onClick = { onSelectTheme(pref) }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.borderHairline
                )

                SettingToggleRow(
                    title = "Tipografía ampliada (+12%)",
                    subtitle = "Mayor cuerpo de lectura manteniendo jerarquía editorial en pantalla.",
                    checked = settings.largeTextEnabled,
                    onCheckedChange = onToggleLargeText
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingToggleRow(
                    title = "Respuesta háptica sutil",
                    subtitle = "Microvibración discreta al girar la brújula, deslizar escalas o respirar.",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onToggleHaptics
                )
            }
        }

        // 2. Privacidad, Modo Discreto y Permisos Revocables
        item {
            BrujulaSurface {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PrivacyTip,
                        contentDescription = null,
                        tint = colors.jadePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacidad y permisos revocables",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingToggleRow(
                    icon = Icons.Outlined.VisibilityOff,
                    title = "Modo discreto reforzado",
                    subtitle = "Sustituye textos sensibles en cabecera y avisos por vocabulario completamente neutro.",
                    checked = settings.discreetModeEnabled,
                    onCheckedChange = onToggleDiscreetMode
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.borderHairline
                )

                SettingToggleRow(
                    icon = Icons.Outlined.LocationOn,
                    title = "Geolocalización local opcional",
                    subtitle = "Compara tu ubicación únicamente dentro del teléfono. Desactivar no limita el mapa manual.",
                    checked = settings.optionalGpsEnabled,
                    onCheckedChange = { enable ->
                        if (enable) {
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
        }

        // 3. Notificaciones, Tope Diario y Pausa de 4 Horas
        item {
            BrujulaSurface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = colors.jadePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Avisos discretos y fatiga cero",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                    }
                    BrujulaTagPill(
                        label = "Hoy ${settings.notificationsSentToday}/${settings.maxDailyNotifications}",
                        selected = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingToggleRow(
                    title = "Permitir avisos adaptativos neutros",
                    subtitle = "Nunca muestran sustancias, sexualidad ni nombres de lugares en pantalla de bloqueo.",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enable ->
                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onToggleNotifications(enable)
                        }
                    }
                )

                if (isPaused4h) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.amberWash,
                        border = BorderStroke(0.8.dp, colors.amberAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pausa automática de 4 h activa tras rechazo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onAmberWash,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onResumeFourHourPause) {
                                Text("Reanudar", color = colors.onAmberWash)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BrujulaGhostButton(
                    text = "Enviar aviso neutro de prueba",
                    icon = Icons.Outlined.NotificationsNone,
                    onClick = onTestNeutralNotification,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. Motor JITAI · Umbrales Experimentales y Trazabilidad Explicable
        item {
            BrujulaSurface(
                borderColor = colors.borderStrong
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Rule,
                            contentDescription = null,
                            tint = colors.jadePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Motor JITAI explicable",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                    }
                    BrujulaTagPill(
                        label = "Regla v${settings.ruleVersion}",
                        selected = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceSubtle,
                    border = BorderStroke(0.8.dp, colors.borderHairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nota metodológica: estos valores son configuraciones experimentales pactadas, no criterios clínicos validados. El deseo sexual independiente jamás activa avisos por sí solo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                BrujulaPrecisionScale(
                    title = "Umbral experimental de craving",
                    subtitle = "Ofrecer apoyo breve o enfermería si ≥ valor (con disponibilidad y sin conducir)",
                    value = draftCraving,
                    onValueChange = { draftCraving = it },
                    minAnchorLabel = "5 / 10",
                    maxAnchorLabel = "9 / 10",
                    accentColor = colors.amberAccent,
                    valueRange = 5..9
                )

                Spacer(modifier = Modifier.height(14.dp))

                BrujulaPrecisionScale(
                    title = "Umbral experimental de control percibido",
                    subtitle = "Ofrecer apoyo breve o enfermería si ≤ valor",
                    value = draftControl,
                    onValueChange = { draftControl = it },
                    minAnchorLabel = "1 / 10",
                    maxAnchorLabel = "5 / 10",
                    accentColor = colors.jadePrimary,
                    valueRange = 1..5
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Máximo de notificaciones automáticas al día",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2).forEach { cap ->
                        BrujulaTagPill(
                            label = "$cap aviso${if (cap > 1) "s" else ""} / día",
                            selected = draftMaxDaily == cap,
                            onClick = { draftMaxDaily = cap }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Periodo de silencio nocturno",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple("23:00 – 08:00", 23, 8),
                        Triple("22:00 – 07:00", 22, 7),
                        Triple("00:00 – 09:00", 0, 9)
                    )
                    presets.forEach { (label, startH, endH) ->
                        BrujulaTagPill(
                            label = label,
                            selected = draftQuietStart == startH && draftQuietEnd == endH,
                            onClick = {
                                draftQuietStart = startH
                                draftQuietEnd = endH
                            }
                        )
                    }
                }

                if (rulesChanged) {
                    Spacer(modifier = Modifier.height(14.dp))
                    BrujulaPrimaryButton(
                        text = "Guardar y versionar reglas JITAI",
                        onClick = {
                            onSaveExperimentalRules(
                                draftCraving,
                                draftControl,
                                draftMaxDaily,
                                draftQuietStart,
                                draftQuietEnd
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.borderHairline
                )

                // Historial de versiones de reglas y trazabilidad reciente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showRuleHistory) "Ocultar historial de versiones" else "Ver historial de reglas (${settings.ruleChangeHistory.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.jadePrimary,
                        modifier = Modifier.clickable {
                            haptics.click()
                            showRuleHistory = !showRuleHistory
                        }
                    )
                    Text(
                        text = if (showAuditTrace) "Ocultar trazas" else "Trazas (${state.jitaiDecisions.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.inkSecondary,
                        modifier = Modifier.clickable {
                            haptics.click()
                            showAuditTrace = !showAuditTrace
                        }
                    )
                }

                AnimatedVisibility(visible = showRuleHistory) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        settings.ruleChangeHistory.forEach { entry ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = colors.inkMuted,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = entry,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkSecondary
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showAuditTrace) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.jitaiDecisions.take(5).forEach { decision ->
                            val stamp = remember(decision.timestampMillis) {
                                Instant.ofEpochMilli(decision.timestampMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .format(timeFormatter)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.surfaceSubtle,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "$stamp · ${decision.triggerSource.label} (${decision.userResponse})",
                                        style = MaterialTheme.typography.labelSmall.merge(TabularNumberStyle),
                                        color = colors.jadePrimary
                                    )
                                    Text(
                                        text = decision.headline,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.inkPrimary
                                    )
                                    Text(
                                        text = decision.ruleTrace,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.inkMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Resumen Clínico Pactado (sin coordenadas) y Borrado Local Completo
        item {
            BrujulaSurface {
                BrujulaSectionTitle(
                    title = "Datos locales y resumen pactado",
                    meta = "Sin nube"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Puedes generar un resumen agregado para revisar en consulta con tu enfermera navegadora (omite deliberadamente coordenadas y notas sensibles de ubicación) o eliminar todos los datos del teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                BrujulaPrimaryButton(
                    text = "Generar resumen pactado (JSON)",
                    icon = Icons.Outlined.IosShare,
                    onClick = {
                        copiedFeedback = false
                        onGenerateSummaryExport()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                BrujulaGhostButton(
                    text = "Borrar todos los registros locales",
                    icon = Icons.Outlined.DeleteForever,
                    accentColor = colors.coralAccent,
                    onClick = { showWipeConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Diálogo de exportación de resumen clínico pactado
    if (!state.exportedSummaryJson.isNullOrBlank()) {
        val jsonText = state.exportedSummaryJson
        AlertDialog(
            onDismissRequest = onDismissSummaryExport,
            title = {
                Text(
                    text = "Resumen clínico pactado",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Incluye únicamente métricas agregadas, versión de reglas e hitos. Las coordenadas de tus zonas nunca se incluyen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surfaceSubtle,
                        border = BorderStroke(0.8.dp, colors.borderHairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = jsonText.take(780) + if (jsonText.length > 780) "\n…" else "",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = colors.inkPrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    if (copiedFeedback) {
                        Text(
                            text = "Copiado al portapapeles.",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.jadePrimary
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrujulaGhostButton(
                        text = "Copiar",
                        icon = Icons.Outlined.ContentCopy,
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Resumen BRÚJULA", jsonText))
                            copiedFeedback = true
                        }
                    )
                    BrujulaPrimaryButton(
                        text = "Compartir",
                        icon = Icons.Outlined.IosShare,
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_SUBJECT, "Resumen pactado BRÚJULA")
                                putExtra(Intent.EXTRA_TEXT, jsonText)
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Compartir resumen pactado")
                            )
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissSummaryExport) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Confirmación de borrado irreversible local
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = {
                Text(
                    text = "Eliminar todos los datos locales",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Se borrarán de este dispositivo tus check-ins EMA, zonas personales, planificación diaria, registros voluntarios de GHB/GBL, hitos y preferencias personalizadas. Como no existe copia en servidor, esta acción es definitiva.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary
                )
            },
            confirmButton = {
                BrujulaPrimaryButton(
                    text = "Sí, borrar todo",
                    containerColor = colors.coralAccent,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    onClick = {
                        showWipeConfirmDialog = false
                        onWipeAllLocalData()
                    }
                )
            },
            dismissButton = {
                BrujulaGhostButton(
                    text = "Cancelar",
                    onClick = { showWipeConfirmDialog = false }
                )
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.inkSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                haptics.tick()
                onCheckedChange(it)
            }
        )
    }
}
