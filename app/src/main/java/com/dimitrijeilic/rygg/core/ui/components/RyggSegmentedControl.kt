package com.dimitrijeilic.rygg.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggElevation
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.concentricTo
import com.dimitrijeilic.rygg.core.ui.theme.ryggElevation
import com.dimitrijeilic.rygg.core.ui.utils.rememberRyggHaptics

// For two to four mutually exclusive, short options: every choice stays visible, and the
// selection slides between them instead of jumping.
@Composable
fun <T> RyggSegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: ((T) -> Int)? = null
) {
    val haptics = rememberRyggHaptics()
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val gap = RyggTheme.dimens.commonContentPadding4
    val height = if (icon != null) RyggTheme.dimens.segmentedHeightTall else RyggTheme.dimens.segmentedHeight

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RyggShapes.large)
            .background(RyggTheme.getColor(RyggColor.Surface))
            .padding(gap)
    ) {
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = RyggMotion.spatialFast(),
            label = "segmentIndicator"
        )
        val segmentShape = concentricTo(RyggTheme.dimens.radius16, gap)

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .ryggElevation(level = RyggElevation.Card, shape = segmentShape)
                .clip(segmentShape)
                .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
        )

        Row(modifier = Modifier.fillMaxWidth().selectableGroup()) {
            options.forEachIndexed { index, option ->
                Segment(
                    text = label(option),
                    iconRes = icon?.invoke(option),
                    selected = index == selectedIndex,
                    onClick = {
                        if (index != selectedIndex) {
                            haptics.tick()
                            onSelect(option)
                        }
                    },
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun Segment(
    text: String,
    @DrawableRes iconRes: Int?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val content by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.TextPrimary)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "segmentContent"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "segmentIcon"
    )

    Column(
        modifier = modifier
            .clip(RyggShapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            }
            .pressScale(interactionSource)
            .padding(horizontal = RyggTheme.dimens.commonContentPadding4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4, Alignment.CenterVertically)
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
        Text(
            text = text,
            style = RyggTheme.typography.labelLarge,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RyggSegmentedControlPreview() {
    RyggTheme {
        RyggSegmentedControl(
            options = listOf("System", "Light", "Dark"),
            selected = "Light",
            onSelect = {},
            label = { it },
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16)
        )
    }
}
