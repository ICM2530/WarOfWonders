package com.example.warofwonders.ui.model

data class UserAuthState(
    val email : String = "",
    val password : String = "",
    val emailError:String = "",
    val passError : String = ""
)