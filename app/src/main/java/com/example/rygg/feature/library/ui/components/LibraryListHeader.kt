package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.library.domain.SortMode

@Composable
internal fun LibraryListHeader(
    activityCount: Int,
    sortMode: SortMode,
    favoritesOnly: Boolean,
    onToggleSort: () -> Unit,
    onToggleFavoritesFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding20,
                vertical = RyggTheme.dimens.commonContentPadding8
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = pluralStringResource(R.plurals.library_activities, activityCount, activityCount),
            style = RyggTheme.typography.titleSmall,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            SortToggle(sortMode = sortMode, onClick = onToggleSort)
            FavoritesFilterToggle(active = favoritesOnly, onClick = onToggleFavoritesFilter)
        }
    }
}

@Composable
private fun SortToggle(
    sortMode: SortMode,
    onClick: () -> Unit
) {
    val isName = sortMode == SortMode.NAME
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
            .clickable { onClick() }
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding8,
                vertical = RyggTheme.dimens.commonContentPadding4
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Icon(
            imageVector = if (isName) Icons.Default.SortByAlpha else Icons.Default.Schedule,
            contentDescription = stringResource(R.string.library_sort),
            tint = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
        Text(
            text = if (isName) {
                stringResource(R.string.library_sort_name)
            } else {
                stringResource(R.string.library_sort_date)
            },
            style = RyggTheme.typography.labelLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}

@Composable
private fun FavoritesFilterToggle(
    active: Boolean,
    onClick: () -> Unit
) {
    Icon(
        imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
        contentDescription = stringResource(R.string.library_favorites_filter),
        tint = if (active) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        modifier = Modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
            .clickable { onClick() }
            .padding(RyggTheme.dimens.commonContentPadding4)
            .size(RyggTheme.dimens.iconSize24)
    )
}
