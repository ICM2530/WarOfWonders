package com.example.warofwonders.ui.model

data class Clan(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val fuerza: Int = 0,
    val historial: Map<String, String> = emptyMap(),
    val miembros: Map<String, Boolean> = emptyMap(),
    val zona: List<Zona> = emptyList()
)