package com.example.warofwonders.ui.screens.map

import com.example.warofwonders.R
import com.example.warofwonders.data.model.ClanData
import com.example.warofwonders.data.model.InterestPoint
import com.example.warofwonders.data.model.LocationData
import com.example.warofwonders.data.model.MarkerData
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.model.Recurso
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val currentLocation: LocationData = LocationData(),
    val isUpdatingLocation: Boolean = false,
    val cameraTarget: LatLng? = null,
    val cameraZoom: Float = 12f,
    val placeQuery: String = "",
    val mapStyleRes: Int = R.raw.map_light,
    val targetMarker: MarkerData? = null,
    val routePoints: List<LatLng> = emptyList(),

    val permissionStatus: Boolean = false,
    val locationUpdates: Boolean = false,
    val distanceToTarget: Double = 0.0,
    val staticMarkers: List<InterestPoint> = emptyList(),
    val savedLocations: List<LocationData> = emptyList(),
    val clans: List<ClanData> = emptyList(),
    val recursoEncontrado: Recurso? = null,
    val mostrarPopupRecurso: Boolean = false,

    val isDarkMap: Boolean = false,
    val isHigh: Boolean = false,
    val isCold: Boolean = false,
    val isHot: Boolean = false,
    val isMedium: Boolean = false,
    val isMagn: Boolean = false,
    val mediumCreatureFound: Boolean = false,
    val mediumCreatureCaptured: Boolean = false,
    val pressureCreatureFound: Boolean = false,
    val pressureCreatureCaptured: Boolean = false,
    val coldCreatureFound: Boolean = false,
    val coldCreatureCaptured: Boolean = false,
    val hotCreatureFound: Boolean = false,
    val hotCreatureCaptured: Boolean = false,
    val armorFound: Boolean = false,
    val armorCaptured: Boolean = false,
    val isCameraFollowing: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val alreadyOwnedCreature: Boolean = false,
    val criaturaDetectada: Criatura? = null
)