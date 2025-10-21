package com.example.warofwonders.ui.screens.map

import com.example.warofwonders.data.model.LocationData

data class MapUiState(
    val currentLocation: LocationData = LocationData(),
    val searchQuery: String = "",
    val permissionStatus: Boolean = false,
    val locationUpdates: Boolean = false
)