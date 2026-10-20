package com.dimitrijeilic.rygg.feature.details.ui.components

import android.net.Uri
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.dimitrijeilic.rygg.core.ui.components.RyggAsyncImage
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import kotlinx.coroutines.launch

// Pinch and double-tap zoom. Drags are only consumed while zoomed in (or pinching), so at 1x the
// surrounding pager keeps its swipe.
@Composable
internal fun ZoomableImage(
    uri: Uri,
    isAvailable: Boolean,
    contentDescription: String?,
    onZoomedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()
    val reduceMotion = RyggTheme.reduceMotion

    fun clamp(candidate: Offset, atScale: Float): Offset {
        val maxX = size.width * (atScale - 1f) / 2f
        val maxY = size.height * (atScale - 1f) / 2f
        return Offset(candidate.x.coerceIn(-maxX, maxX), candidate.y.coerceIn(-maxY, maxY))
    }

    LaunchedEffect(scale > 1f) { onZoomedChange(scale > 1f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tap ->
                        val targetScale = if (scale > 1f) 1f else DOUBLE_TAP_SCALE
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val targetOffset = clamp((center - tap) * (targetScale - 1f), targetScale)
                        if (reduceMotion) {
                            scale = targetScale
                            offset = targetOffset
                        } else {
                            val startScale = scale
                            val startOffset = offset
                            scope.launch {
                                animate(0f, 1f) { fraction, _ ->
                                    scale = startScale + (targetScale - startScale) * fraction
                                    offset = startOffset + (targetOffset - startOffset) * fraction
                                }
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pinching = event.changes.count { it.pressed } > 1
                        if (pinching || scale > 1f) {
                            val newScale = (scale * event.calculateZoom()).coerceIn(1f, MAX_SCALE)
                            offset = if (newScale == 1f) Offset.Zero else clamp(offset + event.calculatePan(), newScale)
                            scale = newScale
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        RyggAsyncImage(
            uri = uri,
            isAvailable = isAvailable,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

private const val MAX_SCALE = 5f
private const val DOUBLE_TAP_SCALE = 2.5f
