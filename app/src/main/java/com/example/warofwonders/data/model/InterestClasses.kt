package com.example.warofwonders.data.model

import org.json.JSONArray
import org.json.JSONObject

data class Recurso(val nombre: String, val cantidad: Int)
data class Clan(val nombre: String, val poder: Int)

data class PuntoInteres(
    val id: Int,
    val nombre: String,
    val lat: Double,
    val lng: Double,
    val clima: String,
    val altitud: Int,
    val zona: String,
    val recursos: List<Recurso>,
    val clanesPeleando: List<Clan>
) {
    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("nombre", nombre)
        json.put("lat", lat)
        json.put("lng", lng)
        json.put("clima", clima)
        json.put("altitud", altitud)
        json.put("zona", zona)

        val recursosArray = org.json.JSONArray()
        recursos.forEach {
            val r = JSONObject()
            r.put("nombre", it.nombre)
            r.put("cantidad", it.cantidad)
            recursosArray.put(r)
        }
        json.put("recursos", recursosArray)

        val clanesArray = org.json.JSONArray()
        clanesPeleando.forEach {
            val c = JSONObject()
            c.put("nombre", it.nombre)
            c.put("poder", it.poder)
            clanesArray.put(c)
        }
        json.put("clanesPeleando", clanesArray)

        return json
    }

    companion object {
        fun fromJSON(obj: JSONObject): PuntoInteres {
            val recursosArray = obj.optJSONArray("recursos") ?: org.json.JSONArray()
            val recursos = mutableListOf<Recurso>()
            for (i in 0 until recursosArray.length()) {
                val r = recursosArray.getJSONObject(i)
                recursos.add(Recurso(r.getString("nombre"), r.getInt("cantidad")))
            }

            val clanesArray = obj.optJSONArray("clanesPeleando") ?: org.json.JSONArray()
            val clanes = mutableListOf<Clan>()
            for (i in 0 until clanesArray.length()) {
                val c = clanesArray.getJSONObject(i)
                clanes.add(Clan(c.getString("nombre"), c.getInt("poder")))
            }

            return PuntoInteres(
                id = obj.getInt("id"),
                nombre = obj.getString("nombre"),
                lat = obj.getDouble("lat"),
                lng = obj.getDouble("lng"),
                clima = obj.getString("clima"),
                altitud = obj.getInt("altitud"),
                zona = obj.getString("zona"),
                recursos = recursos,
                clanesPeleando = clanes
            )
        }
    }
}