package com.example.rygg.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

// Two stacked shadows: Compose has no multi-shadow API, and a tight layer under a wide faint one
// is what keeps an element from reading as a sticker. On dark the surface tokens carry depth
// instead, so nothing is drawn.
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

enum class RyggElevation(val near: Dp, val far: Dp) {
    None(Dimensions.elevation0, Dimensions.elevation0),
    Card(Dimensions.elevation2, Dimensions.elevation8),
    Raised(Dimensions.elevation4, Dimensions.elevation16),
    Floating(Dimensions.elevation8, Dimensions.elevation24)
}

// Not black: black over a coloured ground desaturates into grey haze. This is the app's own
// graphite with the lightness pulled down.
private val ShadowTint = Color(0xFF141A20)

private const val NEAR_ALPHA = 0.18f
private const val FAR_ALPHA = 0.10f
