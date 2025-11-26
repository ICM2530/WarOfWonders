package com.example.warofwonders.data.source.remote

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class OsrmDataSource() {
    private val baseUrl: String = "https://router.project-osrm.org"
    private val client = OkHttpClient()

    suspend fun fetchRouteGeoJson(
        points: List<LatLng>,
        profile: String,
        overview: String,
        geometries: String
    ): List<LatLng> =
        withContext(Dispatchers.IO) {  // Switch al dispatcher IO para operaciones de red
            try {
                val coordinates =
                    points.joinToString(separator = ";") { "${it.longitude},${it.latitude}" }
                val url =
                    "$baseUrl/route/v1/$profile/$coordinates?overview=$overview&geometries=$geometries"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext emptyList()

                val json = JSONObject(body)
                val routes = json.optJSONArray("routes") ?: return@withContext emptyList()
                if (routes.length() == 0) return@withContext emptyList()  // Salir solo del bloque de withContext, no de la función que lo contiene.

                val geometry = routes
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                val pointsList = mutableListOf<LatLng>()
                for (i in 0 until geometry.length()) {
                    val coord = geometry.getJSONArray(i)
                    val lon = coord.getDouble(0)
                    val lat = coord.getDouble(1)
                    pointsList.add(LatLng(lat, lon))
                }
                pointsList
            } catch (e: Exception) {
                emptyList()
            }
        }
}