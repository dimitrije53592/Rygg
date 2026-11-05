package com.dimitrijeilic.rygg.feature.map.di

import com.dimitrijeilic.rygg.feature.map.data.OpenFreeMapStyleSource
import com.dimitrijeilic.rygg.feature.map.domain.MapStyleSource
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
    abstract fun bindMapStyleSource(impl: OpenFreeMapStyleSource): MapStyleSource
}
