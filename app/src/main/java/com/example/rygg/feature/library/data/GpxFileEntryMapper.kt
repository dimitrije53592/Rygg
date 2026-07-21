package com.example.rygg.feature.library.data

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity
import com.example.rygg.feature.library.domain.GpxFileEntry

fun GpxFileEntryEntity.toDomain(): GpxFileEntry = GpxFileEntry(
    id = id,
    fileName = fileName,
    contentHash = contentHash,
    name = name,
    description = description,
    color = color,
    discipline = runCatching { Discipline.valueOf(discipline) }.getOrDefault(Discipline.HIKE),
    distanceMeters = distanceMeters,
    ascentMeters = ascentMeters,
    descentMeters = descentMeters,
    elevationMeters = elevationMeters,
    pointCount = pointCount,
    routeCount = routeCount,
    waypointCount = waypointCount,
    hasTime = hasTime,
    startTimeMillis = startTimeMillis,
    movingTimeMillis = movingTimeMillis,
    totalTimeMillis = totalTimeMillis,
    minLat = minLat,
    minLon = minLon,
    maxLat = maxLat,
    maxLon = maxLon,
    pathPoints = decodePath(thumbnailPath),
    folder = folder,
    tags = tags,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)

fun GpxFileEntry.toEntity(): GpxFileEntryEntity = GpxFileEntryEntity(
    id = id,
    fileName = fileName,
    contentHash = contentHash,
    name = name,
    description = description,
    color = color,
    discipline = discipline.name,
    distanceMeters = distanceMeters,
    ascentMeters = ascentMeters,
    descentMeters = descentMeters,
    elevationMeters = elevationMeters,
    pointCount = pointCount,
    routeCount = routeCount,
    waypointCount = waypointCount,
    hasTime = hasTime,
    startTimeMillis = startTimeMillis,
    movingTimeMillis = movingTimeMillis,
    totalTimeMillis = totalTimeMillis,
    minLat = minLat,
    minLon = minLon,
    maxLat = maxLat,
    maxLon = maxLon,
    thumbnailPath = encodePath(pathPoints),
    folder = folder,
    tags = tags,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)

private fun encodePath(points: List<GeoPoint>): String =
    points.joinToString(";") { "${it.lat},${it.lon}" }

private fun decodePath(value: String): List<GeoPoint> {
    if (value.isBlank()) return emptyList()
    return value.split(";").mapNotNull { pair ->
        val parts = pair.split(",")
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lon = parts.getOrNull(1)?.toDoubleOrNull()
        if (lat != null && lon != null) GeoPoint(lat, lon) else null
    }
}
