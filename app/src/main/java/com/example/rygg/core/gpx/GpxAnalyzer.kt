package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.core.gpx.model.GpxAnalysis
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxPoint
import java.time.Instant
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil

class GpxAnalyzer @Inject constructor() {
    fun analyze(gpxDocument: GpxDocument): GpxAnalysis {
        val paths = gpxDocument.trackSegments()
        val pathPoints = paths.flatten()
        val boundsPoints = pathPoints + gpxDocument.waypoints
        val elevations = boundsPoints.mapNotNull { it.ele }
        val times = pathPoints.mapNotNull { it.time }
        val moving = movingStats(paths)

        return GpxAnalysis(
            name = resolveName(gpxDocument),
            description = gpxDocument.metadata?.desc.orEmpty(),
            distanceMeters = paths.sumOf { pathDistance(it) },
            ascentMeters = paths.sumOf { pathAscent(it) },
            descentMeters = paths.sumOf { pathDescent(it) },
            elevationMeters = elevations.maxOrNull(),
            pointCount = pathPoints.size,
            routeCount = gpxDocument.tracks.size + gpxDocument.routes.size,
            waypointCount = gpxDocument.waypoints.size,
            hasTime = times.isNotEmpty(),
            startTimeMillis = (gpxDocument.metadata?.time ?: times.minOrNull())?.toEpochMilli(),
            movingTimeMillis = moving?.movingMillis,
            totalTimeMillis = totalTimeMillis(times),
            avgSpeedMps = moving?.avgSpeedMps,
            maxSpeedMps = maxSpeedMps(paths),
            minLat = gpxDocument.metadata?.bounds?.minLat ?: boundsPoints.minOfOrNull { it.lat },
            minLon = gpxDocument.metadata?.bounds?.minLon ?: boundsPoints.minOfOrNull { it.lon },
            maxLat = gpxDocument.metadata?.bounds?.maxLat ?: boundsPoints.maxOfOrNull { it.lat },
            maxLon = gpxDocument.metadata?.bounds?.maxLon ?: boundsPoints.maxOfOrNull { it.lon },
            creator = gpxDocument.creator,
            simplifiedPath = simplifiedPath(paths, gpxDocument.waypoints)
        )
    }

    private fun simplifiedPath(paths: List<List<GpxPoint>>, waypoints: List<GpxPoint>): List<GeoPoint> {
        val source = paths.maxByOrNull { it.size }?.takeIf { it.isNotEmpty() } ?: waypoints
        if (source.isEmpty()) return emptyList()
        if (source.size <= MAX_THUMBNAIL_POINTS) return source.map { GeoPoint(it.lat, it.lon) }

        val step = ceil(source.size.toDouble() / MAX_THUMBNAIL_POINTS).toInt()
        val result = mutableListOf<GeoPoint>()
        var index = 0
        while (index < source.size) {
            result += GeoPoint(source[index].lat, source[index].lon)
            index += step
        }
        val last = source.last()
        if (result.last().lat != last.lat || result.last().lon != last.lon) {
            result += GeoPoint(last.lat, last.lon)
        }
        return result
    }

    private fun resolveName(gpxDocument: GpxDocument): String =
        gpxDocument.metadata?.name
            ?: gpxDocument.tracks.firstNotNullOfOrNull { it.name }
            ?: gpxDocument.routes.firstNotNullOfOrNull { it.name }
            ?: ""

    private fun pathDistance(points: List<GpxPoint>): Double =
        points.zipWithNext().sumOf { (a, b) -> haversineMeters(a.lat, a.lon, b.lat, b.lon) }

    private fun pathAscent(points: List<GpxPoint>): Double =
        elevationDeltas(points).filter { it > ELEVATION_NOISE_METERS }.sum()

    private fun pathDescent(points: List<GpxPoint>): Double =
        elevationDeltas(points).filter { it < -ELEVATION_NOISE_METERS }.sumOf { abs(it) }

    private fun elevationDeltas(points: List<GpxPoint>): List<Double> =
        points.zipWithNext().mapNotNull { (a, b) ->
            val from = a.ele
            val to = b.ele
            if (from != null && to != null) to - from else null
        }

    private fun totalTimeMillis(times: List<Instant>): Long? {
        if (times.isEmpty()) return null
        return times.max().toEpochMilli() - times.min().toEpochMilli()
    }

