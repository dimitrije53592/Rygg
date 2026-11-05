package com.dimitrijeilic.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun OfflineStatusChip(
    isCovered: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RyggShapes.chip)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated).copy(alpha = CHIP_BACKGROUND_ALPHA))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding8
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = RyggTheme.getColor(if (isCovered) RyggColor.TextSecondary else RyggColor.Warning),
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
        Text(
            text = stringResource(if (isCovered) R.string.map_offline_covered else R.string.map_offline_not_covered),
            style = RyggTheme.typography.labelMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}

private const val CHIP_BACKGROUND_ALPHA = 0.9f

@Preview
@Composable
private fun OfflineStatusChipPreview() {
    RyggTheme {
        OfflineStatusChip(isCovered = false)
    }
}
