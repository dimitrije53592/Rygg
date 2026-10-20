package com.dimitrijeilic.rygg.feature.library.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.dimitrijeilic.rygg.core.ui.components.pressScale
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun LibraryFilterChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes iconRes: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.Surface)
        },
        animationSpec = RyggMotion.effects(),
        label = "chipContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.OnBrand)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "chipContent"
    )

    Row(
        modifier = Modifier
            .pressScale(interactionSource)
            .clip(RyggShapes.chip)
            .background(container)
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        width = RyggTheme.dimens.border1,
                        color = RyggTheme.getColor(RyggColor.Outline),
                        shape = RyggShapes.chip
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .defaultMinSize(minHeight = RyggTheme.dimens.buttonSize40)
            .padding(horizontal = RyggTheme.dimens.commonContentPadding16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(iconRes),
                tint = content,
                contentDescription = null,
                modifier = Modifier.size(RyggTheme.dimens.iconSize16)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing8))
        }
        Text(
            text = title,
            color = content,
            style = RyggTheme.typography.labelLarge
        )
    }
}
