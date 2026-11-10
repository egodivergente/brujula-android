package es.brujula.jitai.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Paleta identitaria BRÚJULA: Verde Jade, Marfil Cálido, Azul Petróleo y Acentos Ámbar/Coral
object BrujulaPalette {
    // Marfil Cálido (Modo Claro)
    val WarmIvoryCanvas = Color(0xFFFAF7F1)
    val WarmIvoryElevated = Color(0xFFFFFDF9)
    val WarmIvorySubtle = Color(0xFFF1ECE2)
    val WarmIvoryHairline = Color(0xFFDFD7C8)

    // Azul Petróleo (Profundidad, serenidad y contraste clínico cálido)
    val PetroleumDeep = Color(0xFF0E2530)
    val PetroleumNightCanvas = Color(0xFF0A161D)
    val PetroleumNightElevated = Color(0xFF11232E)
    val PetroleumNightSubtle = Color(0xFF172E3C)
    val PetroleumNightHairline = Color(0xFF244253)
    val PetroleumMid = Color(0xFF1D4658)
    val PetroleumMutedLight = Color(0xFF496573)
    val PetroleumMutedDark = Color(0xFFA7BDC7)

    // Verde Jade (Orientación, autonomía, calma vital)
    val JadeDeep = Color(0xFF1B7463)
    val JadeMid = Color(0xFF289681)
    val JadeLuminous = Color(0xFF48C4AC)
    val JadeWashLight = Color(0xFFDEF1EC)
    val JadeWashDark = Color(0xFF143B35)

    // Ámbar Cálido (Atención contextual sin alarma)
    val AmberDeep = Color(0xFFB86E14)
    val AmberWarm = Color(0xFFD98A29)
    val AmberLuminous = Color(0xFFF0AD4E)
    val AmberWashLight = Color(0xFFF9EFE0)
    val AmberWashDark = Color(0xFF362814)

    // Coral Terracota (Acompañamiento cercano en momentos intensos, nunca punitivo)
    val CoralDeep = Color(0xFFC04E39)
    val CoralWarm = Color(0xFFD8634C)
    val CoralLuminous = Color(0xFFEF826C)
    val CoralWashLight = Color(0xFFFBEAE6)
    val CoralWashDark = Color(0xFF3B1F1B)
}

@Immutable
data class BrujulaSemanticColors(
    val isDark: Boolean,
    val canvas: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val surfaceHero: Color,
    val onSurfaceHero: Color,
    val borderHairline: Color,
    val borderStrong: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkMuted: Color,
    val jadePrimary: Color,
    val jadeLuminous: Color,
    val jadeWash: Color,
    val onJadeWash: Color,
    val petroleumAccent: Color,
    val amberAccent: Color,
    val amberWash: Color,
    val onAmberWash: Color,
    val coralAccent: Color,
    val coralWash: Color,
    val onCoralWash: Color,
    val zoneGreen: Color,
    val zoneGreenWash: Color,
    val zoneAmber: Color,
    val zoneAmberWash: Color,
    val zoneCoral: Color,
    val zoneCoralWash: Color
)

val LightBrujulaColors = BrujulaSemanticColors(
    isDark = false,
    canvas = BrujulaPalette.WarmIvoryCanvas,
    surfaceElevated = BrujulaPalette.WarmIvoryElevated,
    surfaceSubtle = BrujulaPalette.WarmIvorySubtle,
    surfaceHero = BrujulaPalette.PetroleumDeep,
    onSurfaceHero = Color(0xFFF6F2EA),
    borderHairline = BrujulaPalette.WarmIvoryHairline,
    borderStrong = Color(0xFFC8BEAC),
    inkPrimary = BrujulaPalette.PetroleumDeep,
    inkSecondary = Color(0xFF334E5B),
    inkMuted = Color(0xFF637B87),
    jadePrimary = BrujulaPalette.JadeDeep,
    jadeLuminous = BrujulaPalette.JadeMid,
    jadeWash = BrujulaPalette.JadeWashLight,
    onJadeWash = Color(0xFF0E4A3E),
    petroleumAccent = BrujulaPalette.PetroleumMid,
    amberAccent = BrujulaPalette.AmberDeep,
    amberWash = BrujulaPalette.AmberWashLight,
    onAmberWash = Color(0xFF6E3F05),
    coralAccent = BrujulaPalette.CoralDeep,
    coralWash = BrujulaPalette.CoralWashLight,
    onCoralWash = Color(0xFF7A2819),
    zoneGreen = BrujulaPalette.JadeDeep,
    zoneGreenWash = BrujulaPalette.JadeWashLight,
    zoneAmber = BrujulaPalette.AmberWarm,
    zoneAmberWash = BrujulaPalette.AmberWashLight,
    zoneCoral = BrujulaPalette.CoralWarm,
    zoneCoralWash = BrujulaPalette.CoralWashLight
)

val DarkBrujulaColors = BrujulaSemanticColors(
    isDark = true,
    canvas = BrujulaPalette.PetroleumNightCanvas,
    surfaceElevated = BrujulaPalette.PetroleumNightElevated,
    surfaceSubtle = BrujulaPalette.PetroleumNightSubtle,
    surfaceHero = Color(0xFF0E212B),
    onSurfaceHero = Color(0xFFF4EFE6),
    borderHairline = BrujulaPalette.PetroleumNightHairline,
    borderStrong = Color(0xFF355C70),
    inkPrimary = Color(0xFFF3EEE5),
    inkSecondary = BrujulaPalette.PetroleumMutedDark,
    inkMuted = Color(0xFF7894A2),
    jadePrimary = BrujulaPalette.JadeLuminous,
    jadeLuminous = Color(0xFF6CD6C1),
    jadeWash = BrujulaPalette.JadeWashDark,
    onJadeWash = Color(0xFF9BE6D6),
    petroleumAccent = Color(0xFF5FA2BD),
    amberAccent = BrujulaPalette.AmberLuminous,
    amberWash = BrujulaPalette.AmberWashDark,
    onAmberWash = Color(0xFFFAD092),
    coralAccent = BrujulaPalette.CoralLuminous,
    coralWash = BrujulaPalette.CoralWashDark,
    onCoralWash = Color(0xFFF9B6A8),
    zoneGreen = BrujulaPalette.JadeLuminous,
    zoneGreenWash = BrujulaPalette.JadeWashDark,
    zoneAmber = BrujulaPalette.AmberLuminous,
    zoneAmberWash = BrujulaPalette.AmberWashDark,
    zoneCoral = BrujulaPalette.CoralLuminous,
    zoneCoralWash = BrujulaPalette.CoralWashDark
)

val LocalBrujulaColors = staticCompositionLocalOf { LightBrujulaColors }
