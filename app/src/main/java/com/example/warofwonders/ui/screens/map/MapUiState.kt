package com.example.warofwonders.ui.screens.map

import com.example.warofwonders.data.model.LocationData
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val currentLocation: LocationData = LocationData(),
    val staticMarkers: List<LatLng> = emptyList(), // Marcadores estáticos
    val searchMarker: LatLng? = null, // Marcador de búsqueda
    val clickMarker: LatLng? = null,  // Marcador de click largo
    val isDarkMap: Boolean = false,
    val locationUpdates: Boolean = false,
    val permissionStatus: Boolean = false,
    val isCameraFollowing: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val routePoints: List<LatLng> = emptyList()
)