package com.example.warofwonders.ui.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    supportingText = { Text(user.emailError, color = Color.Red) },
                    placeholderText = "Correo",
                    modifier = Modifier.width(250.dp)
                )

                TextFieldImage(
                    value = user.password,
                    onValueChange = { model.updatePassClass(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = { Text(user.passError, color = Color.Red) },
                    placeholderText = "Contraseña",
                    modifier = Modifier.width(250.dp)
                )

                ImageButton(
                    imageRes = R.drawable.button_login,
                    contentDescription = "LogIn",
                    modifier = Modifier.width(120.dp),
                    onClick = { login(model, user.email, user.password, navController, context) }
                )
            }
        }
    }
}

fun login(model: UserAuthViewModel, email: String, password: String, navController: NavController, context: Context) {
    if(validateForm(model, email, password)) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                navController.navigate(AppScreens.Home.name) {
                    popUpTo(AppScreens.LogIn.name) { inclusive = true }
                }
            } else {
                Toast.makeText(
                    context, "Login error ${it.exception.toString()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

fun validateForm(model: UserAuthViewModel, email: String, password: String): Boolean {
    if(email.isEmpty()){ model.updateEmailError("Correo vacío")
        return false
    } else {model.updateEmailError("")}
    if(!validEmailAddress(email)){model.updateEmailError("Correo inválido")
        return false
    } else {model.updateEmailError("")}
    if(password.isEmpty()) {model.updatePassError("Contraseña vacía")
        return false
    } else {model.updatePassError("")}
    if(password.length < 6) {model.updatePassError("Contraseña demasiado corta")
        return false
    } else {model.updatePassError("")}
    return true
}

fun validEmailAddress(email:String):Boolean{
    val regex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"""
    return email.matches(regex.toRegex())
}