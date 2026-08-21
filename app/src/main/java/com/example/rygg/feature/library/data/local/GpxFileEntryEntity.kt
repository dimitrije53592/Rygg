package com.example.rygg.feature.library.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// The unique index on remoteId stops a route from being inserted twice (e.g. two concurrent pull
// snapshots racing on the same cloud doc). Multiple NULLs are allowed, so guest rows are unaffected.
@Entity(tableName = "library", indices = [Index(value = ["remoteId"], unique = true)])
data class GpxFileEntryEntity(
    // Identity and storage
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val contentHash: String,
    // Cloud sync
    val remoteId: String? = null,
    val ownerUid: String? = null,
    val syncStatus: String = "LOCAL_ONLY",
    val fileDownloaded: Boolean = true,
    // Whether this route's .gpx already lives in Cloud Storage. Lets a metadata-only change
    // (rename, favorite) re-push the doc without re-uploading the unchanged file.
    val fileUploaded: Boolean = false,
    val deletedAt: Long? = null,
    val sharedToken: String? = null,
    // File metadata
    val name: String,
    val description: String,
    val color: String?,
    val discipline: String,
    val source: String,
    val isFavorite: Boolean = false,
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
    // Thumbnail geometry, encoded as "lat,lon;lat,lon;..."
    val thumbnailPath: String,
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
