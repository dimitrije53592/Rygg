package com.dimitrijeilic.rygg.feature.map.domain

import androidx.annotation.StringRes
import com.dimitrijeilic.rygg.R

enum class OffRouteThreshold(
    @StringRes val labelRes: Int,
    val meters: Double
) {
    NEAR(R.string.settings_off_route_near, 30.0),
    DEFAULT(R.string.settings_off_route_default, ON_ROUTE_THRESHOLD_M),
    FAR(R.string.settings_off_route_far, 100.0)
}
