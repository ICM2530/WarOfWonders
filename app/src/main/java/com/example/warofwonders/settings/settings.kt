package com.example.warofwonders.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.chat.ChatScreen
import com.example.warofwonders.navigation.AppScreens

@Composable
fun SettingsScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
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
                painter = painterResource(id = R.drawable.bracketname),
                contentDescription = "Settings",
                modifier = Modifier.size(width = 140.dp, height = 40.dp)
            )
            Text(
                text = "Ajustes",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color.Black
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
                        onClick = { navController.navigate(AppScreens.Home.name) }
                    )
            )
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
            painter = painterResource(id = R.drawable.coinbracket),
            contentDescription = "Option Background",
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.Black
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    val navcontroller= rememberNavController()
    SettingsScreen(navcontroller)
}