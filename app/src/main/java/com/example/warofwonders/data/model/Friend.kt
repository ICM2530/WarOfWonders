package com.example.warofwonders.data.model

data class Friend(
    val uid: String = "",
    val name: String = "",
    val lastname: String = "",
    val email: String = "",
    val profileImage: String? = null,
    val active: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)