package com.dimitrijeilic.rygg.feature.record.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// At most one row: the in-progress recording, checkpointed so it survives a process kill.
@Entity(tableName = "recording_session")
data class RecordingSessionEntity(
    @PrimaryKey val id: Int = ACTIVE_SESSION_ID,
    val discipline: String,
    val startedAt: Long,
    val state: String,
    val elapsedMillis: Long,
    val updatedAt: Long
)

const val ACTIVE_SESSION_ID = 0
