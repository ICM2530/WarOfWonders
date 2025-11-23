package com.example.warofwonders.ui.screens.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.screens.inventory.components.PopupDeCriatura
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

            // SECCIÓN CRIATURAS
            SlotsSectionCriaturas(
                title = "CRIATURAS",
                criaturas = inventario.criaturas,
                onClickCriatura = { criatura ->
                    inventarioVM.seleccionarCriatura(criatura)
                }
            )


            // SECCIÓN RECURSOS
            SlotsSection(
                title = "RECURSOS",
                items = inventario.recursos.map { getDrawableId(it.imagen) }
            )
        }


        if (mostrarPopup && criaturaSeleccionada != null) {
            PopupDeCriatura(
                criatura = criaturaSeleccionada!!,
                onClose = { inventarioVM.cerrarPopup() }
            )
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
