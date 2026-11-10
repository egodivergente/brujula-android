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
import androidx.compose.ui.geometry.Size
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
 * Rosa náutica escultórica y contemplativa.
 * Pensada como refugio visual: respira lentamente y responde al tacto con suavidad.
 */
@Composable
fun LivingCompassCanvas(
    bearingState: CompassBearingState,
    modifier: Modifier = Modifier,
    diameter: Dp = 252.dp,
    onCompassTapped: (() -> Unit)? = null
) {
    val haptics = BrujulaDesign.haptics
    val textMeasurer = rememberTextMeasurer()

    var manualOffsetDeg by remember { mutableFloatStateOf(0f) }
    var lastDetentBucket by remember { mutableIntStateOf(0) }

    val primaryNeedleColor by animateColorAsState(
        targetValue = when (bearingState) {
            CompassBearingState.SERENE_HORIZON -> Color(0xFF47CEB4)
            CompassBearingState.VITAL_AUTONOMY -> Color(0xFF6AE0C9)
            CompassBearingState.ATTENTIVE_BREEZE -> Color(0xFFF2B35A)
            CompassBearingState.INTENSE_SWELL -> Color(0xFFEF8771)
        },
        animationSpec = tween(900),
        label = "needlePrimary"
    )

    val auraColor by animateColorAsState(
        targetValue = when (bearingState) {
            CompassBearingState.SERENE_HORIZON -> Color(0xFF2FA891).copy(alpha = 0.28f)
            CompassBearingState.VITAL_AUTONOMY -> Color(0xFF5AD6BE).copy(alpha = 0.30f)
            CompassBearingState.ATTENTIVE_BREEZE -> Color(0xFFE09435).copy(alpha = 0.26f)
            CompassBearingState.INTENSE_SWELL -> Color(0xFFDF6B54).copy(alpha = 0.28f)
        },
        animationSpec = tween(900),
        label = "compassAura"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "compassSanctuary")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val outerWave by infiniteTransition.animateFloat(
        initialValue = 1.02f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerWave"
    )
    val microDrift by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "microDrift"
    )

    val animatedAngle by animateFloatAsState(
        targetValue = bearingState.targetAzimuthDeg + manualOffsetDeg + microDrift,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "needleAngle"
    )

    val cardinalLabelStyle = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.6.sp,
        color = Color(0xFFEAE3D5)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(diameter)
            .semantics {
                contentDescription = "Brújula personal: ${bearingState.shortTitle}"
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
                        val bucket = (deg / 18f).roundToInt()
                        if (bucket != lastDetentBucket) {
                            lastDetentBucket = bucket
                            haptics.tick()
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension / 2f

            // 1. Doble halo atmosférico respiratorio
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor, auraColor.copy(alpha = 0.06f), Color.Transparent),
                    center = center,
                    radius = maxR * breathScale
                ),
                radius = maxR * breathScale,
                center = center
            )

            drawCircle(
                color = primaryNeedleColor.copy(alpha = 0.14f),
                radius = maxR * 0.90f * outerWave,
                center = center,
                style = Stroke(width = 0.9.dp.toPx())
            )

            // 2. Esfera escultórica en Azul Petróleo profundo con reflejo de cristal
            val dialR = maxR * 0.79f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A3F50),
                        Color(0xFF0E2530),
                        Color(0xFF08171F)
                    ),
                    center = Offset(center.x - dialR * 0.22f, center.y - dialR * 0.25f),
                    radius = dialR * 1.35f
                ),
                radius = dialR,
                center = center
            )

            // Brillo especular superior tipo lente mineral
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFDFBF6).copy(alpha = 0.34f),
                        Color.Transparent
                    ),
                    start = Offset(center.x - dialR, center.y - dialR),
                    end = Offset(center.x + dialR * 0.3f, center.y + dialR * 0.3f)
                ),
                startAngle = 195f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = Offset(center.x - dialR, center.y - dialR),
                size = Size(dialR * 2f, dialR * 2f),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )

            drawCircle(
                color = Color(0xFFF3EEE5).copy(alpha = 0.14f),
                radius = dialR,
                center = center,
                style = Stroke(width = 0.9.dp.toPx())
            )

            // 3. Corona minimalista de 48 marcas suaves
            val outerTickR = dialR * 0.90f
            for (i in 0 until 48) {
                val deg = i * 7.5f
                val rad = Math.toRadians((deg - 90f).toDouble())
                val isCardinal = i % 12 == 0
                val isSub = i % 6 == 0 && !isCardinal
                val length = when {
                    isCardinal -> 9.dp.toPx()
                    isSub -> 5.dp.toPx()
                    else -> 2.2.dp.toPx()
                }
                val alpha = when {
                    isCardinal -> 0.80f
                    isSub -> 0.38f
                    else -> 0.16f
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
                    strokeWidth = if (isCardinal) 1.6.dp.toPx() else 0.85.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Rosa de los vientos en filigrana interior (8 puntas sutiles)
            val starOuter = dialR * 0.50f
            val starInner = dialR * 0.14f
            for (k in 0 until 8) {
                val baseRad = Math.toRadians((k * 45f - 90f).toDouble())
                val leftRad = Math.toRadians((k * 45f - 112.5f).toDouble())
                val rightRad = Math.toRadians((k * 45f - 67.5f).toDouble())
                val rTip = if (k % 2 == 0) starOuter else starOuter * 0.68f
                val petal = Path().apply {
                    moveTo(
                        center.x + rTip * cos(baseRad).toFloat(),
                        center.y + rTip * sin(baseRad).toFloat()
                    )
                    lineTo(
                        center.x + starInner * cos(leftRad).toFloat(),
                        center.y + starInner * sin(leftRad).toFloat()
                    )
                    lineTo(center.x, center.y)
                    lineTo(
                        center.x + starInner * cos(rightRad).toFloat(),
                        center.y + starInner * sin(rightRad).toFloat()
                    )
                    close()
                }
                drawPath(
                    path = petal,
                    color = Color(0xFFF4EFE6).copy(alpha = if (k % 2 == 0) 0.09f else 0.05f)
                )
            }

            drawCircle(
                color = primaryNeedleColor.copy(alpha = 0.22f),
                radius = dialR * 0.54f,
                center = center,
                style = Stroke(width = 0.8.dp.toPx())
            )

            // 5. Puntos cardinales N · E · S · O limpios
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
                    cardinalLabelStyle.copy(color = Color(0xFFF4EFE6).copy(alpha = 0.52f))
                }
                val measured = textMeasurer.measure(letter, style)
                val tx = center.x + labelR * cos(rad).toFloat() - measured.size.width / 2f
                val ty = center.y + labelR * sin(rad).toFloat() - measured.size.height / 2f
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(tx, ty)
                )
            }

            // 6. Aguja magnética estilizada con bisel de luz
            rotate(degrees = animatedAngle, pivot = center) {
                val northTip = Offset(center.x, center.y - dialR * 0.68f)
                val southTip = Offset(center.x, center.y + dialR * 0.54f)
                val halfWidth = 7.dp.toPx()

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
                drawPath(path = northRight, color = primaryNeedleColor.copy(alpha = 0.72f))

                val southPath = Path().apply {
                    moveTo(southTip.x, southTip.y)
                    lineTo(center.x - halfWidth * 0.72f, center.y)
                    lineTo(center.x, center.y + 4.dp.toPx())
                    lineTo(center.x + halfWidth * 0.72f, center.y)
                    close()
                }
                drawPath(path = southPath, color = Color(0xFFEDE6D8).copy(alpha = 0.76f))
            }

            // 7. Eje joya central
            drawCircle(
                color = Color(0xFFE9A54B),
                radius = 6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0xFF0A1D26),
                radius = 2.5.dp.toPx(),
                center = center
            )
        }
    }
}