    // Peak speed measured over a rolling window (>= SPEED_WINDOW_SECONDS) rather than per fix
    // pair: a single GPS jitter jump reads as a huge instantaneous speed, but averaged across a
    // few seconds of travel it disappears. Windows never span a pause gap > MAX_PAUSE_GAP_SECONDS.
    private fun maxSpeedMps(paths: List<List<GpxPoint>>): Double? {
        var maxSpeed: Double? = null
        paths.forEach { points ->
            timedRuns(points).forEach { run ->
                // Cumulative distance/time along the run so any window is an O(1) difference.
                val cumMeters = DoubleArray(run.size)
                val elapsedSeconds = DoubleArray(run.size)
                for (i in 1 until run.size) {
                    val a = run[i - 1]
                    val b = run[i]
                    cumMeters[i] = cumMeters[i - 1] + haversineMeters(a.lat, a.lon, b.lat, b.lon)
                    elapsedSeconds[i] = elapsedSeconds[i - 1] +
                        (b.time!!.toEpochMilli() - a.time!!.toEpochMilli()) / MILLIS_PER_SECOND
                }
                var start = 0
                for (end in 1 until run.size) {
                    // Tightest window that still spans >= SPEED_WINDOW_SECONDS.
                    while (elapsedSeconds[end] - elapsedSeconds[start + 1] >= SPEED_WINDOW_SECONDS) start++
                    val windowSeconds = elapsedSeconds[end] - elapsedSeconds[start]
                    if (windowSeconds >= SPEED_WINDOW_SECONDS) {
                        val speed = (cumMeters[end] - cumMeters[start]) / windowSeconds
                        if (maxSpeed == null || speed > maxSpeed!!) maxSpeed = speed
                    }
                }
            }
        }
        return maxSpeed
    }

    // Moving time and average moving speed share a basis: both accumulate only over pairs whose
    // speed clears MIN_MOVING_SPEED_MPS, so avg speed = moving distance / moving time (never
    // inflated by dividing full distance by a shrunken moving-time denominator).
    private fun movingStats(paths: List<List<GpxPoint>>): MovingStats? {
        var hasTimedPair = false
        var movingSeconds = 0.0
        var movingMeters = 0.0
        paths.forEach { points ->
            points.zipWithNext().forEach { (a, b) ->
                val from = a.time
                val to = b.time
                if (from != null && to != null) {
                    hasTimedPair = true
                    val dt = (to.toEpochMilli() - from.toEpochMilli()) / MILLIS_PER_SECOND
                    if (dt > 0 && dt <= MAX_PAUSE_GAP_SECONDS) {
                        val meters = haversineMeters(a.lat, a.lon, b.lat, b.lon)
                        if (meters / dt >= MIN_MOVING_SPEED_MPS) {
                            movingSeconds += dt
                            movingMeters += meters
                        }
                    }
                }
            }
        }
        if (!hasTimedPair) return null
        return MovingStats(
            movingMillis = (movingSeconds * MILLIS_PER_SECOND).toLong(),
            avgSpeedMps = if (movingSeconds > 0) movingMeters / movingSeconds else null
        )
    }

    // Split a segment into maximal runs of consecutive timed points, cutting where a pair is
    // untimed or its gap exceeds MAX_PAUSE_GAP_SECONDS (a stop the windowed speed must not bridge).
    private fun timedRuns(points: List<GpxPoint>): List<List<GpxPoint>> {
        val runs = mutableListOf<List<GpxPoint>>()
        var current = mutableListOf<GpxPoint>()
        points.forEach { point ->
            val previous = current.lastOrNull()
            val gapSeconds = if (previous?.time != null && point.time != null) {
                (point.time.toEpochMilli() - previous.time.toEpochMilli()) / MILLIS_PER_SECOND
            } else {
                null
            }
            if (point.time == null || (gapSeconds != null && gapSeconds > MAX_PAUSE_GAP_SECONDS)) {
                if (current.size >= 2) runs += current
                current = mutableListOf()
            }
            if (point.time != null) current += point
        }
        if (current.size >= 2) runs += current
        return runs
    }

    private data class MovingStats(val movingMillis: Long, val avgSpeedMps: Double?)

    private companion object {
        const val ELEVATION_NOISE_METERS = 1.0
        const val MIN_MOVING_SPEED_MPS = 0.8
        const val MAX_PAUSE_GAP_SECONDS = 60.0
        const val SPEED_WINDOW_SECONDS = 5.0
        const val MILLIS_PER_SECOND = 1000.0
        const val MAX_THUMBNAIL_POINTS = 48
    }
}
