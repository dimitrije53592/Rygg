package com.dimitrijeilic.rygg.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggPageDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    color: Color = RyggTheme.getColor(RyggColor.OnScrim)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            val dotColor by animateColorAsState(
                targetValue = if (page == currentPage) color else color.copy(alpha = INACTIVE_ALPHA),
                label = "pageDot"
            )
            Box(
                modifier = Modifier
                    .size(RyggTheme.dimens.bulletSize6)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun RyggPageDotsPreview() {
    RyggTheme {
        RyggPageDots(pageCount = 4, currentPage = 1)
    }
}

private const val INACTIVE_ALPHA = 0.45f
