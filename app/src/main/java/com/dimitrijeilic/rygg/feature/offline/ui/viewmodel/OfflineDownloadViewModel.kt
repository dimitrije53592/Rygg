package com.dimitrijeilic.rygg.feature.offline.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.common.Outcome
import com.dimitrijeilic.rygg.core.navigation.OfflineDownload
import com.dimitrijeilic.rygg.core.network.ConnectivityObserver
import com.dimitrijeilic.rygg.core.network.NetworkState
import com.dimitrijeilic.rygg.core.ui.utils.formatDate
import com.dimitrijeilic.rygg.feature.library.data.GpxFileEntryRepository
import com.dimitrijeilic.rygg.feature.map.data.RouteOverviewRepository
import com.dimitrijeilic.rygg.feature.map.domain.MapStyleSource
import com.dimitrijeilic.rygg.feature.map.domain.RouteOverlay
import com.dimitrijeilic.rygg.feature.offline.data.OfflineRegionRepository
import com.dimitrijeilic.rygg.feature.offline.domain.DownloadEstimate
import com.dimitrijeilic.rygg.feature.offline.domain.OfflineRegion
import com.dimitrijeilic.rygg.feature.offline.domain.RegionBounds
import com.dimitrijeilic.rygg.feature.offline.domain.RegionStatus
import com.dimitrijeilic.rygg.feature.offline.service.OfflineDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineDownloadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: OfflineRegionRepository,
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    private val routeOverviewRepository: RouteOverviewRepository,
    connectivityObserver: ConnectivityObserver,
    mapStyleSource: MapStyleSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val routeId = savedStateHandle.toRoute<OfflineDownload>().routeId
    private val styleUrl = mapStyleSource.baseStyleUrl()
    private val form = MutableStateFlow(DownloadForm(name = defaultName()))
    private val route = MutableStateFlow<RouteOverlay?>(null)

    private val _started = MutableSharedFlow<Unit>()
    val started = _started.asSharedFlow()

    private val _error = MutableSharedFlow<Unit>()
    val error = _error.asSharedFlow()

    init {
        routeId?.let { id -> viewModelScope.launch { loadRoute(id) } }
    }

    val uiState: StateFlow<OfflineDownloadUiState> = combine(
        form,
        route,
        repository.observeRegions(),
        connectivityObserver.observeNetwork()
    ) { form, route, regions, network ->
        val estimate = form.frame?.let { DownloadEstimate.of(it) }
        val freeBytes = repository.freeBytes()
        OfflineDownloadUiState(
            styleUrl = styleUrl,
            name = form.name,
            route = route,
            existingRegions = regions,
            estimate = estimate,
            freeBytes = freeBytes,
            network = network,
            isStarting = form.isStarting,
            blocker = when {
                regions.any { it.status == RegionStatus.Downloading } -> DownloadBlocker.ActiveDownload
                !network.isOnline -> DownloadBlocker.Offline
                estimate?.isTooLarge == true -> DownloadBlocker.TooLarge
                freeBytes < DownloadEstimate.MAX_PACK_BYTES + STORAGE_RESERVE_BYTES -> DownloadBlocker.NotEnoughStorage
                else -> null
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OfflineDownloadUiState(styleUrl = styleUrl, name = form.value.name)
    )

    fun onNameChange(name: String) {
        form.update { it.copy(name = name, isNameEdited = true) }
    }

    fun onFrameChanged(bounds: RegionBounds) {
        form.update { it.copy(frame = bounds) }
    }

    fun onDownload() {
        val state = uiState.value
        val frame = form.value.frame ?: return
        if (state.blocker != null || state.isStarting) return
        form.update { it.copy(isStarting = true) }
        viewModelScope.launch {
            val name = form.value.name.trim().ifBlank { defaultName() }
            when (val outcome = repository.startDownload(name, frame, routeId)) {
                is Outcome.Success -> {
                    OfflineDownloadService.start(context, outcome.data)
                    _started.emit(Unit)
                }

                is Outcome.Error -> {
                    form.update { it.copy(isStarting = false) }
                    _error.emit(Unit)
                }

                Outcome.Loading -> Unit
            }
        }
    }

    private suspend fun loadRoute(id: Long) {
        val entry = gpxFileEntryRepository.observeGpxFileEntry(id).firstOrNull() ?: return
        val paths = routeOverviewRepository.loadOverview(entry)
        route.value = RouteOverlay(
            id = entry.id,
            name = entry.name,
            discipline = entry.discipline,
            paths = paths,
            start = paths.firstOrNull { it.isNotEmpty() }?.first()
        )
        form.update { if (it.isNameEdited) it else it.copy(name = entry.name) }
    }

    private fun defaultName(): String =
        context.getString(R.string.offline_download_default_name, formatDate(System.currentTimeMillis()))

    private companion object {
        const val STORAGE_RESERVE_BYTES = 200L * 1024 * 1024
    }
}

private data class DownloadForm(
    val name: String,
    val isNameEdited: Boolean = false,
    val frame: RegionBounds? = null,
    val isStarting: Boolean = false
)

data class OfflineDownloadUiState(
    val styleUrl: String,
    val name: String,
    val route: RouteOverlay? = null,
    val existingRegions: List<OfflineRegion> = emptyList(),
    val estimate: DownloadEstimate? = null,
    val freeBytes: Long = 0L,
    val network: NetworkState = NetworkState.OFFLINE,
    val isStarting: Boolean = false,
    val blocker: DownloadBlocker? = null
) {
    val routeBounds: RegionBounds? get() = route?.let { RegionBounds.of(it.paths.flatten()) }
}

enum class DownloadBlocker {
    ActiveDownload,
    Offline,
    TooLarge,
    NotEnoughStorage
}
