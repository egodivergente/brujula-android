package es.brujula.jitai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.brujula.jitai.core.designsystem.BrujulaDesign
import es.brujula.jitai.core.designsystem.CleanSans
import es.brujula.jitai.core.model.CompassBearingState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Brújula protagonista viva e interactiva.
 * Cambia con suavidad su tono y cadencia respiratoria según el estado indicado por la persona,
 * sin emitir jamás etiquetas diagnósticas. Admite exploración táctil con respuesta háptica sutil.
 */
@Composable
fun LivingCompassCanvas(
    bearingState: CompassBearingState,
    modifier: Modifier = Modifier,
    diameter: Dp = 208.dp,
    onCompassTapped: (() -> Unit)? = null
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val textMeasurer = rememberTextMeasurer()

    var manualOffsetDeg by remember { mutableFloatStateOf(0f) }
    var lastDetentBucket by remember { mutableIntStateOf(0) }

    val primaryNeedleColor by animateColorAsState(
        targetValue = when (bearingState) {
            CompassBearingState.SERENE_HORIZON -> Color(0xFF39BCA3)
            CompassBearingState.VITAL_AUTONOMY -> Color(0xFF5AD6BE)
            CompassBearingState.ATTENTIVE_BREEZE -> Color(0xFFECA84A)
            CompassBearingState.INTENSE_SWELL -> Color(0xFFEB7D67)
        },
        animationSpec = tween(700),
        label = "needlePrimary"
    )

    val auraColor by animateColorAsState(
        targetValue = when (bearingState) {
            CompassBearingState.SERENE_HORIZON -> Color(0xFF289681).copy(alpha = 0.24f)
            CompassBearingState.VITAL_AUTONOMY -> Color(0xFF5AD6BE).copy(alpha = 0.28f)
            CompassBearingState.ATTENTIVE_BREEZE -> Color(0xFFD98A29).copy(alpha = 0.24f)
            CompassBearingState.INTENSE_SWELL -> Color(0xFFD8634C).copy(alpha = 0.26f)
        },
        animationSpec = tween(700),
        label = "compassAura"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "compassBreath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val microDrift by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "microDrift"
    )

    val animatedAngle by animateFloatAsState(
        targetValue = bearingState.targetAzimuthDeg + manualOffsetDeg + microDrift,
        animationSpec = spring(
            dampingRatio = 0.66f,
            stiffness = Spring.StiffnessLow
        ),
        label = "needleAngle"
    )

    val cardinalLabelStyle = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.1.sp,
        color = Color(0xFFEAE3D5)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(diameter)
            .semantics {
                contentDescription = "Brújula personal: ${bearingState.shortTitle}. ${bearingState.poeticReading}"
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onCompassTapped) {
                    detectTapGestures {
                        manualOffsetDeg = 0f
                        haptics.gentlePulse()
                        onCompassTapped?.invoke()
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            manualOffsetDeg = 0f
                            haptics.tick()
                        },
                        onDragCancel = {
                            manualOffsetDeg = 0f
                        }
                    ) { change, _ ->
                        change.consume()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val rad = atan2(change.position.y - cy, change.position.x - cx)
                        val deg = Math.toDegrees(rad.toDouble()).toFloat() + 90f
                        manualOffsetDeg = deg - bearingState.targetAzimuthDeg
                        val bucket = (deg / 15f).roundToInt()
                        if (bucket != lastDetentBucket) {
                            lastDetentBucket = bucket
                            haptics.tick()
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension / 2f

            // 1. Halo ambiental respiratorio
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor, Color.Transparent),
                    center = center,
                    radius = maxR * breathScale
                ),
                radius = maxR * breathScale,
                center = center
            )

            // 2. Esfera principal en Azul Petróleo profundo (identidad constante y serena)
            val dialR = maxR * 0.88f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF173847),
                        Color(0xFF0C2029)
                    ),
                    center = Offset(center.x - dialR * 0.18f, center.y - dialR * 0.2f),
                    radius = dialR * 1.25f
                ),
                radius = dialR,
                center = center
            )

            // Bisel exterior fino marfil/jade
            drawCircle(
                color = Color(0xFFF3EEE5).copy(alpha = 0.24f),
                radius = dialR,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 3. Corona de 72 marcas acimutales de precisión (cada 5°)
            val outerTickR = dialR * 0.91f
            for (i in 0 until 72) {
                val deg = i * 5f
                val rad = Math.toRadians((deg - 90f).toDouble())
                val isCardinal = i % 18 == 0
                val isIntercardinal = i % 9 == 0 && !isCardinal
                val length = when {
                    isCardinal -> 10.dp.toPx()
                    isIntercardinal -> 6.5.dp.toPx()
                    i % 3 == 0 -> 4.5.dp.toPx()
                    else -> 2.5.dp.toPx()
                }
                val alpha = when {
                    isCardinal -> 0.88f
                    isIntercardinal -> 0.55f
                    else -> 0.24f
                }
                val start = Offset(
                    center.x + (outerTickR - length) * cos(rad).toFloat(),
                    center.y + (outerTickR - length) * sin(rad).toFloat()
                )
                val end = Offset(
                    center.x + outerTickR * cos(rad).toFloat(),
                    center.y + outerTickR * sin(rad).toFloat()
                )
                drawLine(
                    color = if (i == 0) primaryNeedleColor else Color(0xFFF4EFE6).copy(alpha = alpha),
                    start = start,
                    end = end,
                    strokeWidth = if (isCardinal) 1.8.dp.toPx() else 0.9.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Anillo orbital interior y estrella sutil de rumbo
            val innerRingR = dialR * 0.56f
            drawCircle(
                color = primaryNeedleColor.copy(alpha = 0.26f),
                radius = innerRingR,
                center = center,
                style = Stroke(width = 0.9.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFF4EFE6).copy(alpha = 0.10f),
                radius = dialR * 0.30f,
                center = center,
                style = Stroke(width = 0.8.dp.toPx())
            )

            // Cruz filigrana interior
            val hairR = innerRingR * 0.88f
            drawLine(
                color = Color(0xFFF4EFE6).copy(alpha = 0.12f),
                start = Offset(center.x - hairR, center.y),
                end = Offset(center.x + hairR, center.y),
                strokeWidth = 0.8.dp.toPx()
            )
            drawLine(
                color = Color(0xFFF4EFE6).copy(alpha = 0.12f),
                start = Offset(center.x, center.y - hairR),
                end = Offset(center.x, center.y + hairR),
                strokeWidth = 0.8.dp.toPx()
            )

            // 5. Puntos cardinales N · E · S · O
            val cardinals = listOf(
                "N" to -90f,
                "E" to 0f,
                "S" to 90f,
                "O" to 180f
            )
            val labelR = dialR * 0.71f
            cardinals.forEach { (letter, angleDeg) ->
                val rad = Math.toRadians(angleDeg.toDouble())
                val style = if (letter == "N") {
                    cardinalLabelStyle.copy(color = primaryNeedleColor, fontWeight = FontWeight.Bold)
                } else {
                    cardinalLabelStyle.copy(color = Color(0xFFF4EFE6).copy(alpha = 0.68f))
                }
                val measured = textMeasurer.measure(letter, style)
                val tx = center.x + labelR * cos(rad).toFloat() - measured.size.width / 2f
                val ty = center.y + labelR * sin(rad).toFloat() - measured.size.height / 2f
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(tx, ty)
                )
            }

            // 6. Aguja magnética bipartita con geometría facetada
            rotate(degrees = animatedAngle, pivot = center) {
                val northTip = Offset(center.x, center.y - dialR * 0.66f)
                val southTip = Offset(center.x, center.y + dialR * 0.56f)
                val halfWidth = 7.5.dp.toPx()

                // Mitad Norte (tono de estado actual: Jade / Ámbar / Coral)
                val northLeft = Path().apply {
                    moveTo(northTip.x, northTip.y)
                    lineTo(center.x - halfWidth, center.y)
                    lineTo(center.x, center.y - 4.dp.toPx())
                    close()
                }
                val northRight = Path().apply {
                    moveTo(northTip.x, northTip.y)
                    lineTo(center.x + halfWidth, center.y)
                    lineTo(center.x, center.y - 4.dp.toPx())
                    close()
                }
                drawPath(path = northLeft, color = primaryNeedleColor)
                drawPath(path = northRight, color = primaryNeedleColor.copy(alpha = 0.78f))

                // Mitad Sur (Marfil cálido equilibrado)
                val southPath = Path().apply {
                    moveTo(southTip.x, southTip.y)
                    lineTo(center.x - halfWidth * 0.78f, center.y)
                    lineTo(center.x, center.y + 4.dp.toPx())
                    lineTo(center.x + halfWidth * 0.78f, center.y)
                    close()
                }
                drawPath(path = southPath, color = Color(0xFFEAE2D3).copy(alpha = 0.82f))
            }

            // 7. Eje central joya en Ámbar y Petróleo
            drawCircle(
                color = Color(0xFFE59A3C),
                radius = 5.6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0xFF0C2029),
                radius = 2.4.dp.toPx(),
                center = center
            )
        }
    }
}
