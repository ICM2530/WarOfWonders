package com.example.warofwonders.data.repository

import android.content.Context
import android.util.Log
import com.example.warofwonders.R
import com.example.warofwonders.data.model.Clan
import com.example.warofwonders.data.model.PuntoInteres
import com.example.warofwonders.data.model.Recurso
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File

class InterestPointRepository(private val context: Context) {

    private val filename = "puntos_interes.json"
    private val file = File(context.getExternalFilesDir(null), filename)

    fun readJSONFile(): List<PuntoInteres> {
        val jsonText: String = try {
            // 1️⃣ Primero busca en almacenamiento externo
            if (file.exists()) {
                file.readText()
            } else {
                // 2️⃣ Si no existe, lee desde res/raw
                context.resources.openRawResource(R.raw.puntos_interes)
                    .bufferedReader().use(BufferedReader::readText)
            }
        } catch (e: Exception) {
            Log.e("PUNTO_INTERES", "Error al leer archivo", e)
            return emptyList()
        }

        return try {
            val jsonArray = JSONArray(jsonText)
            val list = mutableListOf<PuntoInteres>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(parsePunto(obj))
            }
            list
        } catch (e: Exception) {
            Log.e("PUNTO_INTERES", "Error al parsear JSON", e)
            emptyList()
        }
    }

    private fun parsePunto(obj: JSONObject): PuntoInteres {
        val recursosJson = obj.optJSONArray("recursos") ?: JSONArray()
        val recursos = mutableListOf<Recurso>()
        for (i in 0 until recursosJson.length()) {
            val r = recursosJson.getJSONObject(i)
            recursos.add(Recurso(r.getString("nombre"), r.getInt("cantidad")))
        }

        val clanesJson = obj.optJSONArray("clanesPeleando") ?: JSONArray()
        val clanes = mutableListOf<Clan>()
        for (i in 0 until clanesJson.length()) {
            val c = clanesJson.getJSONObject(i)
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
