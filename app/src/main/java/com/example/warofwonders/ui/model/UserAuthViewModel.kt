package com.example.warofwonders.ui.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
class UserAuthViewModel : ViewModel() {
    val userViewModel = MutableStateFlow(UserAuthState())
    val user = userViewModel.asStateFlow()
    fun updateEmailClass(newEmail: String) {
        userViewModel.value = userViewModel.value.copy(email = newEmail)
    }
    fun updatePassClass(newPass: String) {
        userViewModel.value = userViewModel.value.copy(password = newPass)
    }
    fun updateEmailError(error: String) {
        userViewModel.value = userViewModel.value.copy(emailError = error)
    }
    fun updatePassError(error: String) {
        userViewModel.value = userViewModel.value.copy(passError = error)
    }
}