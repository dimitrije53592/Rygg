package com.dimitrijeilic.rygg.feature.record.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recording_session WHERE id = $ACTIVE_SESSION_ID")
    suspend fun getSession(): RecordingSessionEntity?

    @Query("SELECT * FROM recording_point ORDER BY seq ASC")
    suspend fun getPoints(): List<RecordingPointEntity>

    @Upsert
    suspend fun upsertSession(session: RecordingSessionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPoints(points: List<RecordingPointEntity>)

    @Query("DELETE FROM recording_session")
    suspend fun deleteSession()

    @Query("DELETE FROM recording_point")
    suspend fun deletePoints()

    @Transaction
    suspend fun checkpoint(session: RecordingSessionEntity, points: List<RecordingPointEntity>) {
        if (points.isNotEmpty()) insertPoints(points)
        upsertSession(session)
    }

    @Transaction
    suspend fun begin(session: RecordingSessionEntity) {
        deletePoints()
        deleteSession()
        upsertSession(session)
    }

    @Transaction
    suspend fun clear() {
        deletePoints()
        deleteSession()
    }
}
