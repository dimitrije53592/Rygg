package com.example.rygg.feature.map.ui.viewmodel

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.rygg.core.navigation.FollowRoute
import com.example.rygg.feature.library.data.GpxFileEntryRepository
import com.example.rygg.feature.map.domain.MapStyleSource
import com.example.rygg.feature.map.domain.RouteGeometry
import com.example.rygg.feature.map.domain.RouteOverlay
import com.example.rygg.feature.map.domain.RouteProgress
import com.example.rygg.feature.map.domain.TourSample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteFollowingViewModel @Inject constructor(
    private val gpxFileEntryRepository: GpxFileEntryRepository,
    mapStyleSource: MapStyleSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val entryId = savedStateHandle.toRoute<FollowRoute>().entryId

    private val _uiState = MutableStateFlow(
        RouteFollowingUiState(
            styleUrl = mapStyleSource.baseStyleUrl(),
            route = null,
            geometry = null,
            samples = emptyList(),
            progress = null
        )
    )
    val uiState = _uiState.asStateFlow()

    private var lastKnownLocation: Location? = null
    private var isLocationUnavailable: Boolean = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Kick off the full .gpx download for cloud routes; this session renders from the
            // simplified fallback below, and the detail is present next time it's opened.
            gpxFileEntryRepository.ensureRouteFileDownloaded(entryId)

            val entry = gpxFileEntryRepository.observeGpxFileEntries()
                .first()
                .firstOrNull { it.id == entryId }

            val route = entry?.let {
                val content = gpxFileEntryRepository.loadRouteContent(it)
                val paths = content.paths
                RouteOverlay(
                    id = it.id,
                    name = it.name,
                    discipline = it.discipline,
                    paths = paths,
                    start = paths.firstOrNull { path -> path.isNotEmpty() }?.first(),
                    distanceMeters = it.distanceMeters,
                    ascentMeters = it.ascentMeters,
                    descentMeters = it.descentMeters,
                    pointCount = it.pointCount,
                    waypoints = content.waypoints
                )
            }

            if (route != null) {
                val geometry = RouteGeometry.from(route.paths)
                val samples = geometry.evenSamples(TOUR_SAMPLES_NUM)

                _uiState.update {
                    it.copy(
                        route = route,
                        geometry = geometry,
                        samples = samples
                    )
                }

                evaluateState()
            }
        }
    }

    fun onLocationChange(location: Location?, isUnavailable: Boolean) {
        lastKnownLocation = location
        isLocationUnavailable = isUnavailable
        evaluateState()
    }

    private fun evaluateState() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = _uiState.value
            val geometry = currentState.geometry
            val location = lastKnownLocation

            if (isLocationUnavailable) {
                _uiState.update { it.copy(routeFollowingPhase = RouteFollowingPhase.LocationUnavailable) }
                return@launch
            }

            if (location == null || geometry == null) {
                if (currentState.routeFollowingPhase == RouteFollowingPhase.InitialLoading) {
                    _uiState.update { it.copy(routeFollowingPhase = RouteFollowingPhase.InitialLoading) }
                }
                return@launch
            }

            val progress = geometry.progressFor(location.latitude, location.longitude)
            val isOnRoute = progress.isOnRoute(location.accuracy)

            val distance = progress.distanceToRouteMeters
            val isFarAway = distance > FAR_AWAY_THRESHOLD_METERS

            val newPhase = when (currentState.routeFollowingPhase) {
                RouteFollowingPhase.PreviewActive -> RouteFollowingPhase.PreviewActive
                RouteFollowingPhase.FollowingActive -> RouteFollowingPhase.FollowingActive
                else -> if (isFarAway) RouteFollowingPhase.UserFarAway else RouteFollowingPhase.FollowingActive
            }

            _uiState.update {
                it.copy(
                    progress = progress,
                    isOnRoute = isOnRoute,
                    routeFollowingPhase = newPhase
                )
            }
        }
    }

    fun setFreeLook(freeLook: Boolean) {
        _uiState.update {
            it.copy(freeLook = freeLook)
        }
    }

    fun setPendingRebearing(pendingRebearing: Boolean) {
        _uiState.update {
            it.copy(pendingRebearing = pendingRebearing)
        }
    }

    // Only flips the phase; the fly-over itself is driven frame-by-frame by the screen, so the
    // camera runs at vsync instead of chasing values pushed through a flow.
    fun startPreview() {
        val currentState = _uiState.value
        val totalDistance = currentState.geometry?.totalMeters

        if (currentState.samples.size < MIN_SAMPLES || totalDistance == null || totalDistance <= 0.0) return

        _uiState.update { it.copy(routeFollowingPhase = RouteFollowingPhase.PreviewActive) }
    }

    fun onPreviewFinished() {
        _uiState.update { it.copy(routeFollowingPhase = RouteFollowingPhase.UserFarAway) }
    }
}

sealed interface RouteFollowingPhase {
    object LocationUnavailable : RouteFollowingPhase

    object InitialLoading : RouteFollowingPhase

    object UserFarAway : RouteFollowingPhase

    object PreviewActive : RouteFollowingPhase

    object FollowingActive : RouteFollowingPhase
}

data class RouteFollowingUiState(
    val styleUrl: String,
    val route: RouteOverlay?,
    val geometry: RouteGeometry?,
    val samples: List<TourSample>,
    val progress: RouteProgress?,
    val isOnRoute: Boolean = false,
    val freeLook: Boolean = false,
    val pendingRebearing: Boolean = false,
    val routeFollowingPhase: RouteFollowingPhase = RouteFollowingPhase.InitialLoading
)

const val PREVIEW_SPEED_METERS_PER_SECOND = 800.0
private const val TOUR_SAMPLES_NUM = 250
private const val FAR_AWAY_THRESHOLD_METERS = 200.0
private const val MIN_SAMPLES = 2
