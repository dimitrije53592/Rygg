package com.example.rygg.feature.record.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.components.pressScale
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggElevation
import com.example.rygg.core.ui.theme.RyggMotion
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ryggElevation
import com.example.rygg.core.ui.utils.rememberRyggHaptics

// The start button wears GPS readiness as a ring: none before location is granted, a sweeping
// arc while searching, and a closed accent ring once there is a fix. One glance, no extra chip.
@Composable
internal fun RecordStartButton(
    gpsReady: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberRyggHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val reduceMotion = RyggTheme.reduceMotion
    val track = RyggTheme.getColor(RyggColor.Outline)
    val searching = RyggTheme.getColor(RyggColor.TextSecondary)
    val ready = RyggTheme.getColor(RyggColor.AccentBright)
    val stroke = RyggTheme.dimens.gpsRingStroke
    val gap = RyggTheme.dimens.gpsRingGap

    val spin by rememberInfiniteTransition(label = "gpsSearch").animateFloat(
        initialValue = 0f,
        targetValue = FULL_SWEEP,
        animationSpec = infiniteRepeatable(tween(durationMillis = SPIN_MILLIS, easing = LinearEasing)),
        label = "gpsSearchSpin"
    )
    val closed by animateFloatAsState(
        targetValue = if (gpsReady == true) 1f else 0f,
        animationSpec = RyggMotion.spatialSlow(),
        label = "gpsReadyClose"
    )

    Box(
        modifier = modifier
            .size(RyggTheme.dimens.recordButtonSize + (stroke + gap) * 2)
            .drawBehind {
                if (gpsReady == null) return@drawBehind
                val strokePx = stroke.toPx()
                val inset = strokePx / 2
                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                drawArc(
                    color = track,
                    startAngle = 0f,
                    sweepAngle = FULL_SWEEP,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx)
                )
                if (closed > 0f) {
                    drawArc(
                        color = ready,
                        startAngle = START_ANGLE,
                        sweepAngle = FULL_SWEEP * closed,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                } else {
                    rotate(if (reduceMotion) 0f else spin) {
                        drawArc(
                            color = searching,
                            startAngle = START_ANGLE,
                            sweepAngle = SEARCH_SWEEP,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(stroke + gap)
                .size(RyggTheme.dimens.recordButtonSize)
                .pressScale(interactionSource)
                .ryggElevation(level = RyggElevation.Raised, shape = CircleShape)
                .clip(CircleShape)
                .background(RyggTheme.getColor(RyggColor.BrandGreen))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptics.confirm()
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = stringResource(R.string.record_start),
                tint = RyggTheme.getColor(RyggColor.OnBrand),
                modifier = Modifier.size(RyggTheme.dimens.iconSize48)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordStartButtonPreview() {
    RyggTheme {
        RecordStartButton(gpsReady = true, onClick = {})
    }
}

private const val FULL_SWEEP = 360f
private const val SEARCH_SWEEP = 80f
private const val START_ANGLE = -90f
private const val SPIN_MILLIS = 1_400
