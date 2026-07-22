package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatDate
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatElevationDelta
import com.example.rygg.core.ui.utils.formatPointCount
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.GpxFileEntry

@Composable
fun GpxFileEntryCard(
    entry: GpxFileEntry,
    onClick: (GpxFileEntry) -> Unit,
    onFavoriteClick: (GpxFileEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RyggTheme.dimens.radius16))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .clickable { onClick(entry) }
            .padding(RyggTheme.dimens.commonContentPadding12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        Box(modifier = Modifier.size(RyggTheme.dimens.thumbnailSize66)) {
            TrailThumbnail(
                points = entry.pathPoints,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
            )
            DisciplineBadge(
                discipline = entry.discipline,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(RyggTheme.dimens.commonContentPadding4)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name,
                style = RyggTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
            Text(
                text = subtitle(entry),
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing8))
            Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)) {
                statsOf(entry).forEach { stat ->
                    Text(
                        text = stat,
                        style = RyggTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = RyggTheme.getColor(RyggColor.TextPrimary)
                    )
                }
            }
        }
        FavoriteStar(favorite = entry.isFavorite, onClick = { onFavoriteClick(entry) })
    }
}

@Composable
private fun DisciplineBadge(
    discipline: Discipline,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(RyggTheme.dimens.badgeSize24)
            .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
            .background(RyggTheme.getColor(RyggColor.BrandGraphite)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(discipline.iconRes),
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.OnBrand),
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
    }
}

@Composable
private fun FavoriteStar(
    favorite: Boolean,
    onClick: () -> Unit
) {
    Icon(
        imageVector = if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
        contentDescription = null,
        tint = if (favorite) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.MutedGray)
        },
        modifier = Modifier
            .size(RyggTheme.dimens.iconSize24)
            .clickable { onClick() }
    )
}

@Composable
private fun subtitle(entry: GpxFileEntry): String =
    if (entry.hasTime && entry.startTimeMillis != null) {
        formatDate(entry.startTimeMillis)
    } else {
        stringResource(R.string.library_imported)
    }

private fun statsOf(entry: GpxFileEntry): List<String> = buildList {
    if (entry.distanceMeters > 0.0) add(formatDistanceKm(entry.distanceMeters))
    if (entry.elevationMeters != null) add(formatElevationDelta(entry.ascentMeters, entry.descentMeters))
    add(formatPointCount(entry.pointCount))
}

@Preview(showBackground = true)
@Composable
private fun GpxFileEntryCardPreview() {
    RyggTheme {
        Column(
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            GpxFileEntryCard(entry = previewEntry(recorded = true), onClick = {}, onFavoriteClick = {})
            GpxFileEntryCard(entry = previewEntry(recorded = false), onClick = {}, onFavoriteClick = {})
        }
    }
}

private fun previewEntry(recorded: Boolean): GpxFileEntry = GpxFileEntry(
    id = if (recorded) 1L else 2L,
    fileName = "sample.gpx",
    contentHash = "",
    name = if (recorded) "Mali i Veliki Vukan" else "Seven Lakes valley",
    description = "",
    color = null,
    discipline = if (recorded) Discipline.HIKE else Discipline.RIDE,
    isFavorite = recorded,
    distanceMeters = if (recorded) 12_400.0 else 15_200.0,
    ascentMeters = if (recorded) 1_180.0 else 920.0,
    descentMeters = if (recorded) 1_100.0 else 40.0,
    elevationMeters = 837.0,
    pointCount = if (recorded) 1_240 else 512,
    routeCount = 1,
    waypointCount = 8,
    hasTime = recorded,
    startTimeMillis = if (recorded) 1_498_500_000_000 else null,
    movingTimeMillis = if (recorded) 20_400_000 else null,
    totalTimeMillis = if (recorded) 21_000_000 else null,
    minLat = 44.28,
    minLon = 21.50,
    maxLat = 44.31,
    maxLon = 21.54,
    pathPoints = listOf(
        GeoPoint(44.306, 21.503),
        GeoPoint(44.300, 21.510),
        GeoPoint(44.292, 21.520),
        GeoPoint(44.299, 21.534),
        GeoPoint(44.288, 21.537)
    ),
    folder = null,
    tags = emptyList(),
    importedAt = 1_497_500_000_000,
    updatedAt = 1_497_500_000_000,
    creator = "MapSource",
    originalFileName = "sample.gpx"
)
