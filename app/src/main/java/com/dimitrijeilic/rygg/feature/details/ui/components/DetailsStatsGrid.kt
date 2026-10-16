package com.dimitrijeilic.rygg.feature.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggCard
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStatSize
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.formatDurationHoursMinutes
import com.dimitrijeilic.rygg.core.ui.utils.formatPointCount
import com.dimitrijeilic.rygg.core.units.Quantity
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry

// The three headline numbers lead, larger; the other six always follow in two full rows, with a
// dash where the file has no data, so the grid keeps the same shape for every route.
@Composable
fun DetailsStatsGrid(
    entry: GpxFileEntry,
    modifier: Modifier = Modifier
) {
    val units = rememberUnitFormatter()
    val empty = Quantity(stringResource(R.string.follow_stat_empty))

    val headline = listOf(
        DetailsStat(stringResource(R.string.details_stat_distance), units.distanceParts(entry.distanceMeters)),
        DetailsStat(
            stringResource(R.string.details_stat_moving_time),
            entry.movingTimeMillis?.let { Quantity(formatDurationHoursMinutes(it)) } ?: empty
        ),
        DetailsStat(stringResource(R.string.details_stat_ascent), units.elevationParts(entry.ascentMeters))
    )

    val secondary = listOf(
        DetailsStat(stringResource(R.string.details_stat_descent), units.elevationParts(entry.descentMeters)),
        DetailsStat(
            stringResource(R.string.details_stat_high_point),
            entry.elevationMeters?.let { units.elevationParts(it) } ?: empty
        ),
        DetailsStat(
            stringResource(R.string.details_stat_total_time),
            entry.totalTimeMillis?.let { Quantity(formatDurationHoursMinutes(it)) } ?: empty
        ),
        DetailsStat(
            stringResource(R.string.details_stat_avg_speed),
            entry.avgSpeedMps?.let { units.speedParts(it) } ?: empty
        ),
        DetailsStat(
            stringResource(R.string.details_stat_max_speed),
            entry.maxSpeedMps?.let { units.speedParts(it) } ?: empty
        ),
        DetailsStat(stringResource(R.string.details_stat_points), Quantity(formatPointCount(entry.pointCount)))
    )

    RyggCard(modifier = modifier) {
        StatRow(stats = headline, size = RyggStatSize.Large)
        HorizontalDivider(
            color = RyggTheme.getColor(RyggColor.Outline),
            modifier = Modifier.padding(vertical = RyggTheme.dimens.commonContentPadding16)
        )
        Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)) {
            secondary.chunked(COLUMNS).forEach { row ->
                StatRow(stats = row, size = RyggStatSize.Small)
            }
        }
    }
}

@Composable
private fun StatRow(
    stats: List<DetailsStat>,
    size: RyggStatSize
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        stats.forEach { stat ->
            RyggStat(
                quantity = stat.quantity,
                label = stat.label,
                size = size,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class DetailsStat(
    val label: String,
    val quantity: Quantity
)

private const val COLUMNS = 3
