package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatElevationMeters
import kotlin.math.abs

@Composable
fun ElevationProfile(
    samples: List<ElevationSample>,
    modifier: Modifier = Modifier,
    onScrubChange: (Float?) -> Unit = {}
) {
    if (samples.size < 2) return

    val accentColor = RyggTheme.getColor(RyggColor.AccentBright)
    val moderate = RyggTheme.getColor(RyggColor.GradeModerate)
    val steep = RyggTheme.getColor(RyggColor.GradeSteep)
    val verySteep = RyggTheme.getColor(RyggColor.GradeVerySteep)
    val extreme = RyggTheme.getColor(RyggColor.GradeExtreme)
    val gridColor = RyggTheme.getColor(RyggColor.TextSecondary).copy(alpha = GRID_ALPHA)
    val crosshairColor = RyggTheme.getColor(RyggColor.TextPrimary)

    val high = samples.maxOf { it.elevationMeters }
    val low = samples.minOf { it.elevationMeters }
    val totalMeters = samples.last().distanceMeters

    val hairline = RyggTheme.dimens.border1
    val lineStroke = RyggTheme.dimens.border2
    val markerRadius = RyggTheme.dimens.chartMarker5

    val grades = remember(samples) { samples.smoothedGrades() }
    var scrubFraction by remember { mutableStateOf<Float?>(null) }

    fun setScrub(fraction: Float?) {
        scrubFraction = fraction
        onScrubChange(fraction)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RyggShapes.card)
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
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            val readout = scrubFraction?.let { fraction ->
                val sample = samples.sampleAt(fraction)
                stringResource(
                    R.string.details_elevation_readout,
                    formatDistanceKm(sample.distanceMeters),
                    formatElevationMeters(sample.elevationMeters)
                )
            } ?: stringResource(
                R.string.details_elevation_high_low,
                formatElevationMeters(high),
                formatElevationMeters(low)
            )
            Text(
                text = readout,
                style = RyggTheme.textStyles.statValueSmall,
                color = if (scrubFraction != null) {
                    RyggTheme.getColor(RyggColor.TextPrimary)
                } else {
                    RyggTheme.getColor(RyggColor.TextSecondary)
                }
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(RyggTheme.dimens.elevationProfileHeight)
                .pointerInput(samples) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        setScrub((down.position.x / size.width).coerceIn(0f, 1f))
                        var pressed = true
                        while (pressed) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                setScrub((change.position.x / size.width).coerceIn(0f, 1f))
                                change.consume()
                            }
                            pressed = event.changes.any { it.pressed }
                        }
                        setScrub(null)
                    }
                }
        ) {
            val elevationSpan = (high - low).takeIf { it > 0.0 } ?: 1.0
            val distanceSpan = totalMeters.takeIf { it > 0.0 } ?: 1.0
            val plotTop = size.height * VERTICAL_INSET
            val plotHeight = size.height - plotTop

            fun project(sample: ElevationSample): Offset {
                val x = (sample.distanceMeters / distanceSpan).toFloat() * size.width
                val y = plotTop +
                    (1f - ((sample.elevationMeters - low) / elevationSpan).toFloat()) * plotHeight
                return Offset(x, y)
            }

            repeat(GRID_LINES) { index ->
                val y = plotTop + plotHeight * (index + 1) / (GRID_LINES + 1).toFloat()
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = hairline.toPx()
                )
            }

            val projected = samples.map(::project)

            val fillPath = Path().apply {
                moveTo(projected.first().x, projected.first().y)
                projected.drop(1).forEach { lineTo(it.x, it.y) }
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = FILL_ALPHA), Color.Transparent)
                )
            )

            // Segment by segment rather than one path, so the climb bands stay readable.
            val strokeWidth = lineStroke.toPx()
            for (index in 1 until projected.size) {
                drawLine(
                    color = gradeColor(
                        grade = grades[index],
                        accent = accentColor,
                        moderate = moderate,
                        steep = steep,
                        verySteep = verySteep,
                        extreme = extreme
                    ),
                    start = projected[index - 1],
                    end = projected[index],
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            scrubFraction?.let { fraction ->
                val sample = samples.sampleAt(fraction)
                val point = project(sample)
                drawLine(
                    color = crosshairColor.copy(alpha = 0.5f),
                    start = Offset(point.x, plotTop),
                    end = Offset(point.x, size.height),
                    strokeWidth = hairline.toPx()
                )
                drawCircle(color = crosshairColor, radius = markerRadius.toPx(), center = point)
                drawCircle(
                    color = accentColor,
                    radius = markerRadius.toPx(),
                    center = point,
                    style = Stroke(width = lineStroke.toPx())
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(0.0, totalMeters / 2.0, totalMeters).forEach { meters ->
                Text(
                    text = formatDistanceKm(meters),
                    style = RyggTheme.textStyles.trackedLabel,
                    color = RyggTheme.getColor(RyggColor.TextSecondary)
                )
            }
        }
    }
}

private fun List<ElevationSample>.sampleAt(fraction: Float): ElevationSample {
    val target = last().distanceMeters * fraction
    return minByOrNull { abs(it.distanceMeters - target) } ?: first()
}

// Averaged backwards over a fixed distance, or GPS jitter flips the colour band every few pixels.
private fun List<ElevationSample>.smoothedGrades(): List<Double> {
    val grades = DoubleArray(size)
    var windowStart = 0
    for (index in 1 until size) {
        while (this[index].distanceMeters - this[windowStart].distanceMeters > GRADE_WINDOW_METERS &&
            windowStart < index - 1
        ) {
            windowStart++
        }
        val run = this[index].distanceMeters - this[windowStart].distanceMeters
        val rise = this[index].elevationMeters - this[windowStart].elevationMeters
        grades[index] = if (run > 0.0) rise / run else 0.0
    }
    return grades.toList()
}

private fun gradeColor(
    grade: Double,
    accent: Color,
    moderate: Color,
    steep: Color,
    verySteep: Color,
    extreme: Color
): Color {
    val magnitude = abs(grade)
    return when {
        magnitude >= GRADE_EXTREME -> extreme
        magnitude >= GRADE_VERY_STEEP -> verySteep
        magnitude >= GRADE_STEEP -> steep
        magnitude >= GRADE_MODERATE -> moderate
        else -> accent
    }
}

private const val FILL_ALPHA = 0.28f
private const val GRID_ALPHA = 0.12f
private const val GRID_LINES = 3
private const val GRADE_WINDOW_METERS = 60.0
private const val GRADE_MODERATE = 0.03
private const val GRADE_STEEP = 0.07
private const val GRADE_VERY_STEEP = 0.16
private const val GRADE_EXTREME = 0.25

// Inset so a peak touching the maximum is not sliced off by the canvas edge.
private const val VERTICAL_INSET = 0.08f
