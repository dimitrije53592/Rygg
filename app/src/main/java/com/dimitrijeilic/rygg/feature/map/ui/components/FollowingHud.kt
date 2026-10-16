package com.dimitrijeilic.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WrongLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStatSize
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.formatPercent
import com.dimitrijeilic.rygg.core.units.Quantity
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.map.ui.util.absorbTouches

@Composable
internal fun FollowingHud(
    fractionComplete: () -> Float,
    distanceRemainingMeters: Double,
    speed: Quantity,
    elevation: Quantity,
    offRoute: Boolean,
    isPreview: Boolean,
    modifier: Modifier = Modifier
) {
    val units = rememberUnitFormatter()
    val percentText by remember {
        derivedStateOf { formatPercent(fractionComplete().toDouble()) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RyggShapes.sheetTop)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .absorbTouches()
            .padding(RyggTheme.dimens.commonContentPadding20),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        if (offRoute) {
            Badge(
                text = stringResource(R.string.follow_off_route),
                icon = Icons.Outlined.WrongLocation,
                color = RyggTheme.getColor(RyggColor.Error)
            )
        } else if (isPreview) {
            Badge(
                text = stringResource(R.string.follow_preview_badge),
                icon = Icons.Outlined.Visibility,
                color = RyggTheme.getColor(RyggColor.BrandGreen)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            RyggStat(
                quantity = units.distanceParts(distanceRemainingMeters),
                label = stringResource(R.string.follow_distance_left),
                size = RyggStatSize.Hero
            )
            Text(
                text = percentText,
                style = RyggTheme.textStyles.statValue,
                color = RyggTheme.getColor(RyggColor.BrandGreen),
                modifier = Modifier.padding(bottom = RyggTheme.dimens.commonSpacing20)
            )
        }

        LinearProgressIndicator(
            progress = fractionComplete,
            color = RyggTheme.getColor(RyggColor.BrandGreen),
            trackColor = RyggTheme.getColor(RyggColor.Outline),
            strokeCap = StrokeCap.Round,
            gapSize = RyggTheme.dimens.zeroPadding,
            drawStopIndicator = {},
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
        ) {
            RyggStat(
                quantity = speed,
                label = stringResource(R.string.follow_speed),
                modifier = Modifier.weight(1f)
            )
            RyggStat(
                quantity = elevation,
                label = stringResource(R.string.follow_elevation),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Badge(
    text: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = BADGE_ALPHA))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding4
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
        Text(
            text = text.uppercase(),
            style = RyggTheme.textStyles.trackedLabel,
            color = color
        )
    }
}

private const val BADGE_ALPHA = 0.14f
