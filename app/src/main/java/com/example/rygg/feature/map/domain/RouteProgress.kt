package com.example.rygg.feature.map.domain

import com.example.rygg.core.gpx.haversineMeters
import com.example.rygg.core.gpx.model.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

const val ON_ROUTE_THRESHOLD_M = 60.0

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

    fun progressFor(lat: Double, lon: Double): RouteProgress {
        if (points.isEmpty()) return RouteProgress.EMPTY
        var nearestIndex = 0
        var nearestDist = Double.MAX_VALUE
        points.forEachIndexed { index, point ->
            val distance = haversineMeters(lat, lon, point.lat, point.lon)
            if (distance < nearestDist) {
                nearestDist = distance
                nearestIndex = index
            }
        }
        val remaining = (totalMeters - cumulative[nearestIndex]).coerceAtLeast(0.0)
        val fraction = if (totalMeters > 0.0) {
            (cumulative[nearestIndex] / totalMeters).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
        return RouteProgress(
            nearestIndex = nearestIndex,
            distanceToRouteMeters = nearestDist,
            distanceRemainingMeters = remaining,
            fractionComplete = fraction
        )
    }

    fun evenSamples(count: Int): List<TourSample> {
        if (points.size < 2 || totalMeters <= 0.0) {
            return points.map { TourSample(it.lat, it.lon, 0.0) }
        }
        val samples = ArrayList<TourSample>(count + 1)
        var segment = 0
        for (step in 0..count) {
            val target = totalMeters * step / count
            while (segment < points.size - 2 && cumulative[segment + 1] < target) segment++
            val segStart = cumulative[segment]
            val segLength = (cumulative[segment + 1] - segStart).coerceAtLeast(1e-9)
            val fraction = ((target - segStart) / segLength).coerceIn(0.0, 1.0)
            val a = points[segment]
            val b = points[segment + 1]
            samples += TourSample(
                lat = a.lat + (b.lat - a.lat) * fraction,
                lon = a.lon + (b.lon - a.lon) * fraction,
                bearing = segmentBearing(a, b)
            )
        }
        return samples
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

private fun segmentBearing(from: GeoPoint, to: GeoPoint): Double {
    val lat1 = Math.toRadians(from.lat)
    val lat2 = Math.toRadians(to.lat)
    val dLon = Math.toRadians(to.lon - from.lon)
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}

data class RouteProgress(
    val nearestIndex: Int,
    val distanceToRouteMeters: Double,
    val distanceRemainingMeters: Double,
    val fractionComplete: Double
) {
    fun isOnRoute(accuracyMeters: Float): Boolean =
        distanceToRouteMeters <= ON_ROUTE_THRESHOLD_M + accuracyMeters

    companion object {
        val EMPTY = RouteProgress(
            nearestIndex = 0,
            distanceToRouteMeters = Double.MAX_VALUE,
            distanceRemainingMeters = 0.0,
            fractionComplete = 0.0
        )
    }
}
