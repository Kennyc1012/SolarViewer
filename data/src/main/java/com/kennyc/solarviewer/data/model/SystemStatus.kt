package com.kennyc.solarviewer.data.model

enum class SystemStatus {
    COMMUNICATION_ERROR,
    PRODUCTION_ERROR,
    METER_ERROR,
    MICROINVERTER_ERROR,
    BATTERY_ERROR,
    NORMAL,
    UNKNOWN;

    companion object {
        fun from(value: String): SystemStatus = values().firstOrNull { value == it.name } ?: UNKNOWN
    }
}