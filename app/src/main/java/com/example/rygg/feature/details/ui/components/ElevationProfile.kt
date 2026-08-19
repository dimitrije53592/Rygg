package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.R
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatElevationMeters

@Composable
fun ElevationProfile(
    samples: List<ElevationSample>,
    modifier: Modifier = Modifier
) {
    val lineColor = RyggTheme.getColor(RyggColor.BrandGreen)
    val fillTop = lineColor.copy(alpha = 0.28f)
    val fillBottom = lineColor.copy(alpha = 0f)

    val high = samples.maxOf { it.elevationMeters }
    val low = samples.minOf { it.elevationMeters }
    val totalMeters = samples.last().distanceMeters

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RyggTheme.dimens.radius16))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.details_elevation_profile),
                style = RyggTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            Text(
                text = stringResource(
                    R.string.details_elevation_high_low,
                    formatElevationMeters(high),
                    formatElevationMeters(low)
                ),
                style = RyggTheme.typography.labelMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(RyggTheme.dimens.elevationProfileHeight)
        ) {
            val minElevation = low
            val elevationSpan = (high - low).takeIf { it > 0.0 } ?: 1.0
            val distanceSpan = totalMeters.takeIf { it > 0.0 } ?: 1.0

            fun project(sample: ElevationSample): Offset {
                val x = (sample.distanceMeters / distanceSpan).toFloat() * size.width
                val y = size.height -
                    ((sample.elevationMeters - minElevation) / elevationSpan).toFloat() * size.height
                return Offset(x, y)
            }

            val linePath = Path()
            samples.forEachIndexed { index, sample ->
                val offset = project(sample)
                if (index == 0) linePath.moveTo(offset.x, offset.y) else linePath.lineTo(offset.x, offset.y)
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(listOf(fillTop, fillBottom))
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = size.minDimension * 0.02f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDistanceKm(0.0),
                style = RyggTheme.typography.labelSmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
            Text(
                text = formatDistanceKm(totalMeters / 2.0),
                style = RyggTheme.typography.labelSmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
            Text(
                text = formatDistanceKm(totalMeters),
                style = RyggTheme.typography.labelSmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }
    }
}
