package com.example.rygg.feature.record.data

import com.example.rygg.core.common.RyggTimer
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.location.RyggLocationManager
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.record.domain.RecordingSnapshot
import com.example.rygg.feature.record.domain.RecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

// Single source of truth for an in-progress recording. Orchestrates the session state, the
// stopwatch (RyggTimer) and the track/metrics (RouteAccumulator), and drives location itself
// via RyggLocationManager. Driven by RecordingService; observed by the record + preview UIs.
@Singleton
class RecordingController @Inject constructor(
    private val locationManager: RyggLocationManager,
    private val timer: RyggTimer,
    private val route: RouteAccumulator
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val session = MutableStateFlow(Session())
    private var locationJob: Job? = null

    val snapshot: StateFlow<RecordingSnapshot> =
        combine(session, timer.elapsed, route.metrics) { s, elapsed, m ->
            RecordingSnapshot(
                state = s.state,
                discipline = s.discipline,
                elapsedMillis = elapsed,
                distanceMeters = m.distanceMeters,
                currentSpeedMps = m.currentSpeedMps,
                ascentMeters = m.ascentMeters,
                elevationMeters = m.elevationMeters,
                pointCount = m.pointCount,
                waypointCount = m.waypointCount,
                gpsReady = s.gpsReady
            )
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), RecordingSnapshot())

    fun start(discipline: Discipline) {
        route.reset()
        session.value = Session(
            state = RecordingState.RECORDING,
            discipline = discipline,
            startInstant = Instant.now()
        )
        timer.start()
        startLocation()
    }

    fun pause() {
        if (session.value.state != RecordingState.RECORDING) return
        timer.pause()
        stopLocation()
        session.update { it.copy(state = RecordingState.PAUSED) }
    }

    fun resume() {
        if (session.value.state != RecordingState.PAUSED) return
        timer.resume()
        route.breakContinuity()
        startLocation()
        session.update { it.copy(state = RecordingState.RECORDING) }
    }

    fun stop() {
        timer.stop()
        stopLocation()
        session.update { it.copy(state = RecordingState.IDLE) }
    }

    fun addWaypoint(name: String) = route.addWaypoint(name)

    // Build the recorded GPX document; null when nothing usable was captured.
    fun buildDocument(): GpxDocument? = route.buildDocument(session.value.startInstant, CREATOR)

    fun currentDiscipline(): Discipline = session.value.discipline

    fun reset() {
        stopLocation()
        timer.reset()
        route.reset()
        session.value = Session()
    }

    private fun startLocation() {
        stopLocation()
        locationJob = scope.launch {
            locationManager.locationUpdates(minDistanceMeters = RECORDING_MIN_DISTANCE_M).collect { location ->
                if (!session.value.gpsReady) session.update { it.copy(gpsReady = true) }
                if (session.value.state == RecordingState.RECORDING) route.add(location)
            }
        }
    }

    private fun stopLocation() {
        locationJob?.cancel()
        locationJob = null
    }

    private data class Session(
        val state: RecordingState = RecordingState.IDLE,
        val discipline: Discipline = Discipline.HIKE,
        val gpsReady: Boolean = false,
        val startInstant: Instant? = null
    )

    private companion object {
        const val CREATOR = "Rygg"
        const val RECORDING_MIN_DISTANCE_M = 2f
    }
}
