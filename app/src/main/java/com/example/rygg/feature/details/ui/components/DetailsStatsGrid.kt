package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatDurationHoursMinutes
import com.example.rygg.core.ui.utils.formatElevationMeters
import com.example.rygg.core.ui.utils.formatPointCount
import com.example.rygg.feature.library.domain.GpxFileEntry

@Composable
fun DetailsStatsGrid(
    entry: GpxFileEntry,
    modifier: Modifier = Modifier
) {
    val emptyValue = stringResource(R.string.follow_stat_empty)
    val stats = listOf(
        // Row 1: general
        DetailsStat(stringResource(R.string.details_stat_distance), formatDistanceKm(entry.distanceMeters)),
        DetailsStat(
            stringResource(R.string.details_stat_moving_time),
            entry.movingTimeMillis?.let { formatDurationHoursMinutes(it) } ?: emptyValue
        ),
        DetailsStat(stringResource(R.string.details_stat_points), formatPointCount(entry.pointCount)),
        // Row 2: elevation
        DetailsStat(stringResource(R.string.details_stat_ascent), formatElevationMeters(entry.ascentMeters)),
        DetailsStat(stringResource(R.string.details_stat_descent), formatElevationMeters(entry.descentMeters)),
        DetailsStat(
            stringResource(R.string.details_stat_high_point),
            entry.elevationMeters?.let { formatElevationMeters(it) } ?: emptyValue
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RyggTheme.dimens.radius16))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(RyggTheme.dimens.commonContentPadding4),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        stats.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { stat ->
                    StatCell(stat = stat, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCell(
    stat: DetailsStat,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(RyggTheme.dimens.commonContentPadding12),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Text(
            text = stat.value,
            style = RyggTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
        Text(
            text = stat.label.uppercase(),
            style = RyggTheme.typography.labelSmall,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
    }
}

private data class DetailsStat(
    val label: String,
    val value: String
)
