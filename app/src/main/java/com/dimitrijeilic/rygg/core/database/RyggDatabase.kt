package com.dimitrijeilic.rygg.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dimitrijeilic.rygg.core.database.converters.Converters
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryDao
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryEntity

@Database(entities = [GpxFileEntryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RyggDatabase : RoomDatabase() {
    abstract fun gpxFileEntryDao(): GpxFileEntryDao
}
