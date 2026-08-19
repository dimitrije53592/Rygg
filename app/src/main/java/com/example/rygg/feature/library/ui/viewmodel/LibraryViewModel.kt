package com.example.rygg.feature.library.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.asResult
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.library.domain.EntrySource
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.domain.SortMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository
) : ViewModel() {
    private val filter = MutableStateFlow(LibraryFilter())

    init {
        // One-shot cleanup of staged files orphaned by a killed preview in a prior session.
        viewModelScope.launch { gpxFileEntryRepository.reconcileOrphanedFiles() }
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        gpxFileEntryRepository.observeGpxFileEntries().asResult(),
        filter
    ) { outcome, filter ->
        when (outcome) {
            Outcome.Loading -> LibraryUiState(gpxFilesLoadingState = GpxFilesLoadingState.Loading)
            is Outcome.Error ->
                LibraryUiState(gpxFilesLoadingState = GpxFilesLoadingState.Error(outcome.cause.message))

            is Outcome.Success -> LibraryUiState(
                gpxFilesLoadingState = GpxFilesLoadingState.GpxFilesLoaded(
                    gpxFilesEntries = outcome.data.applyFilter(filter).applySort(filter.sortMode)
                ),
                selectedDiscipline = filter.selectedDiscipline,
                selectedSource = filter.selectedSource,
                sortMode = filter.sortMode,
                favoritesOnly = filter.favoritesOnly,
                isLibraryEmpty = outcome.data.isEmpty()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(gpxFilesLoadingState = GpxFilesLoadingState.Loading)
    )

    fun deleteGpxFile(entry: GpxFileEntry) {
        viewModelScope.launch {
            gpxFileEntryRepository.deleteGpxFile(entry)
        }
    }

    fun toggleFavorite(entry: GpxFileEntry) {
        viewModelScope.launch {
            gpxFileEntryRepository.setFavorite(entry.id, !entry.isFavorite)
        }
    }

    fun onDisciplineSelected(discipline: Discipline?) {
        filter.update { it.copy(selectedDiscipline = discipline) }
    }

    // Cycle the source filter: All → Imported → Recorded → All.
    fun onCycleSource() {
        filter.update {
            it.copy(
                selectedSource = when (it.selectedSource) {
                    null -> EntrySource.IMPORTED
                    EntrySource.IMPORTED -> EntrySource.RECORDED
                    EntrySource.RECORDED -> null
                }
            )
        }
    }

    fun onToggleSort() {
        filter.update {
            it.copy(sortMode = if (it.sortMode == SortMode.TIME) SortMode.NAME else SortMode.TIME)
        }
    }

    fun onToggleFavoritesFilter() {
        filter.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    }
}

private data class LibraryFilter(
    val selectedDiscipline: Discipline? = null,
    val selectedSource: EntrySource? = null,
    val sortMode: SortMode = SortMode.NAME,
    val favoritesOnly: Boolean = false
)

private fun List<GpxFileEntry>.applyFilter(filter: LibraryFilter): List<GpxFileEntry> =
    filter {
        (filter.selectedDiscipline == null || it.discipline == filter.selectedDiscipline) &&
            (filter.selectedSource == null || it.source == filter.selectedSource) &&
            (!filter.favoritesOnly || it.isFavorite)
    }

private fun List<GpxFileEntry>.applySort(sortMode: SortMode): List<GpxFileEntry> =
    when (sortMode) {
        SortMode.NAME -> sortedBy { it.name.lowercase() }
        SortMode.TIME -> sortedWith(
            compareByDescending<GpxFileEntry> { it.startTimeMillis != null }
                .thenByDescending { it.startTimeMillis ?: it.importedAt }
        )
    }

sealed interface GpxFilesLoadingState {
    object Loading : GpxFilesLoadingState

    data class GpxFilesLoaded(val gpxFilesEntries: List<GpxFileEntry>) : GpxFilesLoadingState

    data class Error(val errorMessage: String?) : GpxFilesLoadingState
}

data class LibraryUiState(
    val gpxFilesLoadingState: GpxFilesLoadingState,
    val selectedDiscipline: Discipline? = null,
    val selectedSource: EntrySource? = null,
    val sortMode: SortMode = SortMode.TIME,
    val favoritesOnly: Boolean = false,
    val isLibraryEmpty: Boolean = false
)
