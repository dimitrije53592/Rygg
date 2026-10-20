package com.dimitrijeilic.rygg.feature.map.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.dimitrijeilic.rygg.feature.library.data.local.GpxFileEntryEntity

// Device-local cache of a route's simplified geometry for the all-routes map. Never synced; it is
// stale as soon as contentHash no longer matches the library row.
@Entity(
    tableName = "route_overview",
    foreignKeys = [
        ForeignKey(
            entity = GpxFileEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RouteOverviewEntity(
    @PrimaryKey val entryId: Long,
    val contentHash: String,
    val paths: String
)
