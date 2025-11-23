package com.example.warofwonders.ui.screens.map

import com.example.warofwonders.data.model.LocationData
import com.example.warofwonders.data.model.PuntoInteres
import com.example.warofwonders.ui.model.Criatura
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val currentLocation: LocationData = LocationData(),
    val staticMarkers: List<PuntoInteres> = emptyList(),
    val searchMarker: LatLng? = null, // Marcador de búsqueda
    val clickMarker: LatLng? = null,  // Marcador de click largo
    val isDarkMap: Boolean = false,
    val isHigh: Boolean = false,
    val isCold: Boolean = false,
    val isHot: Boolean = false,
    val isMagn: Boolean = false,
    val pressureCreatureFound: Boolean = false,
    val pressureCreatureCaptured: Boolean = false,
    val coldCreatureFound: Boolean = false,
    val coldCreatureCaptured: Boolean = false,
    val hotCreatureFound: Boolean = false,
    val hotCreatureCaptured: Boolean = false,
    val armorFound: Boolean = false,
    val armorCaptured: Boolean = false,
    val locationUpdates: Boolean = false,
    val permissionStatus: Boolean = false,
    val isCameraFollowing: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val alreadyOwnedCreature: Boolean = false,
    val criaturaDetectada: Criatura? = null,
    val routePoints: List<LatLng> = emptyList()
)