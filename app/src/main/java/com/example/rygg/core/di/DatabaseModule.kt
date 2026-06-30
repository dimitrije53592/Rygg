package com.example.rygg.core.di

import android.content.Context
import androidx.room.Room
import com.example.rygg.core.database.RyggDatabase
import com.example.rygg.feature.library.data.local.GpxFileEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RyggDatabase =
        Room.databaseBuilder(context, RyggDatabase::class.java, "rygg.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideGpxFileEntryDao(database: RyggDatabase): GpxFileEntryDao = database.gpxFileEntryDao()
}
