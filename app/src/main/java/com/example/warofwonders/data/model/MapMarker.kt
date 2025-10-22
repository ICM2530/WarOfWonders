package com.example.warofwonders.data.model

import com.google.android.gms.maps.model.LatLng

data class MapMarker(
    val id: String,
    val position: LatLng,
    val title: String = "",
    val snippet: String = "",
    val visible: Boolean = true,
    val iconResId: Int? = null
)