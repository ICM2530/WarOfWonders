package com.example.warofwonders.ui.model


data class Criatura(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val salud: Int = 100,
    val daño: Int = 20,
    val velocidad: Int = 10,
    val poder: Int = 5,
    val imagen: String = "",
    val condiciones: String = "" //“presión alta”, “temperatura baja”
)
