package com.example.rygg.feature.map.domain

import com.example.rygg.core.gpx.EARTH_RADIUS_METERS
import com.example.rygg.core.gpx.haversineMeters
import com.example.rygg.core.gpx.model.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

const val ON_ROUTE_THRESHOLD_M = 60.0

// How far ahead on the resampled grid a camera bearing looks, in samples.
private const val BEARING_LOOKAHEAD_SAMPLES = 8

data class TourSample(
    val lat: Double,
    val lon: Double,
    val bearing: Double
)

class RouteGeometry private constructor(
    val points: List<GeoPoint>,
    private val cumulative: DoubleArray,
    val totalMeters: Double
) {
    val end: GeoPoint? get() = points.lastOrNull()

    // Distance to the nearest point on the trail (projected onto each segment, not just the
    // nearest vertex) so on/off-route stays accurate for any trail shape and sparse polylines.
    fun progressFor(lat: Double, lon: Double): RouteProgress {
        if (points.isEmpty()) return RouteProgress.EMPTY
        if (points.size == 1) {
            return RouteProgress(haversineMeters(lat, lon, points[0].lat, points[0].lon))
        }
        var nearestDist = Double.MAX_VALUE
        for (index in 0 until points.size - 1) {
            val distance = distanceToSegmentMeters(lat, lon, points[index], points[index + 1])
            if (distance < nearestDist) nearestDist = distance
        }
        return RouteProgress(distanceToRouteMeters = nearestDist)
    }

    fun evenSamples(count: Int): List<TourSample> {
        if (points.size < 2 || totalMeters <= 0.0) {
            return points.map { TourSample(it.lat, it.lon, 0.0) }
        }
        val positions = ArrayList<GeoPoint>(count + 1)
        var segment = 0
        for (step in 0..count) {
            val target = totalMeters * step / count
            while (segment < points.size - 2 && cumulative[segment + 1] < target) segment++
            val segStart = cumulative[segment]
            val segLength = (cumulative[segment + 1] - segStart).coerceAtLeast(1e-9)
            val fraction = ((target - segStart) / segLength).coerceIn(0.0, 1.0)
            val a = points[segment]
            val b = points[segment + 1]
            positions += GeoPoint(
                lat = a.lat + (b.lat - a.lat) * fraction,
                lon = a.lon + (b.lon - a.lon) * fraction
            )
        }

        // Bearings look ahead along the resampled grid rather than reading the source segment's
        // heading. Taking it from the source made the heading piecewise-constant, so the camera
        // sat on one angle across a whole segment and then snapped at the vertex; looking ahead
        // spreads the turn out and lets the camera anticipate it.
        val lastIndex = positions.lastIndex
        return positions.mapIndexed { index, position ->
            val ahead = positions[minOf(index + BEARING_LOOKAHEAD_SAMPLES, lastIndex)]
            val bearingFrom = if (ahead == position && index > 0) positions[index - 1] else position
            TourSample(
                lat = position.lat,
                lon = position.lon,
                bearing = if (bearingFrom == ahead) 0.0 else segmentBearing(bearingFrom, ahead)
            )
        }
    }

    companion object {
        fun from(paths: List<List<GeoPoint>>): RouteGeometry {
            val points = paths.filter { it.isNotEmpty() }.flatten()
            if (points.isEmpty()) return RouteGeometry(emptyList(), DoubleArray(0), 0.0)
            val cumulative = DoubleArray(points.size)
            var total = 0.0
            for (index in 1 until points.size) {
                val previous = points[index - 1]
                val current = points[index]
                total += haversineMeters(previous.lat, previous.lon, current.lat, current.lon)
                cumulative[index] = total
            }
            return RouteGeometry(points, cumulative, total)
        }
    }
}

// Perpendicular distance from a point to a segment, using a local equirectangular projection
// to meters (accurate over the short spans between consecutive trail points).
private fun distanceToSegmentMeters(lat: Double, lon: Double, a: GeoPoint, b: GeoPoint): Double {
    val latRefRad = Math.toRadians(a.lat)

    fun x(pLon: Double) = Math.toRadians(pLon - a.lon) * cos(latRefRad) * EARTH_RADIUS_METERS

    fun y(pLat: Double) = Math.toRadians(pLat - a.lat) * EARTH_RADIUS_METERS

    val px = x(lon)
    val py = y(lat)
    val bx = x(b.lon)
    val by = y(b.lat)

    val segLengthSq = bx * bx + by * by
    if (segLengthSq == 0.0) return hypot(px, py)

    val t = ((px * bx + py * by) / segLengthSq).coerceIn(0.0, 1.0)
    return hypot(px - bx * t, py - by * t)
}

private fun segmentBearing(from: GeoPoint, to: GeoPoint): Double {
    val lat1 = Math.toRadians(from.lat)
    val lat2 = Math.toRadians(to.lat)
    val dLon = Math.toRadians(to.lon - from.lon)
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}

data class RouteProgress(
    val distanceToRouteMeters: Double
) {
    fun isOnRoute(accuracyMeters: Float): Boolean =
        distanceToRouteMeters <= ON_ROUTE_THRESHOLD_M + accuracyMeters

    companion object {
        val EMPTY = RouteProgress(distanceToRouteMeters = Double.MAX_VALUE)
    }
}
