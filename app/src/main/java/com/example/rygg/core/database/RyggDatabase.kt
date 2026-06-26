package com.example.rygg.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rygg.feature.home.data.local.ItemDao
import com.example.rygg.feature.home.data.local.ItemEntity

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
abstract class RyggDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
