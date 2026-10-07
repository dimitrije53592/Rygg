package com.example.rygg.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import kotlin.math.cos
import kotlin.math.min

// Stroke weights and marker sizes are picked per surface rather than derived from the canvas, so a
// route reads the same whether it is a list card or a full-bleed hero.
enum class RouteCanvasScale(
    val track: Dp,
    val glow: Dp,
    val marker: Dp
) {
    Card(track = Dp(3f), glow = Dp(10f), marker = Dp(5f)),
    Hero(track = Dp(4f), glow = Dp(14f), marker = Dp(7f))
}

private const val FIT_PADDING_FRACTION = 0.12f
private const val GLOW_ALPHA = 0.22f
private const val CONTOUR_ALPHA = 0.5f

@Composable
fun RouteCanvas(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier,
    scale: RouteCanvasScale = RouteCanvasScale.Card
) {
    val trackColor = RyggTheme.getColor(RyggColor.AccentBright)
    val groundTop = RyggTheme.getColor(RyggColor.MossSurface)
    val groundBottom = RyggTheme.getColor(RyggColor.MossSurfaceDim)
    val contourColor = RyggTheme.getColor(RyggColor.OnBrand).copy(alpha = CONTOUR_ALPHA)
    val startColor = RyggTheme.getColor(RyggColor.OnBrand)

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(groundTop, groundBottom)))
            .screenContourLines(contourColor)
    ) {
        if (points.size < 2) return@Box

        Canvas(modifier = Modifier.fillMaxSize()) {
            val projected = project(points, size.width, size.height)
            val path = Path().apply {
                projected.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }

            // A wide translucent pass under the track reads as the line sitting above the terrain
            // rather than being stamped onto it.
            drawPath(
                path = path,
                color = trackColor.copy(alpha = GLOW_ALPHA),
                style = Stroke(
                    width = scale.glow.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            drawPath(
                path = path,
                color = trackColor,
                style = Stroke(
                    width = scale.track.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            val markerRadius = scale.marker.toPx()
            // Filled start, hollow end — so direction of travel is readable at a glance.
            drawCircle(color = startColor, radius = markerRadius, center = projected.first())
            drawCircle(
                color = trackColor,
                radius = markerRadius,
                center = projected.last(),
                style = Stroke(width = scale.track.toPx())
            )
        }
    }
}

// Longitude degrees shrink by cos(latitude), so projecting lat/lon straight onto x/y stretches
// every route horizontally — about 41% at 45N. Same correction as RouteProgress.toLocalMeters.
private fun DrawScope.project(
    points: List<GeoPoint>,
    width: Float,
    height: Float
): List<Offset> {
    val minLat = points.minOf { it.lat }
    val maxLat = points.maxOf { it.lat }
    val minLon = points.minOf { it.lon }
    val maxLon = points.maxOf { it.lon }

    val lonScale = cos(Math.toRadians((minLat + maxLat) / 2.0)).coerceAtLeast(0.01)
    val spanLat = (maxLat - minLat).takeIf { it > 0.0 } ?: 1e-6
    val spanLon = ((maxLon - minLon) * lonScale).takeIf { it > 0.0 } ?: 1e-6

    val padding = min(width, height) * FIT_PADDING_FRACTION
    val availableWidth = width - padding * 2
    val availableHeight = height - padding * 2
    val fit = min(availableWidth / spanLon.toFloat(), availableHeight / spanLat.toFloat())
    val drawWidth = spanLon.toFloat() * fit
    val drawHeight = spanLat.toFloat() * fit
    val offsetX = padding + (availableWidth - drawWidth) / 2f
    val offsetY = padding + (availableHeight - drawHeight) / 2f

    return points.map { point ->
        val x = offsetX + (((point.lon - minLon) * lonScale) / spanLon).toFloat() * drawWidth
        val y = offsetY + (1f - ((point.lat - minLat) / spanLat).toFloat()) * drawHeight
        Offset(x, y)
    }
}
