package com.kennyc.solarviewer.data.model

data class SolarSystem(val id: String, val name: String, val status: SystemStatus)

val EMPTY_SYSTEM: SolarSystem = SolarSystem("EMPTY", "EMPTY", SystemStatus.UNKNOWN)