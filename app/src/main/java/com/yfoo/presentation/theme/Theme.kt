package com.yfoo.presentation.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//TODO change colors to replicate Badoo and Tinder's white styles more
private val LightThemeColors = lightColors(
    primary = Purple400,
    primaryVariant = Purple600,
    secondary = Green300,
    secondaryVariant = Green500,
    background = Color.White,
    surface = Color.White,
    error = Color.Red,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun YfooTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = LightThemeColors,
        typography = YfooTypography,
        shapes = YfooShapes,
        content = content,
    )
}