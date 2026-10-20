package com.dimitrijeilic.rygg.feature.map.domain

data class FollowSnapshot(
    val isActive: Boolean = false,
    val routeName: String = "",
    val distanceToRouteMeters: Double? = null,
    val isOffRoute: Boolean = false,
    val offRouteEpisode: Int = 0,
    val snoozedUntilMillis: Long = 0L
)
