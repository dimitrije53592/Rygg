package com.example.rygg.feature.record.data

import com.example.rygg.core.gpx.haversineMeters
import com.example.rygg.feature.record.domain.RecordingResolution
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Decides which of the 1 Hz fixes actually become track points. Sits after auto-pause detection,
// so the detector still sees every fix while the stored track thins by roughly 10x. Pure and
// stateful only in the last kept point, so it unit-tests without Android.
@Singleton
class TrackPointFilter @Inject constructor() {
    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastTimeMillis: Long = 0L
    private var lastBearing: Double? = null

    fun shouldKeep(lat: Double, lon: Double, timeMillis: Long, resolution: RecordingResolution): Boolean {
        val previousLat = lastLat
        val previousLon = lastLon
        if (previousLat == null || previousLon == null) return keep(lat, lon, timeMillis, null)

        val moved = haversineMeters(previousLat, previousLon, lat, lon)
        val elapsedSeconds = (timeMillis - lastTimeMillis) / MILLIS_PER_SECOND
        val bearing = if (moved >= MIN_BEARING_MOVE_METERS) bearingTo(previousLat, previousLon, lat, lon) else null

        // A corner is worth a point even before the distance gate: without this, switchbacks get
        // cut into straight lines. The move minimum keeps GPS jitter from faking a turn.
        val turned = bearing != null && lastBearing?.let { angleDelta(it, bearing) > HEADING_CHANGE_DEGREES } == true

        if (moved < resolution.minDistanceMeters && elapsedSeconds < resolution.maxIntervalSeconds && !turned) {
            return false
        }
        return keep(lat, lon, timeMillis, bearing ?: lastBearing)
    }

    fun reset() {
        lastLat = null
        lastLon = null
        lastTimeMillis = 0L
        lastBearing = null
    }

    // True when this exact position is already the last point in the track, so a final flush on
    // stop doesn't duplicate it.
    fun isLastKept(lat: Double, lon: Double): Boolean = lastLat == lat && lastLon == lon

    private fun keep(lat: Double, lon: Double, timeMillis: Long, bearing: Double?): Boolean {
        lastLat = lat
        lastLon = lon
        lastTimeMillis = timeMillis
        lastBearing = bearing
        return true
    }

    private fun bearingTo(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
        val lat1 = Math.toRadians(fromLat)
        val lat2 = Math.toRadians(toLat)
        val dLon = Math.toRadians(toLon - fromLon)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
    }

    private fun angleDelta(from: Double, to: Double): Double {
        val diff = abs(to - from) % FULL_CIRCLE_DEGREES
        return if (diff > HALF_CIRCLE_DEGREES) FULL_CIRCLE_DEGREES - diff else diff
    }

    private companion object {
        const val HEADING_CHANGE_DEGREES = 20.0
        const val MIN_BEARING_MOVE_METERS = 3.0
        const val MILLIS_PER_SECOND = 1000.0
        const val FULL_CIRCLE_DEGREES = 360.0
        const val HALF_CIRCLE_DEGREES = 180.0
    }
}
