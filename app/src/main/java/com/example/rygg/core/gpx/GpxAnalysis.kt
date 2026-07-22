package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GeoPoint

data class GpxAnalysis(
    val name: String,
    val description: String,
    val distanceMeters: Double,
    val ascentMeters: Double,
    val descentMeters: Double,
    val elevationMeters: Double?,
    val pointCount: Int,
    val routeCount: Int,
    val waypointCount: Int,
    val hasTime: Boolean,
    val startTimeMillis: Long?,
    val movingTimeMillis: Long?,
    val totalTimeMillis: Long?,
    val minLat: Double?,
    val minLon: Double?,
    val maxLat: Double?,
    val maxLon: Double?,
    val creator: String?,
    val simplifiedPath: List<GeoPoint> = emptyList()
)
