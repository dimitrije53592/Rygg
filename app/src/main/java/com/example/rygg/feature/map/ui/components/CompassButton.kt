package com.example.rygg.feature.map.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.FloatingActionButton
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
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        contentColor = RyggTheme.getColor(RyggColor.BrandGreen)
    ) {
        Icon(
            imageVector = Icons.Default.Explore,
            contentDescription = stringResource(R.string.map_compass)
        )
    }
}
