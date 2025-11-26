package com.example.warofwonders.ui.screens.combat

import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.Dp
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
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.model.Criatura

@Composable
fun CombatScreen(navController: NavController, attackerId: String?, defenderId: String?, combatViewModel: CombatViewModel = viewModel()) {
    val uiState by combatViewModel.uiState.collectAsState()
    val attacker = uiState.attacker
    val defender = uiState.defender
    val result = uiState.result
    val inventarioVM: InventarioViewModel = viewModel()
    val inventarioState by inventarioVM.inventario.collectAsState()
    val criaturasUsuario = inventarioState.criaturas

    var selectedCriatura by remember { mutableStateOf<Criatura?>(null) }
    var selectionVisible by remember { mutableStateOf(true) }
    var hasConfirmed by remember { mutableStateOf(false) }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    // Cargar inventario al entrar en la pantalla
    androidx.compose.runtime.LaunchedEffect(Unit) {
        inventarioVM.cargarInventario()
    }

    val isLocalAttacker = currentUid != null && currentUid == attackerId
    val isLocalDefender = currentUid != null && currentUid == defenderId
    val selectionAllowed = isLocalAttacker || isLocalDefender

    // Mostrar selector de criatura antes de iniciar combate y cargar preview de combatientes
    androidx.compose.runtime.LaunchedEffect(attackerId, defenderId) {
        // resetear selección cada vez que cambian los ids
        selectedCriatura = null
        selectionVisible = true
        hasConfirmed = false
        // cargar nombres/clanes/imagenes previas
        if (attackerId != null && defenderId != null) {
            combatViewModel.loadCombatantsPreview(attackerId, defenderId)
            combatViewModel.startEncounterListener(attackerId, defenderId)
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
                    // Mostrar imagen de criatura y nombre/clan
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val atkImage = if (selectionVisible && selectedCriatura != null && isLocalAttacker) selectedCriatura?.imagen else attacker?.creatureImage
                        if (!atkImage.isNullOrBlank()) {
                            AsyncImage(model = atkImage, contentDescription = "Atacante criatura", modifier = Modifier.size(64.dp).padding(end = 8.dp), contentScale = ContentScale.Crop)
                        }
                        TopNameLine(
                            title = attacker?.name ?: "User #11",
                            subtitle = attacker?.clan ?: "teusaquillo amigos"
                        )
                    }
                    // mostrar barra de vida y texto numérico
                    HealthBar(current = uiState.attackerHp ?: attacker?.maxHealth ?: 0, max = attacker?.maxHealth ?: 0)
                    Text(text = "HP: ${uiState.attackerHp ?: attacker?.maxHealth ?: 0} / ${attacker?.maxHealth ?: 0}", color = Color.White, fontSize = 12.sp)
                }

                // info del defensor
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 10.dp, top = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        TopNameLine(
                            title = defender?.name ?: "User #18",
                            subtitle = defender?.clan ?: "los piratas"
                        )
                        val defImage = if (selectionVisible && selectedCriatura != null && isLocalDefender) selectedCriatura?.imagen else defender?.creatureImage
                        if (!defImage.isNullOrBlank()) {
                            AsyncImage(model = defImage, contentDescription = "Defensor criatura", modifier = Modifier.size(64.dp).padding(start = 8.dp), contentScale = ContentScale.Crop)
                        }
                    }
                    HealthBar(current = uiState.defenderHp ?: defender?.maxHealth ?: 0, max = defender?.maxHealth ?: 0)
                    Text(text = "HP: ${uiState.defenderHp ?: defender?.maxHealth ?: 0} / ${defender?.maxHealth ?: 0}", color = Color.White, fontSize = 12.sp)
                }

                // Si el combate terminó, mostrar resultado
                if (result != null) {
                    CombatResultDisplay(result = result, combatViewModel = combatViewModel, navController = navController)
                } else {
                    // Si aún no hay resultado y se deben mostrar opciones de selección
                    if (selectionVisible && attackerId != null && defenderId != null) {
                        // UI para elegir criatura del inventario
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(if (isLocalAttacker) "Selecciona una criatura para el atacante:" else if (isLocalDefender) "Selecciona una criatura para el defensor:" else "Selecciona una criatura para el combate:", color = Color.White)
                                Spacer(Modifier.height(8.dp))
                                if (criaturasUsuario.isEmpty()) {
                                    Text("No tienes criaturas. Inicia combate sin criatura.", color = Color.White)
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            // iniciar sin criatura (usa stats por nivel)
                                            selectionVisible = false
                                            combatViewModel.startCombatByIds(attackerId, defenderId)
                                        }, enabled = !uiState.isLoading) { Text("Iniciar sin criatura") }
                                        // El boton de cancelar se mueve a la parte inferior
                                    }
                                } else {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(criaturasUsuario) { criatura ->
                                            val isSelected = selectedCriatura?.id == criatura.id
                                            Card(
                                                modifier = Modifier
                                                    .width(150.dp)
                                                    .height(170.dp)
                                                    .clickable { selectedCriatura = criatura },
                                                shape = RoundedCornerShape(8.dp),
                                                border = if (isSelected) BorderStroke(2.dp, Color.Yellow) else null,
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFF3A3A3A) else Color(0xFF222222)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    if (!criatura.imagen.isNullOrBlank()) {
                                                        AsyncImage(model = criatura.imagen, contentDescription = criatura.nombre, modifier = Modifier.size(72.dp), contentScale = ContentScale.Crop)
                                                        Spacer(Modifier.height(8.dp))
                                                    }
                                                    Text(criatura.nombre, color = Color.White, fontSize = 13.sp, maxLines = 1)
                                                    Spacer(Modifier.height(6.dp))
                                                    Text("HP: ${criatura.salud}", color = Color.White, fontSize = 12.sp)
                                                    Text("DMG: ${criatura.dano}", color = Color.White, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    // Eliminado el botón duplicado aquí para dejar solo el botón inferior
                                }
                            }
                        }
                    }
                }
            }

            // Fila central con las imágenes de las criaturas (aparecen solo cuando el combate ha iniciado)
            if (uiState.isLoading) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                        val atkCenterImage = attacker?.creatureImage ?: (if (selectionVisible && selectedCriatura != null && isLocalAttacker) selectedCriatura?.imagen else null)
                        if (!atkCenterImage.isNullOrBlank()) {
                            AsyncImage(model = atkCenterImage, contentDescription = "Atacante criatura (centro)", modifier = Modifier.size(120.dp), contentScale = ContentScale.Crop)
                        } else {
                            Spacer(modifier = Modifier.size(120.dp))
                        }

                        val defCenterImage = defender?.creatureImage ?: (if (selectionVisible && selectedCriatura != null && isLocalDefender) selectedCriatura?.imagen else null)
                        if (!defCenterImage.isNullOrBlank()) {
                            AsyncImage(model = defCenterImage, contentDescription = "Defensor criatura (centro)", modifier = Modifier.size(120.dp), contentScale = ContentScale.Crop)
                        } else {
                            Spacer(modifier = Modifier.size(120.dp))
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
                    // (El estatus movido a la pantalla de abajo)
                    // Area inferior (Placeholders eliminados)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))
                        if (result == null && !uiState.isLoading) {
                            if (attackerId == null && defenderId == null) {
                                Text("No hay nadie a quien enfrentar!...", color = Color.White)
                            } else {
                                val atk = attacker
                                val def = defender
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            // Confirmar la selección en Firebase y esperar al otro jugador
                                            hasConfirmed = true
                                            combatViewModel.confirmSelectionForEncounter(attackerId, defenderId, selectedCriatura)
                                        },
                                        enabled = ((!uiState.isLoading) && !hasConfirmed && (((selectedCriatura != null) && selectionAllowed) || ((atk != null && def != null)))),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Text(text = if (!hasConfirmed) "Confirmar y esperar" else "Esperando...", color = Color.White)
                                    }

                                    Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))) {
                                        Text("Cancelar", color = Color.White)
                                    }
                                }
                            }
                        } else if (uiState.isLoading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Combate en progreso...", color = Color.White)
                                Spacer(Modifier.height(6.dp))
                                Text(text = if (uiState.result == null) "Ronda activa" else "", color = Color.White)
                            }
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
            val winnerName = combatViewModel.getWinner()?.name ?: result.winnerId
            Text(
                "${winnerName} gana!",
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
    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun HealthBar(current: Int, max: Int) {
    // Se podria renderizar la barra (imagen de fondo) y superponer un rectángulo o texto aca?
    Box(modifier = Modifier.height(18.dp).width(120.dp), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.barra),
            contentDescription = "Barra de vida",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(text = "$current / $max", color = Color.White, fontSize = 10.sp)
    }
}