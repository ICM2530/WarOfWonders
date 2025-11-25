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
import androidx.compose.ui.window.Dialog
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

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                repeat(columns) { columnIndex ->
                    val index = rowIndex * columns + columnIndex

                    // Cada card ocupa exactamente 1/3 del ancho
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f) // Mantiene proporción igual en todos los dispositivos
                    ) {

                        if (index < recursos.size) {
                            val recurso = recursos[index]

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xD7B68047)
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                ) {

                                    AsyncImage(
                                        model = recurso.imagen,
                                        contentDescription = recurso.nombre,
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .aspectRatio(1f)
                                            .clickable { recursoSeleccionado = recurso },
                                        contentScale = ContentScale.Fit
                                    )

                                    Text(
                                        text = recurso.nombre,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(getDrawableId("moneda")),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "${recurso.precio}",
                                            color = Color.Yellow,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Button(
                                        onClick = { onComprar(recurso) },
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(30.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8A5A33),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Comprar", fontSize = 6.sp)
                                    }
                                }
                            }

                        } else {

                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xD7B68047)
                                )
                            ) {}
                        }
                    }
                }
            }
        }
    }


    if (recursoSeleccionado != null) {
        Dialog(onDismissRequest = { recursoSeleccionado = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723))
                ) {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AsyncImage(
                            model = recursoSeleccionado?.imagen,
                            contentDescription = recursoSeleccionado?.nombre,
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = recursoSeleccionado?.nombre ?: "",
                            color = Color.White,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Protección: ${recursoSeleccionado?.proteccion}",
                            color = Color.Green,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "Daño: ${recursoSeleccionado?.dano}",
                            color = Color.Red,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "Precio: ${recursoSeleccionado?.precio}",
                            color = Color.Yellow,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onComprar(recursoSeleccionado!!) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8A5A33),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Comprar")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { recursoSeleccionado = null }) {
                            Text("Cerrar", color = Color.White)
                        }
                    }
                }
            }
        }
    }


}
