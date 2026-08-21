package com.example.rygg.feature.map.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.rygg.core.gpx.model.RouteFileContent
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.map.domain.MapStyleSource
import com.example.rygg.feature.map.domain.RouteOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.rygg.core.navigation.Map as MapRoute

@HiltViewModel
class MapViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    mapStyleSource: MapStyleSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val styleUrl = mapStyleSource.baseStyleUrl()
    private val focusEntryId = savedStateHandle.toRoute<MapRoute>().entryId
    private val contentCache = mutableMapOf<Long, CachedContent>()

    init {
        // Pull the focused route's full .gpx so it upgrades from the simplified fallback.
        focusEntryId?.let { id ->
            viewModelScope.launch { gpxFileEntryRepository.ensureRouteFileDownloaded(id) }
        }
    }

    val uiState: StateFlow<MapUiState> = gpxFileEntryRepository.observeGpxFileEntries()
        .map { entries ->
            MapUiState(
                styleUrl = styleUrl,
                routes = entries.map { it.toRouteOverlay() },
                focusEntryId = focusEntryId
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState(styleUrl = styleUrl, focusEntryId = focusEntryId)
        )

    private suspend fun GpxFileEntry.toRouteOverlay(): RouteOverlay {
        val cached = contentCache[id]
        // Key on fileName too: a .gpx download changes fileName without bumping updatedAt.
        val content = if (cached != null && cached.updatedAt == updatedAt && cached.fileName == fileName) {
            cached.content
        } else {
            gpxFileEntryRepository.loadRouteContent(this).also {
                contentCache[id] = CachedContent(updatedAt, fileName, it)
            }
        }
        return RouteOverlay(
            id = id,
            name = name,
            discipline = discipline,
            paths = content.paths,
            start = content.paths.firstOrNull { it.isNotEmpty() }?.first(),
            distanceMeters = distanceMeters,
            ascentMeters = ascentMeters,
            descentMeters = descentMeters,
            pointCount = pointCount,
            waypoints = content.waypoints
        )
    }
}

private data class CachedContent(
    val updatedAt: Long,
    val fileName: String,
    val content: RouteFileContent
)

data class MapUiState(
    val styleUrl: String,
    val routes: List<RouteOverlay> = emptyList(),
    val focusEntryId: Long? = null
)
