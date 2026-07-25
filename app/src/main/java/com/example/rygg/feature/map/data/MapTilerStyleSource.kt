package com.example.rygg.feature.map.data

import com.example.rygg.BuildConfig
import com.example.rygg.feature.map.domain.MapStyleSource
import javax.inject.Inject

class MapTilerStyleSource @Inject constructor() : MapStyleSource {
    override fun outdoorStyleUrl(): String =
        "$MAPTILER_BASE_URL/$OUTDOOR_MAP_ID/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

    private companion object {
        const val MAPTILER_BASE_URL = "https://api.maptiler.com/maps"
        const val OUTDOOR_MAP_ID = "outdoor-v2"
    }
}
