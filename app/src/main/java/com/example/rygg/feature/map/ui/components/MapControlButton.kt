package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.rygg.core.ui.components.pressScale
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

// Styled against the basemap rather than the theme. Deliberately no Modifier.blur: it blurs the
// composable's own drawing, not the map behind it, so it would only soften the icon.
@Composable
internal fun MapControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val surface = RyggTheme.getColor(RyggColor.SurfaceElevated)
    val edge = RyggTheme.getColor(RyggColor.OnBrand)

    Box(
        modifier = modifier
            .size(RyggTheme.dimens.iconSize48)
            .pressScale(interactionSource)
            .shadow(elevation = RyggTheme.dimens.elevation8, shape = CircleShape)
            .clip(CircleShape)
            .background(surface.copy(alpha = GLASS_ALPHA))
            .border(
                width = RyggTheme.dimens.border1,
                color = edge.copy(alpha = EDGE_ALPHA),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private const val GLASS_ALPHA = 0.84f
private const val EDGE_ALPHA = 0.22f
