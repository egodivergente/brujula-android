package es.brujula.jitai.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun BrujulaSurface(
    modifier: Modifier = Modifier,
    containerColor: Color = BrujulaDesign.colors.surfaceElevated,
    borderColor: Color = BrujulaDesign.colors.borderHairline.copy(alpha = 0.55f),
    cornerRadius: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val haptics = BrujulaDesign.haptics
    val baseModifier = modifier
        .clip(shape)
        .background(containerColor, shape)
        .border(0.6.dp, borderColor, shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(role = Role.Button) {
                    haptics.click()
                    onClick()
                }
            } else {
                Modifier
            }
        )
        .padding(contentPadding)

    Column(
        modifier = baseModifier,
        content = content
    )
}

@Composable
fun BrujulaEditorialHeader(
    eyebrow: String = "",
    title: String,
    subtitle: String? = null,
    trailingActionLabel: String? = null,
    onTrailingAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = colors.inkPrimary,
                modifier = Modifier.weight(1f)
            )
            if (trailingActionLabel != null && onTrailingAction != null) {
                Spacer(modifier = Modifier.width(12.dp))
                BrujulaTagPill(
                    label = trailingActionLabel,
                    selected = true,
                    onClick = {
                        haptics.click()
                        onTrailingAction()
                    }
                )
            }
        }
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted
            )
        }
    }
}

@Composable
fun BrujulaSectionTitle(
    title: String,
    meta: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.inkPrimary
            )
            if (!meta.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = colors.jadePrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        haptics.click()
                        onAction()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun BrujulaTagPill(
    label: String,
    selected: Boolean = false,
    accentColor: Color = BrujulaDesign.colors.jadePrimary,
    washColor: Color = BrujulaDesign.colors.jadeWash,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    val bg by animateColorAsState(
        targetValue = if (selected) washColor else colors.surfaceSubtle.copy(alpha = 0.8f),
        label = "pillBg"
    )
    val border by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.45f) else Color.Transparent,
        label = "pillBorder"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) accentColor else colors.inkSecondary,
        label = "pillFg"
    )

    val shape = RoundedCornerShape(50)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(shape)
            .background(bg, shape)
            .border(0.75.dp, border, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        haptics.tick()
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BrujulaStatusDot(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = BrujulaDesign.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.inkSecondary
        )
    }
}

@Composable
fun BrujulaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = BrujulaDesign.colors.jadePrimary,
    contentColor: Color = if (BrujulaDesign.colors.isDark) Color(0xFF062922) else Color.White,
    enabled: Boolean = true
) {
    val haptics = BrujulaDesign.haptics
    Button(
        onClick = {
            haptics.confirm()
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun BrujulaGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = BrujulaDesign.colors.inkPrimary
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    OutlinedButton(
        onClick = {
            haptics.click()
            onClick()
        },
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, colors.borderStrong.copy(alpha = 0.65f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = accentColor
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = accentColor
        )
    }
}

/**
 * Control deslizante orgánico y sensorial: muestra palabras cualitativas suaves por defecto
 * en lugar de cifras frías para reducir el ruido mental en momentos sensibles.
 */
@Composable
fun BrujulaPrecisionScale(
    title: String,
    subtitle: String? = null,
    value: Int,
    onValueChange: (Int) -> Unit,
    minAnchorLabel: String = "",
    maxAnchorLabel: String = "",
    accentColor: Color,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 0..10,
    showNumericBadge: Boolean = false,
    qualitativeLabel: ((Int) -> String)? = null
) {
    val colors = BrujulaDesign.colors
    val haptics = BrujulaDesign.haptics
    var lastHapticValue by remember { mutableIntStateOf(value) }

    val fraction = ((value - valueRange.first).toFloat() / (valueRange.last - valueRange.first).coerceAtLeast(1))
        .coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scaleFraction"
    )

    val badgeText = when {
        qualitativeLabel != null -> qualitativeLabel(value)
        showNumericBadge -> "$value"
        else -> defaultQualitativeWord(value, valueRange)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$title: $badgeText"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val stepCount = (valueRange.last - valueRange.first).coerceAtLeast(1)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(valueRange) {
                    detectTapGestures { offset ->
                        val horizontalPad = 14.dp.toPx()
                        val usable = (size.width - horizontalPad * 2).coerceAtLeast(1f)
                        val raw = ((offset.x - horizontalPad) / usable).coerceIn(0f, 1f)
                        val mapped = (valueRange.first + raw * stepCount).roundToInt()
                            .coerceIn(valueRange.first, valueRange.last)
                        if (mapped != lastHapticValue) {
                            lastHapticValue = mapped
                            haptics.tick()
                        }
                        onValueChange(mapped)
                    }
                }
                .pointerInput(valueRange) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val horizontalPad = 14.dp.toPx()
                        val usable = (size.width - horizontalPad * 2).coerceAtLeast(1f)
                        val raw = ((change.position.x - horizontalPad) / usable).coerceIn(0f, 1f)
                        val mapped = (valueRange.first + raw * stepCount).roundToInt()
                            .coerceIn(valueRange.first, valueRange.last)
                        if (mapped != lastHapticValue) {
                            lastHapticValue = mapped
                            haptics.tick()
                        }
                        onValueChange(mapped)
                    }
                }
        ) {
            val pad = 14.dp.toPx()
            val trackWidth = (size.width - pad * 2).coerceAtLeast(1f)
            val centerY = size.height / 2f
            val trackHeight = 11.dp.toPx()

            drawRoundRect(
                color = colors.surfaceSubtle,
                topLeft = Offset(pad, centerY - trackHeight / 2f),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            val activeWidth = (trackWidth * animatedFraction).coerceAtLeast(trackHeight)
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.55f), accentColor),
                    startX = pad,
                    endX = pad + activeWidth
                ),
                topLeft = Offset(pad, centerY - trackHeight / 2f),
                size = Size(activeWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            val thumbX = pad + trackWidth * animatedFraction
            drawCircle(
                color = accentColor.copy(alpha = 0.22f),
                radius = 16.dp.toPx(),
                center = Offset(thumbX, centerY)
            )
            drawCircle(
                color = accentColor,
                radius = 10.dp.toPx(),
                center = Offset(thumbX, centerY)
            )
            drawCircle(
                color = colors.surfaceElevated,
                radius = 4.2.dp.toPx(),
                center = Offset(thumbX, centerY)
            )
        }

        if (minAnchorLabel.isNotBlank() || maxAnchorLabel.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = minAnchorLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted.copy(alpha = 0.8f)
                )
                Text(
                    text = maxAnchorLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun defaultQualitativeWord(value: Int, range: IntRange): String {
    if (range != 0..10) return "$value"
    return when (value) {
        0, 1 -> "Muy bajo"
        2, 3 -> "Suave"
        4, 5, 6 -> "Medio"
        7, 8 -> "Alto"
        else -> "Muy intenso"
    }
}
