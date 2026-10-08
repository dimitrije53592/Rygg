package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
internal fun CompassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MapControlButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Explore,
            contentDescription = stringResource(R.string.map_compass),
            tint = RyggTheme.getColor(RyggColor.BrandGreen),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
    }
}
