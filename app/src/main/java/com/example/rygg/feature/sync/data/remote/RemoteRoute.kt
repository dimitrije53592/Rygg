package com.example.rygg.feature.sync.data.remote

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity
import com.example.rygg.feature.library.domain.EntrySource
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.domain.SyncStatus

// Firestore document shape for a synced route's metadata (stored under
// users/{uid}/routes/{remoteId}). All fields have defaults so Firestore's reflective
// deserialization (toObject) has a no-arg constructor to call. Geometry is the same
// encoded thumbnail string the Room entity uses, so no GeoPoint re-encoding is needed.
data class RemoteRoute(
    val remoteId: String = "",
    val name: String = "",
    val description: String = "",
    val color: String? = null,
    val discipline: String = "",
    val source: String = "",
    val isFavorite: Boolean = false,
    val distanceMeters: Double = 0.0,
    val ascentMeters: Double = 0.0,
    val descentMeters: Double = 0.0,
    val elevationMeters: Double? = null,
    val pointCount: Int = 0,
    val routeCount: Int = 0,
    val waypointCount: Int = 0,
    val hasTime: Boolean = false,
    val startTimeMillis: Long? = null,
    val movingTimeMillis: Long? = null,
    val totalTimeMillis: Long? = null,
    val minLat: Double? = null,
    val minLon: Double? = null,
    val maxLat: Double? = null,
    val maxLon: Double? = null,
    val thumbnailPath: String = "",
    val folder: String? = null,
    val tags: List<String> = emptyList(),
    val importedAt: Long = 0,
    val updatedAt: Long = 0,
    val creator: String? = null,
    val originalFileName: String? = null,
    val contentHash: String = "",
    val storagePath: String? = null,
    val deletedAt: Long? = null
)

// Public snapshot created at share time (stored under sharedRoutes/{token}); readable by
// anyone holding the unguessable token, decoupled from the owner's private library.
data class SharedRoute(
    val token: String = "",
    val ownerUid: String = "",
    val sharedStoragePath: String = "",
    val route: RemoteRoute = RemoteRoute()
)

// A transient, read-only domain entry for rendering a shared route in the Details screen
// (not persisted). Geometry comes from the encoded thumbnail, so no file is needed.
fun RemoteRoute.toPreviewEntry(): GpxFileEntry = GpxFileEntry(
    id = 0L,
    fileName = "",
    contentHash = contentHash,
    remoteId = remoteId,
    ownerUid = null,
    syncStatus = SyncStatus.LOCAL_ONLY,
    fileDownloaded = false,
    deletedAt = null,
    sharedToken = null,
    name = name,
    description = description,
    color = color,
    discipline = runCatching { Discipline.valueOf(discipline) }.getOrDefault(Discipline.HIKE),
    source = runCatching { EntrySource.valueOf(source) }.getOrDefault(EntrySource.IMPORTED),
    isFavorite = false,
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
    pathPoints = decodeThumbnail(thumbnailPath),
    folder = folder,
    tags = tags,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)

private fun decodeThumbnail(value: String): List<GeoPoint> {
    if (value.isBlank()) return emptyList()
    return value.split(";").mapNotNull { pair ->
        val parts = pair.split(",")
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lon = parts.getOrNull(1)?.toDoubleOrNull()
        if (lat != null && lon != null) GeoPoint(lat, lon) else null
    }
}

fun GpxFileEntryEntity.toRemote(remoteId: String, storagePath: String): RemoteRoute = RemoteRoute(
    remoteId = remoteId,
    name = name,
    description = description,
    color = color,
    discipline = discipline,
    source = source,
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
    thumbnailPath = thumbnailPath,
    folder = folder,
    tags = tags,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName,
    contentHash = contentHash,
    storagePath = storagePath,
    deletedAt = deletedAt
)

// A brand-new local row for a route first seen on another device. The file isn't here yet
// (fileDownloaded = false); a download worker fills fileName in later.
fun RemoteRoute.toNewEntity(ownerUid: String): GpxFileEntryEntity = GpxFileEntryEntity(
    id = 0,
    fileName = "",
    contentHash = contentHash,
    remoteId = remoteId,
    ownerUid = ownerUid,
    syncStatus = "SYNCED",
    fileDownloaded = false,
    deletedAt = null,
    sharedToken = null,
    name = name,
    description = description,
    color = color,
    discipline = discipline,
    source = source,
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
    thumbnailPath = thumbnailPath,
    folder = folder,
    tags = tags,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)

// Apply newer remote metadata onto an existing local row, preserving local-only concerns
// (row id, on-disk file, download + sync bookkeeping).
fun RemoteRoute.toUpdatedEntity(local: GpxFileEntryEntity): GpxFileEntryEntity = local.copy(
    name = name,
    description = description,
    color = color,
    discipline = discipline,
    source = source,
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
    thumbnailPath = thumbnailPath,
    folder = folder,
    tags = tags,
    updatedAt = updatedAt
)

// A saved copy of someone else's shared route: a fresh, unsynced local row owned by the
// saver (or a guest row when signed out). The file is already on disk (fileDownloaded).
fun RemoteRoute.toSavedCopyEntity(fileName: String, ownerUid: String?, now: Long): GpxFileEntryEntity =
    GpxFileEntryEntity(
        id = 0,
        fileName = fileName,
        contentHash = contentHash,
        remoteId = null,
        ownerUid = ownerUid,
        syncStatus = "LOCAL_ONLY",
        fileDownloaded = true,
        deletedAt = null,
        sharedToken = null,
        name = name,
        description = description,
        color = color,
        discipline = discipline,
        source = source,
        isFavorite = false,
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
        thumbnailPath = thumbnailPath,
        folder = folder,
        tags = tags,
        importedAt = now,
        updatedAt = now,
        creator = creator,
        originalFileName = originalFileName
    )
