package com.dimitrijeilic.rygg.feature.map.domain

class OffRouteDetector(
    private val persistMillis: Long = OFF_ROUTE_PERSIST_MS,
    private val rejoinFixes: Int = REJOIN_FIXES
) {
    var isOffRoute: Boolean = false
        private set

    var episode: Int = 0
        private set

    private var outsideSinceMillis: Long? = null
    private var insideFixes = 0

    fun update(
        distanceToRouteMeters: Double,
        accuracyMeters: Float,
        thresholdMeters: Double,
        nowMillis: Long
    ): Boolean {
        val outside = distanceToRouteMeters > thresholdMeters + accuracyMeters
        if (outside) {
            insideFixes = 0
            val since = outsideSinceMillis ?: nowMillis.also { outsideSinceMillis = it }
            if (!isOffRoute && nowMillis - since >= persistMillis) {
                isOffRoute = true
                episode++
            }
        } else {
            outsideSinceMillis = null
            if (isOffRoute && ++insideFixes >= rejoinFixes) {
                isOffRoute = false
                insideFixes = 0
            }
        }
        return isOffRoute
    }
}

const val OFF_ROUTE_PERSIST_MS = 10_000L
private const val REJOIN_FIXES = 3
