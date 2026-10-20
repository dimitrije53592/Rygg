package com.dimitrijeilic.rygg.feature.library.domain

import com.dimitrijeilic.rygg.core.gpx.model.GeoPoint
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline

data class GpxFileEntry(
    // Identity and storage
    val id: Long,
    val fileName: String,
    val contentHash: String,
    // Cloud sync
    val remoteId: String? = null,
    val ownerUid: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val fileDownloaded: Boolean = true,
    val deletedAt: Long? = null,
    val sharedToken: String? = null,
    // File metadata
    val name: String,
    val description: String,
    val color: String?,
    val discipline: Discipline,
    val source: EntrySource,
    val isFavorite: Boolean,
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
    val avgSpeedMps: Double? = null,
    val maxSpeedMps: Double? = null,
    // Bounds
    val minLat: Double?,
    val minLon: Double?,
    val maxLat: Double?,
    val maxLon: Double?,
    // Thumbnail geometry (downsampled trail contour)
    val pathPoints: List<GeoPoint>,
    // Photos in display order; the first is the cover
    val images: List<RouteImage> = emptyList(),
    // Organisation
    val folder: String?,
    // Organisation
    val importedAt: Long,
    val updatedAt: Long,
    // Misc
    val creator: String?,
    val originalFileName: String?
)
