package com.dimitrijeilic.rygg.feature.library.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// A photo attached to a library route. The id is a UUID shared across devices; the bytes live at
// filesDir/images/{id}.jpg locally and users/{uid}/images/{id}.jpg in Cloud Storage.
@Entity(
    tableName = "route_image",
    foreignKeys = [
        ForeignKey(
            entity = GpxFileEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["entryId"])]
)
data class RouteImageEntity(
    @PrimaryKey val id: String,
    val entryId: Long,
    val position: Int,
    val width: Int,
    val height: Int,
    val createdAt: Long,
    val fileDownloaded: Boolean = true,
    // Only uploaded images are listed in the route's cloud doc, so other devices never reference
    // bytes that aren't in Storage yet.
    val fileUploaded: Boolean = false
)
