package com.example.warofwonders.ui.screens.combat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.warofwonders.R
import androidx.compose.ui.graphics.Color
import com.example.warofwonders.ui.model.CombatViewModel

@Composable
fun CombatScreen(navController: NavController, attackerId: String?, defenderId: String?, combatViewModel: CombatViewModel = viewModel()) {
    val uiState by combatViewModel.uiState.collectAsState()
    val attacker = uiState.attacker
    val defender = uiState.defender
    val result = uiState.result

    // Cuando este composable se muestre con IDs de atacante/defensor, preguntarle al ViewModel que cargue e inicie el combate
    androidx.compose.runtime.LaunchedEffect(attackerId, defenderId) {
        if (attackerId != null && defenderId != null) {
            combatViewModel.startCombatByIds(attackerId, defenderId)
        }
    }

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

                // info del atacante
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp, top = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    TopNameLine(
                        title = attacker?.name ?: "User #11",
                        subtitle = attacker?.clan ?: "teusaquillo amigos"
                    )
                    HealthBar()
                }

                // info del defensor
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 10.dp, top = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    TopNameLine(
                        title = defender?.name ?: "User #18",
                        subtitle = defender?.clan ?: "los piratas"
                    )
                    HealthBar()
                }

                // Resultado de combate o accion
                if (result != null) {
                    CombatResultDisplay(result = result, combatViewModel = combatViewModel, navController = navController)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                            .padding(top = 170.dp),

                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animal atacante
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${attacker?.name ?: "bear"} lvl${attacker?.level ?: 1}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Image(
                                painter = painterResource(id = R.drawable.oso),
                                contentDescription = "Oso",
                                modifier = Modifier.size(130.dp)
                            )
                        }

                        // Animal defensor
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${defender?.name ?: "rino"} lvl${defender?.level ?: 2}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Image(
                                painter = painterResource(id = R.drawable.rino),
                                contentDescription = "Rino",
                                modifier = Modifier.size(130.dp)
                            )
                        }
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

                    Image(
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
                
                if (result == null && !uiState.isLoading) {
                    if (attackerId == null && defenderId == null) {
                        Text("No hay nadie a quien enfrentar!...", color = Color.White)
                    }
                    else{
                        Button(
                            onClick = {
                                val atk = attacker
                                val def = defender
                                if (atk != null && def != null) {
                                    combatViewModel.startCombat(atk, def)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.play),
                                contentDescription = "PLAY",
                                modifier = Modifier
                                    .size(width = 200.dp, height = 90.dp)
                                    .clickable { }
                            )
                        }
                    }
                } else if (uiState.isLoading) {
                    Text("Combate en progreso...", color = Color.White)
                }
                
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CombatResultDisplay(result: com.example.warofwonders.data.model.CombatResult, combatViewModel: CombatViewModel, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Resultado de batalla",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "${result.winnerId} gana!",
                color = Color.Yellow,
                fontSize = 18.sp
            )
            Text(
                "Recursos transferidos: ${result.resourcesTransferred}",
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                "Rondas: ${result.rounds}",
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    combatViewModel.resetCombat()
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Volver al mapa", color = Color.White)
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