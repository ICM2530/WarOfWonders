package com.example.warofwonders.ui.screens.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.warofwonders.ui.screens.map.components.TextFieldSearch
import com.example.warofwonders.ui.shared.components.AlertDialogPopup
import com.example.warofwonders.ui.shared.utils.isPermissionGranted
import com.example.warofwonders.ui.theme.WarOfWondersTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(locationPermission) {
        viewModel.updatePermissionStatus(isPermissionGranted(context, locationPermission))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updatePermissionStatus(granted)
        if (granted) viewModel.startLocationUpdates()
    }

    var showRationale by remember { mutableStateOf(false) }

    MapScreenContent(
        uiState = uiState,
        onChangePlaceSearch = { search -> viewModel.updateSearchQuery(search) },
        onToggleUpdates = {
            if (uiState.locationUpdates) {
                viewModel.stopLocationUpdates()
            } else {
                if (uiState.permissionStatus) {
                    viewModel.startLocationUpdates()
                } else {
                    permissionLauncher.launch(locationPermission)
                }
            }
        }
    )

    if (showRationale) {
        AlertDialogPopup(
            title = "Permission Required",
            message = "This application requires access to your location to provide"
                    + "you with information based on your current position.",
            onAccept = {
                showRationale = false
                permissionLauncher.launch(locationPermission)
            },
            onCancel = { showRationale = false }
        )
    }
}

@Composable
fun MapScreenContent(
    uiState: MapUiState,
    onChangePlaceSearch: (String) -> Unit,
    onToggleUpdates: () -> Unit
) {
    val bogota = LatLng(4.7110, -74.0721)
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(bogota, 15f)
    }

    // Cuando se obtiene el permiso, centramos la cámara en Bogotá (inicio)
    LaunchedEffect(uiState.permissionStatus) {
        if (uiState.permissionStatus) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(bogota, 15f))
        }
    }

    // Cuando se reciben actualizaciones, centramos la cámara en la nueva ubicación
    LaunchedEffect(uiState.currentLocation) {
        if (uiState.locationUpdates) {
            val newPosition = LatLng(
                uiState.currentLocation.latitude,
                uiState.currentLocation.longitude
            )
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newPosition, 17f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                // Este activa el marcador azul predeterminado
                isMyLocationEnabled = uiState.permissionStatus,
                isTrafficEnabled = true
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        )

        TextFieldSearch(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 32.dp),
            place = uiState.searchQuery,
            placeholderText = "Buscar",
            onPlaceChange = onChangePlaceSearch
        )

        FloatingActionButton(
            onClick = onToggleUpdates,
            containerColor = Color.White,
            contentColor = if (uiState.locationUpdates) Color.Gray else Color.Cyan,
            modifier = Modifier
                .padding(bottom = 24.dp, end = 24.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Mi ubicación"
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapScreenPreview() {
    WarOfWondersTheme {
        MapScreenContent(
            uiState = MapUiState(),
            onChangePlaceSearch = { },
            onToggleUpdates = { }
        )
    }
}
