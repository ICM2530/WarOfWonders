package com.android.taller2.data.repository

import com.example.warofwonders.data.source.local.GeoCoderDataSource
import com.google.android.gms.maps.model.LatLng

class GeoCoderRepository(
    private val geoCoderDataSource: GeoCoderDataSource
) {
    fun getAddressFromLocation(location: LatLng): String? {
        return geoCoderDataSource.findAddress(location)?.substringBefore(",")
    }

    fun getLocationFromAddress(address: String): LatLng? {
        val cleanAddress = address.trim()
        return geoCoderDataSource.findLocation(cleanAddress)
    }
}