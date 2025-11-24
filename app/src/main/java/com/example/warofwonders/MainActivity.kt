package com.example.warofwonders

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.warofwonders.data.repository.GeoRepository
import com.example.warofwonders.data.repository.InterestPointRepository
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.*
import com.example.warofwonders.ui.navigation.NavGraph
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationDataSource = LocationDataSource(locationClient = locationClient)
        val locationRepository = LocationRepository(locationDataSource = locationDataSource)
        val geoRepository = GeoRepository(context = this)
        val lightSensorDataSource = LightSensorDataSource(context = this)
        val barometerSensorDataSource = BarometerSensorDataSource(context = this)
        val temperatureSensorDataSource = TemperatureSensorDataSource(context = this)
        val stepDetectorDataSource = StepDetectorDataSource(context = this)
        val magnetometerDataSource = MagnetometerDataSource(context = this)
        val interestPointRepository = InterestPointRepository(context = this)

        val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val startDestination = if (isUserLoggedIn) {
            AppScreens.Home.name
        } else {
            AppScreens.StartUp.name
        }






        setContent {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                NavGraph(
                    locationRepository = locationRepository,
                    geoRepository = geoRepository,
                    lightSensorDataSource = lightSensorDataSource,
                    barometerSensorDataSource = barometerSensorDataSource,
                    temperatureSensorDataSource = temperatureSensorDataSource,
                    stepDetectorDataSource = stepDetectorDataSource,
                    magnetometerDataSource = magnetometerDataSource,
                    interestPointRepository = interestPointRepository,
                    startDestination = startDestination
                )
            }
        }
    }


}