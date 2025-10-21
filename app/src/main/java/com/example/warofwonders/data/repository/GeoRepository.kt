package com.example.warofwonders.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class GeoRepository(
    private val context: Context
) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getLocationFromAddress(address: String): LatLng? = withContext(Dispatchers.IO) {
        try {
            val results: List<Address>? = geocoder.getFromLocationName(address, 1)
            results?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAddressFromLocation(latLng: LatLng): String? = withContext(Dispatchers.IO) {
        try {
            val results: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            results?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
