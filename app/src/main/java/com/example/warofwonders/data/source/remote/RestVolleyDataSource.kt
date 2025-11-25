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
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class RestVolleyDataSource(
    private val context: Context
) {
    private val baseUrl = "https://sig.catastrobogota.gov.co/arcgis/rest/services/turismo/turismobogota/MapServer/12"

    fun loadInterestPoints(onResult: (List<InterestPointData>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val iconMap = loadRendererIconsAsync()

            val queue = Volley.newRequestQueue(context)
            val urlGeoJson = "$baseUrl/query?where=1=1&outFields=*&f=geojson"

            val req = StringRequest(
                Request.Method.GET,
                urlGeoJson,
                { response ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val list = mutableListOf<InterestPointData>()
                        try {
                            val json = JSONObject(response)
                            val features = json.optJSONArray("features") ?: run {
                                withContext(Dispatchers.Main) { onResult(emptyList()) }
                                return@launch
                            }

                            for (i in 0 until 200) {
                                val f = features.getJSONObject(i)
                                val props = f.getJSONObject("properties")
                                val geom = f.getJSONObject("geometry")
                                val coords = geom.getJSONArray("coordinates")
                                val lon = coords.optDouble(0)
                                val lat = coords.optDouble(1)

                                val iconKey = props.optString("ICONOGRAFIA", "")
                                val bitmap = iconMap[iconKey]

                                list.add(
                                    InterestPointData(
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
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("REST_BOGOTA", "Parse error: ${e.message}")
                        }

                        withContext(Dispatchers.Main) {
                            onResult(list)
                        }
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

    private suspend fun loadRendererIconsAsync(): Map<String, Bitmap?> {
        return withContext(Dispatchers.IO) {
            val iconMap = mutableMapOf<String, Bitmap?>()
            val urlRenderer = "$baseUrl?f=pjson"
            val queue = Volley.newRequestQueue(context)

            val response = suspendCancellableCoroutine<String> { cont ->
                val req = StringRequest(Request.Method.GET, urlRenderer,
                    { cont.resume(it) {} },
                    { cont.resumeWith(Result.failure(it)) }
                )
                queue.add(req)
            }

            try {
                val json = JSONObject(response)
                val infos = json.getJSONObject("drawingInfo")
                    .getJSONObject("renderer")
                    .getJSONArray("uniqueValueInfos")

                for (i in 0 until infos.length()) {
                    val info = infos.getJSONObject(i)
                    val value = info.optString("value", "")
                    val imgData = info.optJSONObject("symbol")?.optString("imageData", "")
                    val bitmap = if (!imgData.isNullOrBlank()) {
                        val bytes = android.util.Base64.decode(imgData, android.util.Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).scale(42, 42)
                    } else null
                    iconMap[value] = bitmap
                }
            } catch (e: Exception) {
                Log.e("REST_BOGOTA", "Renderer parse error: ${e.message}")
            }

            iconMap
        }
    }
}