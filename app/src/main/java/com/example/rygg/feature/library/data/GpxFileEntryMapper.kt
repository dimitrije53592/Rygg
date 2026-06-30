package com.example.rygg.feature.library.data

import androidx.room.TypeConverter
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity
import com.example.rygg.feature.library.domain.GpxFileEntry

fun GpxFileEntryEntity.toDomain(): GpxFileEntry = GpxFileEntry(
    id = id,
    fileName = fileName,
    contentHash = contentHash,
    name = name,
    description = description,
    color = color,
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
    folder = folder,
    tags = tags,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)
