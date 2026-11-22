package com.dimitrijeilic.rygg.feature.offline.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

data class DownloadEstimate(
    val tileCount: Long,
    val areaSquareMeters: Double
) {
    val isTooLarge: Boolean get() = tileCount > MAX_TILES

    companion object {
        const val MIN_ZOOM = 0
        const val MAX_ZOOM = 14
        const val MAX_TILES = 2_800L

        const val MAX_PACK_BYTES = 50L * 1024 * 1024

        fun of(bounds: RegionBounds): DownloadEstimate = DownloadEstimate(
            tileCount = tileCount(bounds, MIN_ZOOM, MAX_ZOOM),
            areaSquareMeters = bounds.areaSquareMeters()
        )

        fun tileCount(bounds: RegionBounds, minZoom: Int, maxZoom: Int): Long =
            (minZoom..maxZoom).sumOf { zoom ->
                val columns = tileX(bounds.east, zoom) - tileX(bounds.west, zoom) + 1
                val rows = tileY(bounds.south, zoom) - tileY(bounds.north, zoom) + 1
                columns.toLong() * rows.toLong()
            }

        private fun tileX(lon: Double, zoom: Int): Int {
            val n = 1 shl zoom
            return floor((lon + 180.0) / 360.0 * n).toInt().coerceIn(0, n - 1)
        }

        private fun tileY(lat: Double, zoom: Int): Int {
            val n = 1 shl zoom
            val clamped = lat.coerceIn(-RegionBounds.MAX_LATITUDE, RegionBounds.MAX_LATITUDE)
            val rad = Math.toRadians(clamped)
            val y = (1.0 - ln(tan(rad) + 1.0 / cos(rad)) / PI) / 2.0 * n
            return floor(y).toInt().coerceIn(0, n - 1)
        }
    }
}
