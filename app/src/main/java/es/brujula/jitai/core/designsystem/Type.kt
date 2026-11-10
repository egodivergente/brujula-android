package es.brujula.jitai.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val EditorialSerif = FontFamily.Serif
val CleanSans = FontFamily.SansSerif

fun buildBrujulaTypography(scale: Float = 1.0f): Typography {
    return Typography(
        displaySmall = TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (28 * scale).sp,
            lineHeight = (34 * scale).sp,
            letterSpacing = (-0.4).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (23 * scale).sp,
            lineHeight = (29 * scale).sp,
            letterSpacing = (-0.3).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (19 * scale).sp,
            lineHeight = (25 * scale).sp,
            letterSpacing = (-0.2).sp
        ),
        titleLarge = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (17 * scale).sp,
            lineHeight = (23 * scale).sp,
            letterSpacing = (-0.15).sp
        ),
        titleMedium = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleSmall = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.Medium,
            fontSize = (13.5 * scale).sp,
            lineHeight = (18 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * scale).sp,
            lineHeight = (22 * scale).sp,
            letterSpacing = 0.05.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.Normal,
            fontSize = (13.5 * scale).sp,
            lineHeight = (19.5 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        bodySmall = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (17 * scale).sp,
            letterSpacing = 0.15.sp
        ),
        labelLarge = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13 * scale).sp,
            lineHeight = (18 * scale).sp,
            letterSpacing = 0.25.sp
        ),
        labelMedium = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5 * scale).sp,
            lineHeight = (15 * scale).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = CleanSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (10.5 * scale).sp,
            lineHeight = (14 * scale).sp,
            letterSpacing = 0.9.sp
        )
    )
}

val TabularNumberStyle = TextStyle(
    fontFamily = CleanSans,
    fontFeatureSettings = "tnum"
)
