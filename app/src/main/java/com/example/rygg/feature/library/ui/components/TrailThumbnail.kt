package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import kotlin.math.min

@Composable
fun TrailThumbnail(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier
) {
    val trailColor = RyggTheme.getColor(RyggColor.BrandGreen)
    val backgroundTop = RyggTheme.getColor(RyggColor.MossSurface)
    val backgroundBottom = RyggTheme.getColor(RyggColor.MossSurfaceDim)

    Canvas(modifier = modifier) {
        drawRect(brush = Brush.linearGradient(listOf(backgroundTop, backgroundBottom)))

        if (points.size < 2) return@Canvas

        val minLat = points.minOf { it.lat }
        val maxLat = points.maxOf { it.lat }
        val minLon = points.minOf { it.lon }
        val maxLon = points.maxOf { it.lon }
        val spanLat = (maxLat - minLat).takeIf { it > 0.0 } ?: 1e-6
        val spanLon = (maxLon - minLon).takeIf { it > 0.0 } ?: 1e-6

        val padding = size.minDimension * 0.18f
        val availableWidth = size.width - padding * 2
        val availableHeight = size.height - padding * 2
        val scale = min(availableWidth / spanLon.toFloat(), availableHeight / spanLat.toFloat())
        val drawWidth = spanLon.toFloat() * scale
        val drawHeight = spanLat.toFloat() * scale
        val offsetX = padding + (availableWidth - drawWidth) / 2f
        val offsetY = padding + (availableHeight - drawHeight) / 2f

        fun project(point: GeoPoint): Offset {
            val x = offsetX + ((point.lon - minLon) / spanLon).toFloat() * drawWidth
            val y = offsetY + (1f - ((point.lat - minLat) / spanLat).toFloat()) * drawHeight
            return Offset(x, y)
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            val offset = project(point)
            if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
        }

        val strokeWidth = size.minDimension * 0.06f
        drawPath(
            path = path,
            color = trailColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(color = trailColor, radius = strokeWidth, center = project(points.first()))
    }
}
