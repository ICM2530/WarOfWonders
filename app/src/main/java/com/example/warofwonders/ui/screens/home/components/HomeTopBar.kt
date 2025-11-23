package com.example.warofwonders.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.navigation.AppScreens

@Composable
fun HomeTopBar(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // --- Fila superior: perfil + iconos ---
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bracket_name),
                    contentDescription = "Usuario",
                    modifier = Modifier
                        .fillMaxSize()

                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "World Recoverer",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Teusaquillo amigos",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bell_icon),
                    contentDescription = "Notificaciones",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            navController.navigate(AppScreens.Contacts.name)
                        }
                )

                Image(
                    painter = painterResource(id = R.drawable.iconcontactos),
                    contentDescription = "Contactos",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.navigate(AppScreens.Contacts.name)
                        }
                )

                Image(
                    painter = painterResource(id = R.drawable.configbutton),
                    contentDescription = "Configuración",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            navController.navigate(AppScreens.Settings.name)
                        }
                )
            }
        }


        Row(
            modifier = Modifier
                .padding(top = 48.dp)
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bloque de monedas: fijo y centrado dentro del contenedor
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bracket_coin),
                    contentDescription = "Monedas",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    text = "000",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Columna con shop arriba y cámara abajo (alineadas a la derecha)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.shop),
                    contentDescription = "Tienda",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            navController.navigate(AppScreens.Contacts.name)
                        }
                )

                Image(
                    painter = painterResource(id = R.drawable.camara),
                    contentDescription = "Cámara",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            navController.navigate(AppScreens.Camera.name)
                        }
                )
            }
        }

    }
}
