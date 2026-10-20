package com.dimitrijeilic.rygg.feature.library.ui.components

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
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.SwipeToDeleteBox
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.formatMonthYear
import com.dimitrijeilic.rygg.core.ui.utils.monthKey
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry
import com.dimitrijeilic.rygg.feature.library.domain.SortMode
import com.dimitrijeilic.rygg.feature.library.ui.viewmodel.LibraryFolder

@Composable
internal fun GpxFileEntryList(
    folders: List<LibraryFolder>,
    expandedFolders: Set<String>,
    entries: List<GpxFileEntry>,
    sortMode: SortMode,
    onToggleFolder: (String) -> Unit,
    onRenameFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
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
        folders.forEach { folder ->
            val expanded = folder.name in expandedFolders
            item(key = "folder-${folder.name}") {
                SwipeToDeleteBox(
                    onDelete = { onDeleteFolder(folder.name) },
                    modifier = Modifier.animateItem(placementSpec = RyggMotion.spatial())
                ) {
                    LibraryFolderCard(
                        name = folder.name,
                        routeCount = folder.entries.size,
                        expanded = expanded,
                        onToggle = { onToggleFolder(folder.name) },
                        onRename = { onRenameFolder(folder.name) }
                    )
                }
            }
            if (expanded) {
                items(folder.entries, key = { it.id }) { entry ->
                    EntryRow(
                        entry = entry,
                        onEntryClick = onEntryClick,
                        onFavoriteClick = onFavoriteClick,
                        onDeleteEntry = onDeleteEntry,
                        modifier = Modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding12)
                    )
                }
            }
        }
        if (folders.isNotEmpty() && entries.isNotEmpty()) {
            item(key = UNFILED_SECTION_KEY) {
                SectionHeader(stringResource(R.string.library_unfiled_section).uppercase())
            }
        }
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
    onDeleteEntry: (GpxFileEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    SwipeToDeleteBox(
        onDelete = { onDeleteEntry(entry) },
        modifier = modifier.animateItem(placementSpec = RyggMotion.spatial())
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
private const val UNFILED_SECTION_KEY = "unfiled_section"
