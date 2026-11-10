package es.brujula.jitai.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.brujula.jitai.core.data.CuratedClinicalContent
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.BrujulaEditorialHeader
import es.brujula.jitai.core.designsystem.BrujulaGhostButton
import es.brujula.jitai.core.designsystem.BrujulaPrimaryButton
import es.brujula.jitai.core.designsystem.BrujulaSurface
import es.brujula.jitai.core.designsystem.BrujulaTagPill
import es.brujula.jitai.core.designsystem.TabularNumberStyle
import es.brujula.jitai.core.model.NurseNavigatorProfile
import es.brujula.jitai.ui.BrujulaUiState
import es.brujula.jitai.ui.components.GhbVoluntaryTrackerCard
import kotlinx.coroutines.delay

private enum class SupportSection(val label: String) {
    BREATHING("Respirar"),
    COPING("Afrontamiento"),
    GUIDE("Guía IA revisada"),
    NURSE("Enfermería y GHB")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportScreen(
    state: BrujulaUiState,
    initialSectionKey: String?,
    onCompletePractice: (toolId: String, title: String, seconds: Int) -> Unit,
    onSaveNurseProfile: (NurseNavigatorProfile) -> Unit,
    onLogGhb: (String) -> Unit,
    onDeleteGhb: (Long) -> Unit,
    onClearGhb: () -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val context = LocalContext.current

    var activeTab by remember(initialSectionKey) {
        mutableStateOf(
            when (initialSectionKey) {
                "breathing" -> SupportSection.BREATHING
                "coping" -> SupportSection.COPING
                "guide" -> SupportSection.GUIDE
                "nurse" -> SupportSection.NURSE
                else -> SupportSection.BREATHING
            }
        )
    }

    // Estado del estudio de respiración guiada
    val patterns = CuratedClinicalContent.breathingPatterns
    var selectedPattern by remember { mutableStateOf(patterns.first()) }
    var isBreathingActive by remember { mutableStateOf(false) }
    var phaseLabel by remember { mutableStateOf("Pulsa iniciar cuando estés cómodo") }
    var phaseTargetScale by remember { mutableStateOf(0.58f) }
    var phaseDurationMs by remember { mutableIntStateOf(1000) }
    var completedCycles by remember { mutableIntStateOf(0) }
    var elapsedBreathingSec by remember { mutableIntStateOf(0) }

    LaunchedEffect(isBreathingActive, selectedPattern) {
        if (!isBreathingActive) {
            phaseLabel = "Pulsa iniciar cuando estés cómodo"
            phaseTargetScale = 0.58f
            phaseDurationMs = 800
            return@LaunchedEffect
        }
        completedCycles = 0
        elapsedBreathingSec = 0
        while (isBreathingActive && completedCycles < selectedPattern.recommendedCycles) {
            // Inhala
            phaseLabel = "Inhala suave por la nariz (${selectedPattern.inhaleSec} s)"
            phaseDurationMs = selectedPattern.inhaleSec * 1000
            phaseTargetScale = 0.96f
            haptics.gentlePulse()
            repeat(selectedPattern.inhaleSec) {
                delay(1000L)
                elapsedBreathingSec += 1
            }
            if (!isBreathingActive) break

            // Retención post-inhalación
            if (selectedPattern.holdAfterInhaleSec > 0) {
                phaseLabel = "Sostén sin tensión (${selectedPattern.holdAfterInhaleSec} s)"
                phaseDurationMs = 400
                phaseTargetScale = 0.98f
                haptics.tick()
                repeat(selectedPattern.holdAfterInhaleSec) {
                    delay(1000L)
                    elapsedBreathingSec += 1
                }
            }
            if (!isBreathingActive) break

            // Exhala
            phaseLabel = "Exhala largo soltando hombros (${selectedPattern.exhaleSec} s)"
            phaseDurationMs = selectedPattern.exhaleSec * 1000
            phaseTargetScale = 0.50f
            haptics.gentlePulse()
            repeat(selectedPattern.exhaleSec) {
                delay(1000L)
                elapsedBreathingSec += 1
            }
            if (!isBreathingActive) break

            // Pausa post-exhalación
            if (selectedPattern.holdAfterExhaleSec > 0) {
                phaseLabel = "Reposa en calma (${selectedPattern.holdAfterExhaleSec} s)"
                phaseDurationMs = 400
                phaseTargetScale = 0.50f
                repeat(selectedPattern.holdAfterExhaleSec) {
                    delay(1000L)
                    elapsedBreathingSec += 1
                }
            }
            completedCycles += 1
        }
        if (isBreathingActive && completedCycles >= selectedPattern.recommendedCycles) {
            isBreathingActive = false
            haptics.confirm()
            onCompletePractice(selectedPattern.id, selectedPattern.title, elapsedBreathingSec)
            phaseLabel = "Serie completada y guardada en tu progreso privado"
        }
    }

    val orbScale by animateFloatAsState(
        targetValue = phaseTargetScale,
        animationSpec = tween(durationMillis = phaseDurationMs),
        label = "breathingOrbScale"
    )

    // Estado del asistente acotado de contenidos revisados
    var guidanceQuery by remember { mutableStateOf("") }
    val (urgentDetected, matchedTopics) = remember(guidanceQuery) {
        CuratedClinicalContent.queryReviewedGuidance(guidanceQuery)
    }

    // Edición del contacto enfermero
    var showEditNurseDialog by remember { mutableStateOf(false) }
    var draftNurseName by remember(state.nurseProfile) { mutableStateOf(state.nurseProfile.nurseName) }
    var draftCenter by remember(state.nurseProfile) { mutableStateOf(state.nurseProfile.centerName) }
    var draftPhone by remember(state.nurseProfile) { mutableStateOf(state.nurseProfile.phone) }
    var draftSchedule by remember(state.nurseProfile) { mutableStateOf(state.nurseProfile.scheduleLabel) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BrujulaEditorialHeader(
                eyebrow = "Herramientas permanentes · Parte 2 §2.4 y §2.5",
                title = "Apoyo y cuidado",
                subtitle = "Ejercicios fisiológicos breves, recursos revisados de reducción de daños y contacto directo con tu enfermera navegadora."
            )
        }

        // Franja permanente de urgencia 112 (CAME D3→C3: acceso directo al 112 sin esperar al chat)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.coralWash)
                    .border(0.9.dp, colors.coralAccent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "URGENCIA SANITARIA · SIN VIGILANCIA 24 H",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onCoralWash
                    )
                    Text(
                        text = "Ante pérdida de conciencia, dolor torácico o crisis aguda, llama al 112 sin esperar respuesta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkPrimary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                BrujulaPrimaryButton(
                    text = "112",
                    icon = Icons.Outlined.Emergency,
                    containerColor = colors.coralAccent,
                    contentColor = Color.White,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                    }
                )
            }
        }

        // Selector de sección
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                SupportSection.entries.forEach { tab ->
                    val icon = when (tab) {
                        SupportSection.BREATHING -> Icons.Outlined.Air
                        SupportSection.COPING -> Icons.Outlined.SelfImprovement
                        SupportSection.GUIDE -> Icons.Outlined.PsychologyAlt
                        SupportSection.NURSE -> Icons.Outlined.MedicalServices
                    }
                    BrujulaTagPill(
                        label = tab.label,
                        icon = icon,
                        selected = activeTab == tab,
                        onClick = { activeTab = tab }
                    )
                }
            }
        }

        when (activeTab) {
            SupportSection.BREATHING -> {
                item {
                    BrujulaSurface {
                        Text(
                            text = "Selecciona cadencia respiratoria",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            patterns.forEach { pat ->
                                BrujulaTagPill(
                                    label = pat.title,
                                    selected = selectedPattern.id == pat.id,
                                    onClick = {
                                        isBreathingActive = false
                                        selectedPattern = pat
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedPattern.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Esfera respiratoria animada en Jade y Petróleo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(206.dp)
                        ) {
                            Canvas(modifier = Modifier.size(196.dp)) {
                                val maxR = size.minDimension / 2f
                                drawCircle(
                                    color = colors.jadePrimary.copy(alpha = 0.16f),
                                    radius = maxR * 0.95f,
                                    style = Stroke(width = 1.2.dp.toPx())
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            colors.jadeLuminous.copy(alpha = 0.85f),
                                            colors.petroleumAccent.copy(alpha = 0.85f)
                                        )
                                    ),
                                    radius = maxR * orbScale
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isBreathingActive) {
                                        "Ciclo ${completedCycles + 1}/${selectedPattern.recommendedCycles}"
                                    } else {
                                        "${selectedPattern.recommendedCycles} ciclos"
                                    },
                                    style = MaterialTheme.typography.labelLarge.merge(TabularNumberStyle),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = phaseLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BrujulaPrimaryButton(
                                text = if (isBreathingActive) "Detener pausa" else "Iniciar ejercicio",
                                icon = if (isBreathingActive) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                onClick = {
                                    if (isBreathingActive && elapsedBreathingSec >= 15) {
                                        onCompletePractice(
                                            selectedPattern.id,
                                            selectedPattern.title,
                                            elapsedBreathingSec
                                        )
                                    }
                                    isBreathingActive = !isBreathingActive
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            SupportSection.COPING -> {
                items(CuratedClinicalContent.copingExercises, key = { it.id }) { exercise ->
                    BrujulaSurface {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.jadePrimary
                            )
                            Text(
                                text = exercise.durationLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.inkMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exercise.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exercise.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        exercise.steps.forEachIndexed { idx, step ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${idx + 1}.",
                                    style = MaterialTheme.typography.labelLarge.merge(TabularNumberStyle),
                                    color = colors.jadePrimary,
                                    modifier = Modifier.width(20.dp)
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        BrujulaGhostButton(
                            text = "Registrar práctica en mi progreso",
                            icon = Icons.Outlined.CheckCircleOutline,
                            accentColor = colors.jadePrimary,
                            onClick = {
                                onCompletePractice(exercise.id, exercise.title, 180)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            SupportSection.GUIDE -> {
                item {
                    BrujulaSurface(
                        containerColor = colors.surfaceSubtle
                    ) {
                        Text(
                            text = "IDENTIFICACIÓN TRANSPARENTE · IA ACOTADA A CONTENIDOS REVISADOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.petroleumAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Este asistente automatizado responde únicamente con materiales revisados por profesionales de salud sexual y reducción de daños. No realiza diagnósticos, no calcula dosis ni modifica tus reglas clínicas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = guidanceQuery,
                            onValueChange = { guidanceQuery = it },
                            leadingIcon = {
                                Icon(Icons.Outlined.Search, contentDescription = null)
                            },
                            label = { Text("Consultar tema revisado") },
                            placeholder = { Text("Ej. PrEP, PEP, hidratación, sueño, límites, GHB...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (urgentDetected) {
                    item {
                        BrujulaSurface(
                            containerColor = colors.coralWash,
                            borderColor = colors.coralAccent
                        ) {
                            Text(
                                text = "Detectada consulta de posible riesgo agudo",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onCoralWash
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Si tú o alguien contigo tiene dificultad para respirar, no despierta o presenta dolor torácico intenso, no esperes a leer guías: llama inmediatamente al 112.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            BrujulaPrimaryButton(
                                text = "Llamar ahora al 112",
                                icon = Icons.Outlined.Emergency,
                                containerColor = colors.coralAccent,
                                contentColor = Color.White,
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                items(matchedTopics, key = { it.id }) { topic ->
                    BrujulaSurface {
                        Text(
                            text = topic.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.jadePrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = topic.reviewedResponse,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = colors.borderHairline)
                        Spacer(modifier = Modifier.height(8.dp))
                        topic.keyTakeaways.forEach { bullet ->
                            Text(
                                text = "• $bullet",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkPrimary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            SupportSection.NURSE -> {
                item {
                    BrujulaSurface {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "REFERENTE CLÍNICO CONFIGURABLE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.jadePrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.nurseProfile.nurseName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = state.nurseProfile.centerName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.inkSecondary
                                )
                            }
                            TextButton(onClick = { showEditNurseDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Configurar")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = colors.borderHairline)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Horario acordado: ${state.nurseProfile.scheduleLabel}",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = state.nurseProfile.responseSlaLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Coordinación: adicciones, salud mental, salud sexual (PrEP/ITS) y entidades comunitarias.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BrujulaPrimaryButton(
                                text = "Llamar a enfermería",
                                icon = Icons.Outlined.Call,
                                onClick = {
                                    val cleanPhone = state.nurseProfile.phone.filter { it.isDigit() || it == '+' }
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    GhbVoluntaryTrackerCard(
                        logs = state.ghbLogs,
                        discreetMode = state.settings.discreetModeEnabled,
                        onLogNewEntry = onLogGhb,
                        onDeleteEntry = onDeleteGhb,
                        onClearAll = onClearGhb
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }
    }

    if (showEditNurseDialog) {
        AlertDialog(
            onDismissRequest = { showEditNurseDialog = false },
            title = { Text("Contacto de enfermería navegadora") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftNurseName,
                        onValueChange = { draftNurseName = it },
                        label = { Text("Profesional de referencia") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftCenter,
                        onValueChange = { draftCenter = it },
                        label = { Text("Centro o unidad asistencial") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftPhone,
                        onValueChange = { draftPhone = it },
                        label = { Text("Teléfono de contacto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftSchedule,
                        onValueChange = { draftSchedule = it },
                        label = { Text("Horario de atención acordado") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveNurseProfile(
                            state.nurseProfile.copy(
                                nurseName = draftNurseName.trim().ifBlank { state.nurseProfile.nurseName },
                                centerName = draftCenter.trim().ifBlank { state.nurseProfile.centerName },
                                phone = draftPhone.trim().ifBlank { state.nurseProfile.phone },
                                scheduleLabel = draftSchedule.trim().ifBlank { state.nurseProfile.scheduleLabel }
                            )
                        )
                        showEditNurseDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNurseDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
