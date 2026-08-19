package com.example.rygg.feature.record.data

import android.location.Location
import com.example.rygg.core.gpx.haversineMeters
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxMetadata
import com.example.rygg.core.gpx.model.GpxPoint
import com.example.rygg.core.gpx.model.Track
import com.example.rygg.core.gpx.model.TrackSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import javax.inject.Inject

// Accumulates the recorded track + waypoints and derives live route metrics from each fix.
class RouteAccumulator @Inject constructor() {
    private val trackPoints = mutableListOf<GpxPoint>()
    private val waypoints = mutableListOf<GpxPoint>()

    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastEle: Double? = null

    private val _metrics = MutableStateFlow(RouteMetrics())
    val metrics: StateFlow<RouteMetrics> = _metrics.asStateFlow()

    fun add(location: Location) {
        val ele = if (location.hasAltitude()) location.altitude else null
        trackPoints += GpxPoint(
            lat = location.latitude,
            lon = location.longitude,
            ele = ele,
            time = Instant.now()
        )
        val addedDistance = if (lastLat != null && lastLon != null) {
            haversineMeters(lastLat!!, lastLon!!, location.latitude, location.longitude)
        } else {
            0.0
        }
        val previousEle = lastEle
        val addedAscent = if (previousEle != null && ele != null && ele - previousEle > ELEVATION_NOISE_METERS) {
            ele - previousEle
        } else {
            0.0
        }
        lastLat = location.latitude
        lastLon = location.longitude
        if (ele != null) lastEle = ele

        _metrics.update {
            it.copy(
                distanceMeters = it.distanceMeters + addedDistance,
                ascentMeters = it.ascentMeters + addedAscent,
                currentSpeedMps = if (location.hasSpeed()) location.speed.toDouble() else it.currentSpeedMps,
                elevationMeters = ele ?: it.elevationMeters,
                pointCount = trackPoints.size
            )
        }
    }

    // Drop the last known position so the first fix after a resume isn't bridged into the track:
    // no phantom distance or ascent is counted across whatever happened while paused. See #3.
    fun breakContinuity() {
        lastLat = null
        lastLon = null
        lastEle = null
    }

    fun addWaypoint(name: String) {
        val lat = lastLat ?: return
        val lon = lastLon ?: return
        waypoints += GpxPoint(lat = lat, lon = lon, ele = lastEle, time = Instant.now(), name = name, sym = "Flag")
        _metrics.update { it.copy(waypointCount = waypoints.size) }
    }

    // Build the recorded GPX document; null when nothing usable was captured.
    fun buildDocument(startInstant: Instant?, creator: String): GpxDocument? {
        if (trackPoints.size < 2) return null
        return GpxDocument(
            creator = creator,
            metadata = GpxMetadata(time = startInstant),
            waypoints = waypoints.toList(),
            tracks = listOf(Track(segments = listOf(TrackSegment(points = trackPoints.toList()))))
        )
    }

    fun reset() {
        trackPoints.clear()
        waypoints.clear()
        lastLat = null
        lastLon = null
        lastEle = null
        _metrics.value = RouteMetrics()
    }

    private companion object {
        const val ELEVATION_NOISE_METERS = 1.0
    }
}

data class RouteMetrics(
    val distanceMeters: Double = 0.0,
    val ascentMeters: Double = 0.0,
    val currentSpeedMps: Double = 0.0,
    val elevationMeters: Double? = null,
    val pointCount: Int = 0,
    val waypointCount: Int = 0
)
