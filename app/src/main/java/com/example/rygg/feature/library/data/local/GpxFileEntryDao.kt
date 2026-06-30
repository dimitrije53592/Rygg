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
    suspend fun insert(item: GpxFileEntryEntity)

    @Query("DELETE FROM library WHERE id = :id")
    suspend fun deleteById(id: Long)
}
