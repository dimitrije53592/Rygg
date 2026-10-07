package com.example.rygg.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

// A single shadow is a step function; a real penumbra is a gradient. Compose has no multi-shadow
// API, so we stack a tight dark layer under a wide faint one — the near/far pair below approximates
// the doubling-offset falloff that makes an element read as an object rather than a sticker.
//
// The tint is deliberately not black: black over a coloured ground desaturates into grey haze. This
// is the graphite hue of the app's own background with the lightness pulled down.
private val ShadowTint = Color(0xFF141A20)

private const val NEAR_ALPHA = 0.18f
private const val FAR_ALPHA = 0.10f

enum class RyggElevation(val near: Dp, val far: Dp) {
    None(Dimensions.elevation0, Dimensions.elevation0),
    Card(Dimensions.elevation2, Dimensions.elevation8),
    Raised(Dimensions.elevation4, Dimensions.elevation16),
    Floating(Dimensions.elevation8, Dimensions.elevation24)
}

// Shadows barely register against a dark ground, which is why dark-theme Material separates
// surfaces by lightness instead. On dark we draw nothing and let the surface token do the work.
@Composable
fun Modifier.ryggElevation(
    level: RyggElevation,
    shape: Shape
): Modifier {
    if (level == RyggElevation.None || RyggTheme.isDarkMode) return this
    return this
        .shadow(
            elevation = level.far,
            shape = shape,
            clip = false,
            ambientColor = ShadowTint.copy(alpha = FAR_ALPHA),
            spotColor = ShadowTint.copy(alpha = FAR_ALPHA)
        )
        .shadow(
            elevation = level.near,
            shape = shape,
            clip = false,
            ambientColor = ShadowTint.copy(alpha = NEAR_ALPHA),
            spotColor = ShadowTint.copy(alpha = NEAR_ALPHA)
        )
}
