package com.example.rygg.feature.library.domain

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.feature.auth.domain.Discipline

data class GpxFileEntry(
    // Identity and storage
    val id: Long,
    val fileName: String,
    val contentHash: String,
    // File metadata
    val name: String,
    val description: String,
    val color: String?,
    val discipline: Discipline,
    // Computed stats
    val distanceMeters: Double,
    val ascentMeters: Double,
    val descentMeters: Double,
    val elevationMeters: Double?,
    val pointCount: Int,
    val routeCount: Int,
    val waypointCount: Int,
    // Time related data
    val hasTime: Boolean,
    val startTimeMillis: Long?,
    val movingTimeMillis: Long?,
    val totalTimeMillis: Long?,
    // Bounds
    val minLat: Double?,
    val minLon: Double?,
    val maxLat: Double?,
    val maxLon: Double?,
    // Thumbnail geometry (downsampled trail contour)
    val pathPoints: List<GeoPoint>,
    // Organisation
    val folder: String?,
    val tags: List<String>,
    // Organisation
    val importedAt: Long,
    val updatedAt: Long,
    // Misc
    val creator: String?,
    val originalFileName: String?
)
