package com.example.warofwonders.ui.screens.inventory.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.warofwonders.ui.model.Recurso
import com.example.warofwonders.ui.screens.inventory.getDrawableId

@Composable
fun PopupRecursos(
    recursos: List<Recurso>,
    recursosEquipados: List<Recurso>,
    saludActual: Int,
    saludFlotante: Int,
    onSelect: (Recurso) -> Unit,
    onUnselect: (Recurso) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = Color(0xFFe0c89a),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {

        Column(modifier = Modifier.padding(20.dp)) {

            Text("Selecciona un recurso")

            Text("Salud actual: $saludActual")

            AnimatedVisibility(visible = saludFlotante != 0) {
                Text(
                    text = if (saludFlotante > 0) "+$saludFlotante" else "$saludFlotante",
                    color = if (saludFlotante > 0) Color.Green else Color.Red,
                    modifier = Modifier.padding(4.dp)
                )
            }

            recursos.forEach { recurso ->
                val estaEquipado = recursosEquipados.any { it.id == recurso.id }

                Row(verticalAlignment = Alignment.CenterVertically) {


                    AsyncImage(
                        model = recurso.imagen,
                        contentDescription = recurso.nombre,
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )

                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(recurso.nombre)
                        Text("Protección: ${recurso.proteccion}")
                    }


                    if (!estaEquipado) {
                        Button(onClick = { onSelect(recurso) },
                            modifier = Modifier.padding(start = 8.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF795C34),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Equipar")

                        }
                    } else {
                        Button(onClick = { onUnselect(recurso) },
                            modifier = Modifier.padding(start = 1.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF795C34),
                                contentColor = Color.White
                            )
                            ) {
                            Text("Quitar")
                        }
                    }
                }
            }

            Button(onClick ={ onClose()},
                modifier = Modifier.padding(start = 1.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF342711),
                    contentColor = Color.White
                )
                ) { Text("Cerrar") }
        }
    }
}
