package com.example.warofwonders.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
fun ProfileScreen(
    id: Int = 7777,
    usuario: String = "Viejodamis",
    tipo: String = "Líder",
    clan: String = "Sieg Zeon",
    experiencia: Int = 1200,
    pais: String = "Colombia",
    imagen: Int = R.drawable.avatar,
    navController: NavHostController,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.profilebackground),
            contentDescription = "Profile background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(125.dp))
            // Imagen de perfil
            Image(
                painter = painterResource(id = imagen),
                contentDescription = "User profile image",
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .border(25.dp, Color.DarkGray, CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Datos principales
            Text(usuario, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ID: $id", fontSize = 32.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Rol: $tipo", fontSize = 32.sp, color = Color.Red)
            Text("Clan: $clan", fontSize = 32.sp, color = Color.Cyan)
            Text("Experiencia: $experiencia", fontSize = 32.sp, color = Color.Green)
            Text("País: $pais", fontSize = 32.sp, color = Color.Yellow)

            Spacer(modifier = Modifier.height(16.dp))

            // Botones
            Row(
                modifier = Modifier
                    .padding(1.dp)
                    .clickable(
                        onClick = { }
                    ),
                horizontalArrangement = Arrangement.spacedBy(24.dp)

            ) {
                Image(
                    painter = painterResource(id = R.drawable.returnbutton),
                    contentDescription = "retorno",
                    modifier = Modifier.size(100.dp).clickable {
                        navController.navigate(AppScreens.Home.name)
                    }
                )

                Image(
                    painter = painterResource(id = R.drawable.editbutton),
                    contentDescription = "editar",
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    WarOfWondersTheme {
        ProfileScreen(navController = rememberNavController())
    }
}
