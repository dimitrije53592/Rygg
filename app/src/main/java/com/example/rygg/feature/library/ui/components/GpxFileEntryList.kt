package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.components.SwipeToDeleteBox
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggMotion
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatMonthYear
import com.example.rygg.core.ui.utils.monthKey
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.domain.SortMode

@Composable
internal fun GpxFileEntryList(
    entries: List<GpxFileEntry>,
    sortMode: SortMode,
    onEntryClick: (GpxFileEntry) -> Unit,
    onFavoriteClick: (GpxFileEntry) -> Unit,
    onDeleteEntry: (GpxFileEntry) -> Unit
) {
    val listState = rememberLazyListState()
    // Re-sorting keeps the scroll offset, which would park the first section heading off-screen.
    LaunchedEffect(sortMode) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = RyggTheme.dimens.commonContentPadding16,
            end = RyggTheme.dimens.commonContentPadding16,
            top = RyggTheme.dimens.commonContentPadding4,
            bottom = RyggTheme.dimens.commonContentPadding80
        ),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        if (sortMode == SortMode.TIME) {
            val timed = entries.filter { it.startTimeMillis != null }
            val untimed = entries.filter { it.startTimeMillis == null }
            timed.groupBy { monthKey(requireNotNull(it.startTimeMillis)) }
                .forEach { (key, monthEntries) ->
                    item(key = "month-$key") {
                        SectionHeader(
                            formatMonthYear(requireNotNull(monthEntries.first().startTimeMillis)).uppercase()
                        )
                    }
                    items(monthEntries, key = { it.id }) { entry ->
                        EntryRow(entry, onEntryClick, onFavoriteClick, onDeleteEntry)
                    }
                }
            if (untimed.isNotEmpty()) {
                item(key = UNDATED_SECTION_KEY) {
                    SectionHeader(stringResource(R.string.library_undated_section).uppercase())
                }
                items(untimed, key = { it.id }) { entry ->
                    EntryRow(entry, onEntryClick, onFavoriteClick, onDeleteEntry)
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                EntryRow(entry, onEntryClick, onFavoriteClick, onDeleteEntry)
            }
        }
    }
}

@Composable
private fun LazyItemScope.EntryRow(
    entry: GpxFileEntry,
    onEntryClick: (GpxFileEntry) -> Unit,
    onFavoriteClick: (GpxFileEntry) -> Unit,
    onDeleteEntry: (GpxFileEntry) -> Unit
) {
    SwipeToDeleteBox(
        onDelete = { onDeleteEntry(entry) },
        modifier = Modifier.animateItem(placementSpec = RyggMotion.spatial())
    ) {
        GpxFileEntryCard(
            entry = entry,
            onClick = onEntryClick,
            onFavoriteClick = onFavoriteClick
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = RyggTheme.textStyles.trackedLabel,
        color = RyggTheme.getColor(RyggColor.TextSecondary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding4,
                vertical = RyggTheme.dimens.commonContentPadding8
            )
    )
}

private const val UNDATED_SECTION_KEY = "undated_section"
