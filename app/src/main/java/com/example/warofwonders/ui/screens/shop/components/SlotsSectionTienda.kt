package com.example.warofwonders.ui.screens.shop.components


import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.warofwonders.ui.model.Recurso
import com.example.warofwonders.ui.screens.inventory.getDrawableId

@Composable
fun SlotsSectionTienda(
    recursos: List<Recurso>,
    columns: Int = 3,
    onComprar: (Recurso) -> Unit
) {
    val rows = (recursos.size + columns - 1) / columns
    var recursoSeleccionado by remember { mutableStateOf<Recurso?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(columns) { columnIndex ->
                    val index = rowIndex * columns + columnIndex
                    if (index < recursos.size) {
                        val recurso = recursos[index]

                        Card(
                            modifier = Modifier.size(130.dp, 170.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xD7B68047)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = recurso.imagen,
                                    contentDescription = recurso.nombre,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { recursoSeleccionado = recurso },
                                    contentScale = ContentScale.Fit,
                                    placeholder = painterResource(R.drawable.ic_menu_gallery),
                                    error = painterResource(R.drawable.ic_menu_close_clear_cancel)
                                )

                                Text(
                                    text = recurso.nombre,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(getDrawableId("moneda")),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "${recurso.precio}",
                                        color = Color.Yellow,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { onComprar(recurso) },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF8A5A33),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Comprar", fontSize = 12.sp)
                                }
                            }
                        }

                    } else {
                        Card(
                            modifier = Modifier.size(130.dp, 170.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xD7B68047))
                        ) {}
                    }
                }
            }
        }
    }

    recursoSeleccionado?.let { recurso ->

        Box(
            modifier = Modifier
                .fillMaxSize(),

            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xD7B68047),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentHeight()
                    .widthIn(min = 260.dp, max = 320.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = recurso.nombre,
                        color = Color.White,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AsyncImage(
                        model = recurso.imagen,
                        contentDescription = recurso.nombre,
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Protección: ${recurso.proteccion}", color = Color.White)
                    Text("Precio: ${recurso.precio}", color = Color.Yellow)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { recursoSeleccionado = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF342711),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}
