package com.example.warofwonders.data.model

import org.json.JSONArray
import org.json.JSONObject

data class Recurso(val nombre: String, val cantidad: Int)
data class Clan(val nombre: String, val poder: Int)

data class PuntoInteres(
    val id: Int,
    val clima: String,
    val altitud: Int,
    val zona: String,
    val recursos: List<Recurso>,
    val clanesPeleando: List<Clan>
) {
    fun toJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("clima", clima)
        obj.put("altitud", altitud)
        obj.put("zona", zona)

        val recursosArray = JSONArray()
        for (r in recursos) {
            val rObj = JSONObject()
            rObj.put("nombre", r.nombre)
            rObj.put("cantidad", r.cantidad)
            recursosArray.put(rObj)
        }
        obj.put("recursos", recursosArray)

        val clanesArray = JSONArray()
        for (c in clanesPeleando) {
            val cObj = JSONObject()
            cObj.put("nombre", c.nombre)
            cObj.put("poder", c.poder)
            clanesArray.put(cObj)
        }
        obj.put("clanesPeleando", clanesArray)

        return obj
    }

    companion object {
        fun fromJSON(obj: JSONObject): PuntoInteres {
            val recursosList = mutableListOf<Recurso>()
            val recursosArray = obj.getJSONArray("recursos")
            for (i in 0 until recursosArray.length()) {
                val rObj = recursosArray.getJSONObject(i)
                recursosList.add(Recurso(rObj.getString("nombre"), rObj.getInt("cantidad")))
            }

            val clanesList = mutableListOf<Clan>()
            val clanesArray = obj.getJSONArray("clanesPeleando")
            for (i in 0 until clanesArray.length()) {
                val cObj = clanesArray.getJSONObject(i)
                clanesList.add(Clan(cObj.getString("nombre"), cObj.getInt("poder")))
            }

            return PuntoInteres(
                id = obj.getInt("id"),
                clima = obj.getString("clima"),
                altitud = obj.getInt("altitud"),
                zona = obj.getString("zona"),
                recursos = recursosList,
                clanesPeleando = clanesList
            )
        }
    }
}