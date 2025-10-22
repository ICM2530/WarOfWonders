package com.example.warofwonders.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.model.firebaseAuth
import com.example.warofwonders.ui.navigation.AppScreens

@Composable
fun SettingsScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo (si lo tienes)
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Título
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.chatframe),
                contentDescription = "Título Ajustes",
                modifier = Modifier.size(width = 200.dp, height = 60.dp)
            )
            Text(
                text = "Ajustes",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }

        // Opciones
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SettingOption("Sonido")
            SettingOption("Notificaciones")
            SettingOption("Idioma")
            SettingOption("Cuenta")

            // Botón de cerrar sesión
            Box(contentAlignment = Alignment.Center) {
                ImageButton(
                    imageRes = R.drawable.chatframe,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier
                        .size(width = 220.dp, height = 60.dp),
                    onClick = {
                        firebaseAuth.signOut()
                        navController.navigate(AppScreens.StartUp.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
                Text(
                    text = "Cerrar sesión",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                )
            }
        }


    }
}

@Composable
fun SettingOption(text: String) {
    Box(
        modifier = Modifier.size(width = 220.dp, height = 55.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.chatframe),
            contentDescription = "Opción",
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        )
    }
}
