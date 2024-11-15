package com.example.supabasedemo.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

//colors
data class AppColors(
    val black: androidx.compose.ui.graphics.Color,
    val backgroundB: androidx.compose.ui.graphics.Color,
    val buttonBLight: androidx.compose.ui.graphics.Color,
    val buttonBDark: androidx.compose.ui.graphics.Color,
    val onBackgroundB: androidx.compose.ui.graphics.Color
    )

//typography
data class AppTypography(
    val titleLarge: androidx.compose.ui.text.TextStyle,
    val titleSmall: androidx.compose.ui.text.TextStyle,
    val body: androidx.compose.ui.text.TextStyle,
    val labelLarge: androidx.compose.ui.text.TextStyle,
    val LabelMedium: androidx.compose.ui.text.TextStyle,
    val LabelSmall: androidx.compose.ui.text.TextStyle
    )

//shapes
data class AppShapes(
    val band: androidx.compose.ui.graphics.Shape,
    val container: androidx.compose.ui.graphics.Shape,
    val button: androidx.compose.ui.graphics.Shape
)

//sizes
data class AppSizes(
    val large: Dp,
    val medium: Dp,
    val small: Dp,
    val extraSmall: Dp
    )

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        black = TODO(),
        backgroundB = TODO(),
        buttonBLight = TODO(),
        buttonBDark = TODO(),
        onBackgroundB = TODO()
    )
}

val LocalAppTypography = staticCompositionLocalOf { 
    AppTypography(
        titleLarge = TODO(),
        titleSmall = TODO(),
        body = TODO(),
        labelLarge = TODO(),
        LabelMedium = TODO(),
        LabelSmall = TODO(),
    )
}

val LocalAppShapes = staticCompositionLocalOf {
    AppShapes(
        band = TODO(),
        container = TODO(),
        button = TODO(),
    )
}

val LocalAppSizes = staticCompositionLocalOf {
    AppSizes(
        large = TODO(),
        medium = TODO(),
        small = TODO(),
        extraSmall = TODO(),
    )
}

