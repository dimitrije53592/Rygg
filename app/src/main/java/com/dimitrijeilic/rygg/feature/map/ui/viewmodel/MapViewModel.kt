package com.dimitrijeilic.rygg.feature.map.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dimitrijeilic.rygg.core.gpx.model.GeoPoint
import com.dimitrijeilic.rygg.core.gpx.model.RouteFileContent
import com.dimitrijeilic.rygg.core.gpx.model.Waypoint
import com.dimitrijeilic.rygg.feature.library.data.GpxFileEntryRepository
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry
import com.dimitrijeilic.rygg.feature.map.data.RouteOverviewRepository
import com.dimitrijeilic.rygg.feature.map.domain.MapStyleSource
import com.dimitrijeilic.rygg.feature.map.domain.RouteOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.dimitrijeilic.rygg.core.navigation.Map as MapRoute

@HiltViewModel
class MapViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    private val routeOverviewRepository: RouteOverviewRepository,
    mapStyleSource: MapStyleSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val styleUrl = mapStyleSource.baseStyleUrl()
    private val focusEntryId = savedStateHandle.toRoute<MapRoute>().entryId

    // Only the route the user is looking at gets its full .gpx parsed; the rest draw from cached
    // simplified overviews, so the map stays fast with a large library.
    private val detailedRouteId = MutableStateFlow(focusEntryId)
    private val overviewCache = mutableMapOf<Long, CachedOverview>()
    private var detailedCache: CachedContent? = null

    init {
        // Pull the focused route's full .gpx so it upgrades from the simplified fallback.
        focusEntryId?.let { id ->
            viewModelScope.launch { gpxFileEntryRepository.ensureRouteFileDownloaded(id) }
        }
    }

    val uiState: StateFlow<MapUiState> =
        combine(gpxFileEntryRepository.observeGpxFileEntries(), detailedRouteId) { entries, detailedId ->
            MapUiState(
                styleUrl = styleUrl,
                routes = entries.map { entry ->
                    if (entry.id == detailedId) entry.toDetailedOverlay() else entry.toOverviewOverlay()
                },
                focusEntryId = focusEntryId
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState(styleUrl = styleUrl, focusEntryId = focusEntryId)
        )

    fun onFocusRoute(id: Long) {
        detailedRouteId.value = id
    }

    private suspend fun GpxFileEntry.toOverviewOverlay(): RouteOverlay {
        val cached = overviewCache[id]
        val paths = if (cached != null && cached.contentHash == contentHash && cached.fileName == fileName) {
            cached.paths
        } else {
            routeOverviewRepository.loadOverview(this).also {
                overviewCache[id] = CachedOverview(contentHash, fileName, it)
            }
        }
        return toOverlay(paths = paths, waypoints = emptyList())
    }

    private suspend fun GpxFileEntry.toDetailedOverlay(): RouteOverlay {
        val cached = detailedCache
        // Key on fileName too: a .gpx download changes fileName without bumping updatedAt.
        val content = if (cached != null && cached.id == id && cached.updatedAt == updatedAt && cached.fileName == fileName) {
            cached.content
        } else {
            gpxFileEntryRepository.loadRouteContent(this).also {
                detailedCache = CachedContent(id, updatedAt, fileName, it)
            }
        }
        return toOverlay(paths = content.paths, waypoints = content.waypoints)
    }

    private fun GpxFileEntry.toOverlay(paths: List<List<GeoPoint>>, waypoints: List<Waypoint>) = RouteOverlay(
        id = id,
        name = name,
        discipline = discipline,
        paths = paths,
        start = paths.firstOrNull { it.isNotEmpty() }?.first(),
        distanceMeters = distanceMeters,
        ascentMeters = ascentMeters,
        descentMeters = descentMeters,
        pointCount = pointCount,
        waypoints = waypoints
    )
}

private data class CachedOverview(
    val contentHash: String,
    val fileName: String,
    val paths: List<List<GeoPoint>>
)

private data class CachedContent(
    val id: Long,
    val updatedAt: Long,
    val fileName: String,
    val content: RouteFileContent
)

data class MapUiState(
    val styleUrl: String,
    val routes: List<RouteOverlay> = emptyList(),
    val focusEntryId: Long? = null
)
