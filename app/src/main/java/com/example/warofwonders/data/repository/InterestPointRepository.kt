package com.example.warofwonders.data.repository

import android.content.Context
import android.util.Log
import com.example.warofwonders.data.model.Clan
import com.example.warofwonders.data.model.PuntoInteres
import com.example.warofwonders.data.model.Recurso
import org.json.JSONArray
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class InterestPointRepository(private val context: Context) {

    private val filename = "puntos_interes.json"
    private val file = File(context.getExternalFilesDir(null), filename)

    private val puntos = mutableListOf<PuntoInteres>()

    init {
        puntos.add(
            PuntoInteres(
                id = 1,
                clima = "Frío",
                altitud = 2600,
                zona = "Montaña",
                recursos = listOf(Recurso("Agua", 30), Recurso("Madera", 15)),
                clanesPeleando = listOf(Clan("Dragones", 120), Clan("Fénix", 90))
            )
        )
        puntos.add(
            PuntoInteres(
                id = 2,
                clima = "Cálido",
                altitud = 200,
                zona = "Costa",
                recursos = listOf(Recurso("Pescado", 50), Recurso("Arena", 100)),
                clanesPeleando = listOf(Clan("Tiburones", 70))
            )
        )
    }

    fun writeJSONFile() {
        val jsonArray = JSONArray()
        for (p in puntos) jsonArray.put(p.toJSON())

        try {
            val output = BufferedWriter(FileWriter(file))
            output.write(jsonArray.toString(2))
            output.close()
            Log.i("PUNTO_INTERES", "Archivo guardado en: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("PUNTO_INTERES", "Error al escribir archivo", e)
        }
    }

    fun readJSONFile(): List<PuntoInteres> {
        if (!file.exists()) return emptyList()

        return try {
            val input = BufferedReader(FileReader(file))
            val jsonText = input.readText()
            input.close()

            val jsonArray = JSONArray(jsonText)
            val list = mutableListOf<PuntoInteres>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(PuntoInteres.Companion.fromJSON(obj))
            }
            list
        } catch (e: Exception) {
            Log.e("PUNTO_INTERES", "Error al leer archivo", e)
            emptyList()
        }
    }
}