package com.dimitrijeilic.rygg.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryDao
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryEntity

@Database(entities = [GpxFileEntryEntity::class], version = 1, exportSchema = false)
abstract class RyggDatabase : RoomDatabase() {
    abstract fun gpxFileEntryDao(): GpxFileEntryDao
}
