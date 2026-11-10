package es.brujula.jitai.core.designsystem

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

@Immutable
class BrujulaHaptics(
    private val view: View?,
    private val enabled: Boolean
) {
    fun tick() {
        if (!enabled || view == null) return
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
        view.performHapticFeedback(constant)
    }

    fun click() {
        if (!enabled || view == null) return
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    fun confirm() {
        if (!enabled || view == null) return
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }
        view.performHapticFeedback(constant)
    }

    fun gentlePulse() {
        if (!enabled || view == null) return
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}

val LocalBrujulaHaptics = staticCompositionLocalOf { BrujulaHaptics(null, false) }

@Composable
fun rememberBrujulaHaptics(enabled: Boolean): BrujulaHaptics {
    val view = LocalView.current
    return remember(view, enabled) { BrujulaHaptics(view, enabled) }
}
