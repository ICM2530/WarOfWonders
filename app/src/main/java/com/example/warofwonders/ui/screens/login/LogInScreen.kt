package com.example.warofwonders.ui.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.components.TextFieldImage
import com.example.warofwonders.ui.model.UserAuthViewModel
import com.example.warofwonders.ui.model.firebaseAuth
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LogInScreen(navController: NavHostController) {
    val model: UserAuthViewModel = viewModel()
    val context = LocalContext.current
    val user by model.user.collectAsState()

    LaunchedEffect(Unit) {
        firebaseAuth.currentUser?.let {
            navController.navigate(AppScreens.Home.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.size(280.dp),
                    painter = painterResource(id = R.drawable.tittle_post),
                    contentDescription = "Title"
                )

                TextFieldImage(
                    value = user.email,
                    onValueChange = { model.updateEmailClass(it) },
                    placeholderText = "Correo",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.width(250.dp)
                )
                AnimatedErrorText(user.emailError)

                TextFieldImage(
                    value = user.password,
                    onValueChange = { model.updatePassClass(it) },
                    placeholderText = "Contraseña",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.width(250.dp)
                )
                AnimatedErrorText(user.passError)

                ImageButton(
                    imageRes = R.drawable.button_login,
                    contentDescription = "LogIn",
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        login(model, user.email, user.password, navController, context)
                    }
                )
            }
        }
    }
}


@Composable
fun AnimatedErrorText(error: String) {
    AnimatedVisibility(
        visible = error.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
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

fun login(
    model: UserAuthViewModel,
    email: String,
    password: String,
    navController: NavController,
    context: Context
) {
    if (validateForm(model, email, password)) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {

                val uid = firebaseAuth.currentUser?.uid
                if (uid != null) {
                    FirebaseDatabase.getInstance().reference
                        .child("users")
                        .child(uid)
                        .child("active")
                        .setValue(true)
                }

                navController.navigate(AppScreens.Home.name) {
                    popUpTo(AppScreens.LogIn.name) { inclusive = true }
                }
            } else {
                Toast.makeText(
                    context, "Error en ingreso ${it.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

fun validateForm(model: UserAuthViewModel, email: String, password: String): Boolean {
    if (email.isEmpty()) {
        model.updateEmailError("Correo vacío")
        return false
    } else model.updateEmailError("")

    if (!validEmailAddress(email)) {
        model.updateEmailError("Correo inválido")
        return false
    } else model.updateEmailError("")

    if (password.isEmpty()) {
        model.updatePassError("Contraseña vacía")
        return false
    } else model.updatePassError("")

    if (password.length < 6) {
        model.updatePassError("Contraseña demasiado corta")
        return false
    } else model.updatePassError("")

    return true
}

fun validEmailAddress(email: String): Boolean {
    val regex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"""
    return email.matches(regex.toRegex())
}
