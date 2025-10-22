package com.example.warofwonders.ui.screens.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.components.TextFieldImage
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.MyUserViewModel
import com.example.warofwonders.ui.model.SignUpViewModel
import com.example.warofwonders.ui.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val signUpViewModel: SignUpViewModel = viewModel()
    val usersViewModel: MyUserViewModel = viewModel()
    val state by signUpViewModel.form.collectAsState()

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

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
            horizontalAlignment = Alignment.CenterHorizontally // centramos el contenido principal
        ) {
            // Contenedor con scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth() // <- asegura que ocupe todo el ancho
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally // <- centra todos los elementos dentro
            ) {
                Image(
                    modifier = Modifier.size(280.dp, 220.dp),
                    painter = painterResource(id = R.drawable.tittle_post),
                    contentDescription = "Title"
                )

                // --- Campos del formulario ---
                TextFieldImage(
                    value = state.name,
                    onValueChange = signUpViewModel::updateName,
                    placeholderText = "Nombre",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.nameError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.lastName,
                    onValueChange = signUpViewModel::updateLastName,
                    placeholderText = "Apellido",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.lastNameError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.phone,
                    onValueChange = signUpViewModel::updatePhone,
                    placeholderText = "Teléfono",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.phoneError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.email,
                    onValueChange = signUpViewModel::updateEmail,
                    placeholderText = "Correo electrónico",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.emailError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.password,
                    onValueChange = signUpViewModel::updatePassword,
                    placeholderText = "Contraseña",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.passError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = ""
                    },
                    placeholderText = "Confirmar contraseña",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(confirmPasswordError)

                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Botón fijo centrado ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                ImageButton(
                    imageRes = R.drawable.button_signup,
                    contentDescription = "Registrar",
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        val valid = validateSignUpForm(signUpViewModel, state)
                        if (confirmPassword != state.password) {
                            confirmPasswordError = "Las contraseñas no coinciden"
                        }
                        if (valid && confirmPasswordError.isEmpty()) {
                            usersViewModel.saveUser(state)
                            clearForm(signUpViewModel)
                            confirmPassword = ""
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
}

// --- Composable para mostrar errores con animación suave ---

@Composable
fun AnimatedErrorText(error: String) {
    androidx.compose.animation.AnimatedVisibility(
        visible = error.isNotEmpty(),
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
    ) {
        Text(
            text = error,
            color = Color.White,
            modifier = Modifier
                .padding(top = 2.dp)
                .width(260.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}


// --- Validaciones ---
fun validateSignUpForm(signUpViewModel: SignUpViewModel, s: MyUserState): Boolean {
    var ok = true

    if (s.name.isEmpty()) {
        signUpViewModel.updateNameError("Campo obligatorio")
        ok = false
    } else signUpViewModel.updateNameError("")

    if (s.lastName.isEmpty()) {
        signUpViewModel.updateLastNameError("Campo obligatorio")
        ok = false
    } else signUpViewModel.updateLastNameError("")

    if (s.phone.isEmpty()) {
        signUpViewModel.updatePhoneError("Campo obligatorio")
        ok = false
    } else signUpViewModel.updatePhoneError("")

    if (s.email.isEmpty()) {
        signUpViewModel.updateEmailError("Campo obligatorio")
        ok = false
    } else if (!validEmailAddress(s.email)) {
        signUpViewModel.updateEmailError("Correo inválido")
        ok = false
    } else signUpViewModel.updateEmailError("")

    if (s.password.isEmpty()) {
        signUpViewModel.updatePassError("Campo obligatorio")
        ok = false
    } else if (s.password.length < 6) {
        signUpViewModel.updatePassError("Mínimo 6 caracteres")
        ok = false
    } else signUpViewModel.updatePassError("")

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
