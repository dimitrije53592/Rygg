package com.example.rygg.feature.library.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library")
data class GpxFileEntryEntity(
    // Identity and storage
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val contentHash: String,

    // File metadata
    val name: String,
    val description: String,
    val color: String?,

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

    //Organisation
    val folder: String?,
    val tags: List<String>,

    //Organisation
    val importedAt: Long,
    val updatedAt: Long,

    // Misc
    val creator: String?,
    val originalFileName: String?
)
