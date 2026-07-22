package com.example.rygg.feature.library.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.library.ui.screen.LibraryScreen
import com.example.rygg.feature.library.ui.screen.LibraryScreenParams
import com.example.rygg.feature.library.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryWrapper(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreen(
        params = LibraryScreenParams(
            uiState = uiState,
            onImport = { uri, discipline ->
                viewModel.importGpxFile(uri, discipline)
            },
            onEntryClick = {},
            onFavoriteClick = { entry ->
                viewModel.toggleFavorite(entry)
            },
            onDeleteEntry = { entry ->
                viewModel.deleteGpxFile(entry)
            },
            onDisciplineSelected = { discipline ->
                viewModel.onDisciplineSelected(discipline)
            },
            onToggleSort = { viewModel.onToggleSort() },
            onToggleFavoritesFilter = { viewModel.onToggleFavoritesFilter() }
        )
    )
}
