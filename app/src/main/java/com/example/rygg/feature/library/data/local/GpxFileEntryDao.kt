package com.example.rygg.feature.library.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GpxFileEntryDao {
    @Query("SELECT * FROM library ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<GpxFileEntryEntity>>

    @Insert
    suspend fun insert(entry: GpxFileEntryEntity): Long

    @Query("DELETE FROM library WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE library SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)
}
