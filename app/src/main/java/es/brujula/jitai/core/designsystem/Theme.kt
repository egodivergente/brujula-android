package es.brujula.jitai.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import es.brujula.jitai.core.model.ThemePreference

private val LightM3Scheme = lightColorScheme(
    primary = BrujulaPalette.JadeDeep,
    onPrimary = Color.White,
    primaryContainer = BrujulaPalette.JadeWashLight,
    onPrimaryContainer = Color(0xFF093A30),
    secondary = BrujulaPalette.PetroleumMid,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE7EC),
    onSecondaryContainer = BrujulaPalette.PetroleumDeep,
    tertiary = BrujulaPalette.AmberDeep,
    onTertiary = Color.White,
    tertiaryContainer = BrujulaPalette.AmberWashLight,
    onTertiaryContainer = Color(0xFF5D3402),
    error = BrujulaPalette.CoralDeep,
    onError = Color.White,
    errorContainer = BrujulaPalette.CoralWashLight,
    onErrorContainer = Color(0xFF6B2012),
    background = BrujulaPalette.WarmIvoryCanvas,
    onBackground = BrujulaPalette.PetroleumDeep,
    surface = BrujulaPalette.WarmIvoryElevated,
    onSurface = BrujulaPalette.PetroleumDeep,
    surfaceVariant = BrujulaPalette.WarmIvorySubtle,
    onSurfaceVariant = Color(0xFF435B67),
    outline = BrujulaPalette.WarmIvoryHairline,
    outlineVariant = Color(0xFFEAE3D6)
)

private val DarkM3Scheme = darkColorScheme(
    primary = BrujulaPalette.JadeLuminous,
    onPrimary = Color(0xFF052E26),
    primaryContainer = BrujulaPalette.JadeWashDark,
    onPrimaryContainer = Color(0xFFA7ECE0),
    secondary = Color(0xFF7CB8D0),
    onSecondary = Color(0xFF072533),
    secondaryContainer = Color(0xFF193949),
    onSecondaryContainer = Color(0xFFCDE5F0),
    tertiary = BrujulaPalette.AmberLuminous,
    onTertiary = Color(0xFF422600),
    tertiaryContainer = BrujulaPalette.AmberWashDark,
    onTertiaryContainer = Color(0xFFFAD39C),
    error = BrujulaPalette.CoralLuminous,
    onError = Color(0xFF4A1309),
    errorContainer = BrujulaPalette.CoralWashDark,
    onErrorContainer = Color(0xFFFAC0B4),
    background = BrujulaPalette.PetroleumNightCanvas,
    onBackground = Color(0xFFF3EEE5),
    surface = BrujulaPalette.PetroleumNightElevated,
    onSurface = Color(0xFFF3EEE5),
    surfaceVariant = BrujulaPalette.PetroleumNightSubtle,
    onSurfaceVariant = BrujulaPalette.PetroleumMutedDark,
    outline = BrujulaPalette.PetroleumNightHairline,
    outlineVariant = Color(0xFF1A3240)
)

object BrujulaDesign {
    val colors: BrujulaSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBrujulaColors.current

    val haptics: BrujulaHaptics
        @Composable
        @ReadOnlyComposable
        get() = LocalBrujulaHaptics.current
}

@Composable
fun BrujulaTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    largeText: Boolean = false,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val semanticColors = if (darkTheme) DarkBrujulaColors else LightBrujulaColors
    val m3Scheme = if (darkTheme) DarkM3Scheme else LightM3Scheme
    val typography = buildBrujulaTypography(if (largeText) 1.12f else 1.0f)
    val haptics = rememberBrujulaHaptics(hapticsEnabled)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalBrujulaColors provides semanticColors,
        LocalBrujulaHaptics provides haptics
    ) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = typography,
            content = content
        )
    }
}
