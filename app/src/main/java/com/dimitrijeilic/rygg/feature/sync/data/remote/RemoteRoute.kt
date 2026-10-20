package com.dimitrijeilic.rygg.feature.sync.data.remote

import com.dimitrijeilic.rygg.core.gpx.decodeGeoPoints
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryEntity
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageEntity
import com.dimitrijeilic.rygg.feature.library.domain.EntrySource
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry
import com.dimitrijeilic.rygg.feature.library.domain.SyncStatus

// Firestore document shape for a synced route's metadata (users/{uid}/routes/{remoteId}). Every
// field has a default so Firestore's reflective toObject() can no-arg construct it.
data class RemoteRoute(
    val remoteId: String = "",
    val name: String = "",
    val description: String = "",
    val color: String? = null,
    val discipline: String = "",
    val source: String = "",
    val favorite: Boolean = false,
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
    val avgSpeedMps: Double? = null,
    val maxSpeedMps: Double? = null,
    val minLat: Double? = null,
    val minLon: Double? = null,
    val maxLat: Double? = null,
    val maxLon: Double? = null,
    val thumbnailPath: String = "",
    val folder: String? = null,
    val importedAt: Long = 0,
    val updatedAt: Long = 0,
    val creator: String? = null,
    val originalFileName: String? = null,
    val contentHash: String = "",
    val storagePath: String? = null,
    val deletedAt: Long? = null,
    // Only photos whose bytes are already in Storage; shared snapshots always leave this empty.
    val images: List<RemoteImage> = emptyList()
)

data class RemoteImage(
    val id: String = "",
    val position: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Long = 0
)

fun RouteImageEntity.toRemote(): RemoteImage = RemoteImage(
    id = id,
    position = position,
    width = width,
    height = height,
    createdAt = createdAt
)

// A photo first seen in a pulled doc: its bytes are in Storage but not on this device yet.
fun RemoteImage.toNewEntity(entryId: Long): RouteImageEntity = RouteImageEntity(
    id = id,
    entryId = entryId,
    position = position,
    width = width,
    height = height,
    createdAt = createdAt,
    fileDownloaded = false,
    fileUploaded = true
)

// Public snapshot created at share time (sharedRoutes/{token}); readable by anyone with the token.
data class SharedRoute(
    val token: String = "",
    val ownerUid: String = "",
    val sharedStoragePath: String = "",
    val route: RemoteRoute = RemoteRoute()
)

// A transient, unpersisted domain entry for previewing a shared route in Details (geometry from
// the thumbnail, so no file needed).
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
    avgSpeedMps = avgSpeedMps,
    maxSpeedMps = maxSpeedMps,
    minLat = minLat,
    minLon = minLon,
    maxLat = maxLat,
    maxLon = maxLon,
    pathPoints = decodeGeoPoints(thumbnailPath),
    folder = folder,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)

fun GpxFileEntryEntity.toRemote(
    remoteId: String,
    storagePath: String,
    images: List<RemoteImage> = emptyList()
): RemoteRoute = RemoteRoute(
    remoteId = remoteId,
    name = name,
    description = description,
    color = color,
    discipline = discipline,
    source = source,
    favorite = isFavorite,
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
    avgSpeedMps = avgSpeedMps,
    maxSpeedMps = maxSpeedMps,
    minLat = minLat,
    minLon = minLon,
    maxLat = maxLat,
    maxLon = maxLon,
    thumbnailPath = thumbnailPath,
    folder = folder,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName,
    contentHash = contentHash,
    storagePath = storagePath,
    deletedAt = deletedAt,
    images = images
)

// A new local row for a route first seen on another device; its file downloads later.
fun RemoteRoute.toNewEntity(ownerUid: String): GpxFileEntryEntity = GpxFileEntryEntity(
    id = 0,
    fileName = "",
    contentHash = contentHash,
    remoteId = remoteId,
    ownerUid = ownerUid,
    syncStatus = "SYNCED",
    fileDownloaded = false,
    fileUploaded = true,
    deletedAt = null,
    sharedToken = null,
    name = name,
    description = description,
    color = color,
    discipline = discipline,
    source = source,
    isFavorite = favorite,
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
    avgSpeedMps = avgSpeedMps,
    maxSpeedMps = maxSpeedMps,
    minLat = minLat,
    minLon = minLon,
    maxLat = maxLat,
    maxLon = maxLon,
    thumbnailPath = thumbnailPath,
    folder = folder,
    importedAt = importedAt,
    updatedAt = updatedAt,
    creator = creator,
    originalFileName = originalFileName
)

// Apply newer remote metadata onto an existing local row, preserving local-only fields (id, file,
// sync bookkeeping). contentHash is carried over so the caller can detect a changed .gpx.
fun RemoteRoute.toUpdatedEntity(local: GpxFileEntryEntity): GpxFileEntryEntity = local.copy(
    contentHash = contentHash,
    name = name,
    description = description,
    color = color,
    discipline = discipline,
    source = source,
    isFavorite = favorite,
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
    avgSpeedMps = avgSpeedMps,
    maxSpeedMps = maxSpeedMps,
    minLat = minLat,
    minLon = minLon,
    maxLat = maxLat,
    maxLon = maxLon,
    thumbnailPath = thumbnailPath,
    folder = folder,
    updatedAt = updatedAt
)

// A saved copy of someone else's shared route: a fresh local row with the file already on disk.
fun RemoteRoute.toSavedCopyEntity(fileName: String, ownerUid: String?, now: Long): GpxFileEntryEntity =
    GpxFileEntryEntity(
        id = 0,
        fileName = fileName,
        contentHash = contentHash,
        remoteId = null,
        ownerUid = ownerUid,
        syncStatus = "LOCAL_ONLY",
        fileDownloaded = true,
        fileUploaded = false,
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
        avgSpeedMps = avgSpeedMps,
        maxSpeedMps = maxSpeedMps,
        minLat = minLat,
        minLon = minLon,
        maxLat = maxLat,
        maxLon = maxLon,
        thumbnailPath = thumbnailPath,
        folder = folder,
        importedAt = now,
        updatedAt = now,
        creator = creator,
        originalFileName = originalFileName
    )
