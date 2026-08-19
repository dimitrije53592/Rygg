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
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatAscent
import com.example.rygg.core.ui.utils.formatDate
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatPointCount
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.ui.paramproviders.GpxFileEntryProvider

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
    if (entry.elevationMeters != null) add(formatAscent(entry.ascentMeters))
    add(formatPointCount(entry.pointCount))
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
