package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxPoint
import java.time.Instant
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GpxAnalyzer @Inject constructor() {
    fun analyze(gpxDocument: GpxDocument): GpxAnalysis {
        val paths = buildPaths(gpxDocument)
        val pathPoints = paths.flatten()
        val boundsPoints = pathPoints + gpxDocument.waypoints
        val elevations = boundsPoints.mapNotNull { it.ele }
        val times = pathPoints.mapNotNull { it.time }

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
            movingTimeMillis = movingTimeMillis(paths),
            totalTimeMillis = totalTimeMillis(times),
            minLat = gpxDocument.metadata?.bounds?.minLat ?: boundsPoints.minOfOrNull { it.lat },
            minLon = gpxDocument.metadata?.bounds?.minLon ?: boundsPoints.minOfOrNull { it.lon },
            maxLat = gpxDocument.metadata?.bounds?.maxLat ?: boundsPoints.maxOfOrNull { it.lat },
            maxLon = gpxDocument.metadata?.bounds?.maxLon ?: boundsPoints.maxOfOrNull { it.lon },
            creator = gpxDocument.creator
        )
    }

    private fun buildPaths(gpxDocument: GpxDocument): List<List<GpxPoint>> =
        gpxDocument.tracks.flatMap { track -> track.segments.map { it.points } } +
            gpxDocument.routes.map { it.points }

    private fun resolveName(gpxDocument: GpxDocument): String =
        gpxDocument.metadata?.name
            ?: gpxDocument.tracks.firstNotNullOfOrNull { it.name }
            ?: gpxDocument.routes.firstNotNullOfOrNull { it.name }
            ?: ""

    private fun pathDistance(points: List<GpxPoint>): Double =
        points.zipWithNext().sumOf { (a, b) -> haversine(a.lat, a.lon, b.lat, b.lon) }

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

    private fun movingTimeMillis(paths: List<List<GpxPoint>>): Long? {
        var hasTimedPair = false
        var movingSeconds = 0.0
        paths.forEach { points ->
            points.zipWithNext().forEach { (a, b) ->
                val from = a.time
                val to = b.time
                if (from != null && to != null) {
                    hasTimedPair = true
                    val dt = (to.toEpochMilli() - from.toEpochMilli()) / MILLIS_PER_SECOND
                    if (dt > 0 && dt <= MAX_PAUSE_GAP_SECONDS) {
                        val speed = haversine(a.lat, a.lon, b.lat, b.lon) / dt
                        if (speed >= MIN_MOVING_SPEED_MPS) {
                            movingSeconds += dt
                        }
                    }
                }
            }
        }
        return if (hasTimedPair) (movingSeconds * MILLIS_PER_SECOND).toLong() else null
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private companion object {
        const val EARTH_RADIUS_METERS = 6_371_000.0
        const val ELEVATION_NOISE_METERS = 1.0
        const val MIN_MOVING_SPEED_MPS = 0.8
        const val MAX_PAUSE_GAP_SECONDS = 60.0
        const val MILLIS_PER_SECOND = 1000.0
    }
}
