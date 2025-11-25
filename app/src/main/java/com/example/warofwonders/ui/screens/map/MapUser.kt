package com.example.warofwonders.ui.screens.map

import com.example.warofwonders.data.model.LocationData

data class MapUser(
    val uid: String = "",
    val displayName: String = "",
    val clanId: String = "",
    val nivel: Int = 1,
    val coins: Long = 0L,
    val criaturas: List<String> = emptyList(),
    val lastLocation: LocationData = LocationData(),
    val active: Boolean = true
)
