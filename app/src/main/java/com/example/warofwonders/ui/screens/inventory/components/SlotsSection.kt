package com.example.warofwonders.ui.screens.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.warofwonders.ui.model.Recurso

@Composable
fun SlotsSection(
    title: String,
    items: List<Recurso>,
    columns: Int = 3
) {
    var recursoSeleccionado by remember { mutableStateOf<Recurso?>(null) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val rows = (items.size + columns - 1) / columns

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(rows) { rowIndex ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(columns) { columnIndex ->
                        val itemIndex = rowIndex * columns + columnIndex

                        Card(
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF965E35)
                            ),
                            border = BorderStroke(2.dp, Color.DarkGray),
                            modifier = Modifier.size(width = 110.dp, height = 80.dp)
                        ) {
                            if (itemIndex < items.size) {
                                val recurso = items[itemIndex]
                                AsyncImage(
                                    model = recurso.imagen,
                                    contentDescription = recurso.nombre,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize()
                                        .clickable { recursoSeleccionado = recurso }, // click para popup
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP DEL RECURSO ---
    recursoSeleccionado?.let { recurso ->
        Box(
            modifier = Modifier
                .fillMaxSize(),

            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xD7B68047), // fondo del popup
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
