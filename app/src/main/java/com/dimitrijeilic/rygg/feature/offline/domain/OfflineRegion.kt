package com.dimitrijeilic.rygg.feature.offline.domain

data class OfflineRegion(
    val id: Long,
    val name: String,
    val bounds: RegionBounds,
    val createdAt: Long,
    val updatedAt: Long,
    val sourceRouteId: Long?,
    val sizeBytes: Long,
    val progress: Float,
    val status: RegionStatus
)

enum class RegionStatus {
    Downloading,
    Paused,
    Failed,
    Complete
}
