package com.example.warofwonders.ui.screens.signup

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.components.TextFieldImage
import com.example.warofwonders.ui.model.database
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.pathUsers
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignUpScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            Image(
                modifier = Modifier.size(280.dp, 220.dp),
                painter = painterResource(id = R.drawable.tittle_post),
                contentDescription = "Title"
            )

            TextFieldImage(
                value = name,
                onValueChange = { name = it },
                placeholderText = "Nombre",
                modifier = Modifier.width(250.dp)
            )

            TextFieldImage(
                value = lastName,
                onValueChange = { lastName = it },
                placeholderText = "Apellido",
                modifier = Modifier.width(250.dp)
            )

            TextFieldImage(
                value = phone,
                onValueChange = { phone = it },
                placeholderText = "Teléfono",
                modifier = Modifier.width(250.dp)
            )

            TextFieldImage(
                value = email,
                onValueChange = { email = it },
                placeholderText = "Correo",
                modifier = Modifier.width(250.dp)
            )

            TextFieldImage(
                value = password,
                onValueChange = { password = it },
                placeholderText = "Contraseña",
                modifier = Modifier.width(250.dp)
            )

            ImageButton(
                imageRes = R.drawable.button_signup,
                contentDescription = "Sign Up",
                modifier = Modifier.width(120.dp),
                onClick = {
                    registerUser(
                        auth,
                        name,
                        lastName,
                        phone,
                        email,
                        password,
                        context,
                        onSuccess = { navController.navigate(AppScreens.LogIn.name) },
                        onFailure = { e -> Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
                    )
                }
            )
        }
    }
}

fun registerUser(
    auth: FirebaseAuth,
    name: String,
    lastName: String,
    phone: String,
    email: String,
    password: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    if (email.isEmpty() || password.isEmpty() || name.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
        Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
            val user = MyUserState(
                name = name,
                lastName = lastName,
                phone = phone,
                email = email
            )

            val ref = database.getReference(pathUsers)
            ref.child(uid).setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } else {
            onFailure(task.exception ?: Exception("Error desconocido"))
        }
    }
}