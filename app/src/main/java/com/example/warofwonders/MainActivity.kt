package com.example.warofwonders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.LocationDataSource
import com.example.warofwonders.ui.navigation.NavGraph
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationDataSource = LocationDataSource(locationClient)
        val locationRepository = LocationRepository(locationDataSource)

        setContent {
            NavGraph(
                locationRepository = locationRepository
            )
        }
    }
}