package com.jujinkim.frequaw.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = lightBlue300,
    primaryVariant = lightBlue300,
    secondary = lightBlue300,
    secondaryVariant = lightBlue300,
    background = black,
    surface = black,
    error = red900,
    onPrimary = white,
    onSecondary = white,
    onBackground = white,
    onSurface = white,
    onError = white,
)

private val LightColorPalette = lightColors(
    primary = lightBlue600,
    primaryVariant = lightBlue600,
    secondary = lightBlue600,
    secondaryVariant = lightBlue600,
    background = white,
    surface = white,
    error = red600,
    onPrimary = white,
    onSecondary = white,
    onBackground = black,
    onSurface = black,
    onError = white,
)

@Composable
fun FrequawTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        content = content
    )
}