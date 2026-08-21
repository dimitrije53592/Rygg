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
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.sync.data.RouteSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    private val routeSyncManager: RouteSyncManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val entryId = savedStateHandle.toRoute<Details>().entryId
    private var profileCache: CachedProfile? = null

    // One-shot share side effects handed to the wrapper (which owns the Context to act on them).
    private val _events = Channel<DetailsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

    fun onRename(newName: String) {
        val entry = loadedEntry() ?: return
        viewModelScope.launch {
            gpxFileEntryRepository.renameGpxFile(entry, newName)
        }
    }

    fun onDelete() {
        val entry = loadedEntry() ?: return
        viewModelScope.launch {
            gpxFileEntryRepository.deleteGpxFile(entry)
        }
    }

    // Upload the route if needed, then emit a cross-device share link (or a failure).
    fun onShareLink() {
        val entry = loadedEntry() ?: return
        viewModelScope.launch {
            val event = when (val outcome = routeSyncManager.createShareLink(entry)) {
                is Outcome.Success -> DetailsEvent.ShareLink(outcome.data, entry.name)
                is Outcome.Error -> DetailsEvent.ShareLinkFailed
                Outcome.Loading -> return@launch
            }
            _events.send(event)
        }
    }

    // Emit the .gpx file's share Uri, or — when the file isn't on this device yet — start its
    // download and signal that it isn't ready (sharing it now would crash in FileProvider).
    fun onShareFile() {
        val entry = loadedEntry() ?: return
        viewModelScope.launch {
            if (!entry.fileDownloaded || entry.fileName.isBlank()) {
                gpxFileEntryRepository.ensureRouteFileDownloaded(entry.id)
                _events.send(DetailsEvent.FileNotReady)
            } else {
                _events.send(DetailsEvent.ShareFile(gpxFileEntryRepository.gpxShareUri(entry), entry.name))
            }
        }
    }

    private fun loadedEntry(): GpxFileEntry? =
        (uiState.value.loadingState as? DetailsLoadingState.Loaded)?.entry

    private suspend fun loadProfile(entry: GpxFileEntry): List<ElevationSample> {
        // A route pulled from another device may not have its .gpx yet — fetch it now.
        if (!entry.fileDownloaded) {
            routeSyncManager.ensureFileDownloaded(entry.id)
            return emptyList()
        }
        // We hold the file: make sure it's backed up so other devices can download it.
        gpxFileEntryRepository.ensureRouteFileUploaded(entry.id)
        val cached = profileCache
        if (cached != null && cached.entryId == entry.id && cached.updatedAt == entry.updatedAt) {
            return cached.samples
        }
        return gpxFileEntryRepository.loadElevationProfile(entry)
            .also { profileCache = CachedProfile(entry.id, entry.updatedAt, it) }
    }
}

// One-shot events the wrapper turns into Context-bound side effects (share sheet, toast).
sealed interface DetailsEvent {
    data class ShareLink(val url: String, val routeName: String) : DetailsEvent

    data class ShareFile(val uri: Uri, val routeName: String) : DetailsEvent

    data object ShareLinkFailed : DetailsEvent

    data object FileNotReady : DetailsEvent
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
