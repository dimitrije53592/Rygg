package com.dimitrijeilic.rygg.feature.record.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recording_point")
data class RecordingPointEntity(
    @PrimaryKey(autoGenerate = true) val seq: Long = 0,
    val segment: Int,
    val lat: Double,
    val lon: Double,
    val ele: Double?,
    val timeMillis: Long?,
    // Non-null marks a waypoint rather than a track point.
    val waypointName: String? = null
)
