package com.kennyc.data_enphase.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.kennyc.solarviewer.data.model.SystemStatus

@Entity(tableName = "systems")
data class RoomSolarSystem(
    @PrimaryKey
    @ColumnInfo(name = "system_id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long,

    @ColumnInfo(name = "status")
    val status: SystemStatus
) {
    class Converters {
        @TypeConverter
        fun fromStatus(value: SystemStatus): String = value.name

        @TypeConverter
        fun toStatus(value: String) = SystemStatus.from(value)
    }
}