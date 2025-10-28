package com.example.warofwonders.ui.model

data class MyUserState(
    val name: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String = "",
    val lastNameError: String = "",
    val phoneError: String = "",
    val emailError: String = "",
    val passError: String = "",

    // Perfil del jugador
    val id: String = "",
    val usuario: String = "",
    val experiencia: Int = 0,
    val nivel: Int = 0,
    val monedas: Int = 0,
    val imagen: String = "",
    val pais: String = "",
    val clanId: String? = null,

    // Inventario
    val criaturas: List<Criatura> = emptyList(),
    val recursos: List<Recurso> = emptyList(),

)

