package com.kennyc.data_enphase.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kennyc.data_enphase.db.model.RoomSolarSystem

@Database(entities = [RoomSolarSystem::class], version = 1)
@TypeConverters(RoomSolarSystem.Converters::class)
abstract class EnphaseDatabase : RoomDatabase() {
    abstract fun dao(): EnphaseDao
}