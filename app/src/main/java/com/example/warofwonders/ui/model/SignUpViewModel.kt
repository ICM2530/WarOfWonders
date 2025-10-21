package com.example.warofwonders.ui.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel : ViewModel() {
    private val _form = MutableStateFlow(MyUserState())
    val form = _form.asStateFlow()

    fun updateName(v: String) {
        _form.value = _form.value.copy(name = v)
    }

    fun updateLastName(v: String) {
        _form.value = _form.value.copy(lastName = v)
    }

    fun updatePhone(v: String) {
        _form.value = _form.value.copy(phone = v)
    }

    fun updateEmail(v: String) {
        _form.value = _form.value.copy(email = v)
    }

    fun updatePassword(v: String) {
        _form.value = _form.value.copy(password = v)
    }

    fun updateNameError(e: String) {
        _form.value = _form.value.copy(nameError = e)
    }

    fun updateLastNameError(e: String) {
        _form.value = _form.value.copy(lastNameError = e)
    }

    fun updatePhoneError(e: String) {
        _form.value = _form.value.copy(phoneError = e)
    }

    fun updateEmailError(e: String) {
        _form.value = _form.value.copy(emailError = e)
    }

    fun updatePassError(e: String) {
        _form.value = _form.value.copy(passError = e)
    }
}