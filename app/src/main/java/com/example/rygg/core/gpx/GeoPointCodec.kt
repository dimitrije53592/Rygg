package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GeoPoint

// Compact polyline encoding ("lat,lon;lat,lon;…") for the thumbnail geometry, shared by the Room
// entity and the Firestore route document so the two never drift apart.

fun encodeGeoPoints(points: List<GeoPoint>): String =
    points.joinToString(";") { "${it.lat},${it.lon}" }

fun decodeGeoPoints(value: String): List<GeoPoint> {
    if (value.isBlank()) return emptyList()
    return value.split(";").mapNotNull { pair ->
        val parts = pair.split(",")
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lon = parts.getOrNull(1)?.toDoubleOrNull()
        if (lat != null && lon != null) GeoPoint(lat, lon) else null
    }
}
