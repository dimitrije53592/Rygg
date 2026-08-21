package com.example.rygg.feature.library.ui.wrapper

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.ui.screen.LibraryScreen
import com.example.rygg.feature.library.ui.screen.LibraryScreenParams
import com.example.rygg.feature.library.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryWrapper(
    onEntryClick: (Long) -> Unit,
    onImport: (Uri, Discipline) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreen(
        params = LibraryScreenParams(
            uiState = uiState,
            onImport = onImport,
            onEntryClick = { entry -> onEntryClick(entry.id) },
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
            onToggleFavoritesFilter = { viewModel.onToggleFavoritesFilter() },
            onCycleSourceFilter = { viewModel.onCycleSource() },
            onOpenProfile = onOpenProfile
        )
    )
}
