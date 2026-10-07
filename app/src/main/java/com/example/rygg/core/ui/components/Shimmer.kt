package com.example.rygg.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

private const val SWEEP_MILLIS = 1400

// Drawn in the draw phase via drawWithCache, so the sweep never triggers recomposition or layout —
// a shimmer that costs a relayout every frame is worse than the spinner it replaced.
@Composable
fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = SWEEP_MILLIS, easing = LinearEasing)),
        label = "shimmerSweep"
    )
    val base = RyggTheme.getColor(RyggColor.Surface)
    val highlight = RyggTheme.getColor(RyggColor.SurfaceElevated)

    return drawWithCache {
        val sweepStart = (progress * 2f - 1f) * size.width
        val brush = Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(sweepStart, 0f),
            end = Offset(sweepStart + size.width, size.height)
        )
        onDrawBehind { drawRect(brush) }
    }
}
