package com.example.warofwonders.data.source.local

import android.content.Context
import com.example.warofwonders.data.model.InterestPointData
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class JsonManagerDataSource(private val context: Context) {
    private val filename = "interest_points.json"

    fun saveInterestPoint(point: InterestPointData) {
        val list = readInterestPoints().toMutableList()
        if (list.none { it.id == point.id }) {
            list.add(point)
        }
        writeList(list)
    }

    fun deleteJsonIfExists(): Boolean {
        val file = File(context.filesDir, filename)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    fun readInterestPoints(): List<InterestPointData> {
        val file = File(context.filesDir, filename)
        if (!file.exists()) file.writeText("[]")
        return try {
            val content = file.readText()
            val jsonArray = JSONArray(content)
            val list = mutableListOf<InterestPointData>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val point = InterestPointData(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    type = obj.getString("type"),
                    iconography = obj.getString("iconography"),
                    address = obj.getString("address"),
                    locality = obj.getString("locality"),
                    admin = obj.optString("admin", null),
                    phone = obj.optString("phone", null),
                    lat = obj.getDouble("lat"),
                    lng = obj.getDouble("lng")
                )
                list.add(point)
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun writeList(list: List<InterestPointData>) {
        val jsonArray = JSONArray()
        list.forEach { point ->
            val obj = JSONObject().apply {
                put("id", point.id)
                put("name", point.name)
                put("type", point.type)
                put("iconography", point.iconography)
                put("address", point.address)
                put("locality", point.locality)
                put("admin", point.admin)
                put("phone", point.phone)
                put("lat", point.lat)
                put("lng", point.lng)
            }
            jsonArray.put(obj)
        }
        val file = File(context.filesDir, filename)
        file.writeText(jsonArray.toString())
    }
}
