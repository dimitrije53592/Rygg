package com.example.rygg.core.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

fun Modifier.headerContourLines(color: Color): Modifier = drawBehind {
    val strokeColor = color.copy(alpha = CONTOUR_ALPHA)
    val stroke = Stroke(width = STROKE_WIDTH.dp.toPx())
    val scaleX = size.width / DESIGN_WIDTH
    val spacing = HEADER_LINE_SPACING.dp.toPx()
    val center = size.height - (TOP_APP_BAR_HEIGHT / HALF).dp.toPx()
    clipRect {
        for (index in 0 until HEADER_LINE_COUNT) {
            val baseline = center + (index - HEADER_LINE_COUNT / INT_HALF) * spacing
            drawWave(baseline, scaleX, stroke, strokeColor)
        }
    }
}

fun Modifier.screenContourLines(color: Color): Modifier = drawBehind {
    val strokeColor = color.copy(alpha = CONTOUR_ALPHA)
    val stroke = Stroke(width = STROKE_WIDTH.dp.toPx())
    val scaleX = size.width / DESIGN_WIDTH
    val spacing = LINE_SPACING.dp.toPx()
    clipRect {
        var baseline = spacing
        while (baseline < size.height) {
            drawWave(baseline, scaleX, stroke, strokeColor)
            baseline += spacing
        }
    }
}

private fun DrawScope.drawWave(
    baseline: Float,
    scaleX: Float,
    stroke: Stroke,
    color: Color
) {
    val path = Path().apply {
        moveTo(WAVE_START_X * scaleX, baseline)
        quadraticTo(
            WAVE_CONTROL_1_X * scaleX,
            baseline + WAVE_CONTROL_1_DY.dp.toPx(),
            WAVE_MID_X * scaleX,
            baseline + WAVE_MID_DY.dp.toPx()
        )
        quadraticTo(
            WAVE_CONTROL_2_X * scaleX,
            baseline + WAVE_CONTROL_2_DY.dp.toPx(),
            WAVE_END_X * scaleX,
            baseline + WAVE_END_DY.dp.toPx()
        )
    }
    drawPath(path = path, color = color, style = stroke)
}

private const val DESIGN_WIDTH = 390f
private const val WAVE_START_X = -10f
private const val WAVE_CONTROL_1_X = 90f
private const val WAVE_MID_X = 190f
private const val WAVE_CONTROL_2_X = 290f
private const val WAVE_END_X = 410f
private const val WAVE_CONTROL_1_DY = -26f
private const val WAVE_MID_DY = -8f
private const val WAVE_CONTROL_2_DY = 10f
private const val WAVE_END_DY = -20f
private const val LINE_SPACING = 38f
private const val HEADER_LINE_SPACING = 24f
private const val HEADER_LINE_COUNT = 3
private const val TOP_APP_BAR_HEIGHT = 64f
private const val STROKE_WIDTH = 1.5f
private const val CONTOUR_ALPHA = 0.12f
private const val HALF = 2f
private const val INT_HALF = 2
