package com.example.warofwonders.data.source.local

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class GeoCoderDataSource(
    context: Context
) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    fun findAddress(location: LatLng): String? {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val result = addresses.first().getAddressLine(0)
            return  result
        }
        return null
    }

    fun findLocation(address: String): LatLng? {
        val addresses = geocoder.getFromLocationName(address, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val result = addresses.first()
            val location = LatLng(result.latitude, result.longitude)
            return location
        }
        return null
    }
}