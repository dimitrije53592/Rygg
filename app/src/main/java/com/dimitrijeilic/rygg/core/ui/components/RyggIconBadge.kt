package com.dimitrijeilic.rygg.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

// Leading glyph for list rows: a small moss squircle, so rows scan by icon before text.
@Composable
fun RyggIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = RyggTheme.getColor(RyggColor.BrandGreen),
    containerColor: Color = RyggTheme.getColor(RyggColor.MossSurface)
) {
    Box(
        modifier = modifier
            .size(RyggTheme.dimens.iconSize32)
            .clip(RyggShapes.small)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(RyggTheme.dimens.iconSize18)
        )
    }
}
