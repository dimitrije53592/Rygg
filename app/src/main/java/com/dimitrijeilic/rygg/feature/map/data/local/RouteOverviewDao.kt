package com.dimitrijeilic.rygg.feature.map.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RouteOverviewDao {
    @Query("SELECT * FROM route_overview WHERE entryId = :entryId")
    suspend fun get(entryId: Long): RouteOverviewEntity?

    @Upsert
    suspend fun upsert(overview: RouteOverviewEntity)
}
