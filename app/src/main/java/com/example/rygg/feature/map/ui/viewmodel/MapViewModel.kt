package com.example.rygg.feature.map.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.map.domain.MapStyleSource
import com.example.rygg.feature.map.domain.RouteOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val pathCache = mutableMapOf<Long, CachedPaths>()

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
        val cached = pathCache[id]
        val paths = if (cached != null && cached.updatedAt == updatedAt) {
            cached.paths
        } else {
            gpxFileEntryRepository.loadPaths(this).also { pathCache[id] = CachedPaths(updatedAt, it) }
        }
        return RouteOverlay(
            id = id,
            name = name,
            discipline = discipline,
            paths = paths,
            start = paths.firstOrNull { it.isNotEmpty() }?.first(),
            distanceMeters = distanceMeters,
            ascentMeters = ascentMeters,
            descentMeters = descentMeters,
            pointCount = pointCount
        )
    }
}

private data class CachedPaths(
    val updatedAt: Long,
    val paths: List<List<GeoPoint>>
)

data class MapUiState(
    val styleUrl: String,
    val routes: List<RouteOverlay> = emptyList(),
    val focusEntryId: Long? = null
)
