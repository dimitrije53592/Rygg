package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatPointCount
import com.example.rygg.feature.library.domain.GpxFileEntry

@Composable
fun DetailsContentsCard(
    entry: GpxFileEntry,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RyggTheme.dimens.radius16))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(horizontal = RyggTheme.dimens.commonContentPadding16)
    ) {
        ContentRow(
            icon = Icons.Default.Timeline,
            title = stringResource(R.string.details_main_track),
            subtitle = stringResource(
                R.string.details_track_summary,
                formatDistanceKm(entry.distanceMeters),
                formatPointCount(entry.pointCount)
            )
        )
        if (entry.waypointCount > 0) {
            ContentRow(
                icon = Icons.Default.Place,
                title = stringResource(R.string.details_waypoints),
                subtitle = stringResource(R.string.details_waypoint_count, entry.waypointCount)
            )
        }
    }
}

@Composable
private fun ContentRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = RyggTheme.dimens.commonContentPadding12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.iconSize32)
                .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
                .background(RyggTheme.getColor(RyggColor.MossSurface)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.BrandGreen),
                modifier = Modifier.size(RyggTheme.dimens.iconSize16)
            )
        }
        Column {
            Text(
                text = title,
                style = RyggTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            Text(
                text = subtitle,
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }
    }
}
