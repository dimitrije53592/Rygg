package com.example.rygg.feature.library.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GpxFileEntryDao {
    // Soft-deleted rows are hidden from the library while the tombstone propagates.
    @Query("SELECT * FROM library WHERE deletedAt IS NULL ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<GpxFileEntryEntity>>

    @Query("SELECT * FROM library WHERE id = :id")
    fun observeById(id: Long): Flow<GpxFileEntryEntity?>

    @Query("SELECT fileName FROM library")
    suspend fun getAllFileNames(): List<String>

    @Query("SELECT * FROM library WHERE id = :id")
    suspend fun getById(id: Long): GpxFileEntryEntity?

    @Insert
    suspend fun insert(entry: GpxFileEntryEntity): Long

    @Update
    suspend fun update(entry: GpxFileEntryEntity)

    @Query("DELETE FROM library WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE library SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE library SET name = :name, fileName = :fileName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNameAndFile(id: Long, name: String, fileName: String, updatedAt: Long)

    // --- Cloud sync ---

    @Query("SELECT * FROM library WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): GpxFileEntryEntity?

    @Query("SELECT * FROM library WHERE ownerUid = :ownerUid AND contentHash = :contentHash LIMIT 1")
    suspend fun getByContentHash(ownerUid: String, contentHash: String): GpxFileEntryEntity?

    // Guest routes eligible for adoption when an account signs in.
    @Query("SELECT * FROM library WHERE ownerUid IS NULL AND deletedAt IS NULL")
    suspend fun getGuestRoutes(): List<GpxFileEntryEntity>

    // Owned rows still needing a push (upload or tombstone), oldest first.
    @Query("SELECT * FROM library WHERE ownerUid = :ownerUid AND syncStatus != 'SYNCED' ORDER BY importedAt ASC")
    suspend fun getPendingSync(ownerUid: String): List<GpxFileEntryEntity>

    // Owned rows already mirrored to the cloud — safe to drop from this device on sign-out.
    @Query("SELECT * FROM library WHERE ownerUid = :ownerUid AND syncStatus = 'SYNCED'")
    suspend fun getOwnedSynced(ownerUid: String): List<GpxFileEntryEntity>

    // Owned rows with an in-flight delete tombstone.
    @Query("SELECT * FROM library WHERE ownerUid = :ownerUid AND syncStatus = 'PENDING_DELETE'")
    suspend fun getOwnedPendingDelete(ownerUid: String): List<GpxFileEntryEntity>

    @Query("UPDATE library SET syncStatus = :status WHERE id = :id")
    suspend fun setSyncStatus(id: Long, status: String)

    // The .gpx upload confirmed: the file is in the cloud and the route is fully synced.
    @Query("UPDATE library SET fileUploaded = 1, syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markFileSynced(id: Long)

    @Query("UPDATE library SET fileUploaded = :uploaded WHERE id = :id")
    suspend fun setFileUploaded(id: Long, uploaded: Boolean)

    // Force a re-fetch of a route whose .gpx content changed in the cloud.
    @Query("UPDATE library SET fileDownloaded = 0 WHERE id = :id")
    suspend fun markFileMissing(id: Long)

    // Mark an owned route dirty after a local edit so the next push re-uploads it.
    @Query("UPDATE library SET syncStatus = 'PENDING_UPLOAD', updatedAt = :now WHERE id = :id")
    suspend fun markPendingUpload(id: Long, now: Long)

    @Query("UPDATE library SET ownerUid = :ownerUid, remoteId = :remoteId, syncStatus = :status WHERE id = :id")
    suspend fun setOwnership(id: Long, ownerUid: String, remoteId: String, status: String)

    @Query("UPDATE library SET fileName = :fileName, fileDownloaded = 1 WHERE id = :id")
    suspend fun setDownloadedFile(id: Long, fileName: String)

    @Query("UPDATE library SET sharedToken = :token WHERE id = :id")
    suspend fun setSharedToken(id: Long, token: String?)

    @Query("UPDATE library SET syncStatus = 'PENDING_DELETE', deletedAt = :deletedAt WHERE id = :id")
    suspend fun markPendingDelete(id: Long, deletedAt: Long)

    // On sign-out: revert an owned-but-unsynced route to a guest row so unuploaded work isn't lost.
    @Query(
        "UPDATE library SET ownerUid = NULL, remoteId = NULL, sharedToken = NULL, " +
            "fileUploaded = 0, syncStatus = 'LOCAL_ONLY' " +
            "WHERE ownerUid = :ownerUid AND syncStatus != 'SYNCED'"
    )
    suspend fun revertOwnedUnsyncedToGuest(ownerUid: String)
}
