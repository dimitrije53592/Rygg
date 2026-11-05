package com.dimitrijeilic.rygg.feature.offline.data

import com.dimitrijeilic.rygg.feature.offline.domain.RegionBounds
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OfflineMetadata(
    val name: String,
    val bounds: RegionBounds,
    val createdAt: Long,
    val updatedAt: Long,
    val sourceRouteId: Long? = null
) {
    fun encode(): ByteArray = json.encodeToString(serializer(), this).toByteArray()

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun decode(bytes: ByteArray?): OfflineMetadata? =
            bytes?.takeIf { it.isNotEmpty() }?.let {
                runCatching { json.decodeFromString(serializer(), it.decodeToString()) }.getOrNull()
            }
    }
}
