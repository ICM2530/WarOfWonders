package com.example.warofwonders.ui.screens.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.warofwonders.ui.components.AlertDialogPopup
import com.example.warofwonders.ui.screens.map.components.TextFieldSearch
import com.example.warofwonders.ui.shared.utils.shouldShowPermissionRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.warofwonders.R
import com.example.warofwonders.ui.shared.utils.bitmapDescriptorFromVector
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline

@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updatePermissionStatus(granted)
        if (granted) viewModel.toggleLocationUpdates()
    }

    DisposableEffect(Unit) {
        viewModel.startLightSensor()
        viewModel.startBarometerSensor()
        viewModel.startTemperatureSensor()
        viewModel.startMagnetometerSensor()
        onDispose { viewModel.stopLightSensor()
                    viewModel.stopBarometerSensor()
                    viewModel.stopTemperatureSensor()
                    viewModel.stopMagnetometerSensor()}
    }

    MapScreenContent(
        uiState = uiState,
        onLocationButtonClick = {
            if (uiState.permissionStatus) {
                viewModel.toggleLocationUpdates()
            } else {
                if (shouldShowPermissionRationale(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    showRationale = true
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        },
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onSearchSubmit = { viewModel.searchLocation() },
        onMapClick = { viewModel.clearMarkers() },
        onMapLongClick = { viewModel.onMapLongClick(it) },
        viewModel = viewModel
    )

    if (showRationale) {
        AlertDialogPopup(
            title = "Permission Required",
            message = "This app requires access to your location.",
            onAccept = {
                showRationale = false
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            onCancel = { showRationale = false }
        )
    }
}

val teusaquilloPolygonPoints = listOf(
    LatLng(4.6488, -74.0930), // Noroeste
    LatLng(4.6488, -74.0660), // Noreste
    LatLng(4.6300, -74.0660), // Sureste
    LatLng(4.6300, -74.0930)  // Suroeste
)

@Composable
fun MapScreenContent(
    uiState: MapUiState,
    onLocationButtonClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onMapClick: () -> Unit,
    onMapLongClick: (LatLng) -> Unit,
    viewModel: MapViewModel
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(uiState.currentLocation.latitude, uiState.currentLocation.longitude),
            17f
        )
    }

    LaunchedEffect(uiState.currentLocation) {
        if (uiState.isCameraFollowing) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(
                            uiState.currentLocation.latitude,
                            uiState.currentLocation.longitude
                        ),
                        17f
                    )
                )
            )
        }
    }

    val mapProperties = MapProperties(
        isMyLocationEnabled = uiState.permissionStatus && uiState.locationUpdates,
        mapStyleOptions = if (uiState.isDarkMap)
            MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_dark)
        else
            MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_light)
    )

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true
            ),
            onMapClick = { onMapClick() },
            onMapLongClick = { onMapLongClick(it) }
        ) {
            // Marcadores
            uiState.staticMarkers.forEach { marker ->
                Marker(
                    state = MarkerState(marker),
                    title = "Estático",
                    snippet = "Punto de interés",
                    icon = bitmapDescriptorFromVector(context = LocalContext.current, R.drawable.castillo)
                )
            }

            uiState.searchMarker?.let { marker ->
                Marker(
                    state = MarkerState(marker),
                    title = "Búsqueda",
                    snippet = "Marcador buscado",
                )
            }

            uiState.clickMarker?.let { marker ->
                Marker(
                    state = MarkerState(marker),
                    title = "Click Largo",
                    snippet = "Marcador agregado",
                )
            }

            // Ruta
            if (uiState.routePoints.isNotEmpty()) {
                Polyline(
                    points = uiState.routePoints,
                    color = if (uiState.isDarkMap) Color.Cyan
                    else Color.Blue,
                    width = 6f
                )
            }

            Polygon(
                points = teusaquilloPolygonPoints,
                fillColor = Color.Gray.copy(alpha = 0.3f),
                strokeColor = Color.Gray.copy(alpha = 0.5f),
                strokeWidth = 2f
            )
        }

        TextFieldSearch(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            place = uiState.searchQuery,
            placeholderText = "Buscar lugar...",
            onPlaceChange = onSearchQueryChange,
            onSearchSubmit = onSearchSubmit
        )

        if (uiState.pressureCreatureFound) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shadowElevation = 6.dp,
                                tonalElevation = 2.dp,
                                color = Color(0xFFc79e63),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "¡HA APARECIDO UNA CRIATURA DE PRESION ALTA!",
                                        color = Color.Black
                                    )

                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.showPressureCreatureAlert(false)
                                            }
                                        ) {
                                            Text("DEJAR IR")
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.capturePressureCreature()
                                                viewModel.showPressureCreatureAlert(false)
                                            }
                                        ) {
                                            Text("ATRAPAR")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else if (uiState.coldCreatureFound) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shadowElevation = 6.dp,
                                tonalElevation = 2.dp,
                                color = Color(0xFFc79e63),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "¡HA APARECIDO UNA CRIATURA BIEN COOL!",
                                        color = Color.Black
                                    )

                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.showColdCreatureAlert(false)
                                            }
                                        ) {
                                            Text("DEJAR IR")
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.captureColdCreature()
                                                viewModel.showColdCreatureAlert(false)
                                            }
                                        ) {
                                            Text("ATRAPAR")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else if (uiState.hotCreatureFound) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shadowElevation = 6.dp,
                                tonalElevation = 2.dp,
                                color = Color(0xFFc79e63),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "¡HA APARECIDO UNA CRIATURA BIEN ARDIENTE!",
                                        color = Color.Black
                                    )

                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.showHotCreatureAlert(false)
                                            }
                                        ) {
                                            Text("DEJAR IR")
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.captureHotCreature()
                                                viewModel.showHotCreatureAlert(false)
                                            }
                                        ) {
                                            Text("ATRAPAR")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else if (uiState.armorFound) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shadowElevation = 6.dp,
                                tonalElevation = 2.dp,
                                color = Color(0xFFc79e63),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "¡HAN APARECIDO UNAS PODEROSAS ARMADURAS!",
                                        color = Color.Black
                                    )

                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        Button(
                                            onClick = {
                                                viewModel.captureArmor()
                                                viewModel.findArmor(false)
                                            }
                                        ) {
                                            Text("Aceptar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(uiState.isHigh) {
            if (uiState.isHigh) {
                viewModel.showPressureCreatureAlert(true)
            }
        }

        LaunchedEffect(uiState.isCold) {
            if (uiState.isCold) {
                viewModel.showColdCreatureAlert(true)
            }
        }

        LaunchedEffect(uiState.isHot) {
            if (uiState.isHot) {
                viewModel.showHotCreatureAlert(true)
            }
        }

        LaunchedEffect(uiState.isMagn) {
            if (uiState.isMagn) {
                viewModel.findArmor(true)
            }
        }

        FloatingActionButton(
            onClick = onLocationButtonClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (uiState.locationUpdates)
                    Icons.Default.LocationOff else Icons.Default.MyLocation,
                contentDescription = "Toggle Location Updates"
            )
        }
    }
}

/**@Preview(showBackground = true)
@Composable
fun MapScreenContentPreview() {
    WarOfWondersTheme {
        MapScreenContent(
            uiState = MapUiState(),
            onLocationButtonClick = { },
            onSearchQueryChange = { },
            onSearchSubmit = { },
            onMapClick = { },
            onMapLongClick = {}
        )
    }
}*/
