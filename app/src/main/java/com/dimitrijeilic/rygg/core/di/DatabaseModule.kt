package com.dimitrijeilic.rygg.core.di

import android.content.Context
import androidx.room.Room
import com.dimitrijeilic.rygg.core.database.RyggDatabase
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryDao
import com.dimitrijeilic.rygg.feature.map.data.local.RouteOverviewDao
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingDao
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
            .build()

    @Provides
    fun provideGpxFileEntryDao(database: RyggDatabase): GpxFileEntryDao = database.gpxFileEntryDao()

    @Provides
    fun provideRecordingDao(database: RyggDatabase): RecordingDao = database.recordingDao()

    @Provides
    fun provideRouteOverviewDao(database: RyggDatabase): RouteOverviewDao = database.routeOverviewDao()
}
