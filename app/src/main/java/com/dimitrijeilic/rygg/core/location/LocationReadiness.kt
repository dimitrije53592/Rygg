package com.dimitrijeilic.rygg.core.location

sealed interface LocationReadiness {
    data object Ready : LocationReadiness

    data object PermissionNeeded : LocationReadiness

    data object PermissionBlocked : LocationReadiness

    data object ServicesOff : LocationReadiness

    data object ApproximateOnly : LocationReadiness
}

enum class LocationPurpose(val requiresPrecise: Boolean) {
    SHOW_POSITION(requiresPrecise = false),
    FOLLOW_ROUTE(requiresPrecise = true),
    RECORD(requiresPrecise = true)
}
