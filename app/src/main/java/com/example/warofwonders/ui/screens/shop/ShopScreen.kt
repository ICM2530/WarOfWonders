package com.example.warofwonders.ui.screens.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {
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




            SlotsSectionTienda(
                recursos = recursos,
                onComprar = { recurso ->
                    shopVM.comprarRecurso(
                        recurso = recurso,
                        usuario = userState,
                        inventarioVM = inventarioVM
                    )
                }
            )
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
