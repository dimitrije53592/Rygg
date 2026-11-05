package com.dimitrijeilic.rygg.feature.map.data

import com.dimitrijeilic.rygg.feature.map.domain.MapStyleSource
import javax.inject.Inject

// Bundled style over OpenFreeMap tiles: free for commercial use and allows offline downloads.
class OpenFreeMapStyleSource @Inject constructor() : MapStyleSource {
    override fun baseStyleUrl(): String = "asset://map/rygg-outdoor.json"
}
