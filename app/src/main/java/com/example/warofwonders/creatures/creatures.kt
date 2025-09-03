package com.example.warofwonders.creatures

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.navigation.AppScreens
import com.example.warofwonders.ui.theme.Green

@Composable
fun CreaturesScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fondo con imagen
        Image(
            painter = painterResource(R.drawable.background), // Cambia por tu fondo
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate(AppScreens.Home.name) },
                modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp), // Para que no agregue padding extra
            ) {
                Image(
                    painter = painterResource(id = R.drawable.returnbutton),
                    contentDescription = "Regresar",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Panel superior de usuario
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF7E481F) // Color sólido tipo madera
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Avatar
                    Image(
                        painter = painterResource(R.drawable.profile1), // avatar del usuario
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "user #11",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(R.drawable.shield),
                                contentDescription = "Editar",
                                modifier = Modifier.size(12.dp),
                                tint = Color.Yellow
                            )
                        }

                        Text(
                            text = "teusaquillo amigos",
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.shield),
                                    contentDescription = "Monedas",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("001", color = Color.White, fontSize = 14.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.shield),
                                    contentDescription = "Nivel",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("lvl 1", color = Color.White, fontSize = 14.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.shield),
                                    contentDescription = "XP",
                                    tint = Color.Green,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("10 xp", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secciones de clima
            ClimateSection(title = "CLIMA MEDIO", items = listOf(R.drawable.oso))
            Spacer(modifier = Modifier.height(16.dp))
            ClimateSection(title = "CLIMA FRIO", items = listOf(R.drawable.pinguino))
            Spacer(modifier = Modifier.height(16.dp))
            ClimateSection(title = "CLIMA CALIDO", items = listOf(R.drawable.rino, R.drawable.fenix))
            Spacer(modifier = Modifier.height(16.dp))
            ClimateSection(title = "ARMADURAS", items = listOf(R.drawable.aradura1, R.drawable.armadura2))
        }
    }
}
@Composable
fun ClimateSection(title: String, items: List<Int>) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3 slots por fila
            repeat(3) { index ->
                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF965E35) // Color sólido tipo madera
                    ),
                    border = BorderStroke(2.dp, Color.DarkGray), // Borde blanco de 2dp
                    modifier = Modifier
                        .size(width = 110.dp, height = 80.dp)
                ) {
                    if (index < items.size) {
                        Image(
                            painter = painterResource(items[index]),
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp).fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreaturesScreenPreview() {
    CreaturesScreen(navController = rememberNavController())
}