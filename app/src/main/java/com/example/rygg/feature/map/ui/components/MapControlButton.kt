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

private const val GLASS_ALPHA = 0.84f
private const val EDGE_ALPHA = 0.22f

// Map chrome floats over the basemap, not over an app surface, so it is styled against the map
// rather than the theme: a translucent disc with a hairline edge and a real shadow, which separates
// it from both pale terrain and dark forest.
//
// Deliberately no Modifier.blur: that blurs a composable's own drawing, not what is behind it, so
// it would only soften the icon. Compose has no backdrop blur without compositing the map itself
// into a layer, which is not worth the cost here.
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
