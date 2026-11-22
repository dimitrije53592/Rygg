package com.dimitrijeilic.rygg.feature.offline.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun DownloadFrameOverlay(
    inset: Dp,
    isTooLarge: Boolean,
    modifier: Modifier = Modifier
) {
    val frameColor = RyggTheme.getColor(if (isTooLarge) RyggColor.Error else RyggColor.BrandGreen)
    val scrimColor = RyggTheme.getColor(RyggColor.ScrimDark).copy(alpha = SCRIM_ALPHA)
    val radius = RyggTheme.dimens.radius16
    val stroke = RyggTheme.dimens.border2

    Canvas(modifier = modifier.fillMaxSize()) {
        val insetPx = inset.toPx()
        val radiusPx = radius.toPx()
        val frameSize = Size(size.width - insetPx * 2, size.height - insetPx * 2)
        val frame = Path().apply {
            addRoundRect(
                RoundRect(
                    left = insetPx,
                    top = insetPx,
                    right = insetPx + frameSize.width,
                    bottom = insetPx + frameSize.height,
                    cornerRadius = CornerRadius(radiusPx)
                )
            )
        }
        clipPath(frame, clipOp = ClipOp.Difference) {
            drawRect(scrimColor)
        }
        drawRoundRect(
            color = frameColor,
            topLeft = Offset(insetPx, insetPx),
            size = frameSize,
            cornerRadius = CornerRadius(radiusPx),
            style = Stroke(width = stroke.toPx())
        )
    }
}

private const val SCRIM_ALPHA = 0.35f

@Preview
@Composable
private fun DownloadFrameOverlayPreview() {
    RyggTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) { drawRect(Color.LightGray) }
            DownloadFrameOverlay(inset = RyggTheme.dimens.commonContentPadding24, isTooLarge = false)
        }
    }
}
