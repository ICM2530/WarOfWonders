package com.example.warofwonders.ui.screens.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.ui.theme.WarOfWondersTheme

@Composable
fun SlotsSection(
    title: String,
    items: List<Int>
) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) { index ->
                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF965E35) // Color sólido tipo madera
                    ),
                    border = BorderStroke(2.dp, Color.DarkGray), // Borde blanco de 2dp
                    modifier = Modifier
                        .size(width = 110.dp, height = 80.dp)
                ) {
                    if (index < items.size) {
                        Image(
                            painter = painterResource(items[index]),
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp).fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SlotsSectionPreview() {
    WarOfWondersTheme {
        SlotsSection(
            title = "Clima frío",
            items = emptyList()
        )
    }
}