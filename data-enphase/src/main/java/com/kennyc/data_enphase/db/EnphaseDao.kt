package com.kennyc.data_enphase.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kennyc.data_enphase.db.model.RoomSolarSystem
import kotlinx.coroutines.flow.Flow

@Dao
interface EnphaseDao {

    @Query("SELECT * FROM systems")
    fun getSystems(): Flow<List<RoomSolarSystem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystems(systems: List<RoomSolarSystem>)
}
