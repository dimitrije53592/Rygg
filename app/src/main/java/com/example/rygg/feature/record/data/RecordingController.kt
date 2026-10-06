package com.example.rygg.feature.record.data

import android.location.Location
import android.os.SystemClock
import com.example.rygg.core.common.RyggTimer
import com.example.rygg.core.gpx.haversineMeters
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.location.RyggLocationManager
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.record.domain.RecordingResolution
import com.example.rygg.feature.record.domain.RecordingSnapshot
import com.example.rygg.feature.record.domain.RecordingState
import com.example.rygg.feature.settings.data.SettingsRepository
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
    private val route: RouteAccumulator,
    private val autoPauseDetector: AutoPauseDetector,
    private val trackPointFilter: TrackPointFilter,
    settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val session = MutableStateFlow(Session())
    private var locationJob: Job? = null

    // Previous accepted fix, kept to derive a speed when a fix carries none and to flush the
    // true stop position when recording ends.
    private var lastFix: Location? = null

    private val resolution: StateFlow<RecordingResolution> = settingsRepository.recordingResolution
        .stateIn(scope, SharingStarted.Eagerly, RecordingResolution.BALANCED)

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
        resetContinuity()
        session.value = Session(
            state = RecordingState.RECORDING,
            discipline = discipline,
            startInstant = Instant.now()
        )
        timer.start()
        startLocation()
    }

    // Manual pause is sticky: it stops the location stream, so auto-resume can't fire until the
    // user resumes. (Auto-pause, by contrast, keeps the stream running to detect movement.)
    fun pause() {
        if (session.value.state != RecordingState.RECORDING) return
        timer.pause()
        stopLocation()
        resetContinuity()
        session.update { it.copy(state = RecordingState.PAUSED) }
    }

    fun resume() {
        val state = session.value.state
        if (state != RecordingState.PAUSED && state != RecordingState.AUTO_PAUSED) return
        timer.resume()
        route.breakContinuity()
        resetContinuity()
        startLocation()
        session.update { it.copy(state = RecordingState.RECORDING) }
    }

    fun stop() {
        timer.stop()
        stopLocation()
        flushFinalFix()
        resetContinuity()
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
                onLocationFix(location)
            }
        }
    }

    private fun onLocationFix(location: Location) {
        val state = session.value.state
        if (state != RecordingState.RECORDING && state != RecordingState.AUTO_PAUSED) return
        if (!isUsableFix(location)) return

        val speed = speedOf(location)
        lastFix = location
        when (autoPauseDetector.onFix(speed, System.currentTimeMillis(), state == RecordingState.AUTO_PAUSED)) {
            AutoPauseAction.PAUSE -> autoPause()
            AutoPauseAction.RESUME -> autoResume()
            AutoPauseAction.NONE -> Unit
        }

        if (session.value.state != RecordingState.RECORDING) return
        if (trackPointFilter.shouldKeep(location.latitude, location.longitude, System.currentTimeMillis(), resolution.value)) {
            route.add(location)
        }
    }

    // Thinning can leave the last fix unrecorded, which would end the track short of where the
    // user actually stopped (and can strand a brief recording under buildDocument's 2-point floor).
    private fun flushFinalFix() {
        val fix = lastFix ?: return
        if (!trackPointFilter.isLastKept(fix.latitude, fix.longitude)) route.add(fix)
    }

    private fun autoPause() {
        timer.pause()
        route.breakContinuity()
        session.update { it.copy(state = RecordingState.AUTO_PAUSED) }
    }

    private fun autoResume() {
        timer.resume()
        route.breakContinuity()
        session.update { it.copy(state = RecordingState.RECORDING) }
    }

    // Drop fixes that would corrupt distance/speed: poor horizontal accuracy, and the stale
    // fusedClient.lastLocation seed replayed at stream start (its position can be far and old).
    private fun isUsableFix(location: Location): Boolean {
        if (!location.hasAccuracy() || location.accuracy > MAX_ACCURACY_METERS) return false
        val ageMs = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / NANOS_PER_MILLI
        return ageMs <= STALE_FIX_MS
    }

    // Doppler speed when the fix carries it (most reliable); otherwise derive from the last fix.
    private fun speedOf(location: Location): Double {
        if (location.hasSpeed()) return location.speed.toDouble()
        val previous = lastFix ?: return 0.0
        val dtSeconds = (location.elapsedRealtimeNanos - previous.elapsedRealtimeNanos) / NANOS_PER_SECOND
        if (dtSeconds <= 0) return 0.0
        val meters = haversineMeters(previous.latitude, previous.longitude, location.latitude, location.longitude)
        return meters / dtSeconds
    }

    private fun resetContinuity() {
        lastFix = null
        autoPauseDetector.reset()
        trackPointFilter.reset()
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

        // Time-based fixes (no displacement gate): a stopped user still yields fixes, so stop/
        // resume can be detected, and per-fix speed is no longer biased by a forced minimum step.
        const val RECORDING_MIN_DISTANCE_M = 0f
        const val MAX_ACCURACY_METERS = 30f
        const val STALE_FIX_MS = 5_000L
        const val NANOS_PER_MILLI = 1_000_000.0
        const val NANOS_PER_SECOND = 1_000_000_000.0
    }
}
