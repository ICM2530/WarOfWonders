package com.example.warofwonders.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.profile.ProfileContent
import com.example.warofwonders.R
import com.example.warofwonders.navigation.AppScreens

//import com.example.warofwonders.navigation.AppScreens

@Composable
fun MapContent(
    navController: NavController,
    puntoNombre: String = "A Bao Qua",
    clima: String = "Soleado",
    altitud: String = "2500 msnm",
    zona: String = "Zona Norte",
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mapbackground),
            contentDescription = "Map background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Punto de interés
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.poi),
                    contentDescription = "Punto de interés",
                    tint = Color.Red,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(puntoNombre, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Clima: $clima", fontSize = 20.sp, color = Color.Black)
                Text("Altitud: $altitud", fontSize = 20.sp, color = Color.Black)
                Text("Zona: $zona", fontSize = 20.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(150.dp))

                Image(
                    painter = painterResource(id = R.drawable.crosshair),
                    contentDescription = "Ubicacion",
                    modifier = Modifier.size(100.dp)
                )

            }

            Spacer(modifier = Modifier.height(150.dp))

            // Botones
            Row(
                modifier = Modifier
                    .padding(1.dp),
                horizontalArrangement = Arrangement.spacedBy(100.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.returnbutton),
                    contentDescription = "retorno",
                    modifier = Modifier.size(100.dp)
                        .clickable(
                            onClick = { navController.navigate(AppScreens.Home.name) }
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.inventorybutton),
                    contentDescription = "inventario",
                    modifier = Modifier.size(200.dp)
                        .clickable(
                            onClick = { navController.navigate(AppScreens.Creatures.name) }
                        )
                )
            }
        }
    }
}

@Composable
fun MapScreen(navController: NavController) {
    MapContent(navController)
    // lógica de navegación aquí si se necesita
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapPreview() {
    MapContent(navController = rememberNavController())
}
