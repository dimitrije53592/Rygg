package com.example.rygg.feature.details.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.asResult
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.navigation.Details
import com.example.rygg.core.ui.utils.RouteShareLinks
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
class DetailsViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val entryId = savedStateHandle.toRoute<Details>().entryId
    private var profileCache: CachedProfile? = null

    val uiState: StateFlow<DetailsUiState> =
        gpxFileEntryRepository.observeGpxFileEntry(entryId).asResult()
            .map { outcome ->
                when (outcome) {
                    Outcome.Loading -> DetailsUiState(DetailsLoadingState.Loading)
                    is Outcome.Error ->
                        DetailsUiState(DetailsLoadingState.Error(outcome.cause.message))

                    is Outcome.Success -> {
                        val entry = outcome.data
                            ?: return@map DetailsUiState(DetailsLoadingState.Error(null))
                        DetailsUiState(
                            DetailsLoadingState.Loaded(
                                entry = entry,
                                elevationProfile = loadProfile(entry)
                            )
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetailsUiState(DetailsLoadingState.Loading)
            )

    fun onToggleFavorite() {
        val entry = loadedEntry() ?: return
        viewModelScope.launch {
            gpxFileEntryRepository.setFavorite(entry.id, !entry.isFavorite)
        }
    }

    fun onDelete() {
        val entry = loadedEntry() ?: return
        viewModelScope.launch {
            gpxFileEntryRepository.deleteGpxFile(entry)
        }
    }

    // Shareable content Uri for the loaded route's .gpx file.
    fun shareFileUri(): Uri? = loadedEntry()?.let { gpxFileEntryRepository.gpxShareUri(it) }

    // Deep link that opens the loaded route in the app.
    fun shareLink(): String? = loadedEntry()?.let { RouteShareLinks.buildUrl(it.id) }

    private fun loadedEntry(): GpxFileEntry? =
        (uiState.value.loadingState as? DetailsLoadingState.Loaded)?.entry

    private suspend fun loadProfile(entry: GpxFileEntry): List<ElevationSample> {
        val cached = profileCache
        if (cached != null && cached.entryId == entry.id && cached.updatedAt == entry.updatedAt) {
            return cached.samples
        }
        return gpxFileEntryRepository.loadElevationProfile(entry)
            .also { profileCache = CachedProfile(entry.id, entry.updatedAt, it) }
    }
}

private data class CachedProfile(
    val entryId: Long,
    val updatedAt: Long,
    val samples: List<ElevationSample>
)

data class DetailsUiState(
    val loadingState: DetailsLoadingState
)

sealed interface DetailsLoadingState {
    data object Loading : DetailsLoadingState

    data class Loaded(
        val entry: GpxFileEntry,
        val elevationProfile: List<ElevationSample>
    ) : DetailsLoadingState

    data class Error(val message: String?) : DetailsLoadingState

    // A finished recording that captured too little movement to build a route (< 2 points).
    data object Empty : DetailsLoadingState
}
