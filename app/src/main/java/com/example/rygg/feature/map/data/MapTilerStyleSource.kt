package com.example.rygg.feature.map.data

import com.example.rygg.BuildConfig
import com.example.rygg.feature.map.domain.MapStyleSource
import javax.inject.Inject

class MapTilerStyleSource @Inject constructor() : MapStyleSource {
    override fun baseStyleUrl(): String =
        "$MAPTILER_BASE_URL/$BASE_MAP_ID/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

    private companion object {
        const val MAPTILER_BASE_URL = "https://api.maptiler.com/maps"
        const val BASE_MAP_ID = "topo-v2"
    }
}
