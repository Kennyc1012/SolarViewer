package com.kennyc.data_enphase.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kennyc.data_enphase.db.model.RoomSolarSystem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface EnphaseDao {

    @Query("SELECT * FROM systems")
    fun getSystems(): Flowable<List<RoomSolarSystem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSystem(system: RoomSolarSystem): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSystems(systems: List<RoomSolarSystem>): Completable
}