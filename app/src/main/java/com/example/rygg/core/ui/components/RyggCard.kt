package com.example.rygg.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggElevation
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ryggElevation

@Composable
fun RyggCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: RyggElevation = RyggElevation.Card,
    contentPadding: Dp = RyggTheme.dimens.commonContentPadding16,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RyggShapes.card
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScale(interactionSource) else Modifier)
            .ryggElevation(level = elevation, shape = shape)
            .clip(shape)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            // Dark mode has no visible shadow, so the hairline carries the card's edge there.
            .then(
                if (RyggTheme.isDarkMode) {
                    Modifier.border(
                        width = RyggTheme.dimens.border1,
                        color = RyggTheme.getColor(RyggColor.Outline),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content
    )
}
