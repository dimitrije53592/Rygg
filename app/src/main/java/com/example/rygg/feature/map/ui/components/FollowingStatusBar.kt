package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.feature.map.ui.util.absorbTouches

@Composable
internal fun FollowingStatusBar(
    isOnRoute: Boolean,
    distanceToRouteMeters: Double,
    speedText: String,
    elevationText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = RyggTheme.dimens.radius24,
                    topEnd = RyggTheme.dimens.radius24
                )
            )
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .absorbTouches()
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding20,
                vertical = RyggTheme.dimens.commonContentPadding16
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusIndicator(
            isOnRoute = isOnRoute,
            distanceToRouteMeters = distanceToRouteMeters
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InlineStat(
                label = stringResource(R.string.follow_elevation),
                value = elevationText
            )
            InlineStat(
                label = stringResource(R.string.follow_speed),
                value = speedText
            )
        }
    }
}

@Composable
private fun StatusIndicator(
    isOnRoute: Boolean,
    distanceToRouteMeters: Double
) {
    val color = if (isOnRoute) {
        RyggTheme.getColor(RyggColor.BrandGreen)
    } else {
        RyggTheme.getColor(RyggColor.Error)
    }
    val text = if (isOnRoute) {
        stringResource(R.string.follow_on_route)
    } else {
        stringResource(R.string.follow_off_route_distance, formatDistanceKm(distanceToRouteMeters))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.statusDotSize10)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            style = RyggTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun InlineStat(
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Text(
            text = label,
            style = RyggTheme.typography.labelSmall,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        Text(
            text = value,
            style = RyggTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}
