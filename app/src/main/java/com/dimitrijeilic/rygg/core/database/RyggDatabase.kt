package com.dimitrijeilic.rygg.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryDao
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryEntity
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageDao
import com.dimitrijeilic.rygg.feature.library.data.local.RouteImageEntity
import com.dimitrijeilic.rygg.feature.map.data.local.RouteOverviewDao
import com.dimitrijeilic.rygg.feature.map.data.local.RouteOverviewEntity
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingDao
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingPointEntity
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingSessionEntity

// From launch on, every version bump ships a Migration and its exported schema under app/schemas:
// once users have libraries, a destructive fallback would wipe them.
@Database(
    entities = [
        GpxFileEntryEntity::class,
        RecordingSessionEntity::class,
        RecordingPointEntity::class,
        RouteOverviewEntity::class,
        RouteImageEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class RyggDatabase : RoomDatabase() {
    abstract fun gpxFileEntryDao(): GpxFileEntryDao

    abstract fun recordingDao(): RecordingDao

    abstract fun routeOverviewDao(): RouteOverviewDao

    abstract fun routeImageDao(): RouteImageDao
}
