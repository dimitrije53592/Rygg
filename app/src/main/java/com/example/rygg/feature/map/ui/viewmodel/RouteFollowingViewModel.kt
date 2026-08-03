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
import com.example.rygg.feature.map.ui.util.CameraAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _cameraAction = MutableSharedFlow<CameraAction>()
    val cameraAction = _cameraAction.asSharedFlow()

    private var lastKnownLocation: Location? = null
    private var isLocationUnavailable: Boolean = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = gpxFileEntryRepository.observeGpxFileEntries()
                .first()
                .firstOrNull { it.id == entryId }

            val route = entry?.let {
                val paths = gpxFileEntryRepository.loadPaths(it)
                RouteOverlay(
                    id = it.id,
                    name = it.name,
                    discipline = it.discipline,
                    paths = paths,
                    start = paths.firstOrNull { path -> path.isNotEmpty() }?.first(),
                    distanceMeters = it.distanceMeters,
                    ascentMeters = it.ascentMeters,
                    descentMeters = it.descentMeters,
                    pointCount = it.pointCount
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

    fun startPreview() {
        val currentState = _uiState.value
        val samples = currentState.samples
        val totalDistance = currentState.geometry?.totalMeters

        if (samples.size < MIN_SAMPLES || totalDistance == null || totalDistance <= 0.0) return

        _uiState.update {
            it.copy(
                routeFollowingPhase = RouteFollowingPhase.PreviewActive,
                previewFraction = PROGRESS_START
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            val startSample = samples.first()

            _cameraAction.emit(
                CameraAction.AnimateTo(
                    longitude = startSample.lon,
                    latitude = startSample.lat,
                    bearing = startSample.bearing
                )
            )
            delay(PREVIEW_HOLD_MS)

            val startNanos = System.nanoTime()
            val durationSeconds = totalDistance / PREVIEW_SPEED_METERS_PER_SECOND
            val durationNanos = (durationSeconds * NANOS_PER_SECOND).toLong()

            val lastIndex = samples.lastIndex
            var fraction = PROGRESS_START

            while (fraction < PROGRESS_COMPLETE) {
                val elapsedNanos = System.nanoTime() - startNanos

                fraction = (elapsedNanos.toDouble() / durationNanos).coerceIn(PROGRESS_START, PROGRESS_COMPLETE)

                _uiState.update { it.copy(previewFraction = fraction) }

                val exactPosition = fraction * lastIndex
                val lowerIndex = exactPosition.toInt()
                val upperIndex = minOf(lowerIndex + NEXT_INDEX_OFFSET, lastIndex)
                val interpolationWeight = exactPosition - lowerIndex

                val sampleA = samples[lowerIndex]
                val sampleB = samples[upperIndex]

                _cameraAction.emit(
                    CameraAction.PositionTo(
                        longitude = sampleA.lon + (sampleB.lon - sampleA.lon) * interpolationWeight,
                        latitude = sampleA.lat + (sampleB.lat - sampleA.lat) * interpolationWeight,
                        bearing = lerpAngle(sampleA.bearing, sampleB.bearing, interpolationWeight)
                    )
                )

                if (fraction < PROGRESS_COMPLETE) {
                    delay(FRAME_DELAY_MS)
                }
            }

            _uiState.update { it.copy(routeFollowingPhase = RouteFollowingPhase.UserFarAway) }
        }
    }

    private fun lerpAngle(from: Double, to: Double, fraction: Double): Double {
        val diff = ((to - from + MODULO_OFFSET_DEGREES) % FULL_CIRCLE_DEGREES) - HALF_CIRCLE_DEGREES
        return (from + diff * fraction + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
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
    val previewFraction: Double = 0.0,
    val freeLook: Boolean = false,
    val pendingRebearing: Boolean = false,
    val routeFollowingPhase: RouteFollowingPhase = RouteFollowingPhase.InitialLoading
)

private const val PREVIEW_SPEED_METERS_PER_SECOND = 800.0
private const val TOUR_SAMPLES_NUM = 250
private const val FAR_AWAY_THRESHOLD_METERS = 200.0
private const val PREVIEW_HOLD_MS = 700L
private const val FRAME_DELAY_MS = 16L
private const val NANOS_PER_SECOND = 1_000_000_000L
private const val FULL_CIRCLE_DEGREES = 360.0
private const val HALF_CIRCLE_DEGREES = 180.0
private const val MODULO_OFFSET_DEGREES = 540.0
private const val MIN_SAMPLES = 2
private const val NEXT_INDEX_OFFSET = 1
private const val PROGRESS_START = 0.0
private const val PROGRESS_COMPLETE = 1.0
