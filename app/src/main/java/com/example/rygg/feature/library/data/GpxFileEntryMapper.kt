package com.example.rygg.feature.library.data

import com.example.rygg.core.gpx.decodeGeoPoints
import com.example.rygg.core.gpx.encodeGeoPoints
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity
import com.example.rygg.feature.library.domain.EntrySource
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.domain.SyncStatus

fun GpxFileEntryEntity.toDomain(): GpxFileEntry = GpxFileEntry(
    id = id,
    fileName = fileName,
    contentHash = contentHash,
    remoteId = remoteId,
    ownerUid = ownerUid,
    syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.LOCAL_ONLY),
    fileDownloaded = fileDownloaded,
    deletedAt = deletedAt,
    sharedToken = sharedToken,
    name = name,
    description = description,
    color = color,
    discipline = runCatching { Discipline.valueOf(discipline) }.getOrDefault(Discipline.HIKE),
    source = runCatching { EntrySource.valueOf(source) }.getOrDefault(EntrySource.IMPORTED),
    isFavorite = isFavorite,
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
    pathPoints = decodeGeoPoints(thumbnailPath),
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
    remoteId = remoteId,
    ownerUid = ownerUid,
    syncStatus = syncStatus.name,
    fileDownloaded = fileDownloaded,
    deletedAt = deletedAt,
    sharedToken = sharedToken,
    name = name,
    description = description,
    color = color,
    discipline = discipline.name,
    source = source.name,
    isFavorite = isFavorite,
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
    thumbnailPath = encodeGeoPoints(pathPoints),
    folder = folder,
    tags = tags,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)
