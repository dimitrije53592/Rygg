package com.example.rygg.feature.library.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.asResult
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
                Outcome.Loading -> LibraryUiState(isLoading = true)
                is Outcome.Success -> LibraryUiState(gpxFileEntries = outcome.data, isLoading = false)
                is Outcome.Error -> LibraryUiState(isLoading = false, errorMessage = outcome.cause.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState()
        )

    fun importGpxFile(uri: Uri) {
        viewModelScope.launch {
            gpxFileEntryRepository.importGpxFile(uri)
        }
    }
}

data class LibraryUiState(
    val gpxFileEntries: List<GpxFileEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
