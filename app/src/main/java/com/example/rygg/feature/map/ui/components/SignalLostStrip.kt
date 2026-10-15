package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
internal fun SignalLostStrip(
    onTurnOn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
            text = stringResource(R.string.follow_signal_off),
            style = RyggTheme.typography.labelMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.weight(1f, fill = false)
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
private fun SignalLostStripPreview() {
    RyggTheme {
        SignalLostStrip(onTurnOn = {})
    }
}
