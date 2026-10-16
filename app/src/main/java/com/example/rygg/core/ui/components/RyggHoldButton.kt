package com.example.rygg.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggMotion
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.rememberRyggHaptics
import kotlinx.coroutines.launch

// A circular action that only fires after being held: a ring fills around it while pressed, so
// an irreversible action can't be triggered by a pocket, a glove or a stray tap. A short tap calls
// [onTap] so the screen can explain the gesture. Screen-reader activation fires directly.
@Composable
fun RyggHoldButton(
    icon: ImageVector,
    contentDescription: String,
    onHoldComplete: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = RyggTheme.dimens.holdButtonSize,
    containerColor: Color = RyggTheme.getColor(RyggColor.Error),
    ringColor: Color = RyggTheme.getColor(RyggColor.Error),
    holdMillis: Int = HOLD_MILLIS
) {
    val haptics = rememberRyggHaptics()
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val currentOnComplete by rememberUpdatedState(onHoldComplete)
    val currentOnTap by rememberUpdatedState(onTap)
    val ringTrack = RyggTheme.getColor(RyggColor.Outline)
    val stroke = RyggTheme.dimens.holdRingStroke
    val gap = RyggTheme.dimens.holdRingGap

    Box(
        modifier = modifier
            .size(size + (stroke + gap) * 2)
            .drawBehind {
                val strokePx = stroke.toPx()
                val inset = strokePx / 2
                val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
                drawArc(
                    color = ringTrack,
                    startAngle = 0f,
                    sweepAngle = FULL_SWEEP,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx)
                )
                drawArc(
                    color = ringColor,
                    startAngle = START_ANGLE,
                    sweepAngle = FULL_SWEEP * progress.value,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
                onClick {
                    currentOnComplete()
                    true
                }
            }
            .pointerInput(holdMillis) {
                detectTapGestures(
                    onPress = {
                        val fill = scope.launch {
                            progress.animateTo(1f, tween(durationMillis = holdMillis, easing = LinearEasing))
                            haptics.confirm()
                            currentOnComplete()
                        }
                        tryAwaitRelease()
                        if (progress.value < 1f) {
                            fill.cancel()
                            scope.launch { progress.animateTo(0f, RyggMotion.effectsFast()) }
                            currentOnTap()
                        } else {
                            scope.launch { progress.snapTo(0f) }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(stroke + gap)
                .size(size)
                .graphicsLayer {
                    val scale = 1f - PRESS_SHRINK * progress.value
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.OnBrand),
                modifier = Modifier.size(RyggTheme.dimens.iconSize40)
            )
        }
    }
}

private const val HOLD_MILLIS = 1_000
private const val FULL_SWEEP = 360f
private const val START_ANGLE = -90f
private const val PRESS_SHRINK = 0.06f
