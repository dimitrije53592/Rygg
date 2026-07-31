package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val EARTH_RADIUS_METERS = 6_371_000.0

fun GpxDocument.trackSegments(): List<List<GpxPoint>> =
    tracks.flatMap { track -> track.segments.map { it.points } } + routes.map { it.points }

fun GpxDocument.trackPaths(): List<List<GeoPoint>> =
    trackSegments().map { segment -> segment.map { GeoPoint(it.lat, it.lon) } }

fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
}
