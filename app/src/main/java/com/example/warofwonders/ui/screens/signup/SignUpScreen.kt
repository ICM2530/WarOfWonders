package com.example.warofwonders.ui.screens.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.components.TextFieldImage
import com.example.warofwonders.ui.model.MyUserViewModel
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.SignUpViewModel
import com.example.warofwonders.ui.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val signUpViewModel: SignUpViewModel = viewModel()
    val usersViewModel: MyUserViewModel = viewModel()
    val state by signUpViewModel.form.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.closechat),
            contentDescription = "Cerrar",
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable {
                    navController.navigate(AppScreens.StartUp.name) {
                        popUpTo(AppScreens.SignUp.name) { inclusive = true }
                    }
                }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(280.dp, 220.dp),
                painter = painterResource(id = R.drawable.tittle_post),
                contentDescription = "Title"
            )

            TextFieldImage(
                value = state.name,
                onValueChange = signUpViewModel::updateName,
                placeholderText = "Nombre",
                modifier = Modifier.width(250.dp)
            )
            if (state.nameError.isNotEmpty())
                Text(state.nameError, color = Color.Red, modifier = Modifier.width(250.dp))

            TextFieldImage(
                value = state.lastName,
                onValueChange = signUpViewModel::updateLastName,
                placeholderText = "Apellido",
                modifier = Modifier.width(250.dp)
            )
            if (state.lastNameError.isNotEmpty())
                Text(state.lastNameError, color = Color.Red, modifier = Modifier.width(250.dp))

            TextFieldImage(
                value = state.phone,
                onValueChange = signUpViewModel::updatePhone,
                placeholderText = "Teléfono",
                modifier = Modifier.width(250.dp)
            )
            if (state.phoneError.isNotEmpty())
                Text(state.phoneError, color = Color.Red, modifier = Modifier.width(250.dp))

            TextFieldImage(
                value = state.email,
                onValueChange = signUpViewModel::updateEmail,
                placeholderText = "Correo",
                modifier = Modifier.width(250.dp)
            )
            if (state.emailError.isNotEmpty())
                Text(state.emailError, color = Color.Red, modifier = Modifier.width(250.dp))

            TextFieldImage(
                value = state.password,
                onValueChange = signUpViewModel::updatePassword,
                placeholderText = "Contraseña",
                modifier = Modifier.width(250.dp)
            )
            if (state.passError.isNotEmpty())
                Text(state.passError, color = Color.Red, modifier = Modifier.width(250.dp))

            Spacer(modifier = Modifier.height(12.dp))

            ImageButton(
                imageRes = R.drawable.button_signup,
                contentDescription = "Registrar",
                modifier = Modifier.width(120.dp),
                onClick = {
                    if (validateSignUpForm(signUpViewModel, state)) {
                        usersViewModel.saveUser(state)
                        clearForm(signUpViewModel)
                        Toast.makeText(context, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        navController.navigate(AppScreens.Home.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

fun validateSignUpForm(signUpViewModel: SignUpViewModel, s: MyUserState): Boolean {
    var ok = true

    if (s.name.isEmpty()) {
        signUpViewModel.updateNameError("Nombre vacío")
        ok = false
    } else signUpViewModel.updateNameError("")

    if (s.lastName.isEmpty()) {
        signUpViewModel.updateLastNameError("Apellido vacío")
        ok = false
    } else signUpViewModel.updateLastNameError("")

    if (s.phone.isEmpty()) {
        signUpViewModel.updatePhoneError("Teléfono vacío")
        ok = false
    } else signUpViewModel.updatePhoneError("")

    if (s.email.isEmpty()) {
        signUpViewModel.updateEmailError("Correo vacío")
        ok = false
    } else signUpViewModel.updateEmailError("")

    if (!validEmailAddress(s.email)) {
        signUpViewModel.updateEmailError("Correo inválido")
        ok = false
    }

    if (s.password.isEmpty()) {
        signUpViewModel.updatePassError("Contraseña vacía")
        ok = false
    } else signUpViewModel.updatePassError("")

    if (s.password.length < 6) {
        signUpViewModel.updatePassError("Contraseña demasiado corta")
        ok = false
    }

    return ok
}

fun clearForm(signUpViewModel: SignUpViewModel) {
    signUpViewModel.updateName("")
    signUpViewModel.updateLastName("")
    signUpViewModel.updatePhone("")
    signUpViewModel.updateEmail("")
    signUpViewModel.updatePassword("")
}

fun validEmailAddress(email: String): Boolean {
    val regex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"""
    return email.matches(regex.toRegex())
}