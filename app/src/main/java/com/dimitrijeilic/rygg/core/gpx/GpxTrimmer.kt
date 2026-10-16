package com.dimitrijeilic.rygg.core.gpx

import com.dimitrijeilic.rygg.core.gpx.model.GpxDocument
import com.dimitrijeilic.rygg.core.gpx.model.GpxPoint
import javax.inject.Inject

class GpxTrimmer @Inject constructor() {
    fun trim(document: GpxDocument, startFraction: Double, endFraction: Double): GpxDocument {
        require(startFraction < endFraction) { "Empty trim range" }
        val total = document.trackSegments().sumOf { it.lengthMeters() }
        val fromMeters = total * startFraction.coerceIn(0.0, 1.0)
        val toMeters = total * endFraction.coerceIn(0.0, 1.0)

        var travelled = 0.0
        fun keep(points: List<GpxPoint>): List<GpxPoint> {
            val kept = mutableListOf<GpxPoint>()
            points.forEachIndexed { index, point ->
                if (index > 0) {
                    val previous = points[index - 1]
                    travelled += haversineMeters(previous.lat, previous.lon, point.lat, point.lon)
                }
                if (travelled in fromMeters..toMeters) kept += point
            }
            return kept
        }

        val tracks = document.tracks.mapNotNull { track ->
            val segments = track.segments
                .map { segment -> segment.copy(points = keep(segment.points)) }
                .filter { it.points.isNotEmpty() }
            if (segments.isEmpty()) null else track.copy(segments = segments)
        }
        val routes = document.routes.mapNotNull { route ->
            val points = keep(route.points)
            if (points.isEmpty()) null else route.copy(points = points)
        }

        val keptPoints = tracks.flatMap { track -> track.segments.flatMap { it.points } } +
            routes.flatMap { it.points }
        require(keptPoints.size >= MIN_KEPT_POINTS) { "Trim range keeps fewer than $MIN_KEPT_POINTS points" }

        return document.copy(
            metadata = document.metadata?.copy(bounds = null),
            tracks = tracks,
            routes = routes,
            waypoints = document.waypoints.filter { it.isNear(keptPoints) }
        )
    }

    private fun List<GpxPoint>.lengthMeters(): Double =
        zipWithNext().sumOf { (a, b) -> haversineMeters(a.lat, a.lon, b.lat, b.lon) }

    private fun GpxPoint.isNear(points: List<GpxPoint>): Boolean =
        points.any { haversineMeters(lat, lon, it.lat, it.lon) <= WAYPOINT_RADIUS_METERS }

    private companion object {
        const val MIN_KEPT_POINTS = 2
        const val WAYPOINT_RADIUS_METERS = 50.0
    }
}
