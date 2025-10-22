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

    val coins: Int = 1000,
    val level: Int = 1,
    val xp: Int = 0,
    val team: String = "Teusaquillo amigos"
)