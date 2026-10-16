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
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.rygg.core.ui.utils.animationsDisabled

val LocalRyggIsDarkMode = staticCompositionLocalOf { false }

val LocalRyggReduceMotion = staticCompositionLocalOf { false }

@Composable
fun RyggTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalRyggIsDarkMode provides darkTheme,
        LocalRyggReduceMotion provides remember(context) { context.animationsDisabled() },
        LocalRyggDimensions provides Dimensions,
        LocalRyggTextStyles provides RyggTextStyles
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

    val textStyles: RyggTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalRyggTextStyles.current

    val isDarkMode: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalRyggIsDarkMode.current

    // Decorative and looping motion (pulses, staggers, rings) checks this and holds still.
    val reduceMotion: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalRyggReduceMotion.current

    @Composable
    @ReadOnlyComposable
    fun getColor(token: RyggColor): Color =
        if (LocalRyggIsDarkMode.current) token.darkColor else token.lightColor
}
