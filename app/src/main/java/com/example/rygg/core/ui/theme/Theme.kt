package com.example.rygg.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalRyggIsDarkMode = staticCompositionLocalOf { false }

@Composable
fun RyggTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalRyggIsDarkMode provides darkTheme,
        LocalRyggDimensions provides Dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RyggTypography,
            content = content
        )
    }
}

object RyggTheme {
    val dimens: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalRyggDimensions.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val isDarkMode: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalRyggIsDarkMode.current

    @Composable
    @ReadOnlyComposable
    fun getColor(token: RyggColor): Color =
        if (LocalRyggIsDarkMode.current) token.darkColor else token.lightColor
}
