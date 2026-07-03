package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.feature.library.domain.GpxFileEntry
import javax.inject.Inject

class GpxAnalyzer @Inject constructor() {
    fun analyze(gpxDocument: GpxDocument): GpxFileEntry {
        return GpxFileEntry(
            id = 0L,
            fileName = "",
            contentHash = "",
            name = "",
            description = "",
            color = null,
            distanceMeters = 0.0,
            ascentMeters = 0.0,
            descentMeters = 0.0,
            elevationMeters = null,
            pointCount = 0,
            routeCount = 0,
            waypointCount = 0,
            hasTime = false,
            startTimeMillis = null,
            movingTimeMillis = null,
            totalTimeMillis = null,
            minLat = null,
            minLon = null,
            maxLat = null,
            maxLon = null,
            folder = null,
            tags = emptyList(),
            importedAt = 0L,
            updatedAt = 0L,
            creator = null,
            originalFileName = null
        )
    }
}