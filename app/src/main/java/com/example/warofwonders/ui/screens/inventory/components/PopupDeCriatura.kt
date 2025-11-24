package com.example.warofwonders.ui.screens.inventory.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.model.Recurso
import com.example.warofwonders.ui.screens.inventory.getDrawableId

@Composable
fun PopupDeCriatura(
    criatura: Criatura,
    onAbrirPopupRecursos: () -> Unit,
    onClose: () -> Unit,
    onDesequipar: (Recurso) -> Unit
) {

    val armaduraEquipada = criatura.recursos.firstOrNull { it.tipo == "armadura" }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Surface(
            color = Color(0xFFc79e63),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(criatura.nombre)

                AsyncImage(
                    model = criatura.imagen,
                    contentDescription = criatura.nombre,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(150.dp),
                    contentScale = ContentScale.Fit
                )



                Text("Salud: ${criatura.salud}")
                Text("Daño: ${criatura.dano}")
                Text("Velocidad: ${criatura.velocidad}")
                Text("Poder: ${criatura.poder}")

                if (armaduraEquipada != null) {
                    Text("Armadura equipada: ${armaduraEquipada.nombre}")

                    Button(
                        onClick = { onDesequipar(armaduraEquipada) },
                        modifier = Modifier.padding(start = 1.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF342711),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Desequipar armadura")
                    }
                }

                Button(
                    onClick = onAbrirPopupRecursos,
                    modifier = Modifier.padding(start = 1.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF342711),
                        contentColor = Color.White
                    )
                ) {
                    Text("Agregar recurso")
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier.padding(start = 1.dp),
                    shape = RoundedCornerShape(6.dp),
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
