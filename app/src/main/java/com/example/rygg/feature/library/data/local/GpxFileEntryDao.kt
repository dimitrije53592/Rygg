package com.example.rygg.feature.library.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GpxFileEntryDao {
    @Query("SELECT * FROM library ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<GpxFileEntryEntity>>

    @Query("SELECT * FROM library WHERE id = :id")
    fun observeById(id: Long): Flow<GpxFileEntryEntity?>

    @Query("SELECT fileName FROM library")
    suspend fun getAllFileNames(): List<String>

    @Insert
    suspend fun insert(entry: GpxFileEntryEntity): Long

    @Query("DELETE FROM library WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE library SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE library SET name = :name, fileName = :fileName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNameAndFile(id: Long, name: String, fileName: String, updatedAt: Long)
}
