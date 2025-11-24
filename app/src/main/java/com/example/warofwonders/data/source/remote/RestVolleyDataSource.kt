package com.example.warofwonders.data.source.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.warofwonders.data.model.InterestPointData
import org.json.JSONObject
import android.util.Base64
import androidx.core.graphics.scale

class RestVolleyDataSource(
    private val context: Context
) {
    private val baseUrl = "https://sig.catastrobogota.gov.co/arcgis/rest/services/turismo/turismobogota/MapServer/12"

    fun loadInterestPoints(onResult: (List<InterestPointData>) -> Unit) {
        loadRendererIcons { iconMap ->   // 1️⃣ Primero cargamos los íconos del renderer
            val queue = Volley.newRequestQueue(context)
            val urlGeoJson = "$baseUrl/query?where=1=1&outFields=*&f=geojson"

            val req = StringRequest(
                Request.Method.GET,
                urlGeoJson,
                { response ->

                    try {
                        val json = JSONObject(response)
                        val features = json.optJSONArray("features") ?: run {
                            onResult(emptyList())
                            return@StringRequest
                        }

                        val list = mutableListOf<InterestPointData>()

                        // ❌ ANTES: limit = minOf(features.length(), 100)
                        // ✅ AHORA: procesar TODO
                        val count = features.length()

                        for (i in 0 until count) {
                            val f = features.getJSONObject(i)
                            val props = f.getJSONObject("properties")
                            val geom = f.getJSONObject("geometry")

                            val coords = geom.getJSONArray("coordinates")
                            val lon = coords.optDouble(0)
                            val lat = coords.optDouble(1)

                            val iconKey = props.optString("ICONOGRAFIA", "")
                            val bitmap = iconMap[iconKey]

                            val poi = InterestPointData(
                                id = props.optInt("OBJECTID"),
                                name = props.optString("NOMATRACTIVO"),
                                type = props.optString("TIPOATRACTIVO"),
                                iconography = iconKey,
                                address = props.optString("DIRECCION"),
                                locality = props.optString("LOCALIDAD"),
                                admin = props.optString("NOMADMIN"),
                                phone = props.optString("TELADMIN"),
                                lat = lat,
                                lng = lon,
                                icon = bitmap
                            )

                            list.add(poi)
                        }

                        onResult(list)

                    } catch (e: Exception) {
                        Log.e("REST_BOGOTA", "Parse error: ${e.message}")
                        onResult(emptyList())
                    }
                },
                {
                    Log.e("REST_BOGOTA", "GeoJSON error ${it.localizedMessage}")
                    onResult(emptyList())
                }
            )

            queue.add(req)
        }
    }

    /**
     * Carga el renderer (drawingInfo) para obtener los íconos base64.
     * Lo hace UNA sola vez.
     */
    private fun loadRendererIcons(onLoaded: (Map<String, Bitmap?>) -> Unit) {
        val urlRenderer = "$baseUrl?f=pjson"

        val queue = Volley.newRequestQueue(context)

        val req = StringRequest(Request.Method.GET, urlRenderer,
            { response ->
                val json = JSONObject(response)

                val iconMap = mutableMapOf<String, Bitmap?>()

                try {
                    val infos = json
                        .getJSONObject("drawingInfo")
                        .getJSONObject("renderer")
                        .getJSONArray("uniqueValueInfos")

                    for (i in 0 until infos.length()) {
                        val info = infos.getJSONObject(i)

                        val value = info.optString("value", "")  // ✔ clave que matchea con ICONOGRAFIA
                        val symbol = info.optJSONObject("symbol")
                        val imgData = symbol?.optString("imageData", "")

                        if (!imgData.isNullOrBlank()) {
                            val bytes = Base64.decode(imgData, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                            val scaledBitmap = bitmap.scale(32, 32)

                            iconMap[value] = scaledBitmap
                        } else {
                            iconMap[value] = null
                        }
                    }
                } catch (e: Exception) {
                    Log.e("REST_BOGOTA", "Renderer parse error: ${e.message}")
                }

                onLoaded(iconMap)
            },
            {
                Log.e("REST_BOGOTA", "Renderer error: ${it.localizedMessage}")
                onLoaded(emptyMap())
            }
        )

        queue.add(req)
    }
}