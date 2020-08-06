package com.kennyc.data_enphase.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kennyc.data_enphase.db.model.RoomSolarSystem

@Dao
interface EnphaseDao {

    @Query("SELECT * FROM systems")
    suspend fun getSystems(): List<RoomSolarSystem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystem(system: RoomSolarSystem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystems(systems: List<RoomSolarSystem>)
}