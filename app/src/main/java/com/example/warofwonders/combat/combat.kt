package com.example.warofwonders.combat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.warofwonders.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.chat.ChatScreen

@Composable
fun CombatScreen(navController: NavController) {

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fonfotel),
            contentDescription = "fondo verde",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.fondocriaturas),
                    contentDescription = "Escenario bosque",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

               //info de jigador 11 que es el principal en el ejemplo del mockup
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp, top = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    TopNameLine(title = "User #11", subtitle = "teusaquillo amigos")
                    HealthBar()
                }

                //el jugador pero del rino (rl nombre)
                //se supone que el subtitulo es el clan
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 10.dp, top = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    TopNameLine(title = "User #18", subtitle = "los piratas")
                    HealthBar()
                }


                Row( //en esta row van los dos animales con su nombre ynivel
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                        .padding(top = 170.dp),

                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //imagen del oso
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("bear lvl1", color = Color.White, fontSize = 12.sp)
                        Image(
                            painter = painterResource(id = R.drawable.oso),
                            contentDescription = "Oso",
                            modifier = Modifier.size(130.dp)
                        )
                    }

                    //imagen del rino
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("rino lvl2", color = Color.White, fontSize = 12.sp)
                        Image(
                            painter = painterResource(id = R.drawable.rino),
                            contentDescription = "Rino",
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    Image( //imagen del inventario
                        painter = painterResource(id = R.drawable.inventario),
                        contentDescription = "Inventario",
                        modifier = Modifier
                            .weight(1f)
                            .height(190.dp),
                        contentScale = ContentScale.FillWidth
                    )

                    Spacer(Modifier.width(10.dp))


                    Column(
                        modifier = Modifier.width(110.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CLIMA MEDIO",
                            color = Color.White,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.mappin),
                            contentDescription = "Pin",
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Image(
                            painter = painterResource(id = R.drawable.oso),
                            contentDescription = "Oso pequeño",
                            modifier = Modifier.size(82.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("bear lvl1", color = Color.White, fontSize = 12.sp)
                    }
                }


                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { /* TODO: acción PLAY */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = "PLAY",
                        modifier = Modifier.size(width = 200.dp, height = 90.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun TopNameLine(title: String, subtitle: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = subtitle,
        color = Color.White,
        fontSize = 11.sp
    )
}

@Composable
private fun HealthBar() {

    Image(
        painter = painterResource(id = R.drawable.barra),
        contentDescription = "Barra de vida",
        modifier = Modifier
            .height(18.dp)
            .width(100.dp),
        contentScale = ContentScale.FillBounds
    )
}



@Preview(showBackground = true)
@Composable
fun CombatscreenPreview() {
    val navcontroller= rememberNavController()
    CombatScreen(navcontroller)
}