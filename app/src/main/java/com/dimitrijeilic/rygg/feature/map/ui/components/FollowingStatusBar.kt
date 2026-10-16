package com.dimitrijeilic.rygg.feature.map.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WrongLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStatSize
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.rememberRyggHaptics
import com.dimitrijeilic.rygg.core.units.Quantity
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.map.ui.util.absorbTouches

// Leaving the route is the one moment this bar must interrupt: a full-width error band, announced
// to screen readers and felt as a haptic, rather than a small change of label colour.
@Composable
internal fun FollowingStatusBar(
    isOnRoute: Boolean,
    distanceToRouteMeters: Double,
    speed: Quantity,
    elevation: Quantity,
    modifier: Modifier = Modifier
) {
    val haptics = rememberRyggHaptics()
    var wasOnRoute by remember { mutableStateOf(isOnRoute) }
    LaunchedEffect(isOnRoute) {
        if (wasOnRoute && !isOnRoute) haptics.reject()
        wasOnRoute = isOnRoute
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RyggShapes.sheetTop)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .absorbTouches()
    ) {
        AnimatedVisibility(
            visible = !isOnRoute,
            enter = expandVertically(RyggMotion.spatial()) + fadeIn(RyggMotion.effects()),
            exit = shrinkVertically(RyggMotion.spatialFast()) + fadeOut(RyggMotion.effectsFast())
        ) {
            OffRouteBand(distanceToRouteMeters = distanceToRouteMeters)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = RyggTheme.dimens.commonContentPadding20,
                    vertical = RyggTheme.dimens.commonContentPadding16
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOnRoute) {
                OnRouteIndicator()
            } else {
                Box(Modifier)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)) {
                RyggStat(
                    quantity = elevation,
                    label = stringResource(R.string.follow_elevation),
                    size = RyggStatSize.Medium
                )
                RyggStat(
                    quantity = speed,
                    label = stringResource(R.string.follow_speed),
                    size = RyggStatSize.Medium
                )
            }
        }
    }
}

@Composable
private fun OffRouteBand(distanceToRouteMeters: Double) {
    val units = rememberUnitFormatter()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RyggTheme.getColor(RyggColor.ErrorSurface))
            .semantics { liveRegion = LiveRegionMode.Assertive }
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding20,
                vertical = RyggTheme.dimens.commonContentPadding12
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Icon(
            imageVector = Icons.Outlined.WrongLocation,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.Error),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
        Text(
            text = stringResource(R.string.follow_off_route_distance, units.distance(distanceToRouteMeters)),
            style = RyggTheme.typography.titleSmall,
            color = RyggTheme.getColor(RyggColor.Error)
        )
    }
}

@Composable
private fun OnRouteIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.statusDotSize10)
                .clip(CircleShape)
                .background(RyggTheme.getColor(RyggColor.BrandGreen))
        )
        Text(
            text = stringResource(R.string.follow_on_route),
            style = RyggTheme.typography.labelLarge,
            color = RyggTheme.getColor(RyggColor.BrandGreen)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FollowingStatusBarOffRoutePreview() {
    RyggTheme {
        FollowingStatusBar(
            isOnRoute = false,
            distanceToRouteMeters = 120.0,
            speed = Quantity("4.2", "km/h"),
            elevation = Quantity("1,820", "m")
        )
    }
}
