package com.example.warofwonders.ui.screens.map

import com.example.warofwonders.data.model.LocationData
import com.google.maps.android.compose.CameraPositionState

data class MapUiState (
    // Datos de ubicación y mapa
    val currentLocation: LocationData = LocationData(),
    val targetLocation: LocationData? = null,
    val savedLocations: List<LocationData> = emptyList(),
    val distanceToTarget: Double = 0.0,
    val cameraPosition: CameraPositionState? = null,
    val isFollowingUser: Boolean = true,

    // Estado de permisos y servicios
    val permissionStatus: Boolean = false,
    val gpsEnabled: Boolean = true,
    val locationUpdates: Boolean = false,

    // Interfaz de búsqueda
    val searchQuery: String = "",
    val searchResults: List<LocationData> = emptyList(),
    val isSearching: Boolean = false,

    // Estados de UI generales
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)