package com.example.warofwonders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.android.taller2.data.repository.GeoCoderRepository
import com.android.taller2.data.repository.RouteRepository
import com.example.warofwonders.data.repository.InterestPointRepository
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.LightSensorDataSource
import com.example.warofwonders.data.source.hardware.BarometerSensorDataSource
import com.example.warofwonders.data.source.hardware.TemperatureSensorDataSource
import com.example.warofwonders.data.source.hardware.StepDetectorDataSource
import com.example.warofwonders.data.source.hardware.MagnetometerDataSource
import com.example.warofwonders.data.source.hardware.LocationDataSource
import com.example.warofwonders.data.source.local.GeoCoderDataSource
import com.example.warofwonders.data.source.remote.OsrmDataSource
import com.example.warofwonders.ui.navigation.NavGraph
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationDataSource = LocationDataSource(locationClient = locationClient)
        val locationRepository = LocationRepository(locationDataSource = locationDataSource)
        val geoCoderDataSource = GeoCoderDataSource(context = this)
        val geoCoderRepository = GeoCoderRepository(geoCoderDataSource = geoCoderDataSource)
        val lightSensorDataSource = LightSensorDataSource(context = this)
        val barometerSensorDataSource = BarometerSensorDataSource(context = this)
        val temperatureSensorDataSource = TemperatureSensorDataSource(context = this)
        val stepDetectorDataSource = StepDetectorDataSource(context = this)
        val magnetometerDataSource = MagnetometerDataSource(context = this)
        val interestPointRepository = InterestPointRepository(context = this)

        val osrmDataSource = OsrmDataSource()
        val routeRepository = RouteRepository(osrmDataSource)

        val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val startDestination = if (isUserLoggedIn) {
            AppScreens.Home.name
        } else {
            AppScreens.StartUp.name
        }

        setContent {
            NavGraph(
                locationRepository = locationRepository,
                geoCoderRepository = geoCoderRepository,
                routeRepository = routeRepository,
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

    fun precargarCriaturas() {
        val db = FirebaseFirestore.getInstance()
        val criaturas = listOf(
            mapOf(
                "id" to "pinguino",
                "nombre" to "pinguino",
                "tipo" to "FRIO",
                "salud" to 100,
                "dano" to 10,
                "velocidad" to 50,
                "poder" to 10,
                "imagen" to "pinguino"
            ),
            mapOf(
                "id" to "fenix",
                "nombre" to "fenix",
                "tipo" to "CALOR",
                "salud" to 100,
                "dano" to 50,
                "velocidad" to 20,
                "poder" to 80,
                "imagen" to "fenix"
            ),
            mapOf(
                "id" to "golempiedra",
                "nombre" to "golem de piedra",
                "tipo" to "PRESION",
                "salud" to 100,
                "dano" to 10,
                "velocidad" to 10,
                "poder" to 10,
                "imagen" to "golempiedra"
            )
        )

        criaturas.forEach { criatura ->
            db.collection("criaturas_disponibles")
                .document(criatura["id"] as String)
                .set(criatura)
        }
    }
}