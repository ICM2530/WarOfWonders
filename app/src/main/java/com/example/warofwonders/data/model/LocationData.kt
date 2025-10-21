package com.example.warofwonders.data.model

import org.json.JSONObject
import java.util.Date

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val date: Date = Date()
) {
    fun toJSON(): JSONObject {
        val obj = JSONObject();
        obj.put("date", date.time)
        obj.put("latitude", latitude)
        obj.put("longitude", longitude)
        obj.put("altitude", altitude)

        return obj
    }
}