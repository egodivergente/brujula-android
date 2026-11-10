package es.brujula.jitai.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import es.brujula.jitai.core.designsystem.EditorialSerif
import es.brujula.jitai.core.model.BreathingPattern
import es.brujula.jitai.core.model.NurseNavigatorProfile
import es.brujula.jitai.ui.BrujulaUiState
import kotlinx.coroutines.delay

private enum class CalmSection(val key: String, val label: String) {
    BREATHING("breathing", "Respirar"),
    COPING("coping", "Soltar"),
    GUIDE("guide", "Guía"),
    NURSE("nurse", "Enfermería")
}

@Composable
fun SupportScreen(
    state: BrujulaUiState,
    initialSectionKey: String?,
    onCompletePractice: (toolId: String, toolTitle: String, durationSeconds: Int) -> Unit,
    onSaveNurseProfile: (NurseNavigatorProfile) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable(initialSectionKey) {
        val mapped = CalmSection.entries.firstOrNull { it.key == initialSectionKey }
            ?: CalmSection.BREATHING
        mutableStateOf(mapped)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BrujulaEditorialHeader(
            title = "Calma y apoyo",
            subtitle = "Un espacio tranquilo, siempre disponible.",
            trailingActionLabel = "112",
            onTrailingAction = {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
            }
        )

        // Navegación suave por pestañas en píldora
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalmSection.entries.forEach { tab ->
                BrujulaTagPill(
                    label = tab.label,
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }

        when (selectedTab) {
            CalmSection.BREATHING -> BreathingSanctuaryPane(
                onCompletePractice = onCompletePractice
            )

            CalmSection.COPING -> CopingProgressivePane(
                onCompletePractice = onCompletePractice
            )

            CalmSection.GUIDE -> CuratedGuidePane()

            CalmSection.NURSE -> NurseSupportPane(
                profile = state.nurseProfile,
                onSaveNurseProfile = onSaveNurseProfile
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun BreathingSanctuaryPane(
    onCompletePractice: (String, String, Int) -> Unit
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val patterns = CuratedClinicalContent.breathingPatterns

    var selectedPattern by remember { mutableStateOf(patterns.first()) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var phaseText by remember { mutableStateOf("Toca para empezar") }
    var phaseScaleTarget by remember { mutableStateOf(0.68f) }
    var elapsedSec by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRunning, selectedPattern) {
        if (!isRunning) {
            phaseText = "Toca para empezar"
            phaseScaleTarget = 0.68f
            return@LaunchedEffect
        }
        elapsedSec = 0
        repeat(selectedPattern.recommendedCycles) {
            // Inhalar
            phaseText = "Inhala suave"
            phaseScaleTarget = 1.0f
            haptics.tick()
            repeat(selectedPattern.inhaleSec) {
                delay(1000L)
                elapsedSec += 1
            }
            // Retener lleno
            if (selectedPattern.holdAfterInhaleSec > 0) {
                phaseText = "Sostén en calma"
                phaseScaleTarget = 1.03f
                repeat(selectedPattern.holdAfterInhaleSec) {
                    delay(1000L)
                    elapsedSec += 1
                }
            }
            // Exhalar
            phaseText = "Suelta despacio"
            phaseScaleTarget = 0.56f
            haptics.gentlePulse()
            repeat(selectedPattern.exhaleSec) {
                delay(1000L)
                elapsedSec += 1
            }
            // Pausa vacía
            if (selectedPattern.holdAfterExhaleSec > 0) {
                phaseText = "Reposa"
                phaseScaleTarget = 0.56f
                repeat(selectedPattern.holdAfterExhaleSec) {
                    delay(1000L)
                    elapsedSec += 1
                }
            }
        }
        isRunning = false
        onCompletePractice(selectedPattern.id, selectedPattern.title, elapsedSec.coerceAtLeast(30))
    }

    val animatedOrbScale by animateFloatAsState(
        targetValue = phaseScaleTarget,
        animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
        label = "breathOrbScale"
    )

    val idleTransition = rememberInfiniteTransition(label = "idleLotus")
    val idleDrift by idleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000),
            repeatMode = RepeatMode.Restart
        ),
        label = "lotusRotation"
    )
    val idleBreath by idleTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    // Escenario contemplativo oscuro petróleo para relajar la vista
    val stageShape = RoundedCornerShape(30.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(stageShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E2530),
                        Color(0xFF091922)
                    )
                ),
                shape = stageShape
            )
            .border(0.8.dp, Color(0xFFF4EFE6).copy(alpha = 0.12f), stageShape)
            .padding(horizontal = 20.dp, vertical = 26.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(256.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (isRunning) {
                            isRunning = false
                            if (elapsedSec >= 15) {
                                onCompletePractice(
                                    selectedPattern.id,
                                    selectedPattern.title,
                                    elapsedSec
                                )
                            }
                        } else {
                            isRunning = true
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension / 2f
                    val effectiveScale = if (isRunning) animatedOrbScale else (0.72f * idleBreath)

                    // Halo exterior brumoso
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4AD2B8).copy(alpha = 0.28f),
                                Color(0xFF289681).copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxR * effectiveScale * 1.05f
                        ),
                        radius = maxR * effectiveScale * 1.05f,
                        center = center
                    )

                    // Loto orgánico de 6 pétalos translúcidos
                    for (i in 0 until 6) {
                        rotate(degrees = idleDrift + i * 60f, pivot = center) {
                            val petalW = maxR * 0.56f * effectiveScale
                            val petalH = maxR * 0.86f * effectiveScale
                            drawOval(
                                color = Color(0xFF5AD8C0).copy(alpha = 0.13f),
                                topLeft = Offset(center.x - petalW / 2f, center.y - petalH / 2f),
                                size = Size(petalW, petalH)
                            )
                        }
                    }

                    // Anillos armónicos concéntricos
                    drawCircle(
                        color = Color(0xFF6AE0C9).copy(alpha = 0.42f),
                        radius = maxR * 0.72f * effectiveScale,
                        center = center,
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFFF6F2EA).copy(alpha = 0.22f),
                        radius = maxR * 0.52f * effectiveScale,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF1A4352).copy(alpha = 0.82f),
                        radius = maxR * 0.42f,
                        center = center
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = phaseText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = EditorialSerif,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color(0xFFF6F2EA),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedPattern.shortTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9EC2CF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = selectedPattern.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC7D6DE),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Selector minimalista de cadencias
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                patterns.forEach { pat: BreathingPattern ->
                    val selected = pat.id == selectedPattern.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) Color(0xFF38B89F).copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                0.8.dp,
                                if (selected) Color(0xFF5AD8C0) else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(50)
                            )
                            .clickable {
                                isRunning = false
                                selectedPattern = pat
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = pat.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color(0xFF8BF0DB) else Color(0xFFD8E4EA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            BrujulaPrimaryButton(
                text = if (isRunning) "Terminar pausa" else "Empezar respiración",
                icon = if (isRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                containerColor = if (isRunning) colors.amberAccent else Color(0xFF38B89F),
                contentColor = Color(0xFF06261F),
                onClick = {
                    if (isRunning) {
                        isRunning = false
                        if (elapsedSec >= 10) {
                            onCompletePractice(
                                selectedPattern.id,
                                selectedPattern.title,
                                elapsedSec
                            )
                        }
                    } else {
                        isRunning = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CopingProgressivePane(
    onCompletePractice: (String, String, Int) -> Unit
) {
    val colors = BrujulaDesign.colors
    var expandedCardId by rememberSaveable { mutableStateOf<String?>(CuratedClinicalContent.copingExercises.first().id) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CuratedClinicalContent.copingExercises.forEach { card ->
            val isExpanded = expandedCardId == card.id

            BrujulaSurface(
                borderColor = if (isExpanded) colors.jadePrimary.copy(alpha = 0.45f) else colors.borderHairline.copy(alpha = 0.50f),
                contentPadding = PaddingValues(20.dp),
                onClick = {
                    expandedCardId = if (isExpanded) null else card.id
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${card.category} · ${card.durationLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.jadePrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = EditorialSerif),
                            color = colors.inkPrimary
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = colors.inkMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = card.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary
                )

                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        card.steps.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp)
                                        .size(6.dp)
                                        .background(colors.jadePrimary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.inkPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        BrujulaTagPill(
                            label = "Me ha servido · guardar pausa",
                            selected = true,
                            icon = Icons.Outlined.Check,
                            onClick = {
                                onCompletePractice(card.id, card.title, 180)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedGuidePane() {
    val colors = BrujulaDesign.colors
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    val (urgentDetected, topics) = remember(query) {
        CuratedClinicalContent.queryReviewedGuidance(query)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar duda concreta (agua, sueño, PrEP, límites…)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = colors.inkMuted
                )
            },
            shape = RoundedCornerShape(50),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (urgentDetected) {
            BrujulaSurface(
                containerColor = colors.coralWash,
                borderColor = colors.coralAccent.copy(alpha = 0.45f),
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "Si hay una urgencia médica ahora",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onCoralWash
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Llama al 112 sin esperar. La prioridad sanitaria es cuidar la vida.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                BrujulaPrimaryButton(
                    text = "Llamar al 112",
                    icon = Icons.Outlined.Call,
                    containerColor = colors.coralAccent,
                    contentColor = Color.White,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                    }
                )
            }
        }

        topics.forEach { topic ->
            val isExpanded = expandedId == topic.id

            BrujulaSurface(
                contentPadding = PaddingValues(20.dp),
                onClick = { expandedId = if (isExpanded) null else topic.id }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (topic.showEmergency112Banner) colors.amberAccent else colors.jadePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = colors.inkMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = topic.reviewedResponse,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary
                )

                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        topic.keyTakeaways.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp)
                                        .size(5.dp)
                                        .background(colors.jadePrimary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NurseSupportPane(
    profile: NurseNavigatorProfile,
    onSaveNurseProfile: (NurseNavigatorProfile) -> Unit
) {
    val colors = BrujulaDesign.colors
    val context = LocalContext.current
    var showEditDialog by rememberSaveable { mutableStateOf(false) }

    BrujulaSurface(contentPadding = PaddingValues(24.dp)) {
        Text(
            text = "Acompañamiento profesional",
            style = MaterialTheme.typography.labelMedium,
            color = colors.jadePrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = profile.nurseName,
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = EditorialSerif),
            color = colors.inkPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = profile.centerName,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${profile.scheduleLabel} · ${profile.contactChannelNote}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BrujulaPrimaryButton(
                text = "Llamar",
                icon = Icons.Outlined.Call,
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile.phone}"))
                    )
                },
                modifier = Modifier.weight(1f)
            )
            BrujulaGhostButton(
                text = "Editar",
                icon = Icons.Outlined.Edit,
                onClick = { showEditDialog = true },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showEditDialog) {
        var name by rememberSaveable { mutableStateOf(profile.nurseName) }
        var center by rememberSaveable { mutableStateOf(profile.centerName) }
        var phone by rememberSaveable { mutableStateOf(profile.phone) }
        var schedule by rememberSaveable { mutableStateOf(profile.scheduleLabel) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Contacto enfermero") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre o referente") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = center,
                        onValueChange = { center = it },
                        label = { Text("Centro o unidad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = schedule,
                        onValueChange = { schedule = it },
                        label = { Text("Horario habitual") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveNurseProfile(
                            profile.copy(
                                nurseName = name.trim().ifBlank { profile.nurseName },
                                centerName = center.trim(),
                                phone = phone.trim(),
                                scheduleLabel = schedule.trim()
                            )
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
