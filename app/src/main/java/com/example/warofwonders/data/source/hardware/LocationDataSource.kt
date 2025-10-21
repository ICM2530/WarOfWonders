package com.example.warofwonders.data.source.hardware

import android.annotation.SuppressLint
import android.os.Looper
import com.example.warofwonders.data.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
class LocationDataSource(
    private val locationClient: FusedLocationProviderClient
) {
    private var locationCallback: LocationCallback? = null

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .build()
        return locationRequest
    }

    private fun createLocationCallback(
        onResult: (LocationData) -> Unit
    ): LocationCallback {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let { loc ->
                    onResult(
                        LocationData(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            altitude = loc.altitude
                        )
                    )
                }
            }
        }
        return locationCallback
    }

    fun startLocationUpdates(
        onResult: (LocationData) -> Unit
    ) {
        stopLocationUpdates()
        val locationRequest = createLocationRequest()
        locationCallback = createLocationCallback(onResult)

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            locationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
    }
}
