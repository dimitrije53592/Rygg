package com.dimitrijeilic.rygg.feature.offline.domain

import com.dimitrijeilic.rygg.core.gpx.haversineMeters
import com.dimitrijeilic.rygg.core.gpx.model.GeoPoint
import kotlinx.serialization.Serializable

@Serializable
data class RegionBounds(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double
) {
    val center: GeoPoint get() = GeoPoint(lat = (south + north) / 2, lon = (west + east) / 2)

    fun contains(point: GeoPoint): Boolean =
        point.lat in south..north && point.lon in west..east

    fun contains(other: RegionBounds): Boolean =
        other.south >= south && other.north <= north && other.west >= west && other.east <= east

    fun areaSquareMeters(): Double {
        val width = haversineMeters(center.lat, west, center.lat, east)
        val height = haversineMeters(south, center.lon, north, center.lon)
        return width * height
    }

    fun padded(fraction: Double): RegionBounds {
        val latPad = (north - south) * fraction
        val lonPad = (east - west) * fraction
        return RegionBounds(
            south = (south - latPad).coerceAtLeast(-MAX_LATITUDE),
            west = (west - lonPad).coerceAtLeast(-180.0),
            north = (north + latPad).coerceAtMost(MAX_LATITUDE),
            east = (east + lonPad).coerceAtMost(180.0)
        )
    }

    companion object {
        const val MAX_LATITUDE = 85.0511

        fun of(points: List<GeoPoint>): RegionBounds? {
            if (points.isEmpty()) return null
            return RegionBounds(
                south = points.minOf { it.lat },
                west = points.minOf { it.lon },
                north = points.maxOf { it.lat },
                east = points.maxOf { it.lon }
            )
        }
    }
}
