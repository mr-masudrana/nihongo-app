package com.example.nihongo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ইন্ডিগো (主色) + সাকুরা গোলাপি + মাচা সবুজ — জাপানি রঙের ছোঁয়া
private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF),
    onPrimaryContainer = Color(0xFF14126B),
    secondary = Color(0xFFD6336C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E3),
    onSecondaryContainer = Color(0xFF3F0019),
    tertiary = Color(0xFF2E7D5B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8F0DC),
    onTertiaryContainer = Color(0xFF00311F),
    background = Color(0xFFFAF8FC),
    onBackground = Color(0xFF1C1B22),
    surface = Color(0xFFFAF8FC),
    onSurface = Color(0xFF1C1B22),
    surfaceVariant = Color(0xFFE5E1EC),
    onSurfaceVariant = Color(0xFF48454F),
    outline = Color(0xFF79757F),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F2FA),
    surfaceContainer = Color(0xFFF0EDF6),
    surfaceContainerHigh = Color(0xFFEAE7F1),
    surfaceContainerHighest = Color(0xFFE4E1EB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBDBBFF),
    onPrimary = Color(0xFF1F1B8C),
    primaryContainer = Color(0xFF3730B8),
    onPrimaryContainer = Color(0xFFE0E0FF),
    secondary = Color(0xFFFFB0C8),
    onSecondary = Color(0xFF650033),
    secondaryContainer = Color(0xFF8A1448),
    onSecondaryContainer = Color(0xFFFFD9E3),
    tertiary = Color(0xFF8DD5B0),
    onTertiary = Color(0xFF00391F),
    tertiaryContainer = Color(0xFF0F513A),
    onTertiaryContainer = Color(0xFFC8F0DC),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF131218),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF48454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF48454F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surfaceContainerLowest = Color(0xFF0E0D13),
    surfaceContainerLow = Color(0xFF1B1A20),
    surfaceContainer = Color(0xFF1F1E25),
    surfaceContainerHigh = Color(0xFF2A292F),
    surfaceContainerHighest = Color(0xFF35343A)
)

// বাংলা অক্ষরের জন্য letterSpacing = 0 এবং একটু বেশি lineHeight — যুক্তাক্ষর কাটা পড়ে না
private val Base = Typography()
private val AppTypography = Typography(
    headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp, lineHeight = 40.sp),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp, lineHeight = 38.sp),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 34.sp),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 30.sp),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 26.sp),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 22.sp),
    bodyLarge = Base.bodyLarge.copy(letterSpacing = 0.sp, lineHeight = 26.sp),
    bodyMedium = Base.bodyMedium.copy(letterSpacing = 0.sp, lineHeight = 22.sp),
    bodySmall = Base.bodySmall.copy(letterSpacing = 0.sp, lineHeight = 18.sp),
    labelLarge = Base.labelLarge.copy(letterSpacing = 0.sp, lineHeight = 22.sp),
    labelMedium = Base.labelMedium.copy(letterSpacing = 0.sp, lineHeight = 18.sp),
    labelSmall = Base.labelSmall.copy(letterSpacing = 0.sp, lineHeight = 16.sp)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun NihongoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
