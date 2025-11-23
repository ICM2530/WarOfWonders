package com.example.warofwonders.ui.screens.inventory.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.screens.inventory.getDrawableId

@Composable
fun PopupDeCriatura(
    criatura: Criatura,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        androidx.compose.material3.Surface(
            color = Color(0xFFc79e63),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(text = criatura.nombre, color = Color.Black)

                Image(
                    painter = painterResource(getDrawableId(criatura.imagen)),
                    contentDescription = criatura.nombre,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(120.dp)
                )

                Text("Salud: ${criatura.salud}")
                Text("Daño: ${criatura.dano}")
                Text("Velocidad: ${criatura.velocidad}")
                Text("Poder: ${criatura.poder}")

                androidx.compose.material3.Button(
                    onClick = onClose,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
