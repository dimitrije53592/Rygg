package com.dimitrijeilic.rygg.feature.map.data

import android.os.SystemClock
import com.dimitrijeilic.rygg.core.location.RyggLocationManager
import com.dimitrijeilic.rygg.feature.map.domain.FollowSnapshot
import com.dimitrijeilic.rygg.feature.map.domain.OffRouteDetector
import com.dimitrijeilic.rygg.feature.map.domain.RouteGeometry
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteFollowingController @Inject constructor(
    private val locationManager: RyggLocationManager,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var trackingJob: Job? = null

    private val _snapshot = MutableStateFlow(FollowSnapshot())
    val snapshot: StateFlow<FollowSnapshot> = _snapshot.asStateFlow()

    fun start(routeName: String, geometry: RouteGeometry) {
        trackingJob?.cancel()
        val detector = OffRouteDetector()
        _snapshot.value = FollowSnapshot(isActive = true, routeName = routeName)

        trackingJob = scope.launch {
            combine(
                locationManager.locationUpdates(minDistanceMeters = 0f),
                settingsRepository.offRouteThreshold
            ) { location, threshold -> location to threshold }
                .collect { (location, threshold) ->
                    val fixMillis = location.elapsedRealtimeNanos / NANOS_PER_MILLI
                    if (SystemClock.elapsedRealtime() - fixMillis > MAX_FIX_AGE_MS) return@collect

                    val progress = geometry.progressFor(location.latitude, location.longitude)
                    detector.update(
                        distanceToRouteMeters = progress.distanceToRouteMeters,
                        accuracyMeters = location.accuracy,
                        thresholdMeters = threshold.meters,
                        nowMillis = fixMillis
                    )
                    _snapshot.update {
                        it.copy(
                            distanceToRouteMeters = progress.distanceToRouteMeters,
                            isOffRoute = detector.isOffRoute,
                            offRouteEpisode = detector.episode
                        )
                    }
                }
        }
    }

    fun snooze() {
        _snapshot.update { it.copy(snoozedUntilMillis = SystemClock.elapsedRealtime() + SNOOZE_MS) }
    }

    fun isSnoozed(): Boolean = SystemClock.elapsedRealtime() < _snapshot.value.snoozedUntilMillis

    fun stop() {
        trackingJob?.cancel()
        trackingJob = null
        _snapshot.value = FollowSnapshot()
    }
}

private const val NANOS_PER_MILLI = 1_000_000L
private const val MAX_FIX_AGE_MS = 10_000L
private const val SNOOZE_MS = 10 * 60 * 1_000L
