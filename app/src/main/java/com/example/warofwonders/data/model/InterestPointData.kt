package com.example.warofwonders.data.model

import android.graphics.Bitmap

data class InterestPointData(
    val id: Int,
    val name: String,
    val type: String,
    val iconography: String,
    val address: String,
    val locality: String,
    val admin: String?,
    val phone: String?,
    val lat: Double,
    val lng: Double,
    val icon: Bitmap? = null
)