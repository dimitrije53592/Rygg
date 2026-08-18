package com.example.rygg.feature.library.ui.paramproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.ui.screen.LibraryScreenParams
import com.example.rygg.feature.library.ui.viewmodel.GpxFilesLoadingState
import com.example.rygg.feature.library.ui.viewmodel.LibraryUiState

// One preview frame per meaningful list state: loading, empty library, populated.
class LibraryParamsProvider : PreviewParameterProvider<LibraryScreenParams> {
    override val values = sequenceOf(
        libraryParams(GpxFilesLoadingState.Loading),
        libraryParams(
            GpxFilesLoadingState.GpxFilesLoaded(emptyList()),
            isLibraryEmpty = true
        ),
        libraryParams(
            GpxFilesLoadingState.GpxFilesLoaded(
                listOf(
                    previewGpxFileEntry(id = 1L, name = "Triglav via Kredarica"),
                    previewGpxFileEntry(
                        id = 2L,
                        name = "Seven Lakes valley",
                        discipline = Discipline.RIDE,
                        isFavorite = false,
                        hasTime = false,
                        tags = emptyList()
                    )
                )
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
        onToggleSort = {},
        onToggleFavoritesFilter = {}
    )
