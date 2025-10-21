package com.example.warofwonders.ui.screens.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.warofwonders.ui.components.AlertDialogPopup
import com.example.warofwonders.ui.screens.map.components.TextFieldSearch
import com.example.warofwonders.ui.shared.utils.shouldShowPermissionRationale
import com.example.warofwonders.ui.theme.WarOfWondersTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updatePermissionStatus(granted)
        if (granted) viewModel.toggleLocationUpdates()
    }

    MapScreenContent(
        uiState = uiState,
        onLocationButtonClick = {
            if (uiState.permissionStatus) {
                viewModel.toggleLocationUpdates()
            } else {
                if (shouldShowPermissionRationale(context, locationPermission)) {
                    showRationale = true
                } else {
                    permissionLauncher.launch(locationPermission)
                }
            }
        },
        onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) }
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
    onLocationButtonClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(uiState.currentLocation.latitude, uiState.currentLocation.longitude)
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(uiState.currentLocation.latitude, uiState.currentLocation.longitude),
            18f
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = uiState.permissionStatus && uiState.locationUpdates
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true
            )
        ) { }

        TextFieldSearch(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            place = uiState.searchQuery,
            placeholderText = "Search",
            onPlaceChange = onSearchQueryChange
        )

        FloatingActionButton(
            onClick = onLocationButtonClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(
                imageVector = if (uiState.locationUpdates) Icons.Default.LocationOff else Icons.Default.MyLocation,
                contentDescription = "Toggle Location Updates"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenContentPreview() {
    WarOfWondersTheme {
        MapScreenContent(
            uiState = MapUiState(),
            onLocationButtonClick = { },
            onSearchQueryChange = { }
        )
    }
}