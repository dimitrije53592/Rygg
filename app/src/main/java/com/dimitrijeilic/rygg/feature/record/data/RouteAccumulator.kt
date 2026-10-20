package com.dimitrijeilic.rygg.feature.record.data

import android.location.Location
import com.dimitrijeilic.rygg.core.gpx.haversineMeters
import com.dimitrijeilic.rygg.core.gpx.model.GpxDocument
import com.dimitrijeilic.rygg.core.gpx.model.GpxMetadata
import com.dimitrijeilic.rygg.core.gpx.model.GpxPoint
import com.dimitrijeilic.rygg.core.gpx.model.Track
import com.dimitrijeilic.rygg.core.gpx.model.TrackSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import javax.inject.Inject

// Accumulates the recorded track + waypoints and derives live route metrics from each fix.
class RouteAccumulator @Inject constructor() {
    private val segments = mutableListOf(mutableListOf<GpxPoint>())
    private val waypoints = mutableListOf<GpxPoint>()

    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastEle: Double? = null

    private val _metrics = MutableStateFlow(RouteMetrics())
    val metrics: StateFlow<RouteMetrics> = _metrics.asStateFlow()

    val currentSegment: Int get() = segments.lastIndex

    fun add(location: Location): GpxPoint {
        val point = GpxPoint(
            lat = location.latitude,
            lon = location.longitude,
            ele = if (location.hasAltitude()) location.altitude else null,
            time = Instant.now()
        )
        accumulate(point, speedMps = if (location.hasSpeed()) location.speed.toDouble() else null)
        return point
    }

    // Drop the last known position so the first fix after a resume isn't bridged into the track:
    // no phantom distance or ascent is counted across whatever happened while paused. See #3.
    // The gap also opens a new GPX segment, so the track isn't drawn straight across it.
    fun breakContinuity() {
        lastLat = null
        lastLon = null
        lastEle = null
        if (segments.last().isNotEmpty()) segments += mutableListOf<GpxPoint>()
    }

    fun addWaypoint(name: String): GpxPoint? {
        val lat = lastLat ?: return null
        val lon = lastLon ?: return null
        val waypoint = GpxPoint(lat = lat, lon = lon, ele = lastEle, time = Instant.now(), name = name, sym = WAYPOINT_SYMBOL)
        waypoints += waypoint
        _metrics.update { it.copy(waypointCount = waypoints.size) }
        return waypoint
    }

    fun restore(recordedSegments: List<List<GpxPoint>>, recordedWaypoints: List<GpxPoint>) {
        reset()
        segments.clear()
        recordedSegments.filter { it.isNotEmpty() }.forEach { segment ->
            segments += mutableListOf<GpxPoint>()
            lastLat = null
            lastLon = null
            lastEle = null
            segment.forEach { accumulate(it, speedMps = null) }
        }
        if (segments.isEmpty()) segments += mutableListOf<GpxPoint>()
        waypoints += recordedWaypoints
        _metrics.update { it.copy(waypointCount = waypoints.size) }
    }

    // Build the recorded GPX document; null when nothing usable was captured.
    fun buildDocument(startInstant: Instant?, creator: String): GpxDocument? {
        val recorded = segments.filter { it.isNotEmpty() }
        if (recorded.sumOf { it.size } < 2) return null
        return GpxDocument(
            creator = creator,
            metadata = GpxMetadata(time = startInstant),
            waypoints = waypoints.toList(),
            tracks = listOf(Track(segments = recorded.map { TrackSegment(points = it.toList()) }))
        )
    }

    fun reset() {
        segments.clear()
        segments += mutableListOf<GpxPoint>()
        waypoints.clear()
        lastLat = null
        lastLon = null
        lastEle = null
        _metrics.value = RouteMetrics()
    }

    private fun accumulate(point: GpxPoint, speedMps: Double?) {
        segments.last() += point
        val previousLat = lastLat
        val previousLon = lastLon
        val addedDistance = if (previousLat != null && previousLon != null) {
            haversineMeters(previousLat, previousLon, point.lat, point.lon)
        } else {
            0.0
        }
        val ele = point.ele
        val previousEle = lastEle
        val addedAscent = if (previousEle != null && ele != null && ele - previousEle > ELEVATION_NOISE_METERS) {
            ele - previousEle
        } else {
            0.0
        }
        lastLat = point.lat
        lastLon = point.lon
        if (ele != null) lastEle = ele

        _metrics.update {
            it.copy(
                distanceMeters = it.distanceMeters + addedDistance,
                ascentMeters = it.ascentMeters + addedAscent,
                currentSpeedMps = speedMps ?: it.currentSpeedMps,
                elevationMeters = ele ?: it.elevationMeters,
                pointCount = segments.sumOf { segment -> segment.size }
            )
        }
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
