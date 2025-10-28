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
import com.example.warofwonders.ui.screens.inventory.components.ProfileUser
import com.example.warofwonders.ui.screens.inventory.components.SlotsSection

@Composable
fun InventoryScreen(
    navController: NavHostController,
    inventarioVM: InventarioViewModel = viewModel()
) {
    val inventario by inventarioVM.inventario.collectAsState()

    // 🔹 Cargar inventario una sola vez al entrar
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
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileUser(navController = navController)

            // 🐉 Mostrar criaturas del usuario
            if (inventario.criaturas.isNotEmpty()) {
                SlotsSection(
                    title = "CRIATURAS",
                    items = inventario.criaturas.map {
                        getDrawableId(it.imagen)
                    }
                )
            } else {
                SlotsSection(title = "CRIATURAS", items = emptyList())
            }

            // ⚒️ Mostrar recursos del usuario
            if (inventario.recursos.isNotEmpty()) {
                SlotsSection(
                    title = "RECURSOS",
                    items = inventario.recursos.map {
                        getDrawableId(it.imagen)
                    }
                )
            } else {
                SlotsSection(title = "RECURSOS", items = emptyList())
            }
        }
    }
}

/**
 * Convierte el nombre de la imagen (guardado en Firebase)
 * a su ID del drawable local.
 */
@Composable
fun getDrawableId(nombre: String): Int {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(nombre) {
        context.resources.getIdentifier(nombre, "drawable", context.packageName)
    }
}
