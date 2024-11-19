package com.example.supabasedemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.internal.wait

private val lightColorScheme = lightColorScheme(
    background = LightBlue,
    onBackground = GreyBlue,
    outline = Black,
    primary = LightBlue,
    onPrimary = LightBlue,
    primaryContainer = LightBlue,
    inversePrimary = LightBlue,
    onPrimaryContainer = LightBlue,
    secondary = DarkBlue,
    onSecondary = DarkBlue,
    secondaryContainer = DarkBlue,
    onSecondaryContainer = DarkBlue,
    tertiary = GreyBlue,
    onTertiary = GreyBlue,
    tertiaryContainer = GreyBlue,
    onTertiaryContainer = GreyBlue,
    surface = BlueBackground,
    onSurface = LightBlue,
    surfaceVariant = DarkBlue,
    surfaceDim = GreyBlue,
    surfaceTint = LightBlue,
    surfaceBright = LightBlue,
    surfaceContainer = LightBlue,
    surfaceContainerLow = DarkBlue,
    surfaceContainerHigh = LightBlue,
    surfaceContainerLowest = DarkBlue,
    surfaceContainerHighest = LightBlue,
    onSurfaceVariant = LightBlue,
    inverseSurface = DarkBlue,
    inverseOnSurface = LightBlue,
    scrim = Color.Blue,
    error = Color.Red,
    errorContainer = Color.White,
    onErrorContainer = Color.Red,
    onError = Color.Red,
    outlineVariant = Color.White,
)

private val darkColorScheme = darkColorScheme(
    background = LightBlueDark,
    onBackground = GreyBlueDark,
    outline = Black,
    primary = LightBlueDark,
    onPrimary = LightBlueDark,
    primaryContainer = LightBlueDark,
    inversePrimary = LightBlueDark,
    onPrimaryContainer = LightBlueDark,
    secondary = DarkBlueDark,
    onSecondary = DarkBlueDark,
    secondaryContainer = DarkBlueDark,
    onSecondaryContainer = DarkBlueDark,
    tertiary = GreyBlueDark,
    onTertiary = GreyBlueDark,
    tertiaryContainer = GreyBlueDark,
    onTertiaryContainer = GreyBlueDark,
    surface = BlueBackgroundDark,
    onSurface = LightBlueDark,
    surfaceVariant = DarkBlueDark,
    surfaceDim = GreyBlueDark,
    surfaceTint = LightBlueDark,
    surfaceBright = LightBlueDark,
    surfaceContainer = LightBlueDark,
    surfaceContainerLow = DarkBlueDark,
    surfaceContainerHigh = LightBlueDark,
    surfaceContainerLowest = DarkBlueDark,
    surfaceContainerHighest = LightBlueDark,
    onSurfaceVariant = LightBlueDark,
    inverseSurface = DarkBlueDark,
    inverseOnSurface = LightBlueDark,
    scrim = Color.Blue,
    error = Color.Red,
    errorContainer = Color.White,
    onErrorContainer = Color.Red,
    onError = Color.Red,
    outlineVariant = Color.White,
)

private val typography = Typography(
    titleLarge = Typography.titleLarge,
    titleSmall = Typography.titleSmall,
    bodyLarge = Typography.bodyLarge,
    labelLarge = Typography.labelLarge,
    labelMedium = Typography.labelMedium,
    labelSmall = Typography.labelSmall
)

private val shape = Shapes(
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
        shapes = shape
    )
}

object AppTheme {
    val colorScheme: ColorScheme @Composable get() = MaterialTheme.colorScheme
    val typography: Typography @Composable get() = MaterialTheme.typography
    val shape: Shapes @Composable get() = MaterialTheme.shapes
}