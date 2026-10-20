package com.dimitrijeilic.rygg.feature.library.ui.paramproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.library.domain.EntrySource
import com.dimitrijeilic.rygg.feature.library.ui.screen.LibraryScreenParams
import com.dimitrijeilic.rygg.feature.library.ui.viewmodel.GpxFilesLoadingState
import com.dimitrijeilic.rygg.feature.library.ui.viewmodel.LibraryFolder
import com.dimitrijeilic.rygg.feature.library.ui.viewmodel.LibraryUiState

// One preview frame per meaningful list state: loading, empty library, populated.
class LibraryParamsProvider : PreviewParameterProvider<LibraryScreenParams> {
    override val values = sequenceOf(
        libraryParams(GpxFilesLoadingState.Loading),
        libraryParams(
            GpxFilesLoadingState.GpxFilesLoaded(folders = emptyList(), gpxFilesEntries = emptyList(), routeCount = 0),
            isLibraryEmpty = true
        ),
        libraryParams(
            GpxFilesLoadingState.GpxFilesLoaded(
                folders = listOf(
                    LibraryFolder(
                        name = "Julian Alps",
                        entries = listOf(previewGpxFileEntry(id = 3L, name = "Mangart saddle"))
                    )
                ),
                gpxFilesEntries = listOf(
                    previewGpxFileEntry(id = 1L, name = "Triglav via Kredarica"),
                    previewGpxFileEntry(
                        id = 2L,
                        name = "Seven Lakes valley",
                        discipline = Discipline.RIDE,
                        source = EntrySource.IMPORTED,
                        isFavorite = false,
                        hasTime = false
                    )
                ),
                routeCount = 3
            )
        )
    )
}

private fun libraryParams(
    loadingState: GpxFilesLoadingState,
    isLibraryEmpty: Boolean = false
): LibraryScreenParams =
    LibraryScreenParams(
        uiState = LibraryUiState(
            gpxFilesLoadingState = loadingState,
            isLibraryEmpty = isLibraryEmpty
        ),
        onImport = { _, _ -> },
        onEntryClick = {},
        onFavoriteClick = {},
        onDeleteEntry = {},
        onDisciplineSelected = {},
        onCreateFolder = { _, _ -> },
        onRenameFolder = { _, _ -> },
        onDeleteFolder = {},
        onSearchQueryChange = {},
        onToggleSort = {},
        onToggleFavoritesFilter = {},
        onCycleSourceFilter = {},
        onOpenProfile = {}
    )
