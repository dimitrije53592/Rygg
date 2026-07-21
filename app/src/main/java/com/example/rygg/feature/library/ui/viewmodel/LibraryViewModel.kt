package com.example.rygg.feature.library.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.asResult
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.library.domain.GpxFileEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository
) : ViewModel() {
    val uiState: StateFlow<LibraryUiState> = gpxFileEntryRepository.observeGpxFileEntries()
        .asResult()
        .map { outcome ->
            when (outcome) {
                Outcome.Loading -> LibraryUiState(gpxFilesLoadingState = GpxFilesLoadingState.Loading)
                is Outcome.Success -> LibraryUiState(gpxFilesLoadingState = GpxFilesLoadingState.GpxFilesLoaded(outcome.data))
                is Outcome.Error -> LibraryUiState(gpxFilesLoadingState = GpxFilesLoadingState.Error(outcome.cause.message))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(gpxFilesLoadingState = GpxFilesLoadingState.Loading)
        )

    fun importGpxFile(uri: Uri, discipline: Discipline) {
        viewModelScope.launch {
            gpxFileEntryRepository.importGpxFile(uri, discipline)
        }
    }
}

sealed interface GpxFilesLoadingState {
    object Loading : GpxFilesLoadingState

    data class GpxFilesLoaded(val gpxFilesEntries: List<GpxFileEntry>) : GpxFilesLoadingState

    data class Error(val errorMessage: String?) : GpxFilesLoadingState
}

data class LibraryUiState(
    val gpxFilesLoadingState: GpxFilesLoadingState
)
