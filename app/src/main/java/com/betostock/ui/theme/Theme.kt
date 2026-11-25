package com.betostock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Paleta del tema claro ("Claro y Práctico"), tomada del DESIGN.md de Stitch. */
object BetoColors {
    val Primary = Color(0xFF0B6FB8)
    val PrimaryDark = Color(0xFF0B4F85)
    val Sky = Color(0xFF4FB3F0)
    val SkyLight = Color(0xFFD6EEFC)
    val Background = Color(0xFFF4F8FA)
    val Card = Color(0xFFFFFFFF)
    val Border = Color(0xFFCFDDE8)
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF334155)

    val Success = Color(0xFF2E7D32)
    val SuccessContainer = Color(0xFFE8F5E9)
    val OnSuccessContainer = Color(0xFF1B5E20)

    val Warning = Color(0xFFE65100)
    val WarningContainer = Color(0xFFFFF3E0)
    val OnWarningContainer = Color(0xFFBF360C)

    val Error = Color(0xFFC62828)
    val ErrorContainer = Color(0xFFFFEBEE)
    val OnErrorContainer = Color(0xFFB71C1C)

    val Disabled = Color(0xFFE2E8F0)
    val OnDisabled = Color(0xFF64748B)
}

private val LightColors = lightColorScheme(
    primary = BetoColors.Primary,
    onPrimary = Color.White,
    primaryContainer = BetoColors.SkyLight,
    onPrimaryContainer = BetoColors.PrimaryDark,
    secondary = BetoColors.Sky,
    onSecondary = BetoColors.TextPrimary,
    secondaryContainer = BetoColors.SkyLight,
    onSecondaryContainer = BetoColors.TextPrimary,
    background = BetoColors.Background,
    onBackground = BetoColors.TextPrimary,
    surface = BetoColors.Card,
    onSurface = BetoColors.TextPrimary,
    surfaceVariant = BetoColors.SkyLight,
    onSurfaceVariant = BetoColors.TextSecondary,
    outline = BetoColors.Border,
    outlineVariant = BetoColors.Border,
    error = BetoColors.Error,
    onError = Color.White,
    errorContainer = BetoColors.ErrorContainer,
    onErrorContainer = BetoColors.OnErrorContainer
)

// Ningún texto baja de 18sp (pensado para lectura cómoda).
private val BetoTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 44.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)
)

private val BetoShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

val PriceDisplay = TextStyle(fontFamily = FontFamily.Default, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 40.sp)
val PriceItem = TextStyle(fontFamily = FontFamily.Default, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)

@Composable
fun BetoStockTheme(content: @Composable () -> Unit) {
    // Por ahora solo existe el tema claro; el tema oscuro se agregará después.
    MaterialTheme(
        colorScheme = LightColors,
        typography = BetoTypography,
        shapes = BetoShapes,
        content = content
    )
}
