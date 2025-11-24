package com.example.warofwonders.ui.model


data class Recurso(
    val id: String = "",
    val nombre: String = "",
    val proteccion: Int = 0,
    val dano: Int = 0,
    val velocidad: Int = 0,
    val precio: Int = 0,
    val tipo: String = "",
    val imagen: String = "",
    val material: String = "",
    val rareza: String = ""
)