package com.example.supabasedemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

private val lightColorScheme = AppColors(
    backgroundB = BlueBackground,
    black = Black,
    buttonBLight = LightblueButton,
    buttonBDark = DarkblueButton,
    onBackgroundB = GreyblueButton,
)

private val darkColorScheme = AppColors(
    backgroundB = BlueBackground,
    black = Black,
    buttonBLight = LightblueButton,
    buttonBDark = DarkblueButton,
    onBackgroundB = GreyblueButton,
)

private val typography = AppTypography(
    titleLarge = TextStyle(
        fontFamily = Jersey
    ),
    titleSmall = TextStyle(
        fontFamily = Jersey
    ),
    body = TextStyle(
        fontFamily = Jersey
    ),
    labelLarge = TextStyle(
        fontFamily = Jersey
    ),
    LabelMedium = TextStyle(
        fontFamily = Jersey
    ),
    LabelSmall = TextStyle(
        fontFamily = Jersey
    ),
)

private val shape = AppShapes(
    band = RectangleShape,
    container = RectangleShape,
    button = RectangleShape
)

private val size = AppSizes(
    large = 24.dp,
    medium = 16.dp,
    small = 12.dp,
    extraSmall = 8.dp
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) darkColorScheme else lightColorScheme
    CompositionLocalProvider(
        LocalAppColors provides colorScheme,
        LocalAppTypography provides typography,
        LocalAppShapes provides shape,
        LocalAppSizes provides size,
        content = content
    )
}

object AppTheme {

    val colorScheme: AppColors
        @Composable get() = LocalAppColors.current
    val typography: AppTypography
        @Composable get() = LocalAppTypography.current
    val shape: AppShapes
        @Composable get() = LocalAppShapes.current
    val size: AppSizes
        @Composable get() = LocalAppSizes.current
}