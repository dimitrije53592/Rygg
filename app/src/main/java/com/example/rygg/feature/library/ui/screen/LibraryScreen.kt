package com.example.rygg.feature.library.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.rememberFilePicker
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.ui.components.GpxFileEntryList
import com.example.rygg.feature.library.ui.components.ImportFab
import com.example.rygg.feature.library.ui.components.LibraryDisciplineBar
import com.example.rygg.feature.library.ui.components.LibraryEmptyState
import com.example.rygg.feature.library.ui.components.LibraryErrorState
import com.example.rygg.feature.library.ui.components.LibraryListHeader
import com.example.rygg.feature.library.ui.components.LibraryLoadingState
import com.example.rygg.feature.library.ui.components.LibraryNoMatchesState
import com.example.rygg.feature.library.ui.viewmodel.GpxFilesLoadingState
import com.example.rygg.feature.library.ui.viewmodel.LibraryUiState

@Composable
fun LibraryScreen(params: LibraryScreenParams) {
    var fabExpanded by remember { mutableStateOf(false) }
    var pendingDiscipline by remember { mutableStateOf(Discipline.HIKE) }
    val launchFilePicker = rememberFilePicker(
        onFilePicked = { uri -> params.onImport(uri, pendingDiscipline) }
    )

    Scaffold(
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.library_title),
                actions = {}
            )
        },
        floatingActionButton = {
            ImportFab(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                onDisciplinePicked = { discipline ->
                    pendingDiscipline = discipline
                    fabExpanded = false
                    launchFilePicker()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .padding(innerPadding)
        ) {
            LibraryDisciplineBar(
                disciplines = Discipline.entries,
                selectedDiscipline = params.uiState.selectedDiscipline,
                onDisciplineSelected = params.onDisciplineSelected
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val state = params.uiState.gpxFilesLoadingState) {
                    is GpxFilesLoadingState.Loading -> LibraryLoadingState()
                    is GpxFilesLoadingState.Error -> LibraryErrorState(state.errorMessage)
                    is GpxFilesLoadingState.GpxFilesLoaded ->
                        LoadedContent(
                            entries = state.gpxFilesEntries,
                            uiState = params.uiState,
                            params = params
                        )
                }
            }
        }
    }
}

@Composable
private fun LoadedContent(
    entries: List<GpxFileEntry>,
    uiState: LibraryUiState,
    params: LibraryScreenParams
) {
    if (uiState.isLibraryEmpty) {
        LibraryEmptyState()
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        LibraryListHeader(
            activityCount = entries.size,
            sortMode = uiState.sortMode,
            favoritesOnly = uiState.favoritesOnly,
            onToggleSort = params.onToggleSort,
            onToggleFavoritesFilter = params.onToggleFavoritesFilter
        )
        if (entries.isEmpty()) {
            LibraryNoMatchesState()
        } else {
            GpxFileEntryList(
                entries = entries,
                sortMode = uiState.sortMode,
                onEntryClick = params.onEntryClick,
                onFavoriteClick = params.onFavoriteClick,
                onDeleteEntry = params.onDeleteEntry
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    RyggTheme {
        LibraryScreen(
            params = LibraryScreenParams(
                uiState = LibraryUiState(
                    gpxFilesLoadingState = GpxFilesLoadingState.GpxFilesLoaded(emptyList()),
                    isLibraryEmpty = true
                ),
                onImport = { _, _ -> },
                onEntryClick = {},
                onFavoriteClick = {},
                onDeleteEntry = {},
                onDisciplineSelected = {},
                onToggleSort = {},
                onToggleFavoritesFilter = {}
            )
        )
    }
}

data class LibraryScreenParams(
    val uiState: LibraryUiState,
    val onImport: (Uri, Discipline) -> Unit,
    val onEntryClick: (GpxFileEntry) -> Unit,
    val onFavoriteClick: (GpxFileEntry) -> Unit,
    val onDeleteEntry: (GpxFileEntry) -> Unit,
    val onDisciplineSelected: (Discipline?) -> Unit,
    val onToggleSort: () -> Unit,
    val onToggleFavoritesFilter: () -> Unit
)
