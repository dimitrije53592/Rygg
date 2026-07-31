package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.rygg.core.ui.utils.formatPercent
import com.example.rygg.feature.map.ui.util.absorbTouches

@Composable
internal fun FollowingHud(
    fractionComplete: Double,
    distanceRemainingMeters: Double,
    speedText: String,
    elevationText: String,
    offRoute: Boolean,
    isPreview: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
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
            .padding(RyggTheme.dimens.commonContentPadding20),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        if (offRoute) {
            BadgeRow(
                text = stringResource(R.string.follow_off_route),
                color = RyggTheme.getColor(RyggColor.Error)
            )
        } else if (isPreview) {
            BadgeRow(
                text = stringResource(R.string.follow_preview_badge),
                color = RyggTheme.getColor(RyggColor.BrandGreen)
            )
        }

        LinearProgressIndicator(
            progress = { fractionComplete.toFloat() },
            color = RyggTheme.getColor(RyggColor.BrandGreen),
            trackColor = RyggTheme.getColor(RyggColor.SurfaceDim),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = stringResource(R.string.follow_distance_left),
                    style = RyggTheme.typography.labelSmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary)
                )
                Text(
                    text = formatDistanceKm(distanceRemainingMeters),
                    style = RyggTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = RyggTheme.getColor(RyggColor.TextPrimary)
                )
            }
            Text(
                text = formatPercent(fractionComplete),
                style = RyggTheme.typography.titleMedium,
                color = RyggTheme.getColor(RyggColor.BrandGreen)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            StatCell(
                label = stringResource(R.string.follow_speed),
                value = speedText,
                modifier = Modifier.weight(1f)
            )
            StatCell(
                label = stringResource(R.string.follow_elevation),
                value = elevationText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
            .background(RyggTheme.getColor(RyggColor.SurfaceDim))
            .padding(RyggTheme.dimens.commonContentPadding12)
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

@Composable
private fun BadgeRow(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
        Text(
            text = text,
            style = RyggTheme.typography.labelMedium,
            color = color
        )
    }
}
