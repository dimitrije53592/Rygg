package com.dimitrijeilic.rygg.feature.library.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteImageDao {
    @Query("SELECT * FROM route_image ORDER BY entryId, position")
    fun observeAll(): Flow<List<RouteImageEntity>>

    @Query("SELECT * FROM route_image WHERE entryId = :entryId ORDER BY position")
    fun observeForEntry(entryId: Long): Flow<List<RouteImageEntity>>

    @Query("SELECT * FROM route_image WHERE entryId = :entryId ORDER BY position")
    suspend fun getForEntry(entryId: Long): List<RouteImageEntity>

    @Query("SELECT * FROM route_image WHERE id = :id")
    suspend fun getById(id: String): RouteImageEntity?

    @Query("SELECT id FROM route_image")
    suspend fun getAllIds(): List<String>

    @Insert
    suspend fun insertAll(images: List<RouteImageEntity>)

    @Query("DELETE FROM route_image WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE route_image SET position = :position WHERE id = :id")
    suspend fun setPosition(id: String, position: Int)

    // One transaction, so observers never see a half-applied order.
    @Transaction
    suspend fun setPositions(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id -> setPosition(id, index) }
    }

    // --- Cloud sync ---

    @Query("UPDATE route_image SET fileUploaded = 1 WHERE id = :id")
    suspend fun markUploaded(id: String)

    @Query("UPDATE route_image SET fileDownloaded = 1 WHERE id = :id")
    suspend fun markDownloaded(id: String)

    // Images of an account's live routes whose bytes still need to move in either direction.
    @Query(
        "SELECT route_image.* FROM route_image JOIN library ON library.id = route_image.entryId " +
            "WHERE library.ownerUid = :ownerUid AND library.remoteId IS NOT NULL AND library.deletedAt IS NULL " +
            "AND (route_image.fileUploaded = 0 OR route_image.fileDownloaded = 0)"
    )
    suspend fun getPendingTransfers(ownerUid: String): List<RouteImageEntity>

    // Images of routes about to revert to guest rows: ones never downloaded have no bytes to keep,
    // and the rest must upload again once the route has a new owner.
    @Query(
        "SELECT route_image.* FROM route_image JOIN library ON library.id = route_image.entryId " +
            "WHERE library.ownerUid = :ownerUid AND (:includeSynced OR library.syncStatus != 'SYNCED')"
    )
    suspend fun getForReverting(ownerUid: String, includeSynced: Boolean): List<RouteImageEntity>

    @Query(
        "UPDATE route_image SET fileUploaded = 0 WHERE entryId IN " +
            "(SELECT id FROM library WHERE ownerUid = :ownerUid AND (:includeSynced OR syncStatus != 'SYNCED'))"
    )
    suspend fun resetUploaded(ownerUid: String, includeSynced: Boolean)
}
