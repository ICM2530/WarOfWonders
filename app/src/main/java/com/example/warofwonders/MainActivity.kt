package com.example.warofwonders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.warofwonders.data.repository.GeoRepository
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.LightSensorDataSource
import com.example.warofwonders.data.source.hardware.BarometerSensorDataSource
import com.example.warofwonders.data.source.hardware.TemperatureSensorDataSource
import com.example.warofwonders.data.source.hardware.StepDetectorDataSource
import com.example.warofwonders.data.source.hardware.LocationDataSource
import com.example.warofwonders.ui.navigation.NavGraph
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationDataSource = LocationDataSource(locationClient = locationClient)
        val locationRepository = LocationRepository(locationDataSource = locationDataSource)
        val geoRepository = GeoRepository(context = this)
        val lightSensorDataSource = LightSensorDataSource(context = this)
        val barometerSensorDataSource = BarometerSensorDataSource(context = this)
        val temperatureSensorDataSource = TemperatureSensorDataSource(context = this)
        val stepDetectorDataSource = StepDetectorDataSource (context = this)

        setContent {
            NavGraph(
                locationRepository = locationRepository,
                geoRepository = geoRepository,
                lightSensorDataSource = lightSensorDataSource,
                barometerSensorDataSource = barometerSensorDataSource,
                temperatureSensorDataSource = temperatureSensorDataSource,
                stepDetectorDataSource = stepDetectorDataSource
            )
        }
    }
}