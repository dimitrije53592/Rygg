package com.example.rygg.feature.record.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
internal fun GpsStatusChip(
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RyggShapes.medium)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding4
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.commonSpacing8)
                .clip(CircleShape)
                .background(
                    RyggTheme.getColor(if (isReady) RyggColor.BrandGreen else RyggColor.TextSecondary)
                )
        )
        Text(
            text = stringResource(if (isReady) R.string.record_gps_ready else R.string.record_gps_waiting),
            style = RyggTheme.typography.labelMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}

@Composable
internal fun LocationPausedBanner(
    onTurnOn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RyggShapes.large)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(
                start = RyggTheme.dimens.commonContentPadding12,
                end = RyggTheme.dimens.commonContentPadding4,
                top = RyggTheme.dimens.commonContentPadding4,
                bottom = RyggTheme.dimens.commonContentPadding4
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOff,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.Error),
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
        Text(
            text = stringResource(R.string.record_location_paused),
            style = RyggTheme.typography.labelMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onTurnOn) {
            Text(
                text = stringResource(R.string.location_turn_on),
                style = RyggTheme.typography.labelLarge,
                color = RyggTheme.getColor(RyggColor.BrandGreen)
            )
        }
    }
}

@Preview
@Composable
private fun GpsStatusChipPreview() {
    RyggTheme {
        GpsStatusChip(isReady = false)
    }
}

@Preview
@Composable
private fun LocationPausedBannerPreview() {
    RyggTheme {
        LocationPausedBanner(onTurnOn = {})
    }
}
