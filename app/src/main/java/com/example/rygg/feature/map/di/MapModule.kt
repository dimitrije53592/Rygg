package com.example.rygg.feature.map.di

import com.example.rygg.feature.map.data.MapTilerStyleSource
import com.example.rygg.feature.map.domain.MapStyleSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {
    @Binds
    @Singleton
    abstract fun bindMapStyleSource(impl: MapTilerStyleSource): MapStyleSource
}
