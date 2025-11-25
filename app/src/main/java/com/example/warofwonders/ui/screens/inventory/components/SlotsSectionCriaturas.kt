package com.example.warofwonders.ui.screens.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.screens.inventory.getDrawableId


@Composable
fun SlotsSectionCriaturas(
    title: String,
    criaturas: List<Criatura>,
    columns: Int = 3,
    onClickCriatura: (Criatura) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {

        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val rows = (criaturas.size + columns - 1) / columns

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(rows) { rowIndex ->

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                    repeat(columns) { columnIndex ->
                        val index = rowIndex * columns + columnIndex

                        if (index < criaturas.size) {
                            val criatura = criaturas[index]


                            Card(
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF965E35)
                                ),
                                border = BorderStroke(2.dp, Color.DarkGray),
                                modifier = Modifier
                                    .size(110.dp, 80.dp)
                                    .clickable {
                                        onClickCriatura(criatura)
                                    }
                            ) {
                                AsyncImage(
                                    model = criatura.imagen,
                                    contentDescription = criatura.nombre,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )

                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF965E35)
                                ),
                                modifier = Modifier.size(110.dp, 80.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
