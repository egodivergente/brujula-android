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
import androidx.compose.material3.Surface
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
    borderColor: Color = BrujulaDesign.colors.borderHairline,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val haptics = BrujulaDesign.haptics
    val baseModifier = modifier
        .clip(shape)
        .background(containerColor, shape)
        .border(0.85.dp, borderColor, shape)
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
    eyebrow: String,
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
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.jadePrimary
            )
            if (trailingActionLabel != null && onTrailingAction != null) {
                Text(
                    text = trailingActionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.jadePrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptics.click()
                            onTrailingAction()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.inkPrimary
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary
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
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        haptics.click()
                        onAction()
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
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
        targetValue = if (selected) washColor else colors.surfaceSubtle,
        label = "pillBg"
    )
    val border by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.65f) else colors.borderHairline,
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
            .defaultMinSize(minHeight = 34.dp)
            .clip(shape)
            .background(bg, shape)
            .border(0.85.dp, border, shape)
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
            .padding(horizontal = 12.dp, vertical = 6.dp)
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
                .size(7.5.dp)
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
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.9.dp, colors.borderStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = accentColor
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
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
 * Control deslizante táctil 0–10 de precisión con marcas de escala, respuesta háptica sutil
 * y codificación cromática no estigmatizante.
 */
@Composable
fun BrujulaPrecisionScale(
    title: String,
    subtitle: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minAnchorLabel: String,
    maxAnchorLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 0..10
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$title: $value sobre ${valueRange.last}"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.14f),
                border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "$value / ${valueRange.last}",
                    style = MaterialTheme.typography.labelLarge.merge(TabularNumberStyle),
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val stepCount = valueRange.last - valueRange.first
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
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
            val trackHeight = 6.dp.toPx()

            // Base track
            drawRoundRect(
                color = colors.surfaceSubtle,
                topLeft = Offset(pad, centerY - trackHeight / 2f),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            // Active fill
            val activeWidth = trackWidth * animatedFraction
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(pad, centerY - trackHeight / 2f),
                size = Size(activeWidth.coerceAtLeast(trackHeight), trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            // Precision tick marks
            for (i in 0..stepCount) {
                val x = pad + (trackWidth / stepCount.toFloat()) * i
                val isSelectedOrBelow = i <= (value - valueRange.first)
                drawCircle(
                    color = if (isSelectedOrBelow) colors.surfaceElevated.copy(alpha = 0.85f) else colors.borderStrong,
                    radius = if (i % 5 == 0) 2.3.dp.toPx() else 1.5.dp.toPx(),
                    center = Offset(x, centerY)
                )
            }

            // Thumb
            val thumbX = pad + activeWidth
            drawCircle(
                color = accentColor.copy(alpha = 0.22f),
                radius = 14.dp.toPx(),
                center = Offset(thumbX, centerY)
            )
            drawCircle(
                color = accentColor,
                radius = 9.dp.toPx(),
                center = Offset(thumbX, centerY)
            )
            drawCircle(
                color = colors.surfaceElevated,
                radius = 3.8.dp.toPx(),
                center = Offset(thumbX, centerY)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = minAnchorLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted
            )
            Text(
                text = maxAnchorLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted
            )
        }
    }
}
