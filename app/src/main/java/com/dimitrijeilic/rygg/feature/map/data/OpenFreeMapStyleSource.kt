package com.dimitrijeilic.rygg.feature.map.data

import com.dimitrijeilic.rygg.core.ui.utils.RouteShareLinks
import com.dimitrijeilic.rygg.feature.map.domain.MapStyleSource
import javax.inject.Inject

class OpenFreeMapStyleSource @Inject constructor() : MapStyleSource {
    override fun baseStyleUrl(): String = "asset://map/rygg-outdoor.json"

    override fun offlinePackStyleUrl(): String = "${RouteShareLinks.BASE}/map/rygg-outdoor.json"
}
