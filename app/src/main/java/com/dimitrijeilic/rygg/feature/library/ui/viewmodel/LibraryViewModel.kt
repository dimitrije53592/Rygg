package com.dimitrijeilic.rygg.feature.library.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.common.asResult
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.library.data.GpxFileEntryRepository
import com.dimitrijeilic.rygg.feature.library.domain.EntrySource
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry
import com.dimitrijeilic.rygg.feature.library.domain.SortMode
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

            is Outcome.Success -> {
                val visible = outcome.data.applyFilter(filter).applySort(filter.sortMode)
                LibraryUiState(
                    gpxFilesLoadingState = GpxFilesLoadingState.GpxFilesLoaded(
                        folders = visible.toFolders(),
                        gpxFilesEntries = visible.filter { it.folder == null },
                        routeCount = visible.size
                    ),
                    selectedDiscipline = filter.selectedDiscipline,
                    selectedSource = filter.selectedSource,
                    sortMode = filter.sortMode,
                    favoritesOnly = filter.favoritesOnly,
                    searchQuery = filter.searchQuery,
                    isFiltering = filter.isActive(),
                    allRoutes = outcome.data.sortedBy { it.name.lowercase() },
                    isLibraryEmpty = outcome.data.isEmpty()
                )
            }
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

    fun onSearchQueryChange(query: String) {
        filter.update { it.copy(searchQuery = query) }
    }

    fun onCreateFolder(name: String, entryIds: Set<Long>) {
        viewModelScope.launch {
            gpxFileEntryRepository.createFolder(name, entryIds)
        }
    }

    fun onRenameFolder(name: String, newName: String) {
        viewModelScope.launch {
            gpxFileEntryRepository.renameFolder(name, newName)
        }
    }

    fun onDeleteFolder(name: String) {
        viewModelScope.launch {
            gpxFileEntryRepository.deleteFolder(name)
        }
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
    val favoritesOnly: Boolean = false,
    val searchQuery: String = ""
)

private fun LibraryFilter.isActive(): Boolean =
    selectedDiscipline != null || selectedSource != null || favoritesOnly || searchQuery.isNotBlank()

private fun List<GpxFileEntry>.applyFilter(filter: LibraryFilter): List<GpxFileEntry> {
    val query = filter.searchQuery.trim()
    return filter {
        (filter.selectedDiscipline == null || it.discipline == filter.selectedDiscipline) &&
            (filter.selectedSource == null || it.source == filter.selectedSource) &&
            (!filter.favoritesOnly || it.isFavorite) &&
            (query.isEmpty() || it.matches(query))
    }
}

private fun GpxFileEntry.matches(query: String): Boolean =
    name.contains(query, ignoreCase = true) ||
        description.contains(query, ignoreCase = true) ||
        folder?.contains(query, ignoreCase = true) == true

private fun List<GpxFileEntry>.applySort(sortMode: SortMode): List<GpxFileEntry> =
    when (sortMode) {
        SortMode.NAME -> sortedBy { it.name.lowercase() }
        SortMode.TIME -> sortedWith(
            compareByDescending<GpxFileEntry> { it.startTimeMillis != null }
                .thenByDescending { it.startTimeMillis ?: it.importedAt }
        )
    }

private fun List<GpxFileEntry>.toFolders(): List<LibraryFolder> =
    mapNotNull { it.folder }
        .distinct()
        .sortedBy { it.lowercase() }
        .map { name -> LibraryFolder(name = name, entries = filter { it.folder == name }) }

data class LibraryFolder(
    val name: String,
    val entries: List<GpxFileEntry>
)

sealed interface GpxFilesLoadingState {
    object Loading : GpxFilesLoadingState

    data class GpxFilesLoaded(
        val folders: List<LibraryFolder>,
        val gpxFilesEntries: List<GpxFileEntry>,
        val routeCount: Int
    ) : GpxFilesLoadingState

    data class Error(val errorMessage: String?) : GpxFilesLoadingState
}

data class LibraryUiState(
    val gpxFilesLoadingState: GpxFilesLoadingState,
    val selectedDiscipline: Discipline? = null,
    val selectedSource: EntrySource? = null,
    val sortMode: SortMode = SortMode.TIME,
    val favoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val isFiltering: Boolean = false,
    val allRoutes: List<GpxFileEntry> = emptyList(),
    val isLibraryEmpty: Boolean = false
)
