package com.example.warofwonders.ui.screens.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.screens.inventory.components.PopupDeCriatura
import com.example.warofwonders.ui.screens.inventory.components.PopupRecursos
import com.example.warofwonders.ui.screens.inventory.components.ProfileUser
import com.example.warofwonders.ui.screens.inventory.components.SlotsSection
import com.example.warofwonders.ui.screens.inventory.components.SlotsSectionCriaturas

@Composable
fun InventoryScreen(
    navController: NavHostController,
    inventarioVM: InventarioViewModel = viewModel()
) {
    val inventario by inventarioVM.inventario.collectAsState()
    val criaturaSeleccionada by inventarioVM.criaturaSeleccionada.collectAsState()
    val mostrarPopup by inventarioVM.mostrarPopup.collectAsState()
    val mostrarPopupRecursos by inventarioVM.mostrarPopupRecursos.collectAsState()
    val saludFlotante by inventarioVM.saludFlotante.collectAsState()





    LaunchedEffect(Unit) {
        inventarioVM.cargarInventario()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background_inventory),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileUser(navController = navController)

            // CRIATURAS
            SlotsSectionCriaturas(
                title = "CRIATURAS",
                criaturas = inventario.criaturas,
                onClickCriatura = { criatura ->
                    inventarioVM.seleccionarCriatura(criatura)
                }
            )

            SlotsSection(
                title = "RECURSOS",
                items = inventario.recursos
            )



        }


        AnimatedVisibility(
            visible = mostrarPopup && criaturaSeleccionada != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                PopupDeCriatura(
                    criatura = criaturaSeleccionada!!,
                    onAbrirPopupRecursos = { inventarioVM.abrirPopupRecursos() },
                    onClose = { inventarioVM.cerrarPopup() },
                    onDesequipar = { recurso ->
                        inventarioVM.quitarArmadura(criaturaSeleccionada!!.id, recurso)
                    }
                )
            }
        }


        AnimatedVisibility(
            visible = mostrarPopupRecursos,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                PopupRecursos(
                    recursos = inventario.recursos,
                    mensajeError = inventarioVM.mensajeError.value,
                    recursosEquipados = criaturaSeleccionada!!.recursos,
                    danoActual = criaturaSeleccionada!!.dano,
                    saludActual = criaturaSeleccionada!!.salud,
                    saludFlotante = saludFlotante ?: 0,
                    onSelect = { recurso ->
                        inventarioVM.asignarRecursoACriatura(criaturaSeleccionada!!.id, recurso)
                        inventarioVM.refrescarCriaturaSeleccionada()
                    },
                    onUnselect = { recurso ->
                        inventarioVM.quitarArmadura(criaturaSeleccionada!!.id, recurso)
                        inventarioVM.refrescarCriaturaSeleccionada()
                    },
                    onClose = { inventarioVM.cerrarPopupRecursos() }
                )


            }
        }

        // SALUD FLOTANTE
        if (saludFlotante != null) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                Text(
                    text = "+${saludFlotante}",
                    color = Color.Green,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            LaunchedEffect(saludFlotante) {
                kotlinx.coroutines.delay(800)
                inventarioVM.limpiarFlotante()
            }
        }
    }
}

@Composable
fun getDrawableId(nombre: String): Int {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(nombre) {
        context.resources.getIdentifier(nombre, "drawable", context.packageName)
    }
}
