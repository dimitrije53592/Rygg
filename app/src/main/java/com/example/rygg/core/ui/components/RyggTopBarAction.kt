package com.example.rygg.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggTopBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBarActionSurface(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = RyggTheme.getColor(RyggColor.OnBrand),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
    }
}

@Composable
fun RyggTopBarAction(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBarActionSurface(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = RyggTheme.getColor(RyggColor.OnBrand),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
    }
}

@Composable
private fun TopBarActionSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val onBrand = RyggTheme.getColor(RyggColor.OnBrand)

    // The disc is 44dp; the touch target is a full 48dp.
    Box(
        modifier = modifier
            .size(RyggTheme.dimens.iconSize48)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.actionSize44)
                .pressScale(interactionSource)
                .clip(CircleShape)
                .background(onBrand.copy(alpha = DISC_ALPHA))
                .border(
                    width = RyggTheme.dimens.border1,
                    color = onBrand.copy(alpha = EDGE_ALPHA),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

private const val DISC_ALPHA = 0.10f
private const val EDGE_ALPHA = 0.18f
