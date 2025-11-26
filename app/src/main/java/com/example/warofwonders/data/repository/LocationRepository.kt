package com.example.warofwonders.data.repository

import com.example.warofwonders.data.model.LocationData
import com.example.warofwonders.data.source.hardware.LocationDataSource

class LocationRepository(
    private val locationDataSource: LocationDataSource,
) {
    fun startLocationUpdates(onResult: (LocationData) -> Unit) {
        locationDataSource.startLocationUpdates { locationData ->
            onResult(locationData)
        }
    }

    fun stopLocationUpdates() {
        locationDataSource.stopLocationUpdates()
    }
}
