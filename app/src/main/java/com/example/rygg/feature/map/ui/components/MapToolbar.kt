package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
internal fun MapToolbar(
    isLocationLoading: Boolean,
    modifier: Modifier,
    onCompassClick: () -> Unit,
    onRecenterClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        CompassButton(
            onClick = onCompassClick
        )
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing16))
        RecenterButton(
            isLoading = isLocationLoading,
            onClick = onRecenterClick,
            modifier = Modifier
        )
    }
}
