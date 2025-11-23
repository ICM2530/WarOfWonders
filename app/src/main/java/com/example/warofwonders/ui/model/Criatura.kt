package com.example.warofwonders.ui.model


data class Criatura(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val salud: Int = 100,
    val dano: Int = 0,
    val velocidad: Int = 0,
    val poder: Int = 0,
    val imagen: String = ""
)
