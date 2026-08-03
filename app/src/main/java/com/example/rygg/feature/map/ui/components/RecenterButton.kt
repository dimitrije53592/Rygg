package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
internal fun RecenterButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        contentColor = RyggTheme.getColor(RyggColor.BrandGreen)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = RyggTheme.getColor(RyggColor.BrandGreen),
                strokeWidth = RyggTheme.dimens.border2,
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        } else {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(R.string.map_recenter)
            )
        }
    }
}
