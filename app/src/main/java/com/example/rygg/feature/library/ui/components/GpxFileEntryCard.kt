package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.rygg.R
import com.example.rygg.core.ui.components.RouteCanvas
import com.example.rygg.core.ui.components.RouteCanvasScale
import com.example.rygg.core.ui.components.pressScale
import com.example.rygg.core.ui.components.sharedRouteCanvas
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggElevation
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ryggElevation
import com.example.rygg.core.ui.utils.formatAscent
import com.example.rygg.core.ui.utils.formatDate
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatDurationHoursMinutes
import com.example.rygg.core.ui.utils.formatPointCount
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.EntrySource
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.domain.SyncStatus
import com.example.rygg.feature.library.ui.paramproviders.GpxFileEntryProvider

// 16:9 keeps the route shape legible while still fitting about two and a half cards on screen.
// Taller crops look better in isolation but cost scanning speed, which in this category reads as a
// regression rather than a refinement.
private const val CARD_ASPECT = 16f / 9f
private const val SCRIM_START = 0.45f
private const val SCRIM_ALPHA = 0.78f
private const val GLASS_ALPHA = 0.32f

@Composable
fun GpxFileEntryCard(
    entry: GpxFileEntry,
    onClick: (GpxFileEntry) -> Unit,
    onFavoriteClick: (GpxFileEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RyggShapes.card
    val interactionSource = remember { MutableInteractionSource() }
    val scrim = RyggTheme.getColor(RyggColor.ScrimDark)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .ryggElevation(level = RyggElevation.Card, shape = shape)
            .clip(shape)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .then(
                if (RyggTheme.isDarkMode) {
                    Modifier.border(
                        width = RyggTheme.dimens.border1,
                        color = RyggTheme.getColor(RyggColor.Outline),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(entry) }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT)
        ) {
            RouteCanvas(
                points = entry.pathPoints,
                scale = RouteCanvasScale.Card,
                modifier = Modifier
                    .fillMaxSize()
                    .sharedRouteCanvas(entry.id)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            SCRIM_START to Color.Transparent,
                            1f to scrim.copy(alpha = SCRIM_ALPHA)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RyggTheme.dimens.commonContentPadding8),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                DisciplineBadge(discipline = entry.discipline)
                FavoriteStar(
                    favorite = entry.isFavorite,
                    onClick = { onFavoriteClick(entry) }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(RyggTheme.dimens.commonContentPadding12)
            ) {
                Text(
                    text = entry.name,
                    style = RyggTheme.typography.titleLarge,
                    color = RyggTheme.getColor(RyggColor.OnScrim),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
                ) {
                    Icon(
                        imageVector = sourceIcon(entry.source),
                        contentDescription = null,
                        tint = RyggTheme.getColor(RyggColor.OnScrim),
                        modifier = Modifier.size(RyggTheme.dimens.iconSize16)
                    )
                    Text(
                        text = subtitle(entry),
                        style = RyggTheme.typography.bodySmall,
                        color = RyggTheme.getColor(RyggColor.OnScrim)
                    )
                    SyncBadge(entry)
                }
            }
        }

        StatStrip(entry)
    }
}

@Composable
private fun StatStrip(entry: GpxFileEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding16,
                vertical = RyggTheme.dimens.commonContentPadding12
            ),
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
    ) {
        if (entry.distanceMeters > 0.0) {
            StatCell(
                label = stringResource(R.string.details_stat_distance),
                value = formatDistanceKm(entry.distanceMeters)
            )
        }
        if (entry.elevationMeters != null) {
            StatCell(
                label = stringResource(R.string.details_stat_ascent),
                value = formatAscent(entry.ascentMeters)
            )
        }
        val movingTime = entry.movingTimeMillis
        if (movingTime != null) {
            StatCell(
                label = stringResource(R.string.details_stat_moving_time),
                value = formatDurationHoursMinutes(movingTime)
            )
        } else {
            StatCell(
                label = stringResource(R.string.details_stat_points),
                value = formatPointCount(entry.pointCount)
            )
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)) {
        Text(
            text = value,
            style = RyggTheme.textStyles.statValue,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
        Text(
            text = label.uppercase(),
            style = RyggTheme.textStyles.trackedLabel,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
    }
}

@Composable
private fun DisciplineBadge(
    discipline: Discipline,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(RyggTheme.dimens.iconSize32)
            .clip(RyggShapes.chip)
            .background(RyggTheme.getColor(RyggColor.ScrimDark).copy(alpha = GLASS_ALPHA)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(discipline.iconRes),
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.OnScrim),
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
    }
}

@Composable
private fun SyncBadge(entry: GpxFileEntry) {
    val icon = when {
        !entry.fileDownloaded -> Icons.Default.CloudDownload
        entry.syncStatus == SyncStatus.SYNCED -> Icons.Default.CloudDone
        entry.syncStatus == SyncStatus.PENDING_UPLOAD -> Icons.Default.CloudUpload
        else -> return
    }
    val description = when {
        !entry.fileDownloaded -> stringResource(R.string.sync_status_download_pending)
        entry.syncStatus == SyncStatus.SYNCED -> stringResource(R.string.sync_status_synced)
        else -> stringResource(R.string.sync_status_pending)
    }
    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = RyggTheme.getColor(RyggColor.OnScrim),
        modifier = Modifier.size(RyggTheme.dimens.iconSize16)
    )
}

// The glyph stays 24dp but the target is a full 48dp, which it was not before.
@Composable
private fun FavoriteStar(
    favorite: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.iconSize48)
            .clip(RyggShapes.chip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = stringResource(
                if (favorite) R.string.details_unfavorite else R.string.details_favorite
            ),
            tint = if (favorite) {
                RyggTheme.getColor(RyggColor.AccentBright)
            } else {
                RyggTheme.getColor(RyggColor.OnScrim)
            },
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
    }
}

@Composable
private fun subtitle(entry: GpxFileEntry): String =
    if (entry.hasTime && entry.startTimeMillis != null) {
        formatDate(entry.startTimeMillis)
    } else {
        stringResource(
            when (entry.source) {
                EntrySource.IMPORTED -> R.string.details_source_imported
                EntrySource.RECORDED -> R.string.details_source_recorded
            }
        )
    }

private fun sourceIcon(source: EntrySource): ImageVector =
    when (source) {
        EntrySource.IMPORTED -> Icons.Default.FileDownload
        EntrySource.RECORDED -> Icons.Default.FiberManualRecord
    }

@Preview(showBackground = true)
@Composable
private fun GpxFileEntryCardPreview(
    @PreviewParameter(GpxFileEntryProvider::class) entry: GpxFileEntry
) {
    RyggTheme {
        GpxFileEntryCard(
            entry = entry,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16)
        )
    }
}
