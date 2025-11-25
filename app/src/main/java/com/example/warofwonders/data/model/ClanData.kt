package com.example.warofwonders.data.model

data class ClanData(
    val id: String = "",
    val nombre: String = "",
    val fuerza: Int = 0,
    val descripcion: String = "",
    val historial: Map<String, String> = emptyMap(),
    val miembros: Map<String, Boolean> = emptyMap(),
    val zona: List<LocationData> = emptyList()
)