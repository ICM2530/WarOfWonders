package com.example.warofwonders.ui.screens.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.example.warofwonders.ui.components.AlertCustomPopup
import com.example.warofwonders.ui.components.AlertDialogPopup
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.components.TextFieldImage
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.ShopViewModel
import com.example.warofwonders.ui.screens.shop.components.SlotsSectionTienda

@Composable
fun ShopScreen(
    navController: NavHostController,
    userState: MyUserState,
    inventarioVM: InventarioViewModel,
    shopVM: ShopViewModel = viewModel(),
) {

    val recursos by shopVM.recursos.collectAsState()
    val mensajeCompra by shopVM.mensajeCompra.collectAsState()

    LaunchedEffect(Unit) {
        shopVM.cargarRecursos()
        inventarioVM.cargarInventario()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // FONDO DE LA TIENDA
        Image(
            painter = painterResource(id = R.drawable.fondotienda),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.chatframe),
                    contentDescription = "Marco tienda",
                    modifier = Modifier
                        .width(250.dp)
                        .height(80.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "TIENDA",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // --- Secciones filtradas ---
            val pociones = recursos.filter { it.tipo == "pocion" }
            if (pociones.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(color = Color(0xFF4F2B12), shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "POCIONES",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                SlotsSectionTienda(
                    recursos = pociones,
                    onComprar = { recurso ->
                        shopVM.comprarRecurso(recurso, userState, inventarioVM)
                    }
                )
            }

            val armaduras = recursos.filter { it.tipo == "armadura" }
            if (armaduras.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(color = Color(0xFF4F2B12), shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ARMADURAS",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                SlotsSectionTienda(
                    recursos = armaduras,
                    onComprar = { recurso ->
                        shopVM.comprarRecurso(recurso, userState, inventarioVM)
                    }
                )
            }

            val criaturas = recursos.filter { it.tipo == "criatura" }
            if (criaturas.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(color = Color(0xFF4F2B12), shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CRIATURAS EXOTICASxc",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                SlotsSectionTienda(
                    recursos = criaturas,
                    onComprar = { recurso ->
                        shopVM.comprarRecurso(recurso, userState, inventarioVM)
                    }
                )
            }
        }

        if (mensajeCompra != null) {
            val (titulo, imagen) = when (mensajeCompra) {
                "Ya tienes este artículo" -> "ARTÍCULO REPETIDO" to R.drawable.flymoney
                "No tienes suficientes monedas" -> "SIN MONEDAS" to R.drawable.flymoney
                else -> "COMPRA REALIZADA" to R.drawable.flymoney
            }

            AlertCustomPopup(
                title = titulo,
                message = mensajeCompra!!,
                imageRes = imagen,
                onAccept = { shopVM.limpiarMensaje() }
            )
        }
    }
}

