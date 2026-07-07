package com.example.rygg.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rygg.core.database.converters.Converters
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import com.example.rygg.feature.library.data.local.GpxFileEntryEntity

@Database(entities = [GpxFileEntryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RyggDatabase : RoomDatabase() {
    abstract fun gpxFileEntryDao(): GpxFileEntryDao
}
