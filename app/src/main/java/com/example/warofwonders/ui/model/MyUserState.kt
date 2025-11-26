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
    var coins: Int = 0,
    val level: Int = 0,
    val xp: Int = 0,
    val clanid: String = "",
    val clanRole: String = "",
    val profileImageUrl: String = "",

    val friends: Map<String, Boolean> = emptyMap(),

    // Inventario
    val criaturas: List<Criatura> = emptyList(),
    val recursos: List<Recurso> = emptyList(),

    )

