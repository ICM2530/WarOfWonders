package com.android.taller2.data.repository

import com.example.warofwonders.data.source.remote.OsrmDataSource
import com.google.android.gms.maps.model.LatLng

class RouteRepository(
    private val osrmDataSource: OsrmDataSource
) {
    suspend fun fetchRouteGeoJson(points: List<LatLng>): List<LatLng> {
        if (points.size < 2) return emptyList()

        return osrmDataSource.fetchRouteGeoJson(
            points = points,
            profile = "driving",
            overview = "full",
            geometries = "geojson"
        )
    }
}