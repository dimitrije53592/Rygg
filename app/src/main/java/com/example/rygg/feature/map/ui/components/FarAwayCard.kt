package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.feature.map.ui.util.absorbTouches

@Composable
fun FarAwayCard(
    distanceMeters: Double,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RyggTheme.dimens.radius24))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .absorbTouches()
            .padding(RyggTheme.dimens.commonContentPadding20),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        Text(
            text = stringResource(R.string.follow_far_title),
            style = RyggTheme.typography.titleMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
        Text(
            text = stringResource(R.string.follow_far_body, formatDistanceKm(distanceMeters)),
            style = RyggTheme.typography.bodyMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)) {
            Button(
                onClick = onPreview,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RyggTheme.getColor(RyggColor.BrandGreen),
                    contentColor = RyggTheme.getColor(RyggColor.OnBrand)
                )
            ) {
                Text(stringResource(R.string.follow_preview))
            }
        }
    }
}
