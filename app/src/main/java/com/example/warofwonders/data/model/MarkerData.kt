package com.example.warofwonders.data.model

import com.google.android.gms.maps.model.LatLng

data class MarkerData(
    val position: LatLng = LatLng(0.0, 0.0),
    val title: String = "",
    val snippet: String = "",
    val iconResId: Int? = null
)