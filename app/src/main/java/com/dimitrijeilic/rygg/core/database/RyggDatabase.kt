package com.dimitrijeilic.rygg.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryDao
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryEntity
import com.dimitrijeilic.rygg.feature.map.data.local.RouteOverviewDao
import com.dimitrijeilic.rygg.feature.map.data.local.RouteOverviewEntity
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingDao
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingPointEntity
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingSessionEntity

@Database(
    entities = [
        GpxFileEntryEntity::class,
        RecordingSessionEntity::class,
        RecordingPointEntity::class,
        RouteOverviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RyggDatabase : RoomDatabase() {
    abstract fun gpxFileEntryDao(): GpxFileEntryDao

    abstract fun recordingDao(): RecordingDao

    abstract fun routeOverviewDao(): RouteOverviewDao
}
