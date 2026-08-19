package com.example.rygg.feature.library.ui.paramproviders

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.GpxFileEntry

// Single source of truth for GpxFileEntry samples shared across preview param providers.
internal fun previewGpxFileEntry(
    id: Long = 1L,
    name: String = "Triglav via Kredarica",
    description: String = "Classic approach to Triglav via the Kredarica hut. Cabled sections near the summit.",
    discipline: Discipline = Discipline.HIKE,
    isFavorite: Boolean = true,
    hasTime: Boolean = true,
    tags: List<String> = listOf("Mountaineering", "Two-day", "Summit")
): GpxFileEntry = GpxFileEntry(
    id = id,
    fileName = "triglav.gpx",
    contentHash = "",
    name = name,
    description = description,
    color = null,
    discipline = discipline,
    isFavorite = isFavorite,
    distanceMeters = 12_400.0,
    ascentMeters = 1_180.0,
    descentMeters = 1_180.0,
    elevationMeters = 2_864.0,
    pointCount = 3_420,
    routeCount = 1,
    waypointCount = 8,
    hasTime = hasTime,
    startTimeMillis = if (hasTime) 1_498_500_000_000 else null,
    movingTimeMillis = if (hasTime) 20_400_000 else null,
    totalTimeMillis = if (hasTime) 21_000_000 else null,
    minLat = 46.36,
    minLon = 13.83,
    maxLat = 46.39,
    maxLon = 13.86,
    pathPoints = listOf(
        GeoPoint(46.36, 13.83),
        GeoPoint(46.37, 13.84),
        GeoPoint(46.38, 13.85),
        GeoPoint(46.39, 13.86)
    ),
    folder = null,
    tags = tags,
    importedAt = 1_497_500_000_000,
    updatedAt = 1_497_500_000_000,
    creator = "MapSource",
    originalFileName = "triglav.gpx"
)
