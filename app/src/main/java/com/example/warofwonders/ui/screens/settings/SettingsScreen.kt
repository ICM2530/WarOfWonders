package com.example.warofwonders.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.ui.model.firebaseAuth
import com.example.warofwonders.ui.navigation.AppScreens
import com.example.warofwonders.ui.theme.WarOfWondersTheme

@Composable
fun SettingsScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bracket_name),
                contentDescription = "Settings",
                modifier = Modifier.size(width = 140.dp, height = 40.dp)
            )
            Text(
                text = "Ajustes",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SettingOption(text = "Sonido")
            SettingOption(text = "Notificaciones")
            SettingOption(text = "Idioma")
            SettingOption(text = "Cuenta")
            IconButton(
                onClick = {
                    firebaseAuth.signOut()
                    navController.navigate(AppScreens.StartUp.name) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.shield),
                    contentDescription = "Logout Icon",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.closechat),
                contentDescription = "Cerrar",
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Home) }
                    )
            )
            IconButton(
                onClick = {
                    firebaseAuth.signOut()
                    navController.navigate(AppScreens.StartUp.name) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.shield),
                    contentDescription = "Logout Icon",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun SettingOption(text: String) {
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bracket_coin),
            contentDescription = "Option Background",
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Black
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WarOfWondersTheme {
        SettingsScreen(navController = rememberNavController())
    }
}