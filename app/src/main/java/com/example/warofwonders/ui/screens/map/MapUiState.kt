package com.example.warofwonders.ui.screens.map

import com.example.warofwonders.R
import com.example.warofwonders.data.model.ClanData
import com.example.warofwonders.data.model.Friend
import com.example.warofwonders.data.model.InterestPointData
import com.example.warofwonders.data.model.LocationData
import com.example.warofwonders.data.model.MarkerData
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.model.Recurso
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val currentLocation: LocationData = LocationData(),
    val isUpdatingLocation: Boolean = false,
    val cameraTarget: LatLng? = null,
    val cameraZoom: Float = 14f,
    val placeQuery: String = "",
    val mapStyleRes: Int = R.raw.map_light,
    val targetMarker: MarkerData? = null,
    val routePoints: List<LatLng> = emptyList(),

    val permissionStatus: Boolean = false,
    val locationUpdates: Boolean = false,
    val interestPoint: List<InterestPointData> = emptyList(),
    val clans: List<ClanData> = emptyList(),
    val recursoEncontrado: Recurso? = null,
    val mostrarPopupRecurso: Boolean = false,

    val friendsList: List<Friend> = emptyList(),
    val showFriendsMarkers: Boolean = false,
    val isActive: Boolean = false,
    val selectedFriendMarker: MarkerData? = null,

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
    val criaturaDetectada: Criatura? = null,
    // Campos de encuentro: cuando se den, la interfaz deberia navegar a la pantalla de combate con estas IDs
    val encounterAttackerId: String? = null,
    val encounterDefenderId: String? = null
    ,
    // Usuarios cercanos (simulados o reales)
    val nearbyUsers: List<MapUser> = emptyList(),
    // Indica si el jugador está dentro de un territorio que NO es de su clan
    val insideEnemyTerritory: Boolean = false
)