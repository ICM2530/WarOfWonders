package com.example.warofwonders.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.data.model.InterestPointData
import com.example.warofwonders.ui.theme.Brown

@Composable
fun PoiBottomCard(
    poi: InterestPointData,
    onVisitClick: () -> Unit,
    onAddClick: () -> Unit,
    canVisit: Boolean,
    isLocal: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE49C6C)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = poi.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Brown
                )

                AssistChip(
                    onClick = {},
                    label = { Text(text = poi.iconography, color = Color.White) },
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Brown
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = poi.address,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = Brown
                    )
                    Spacer(Modifier.width(4.dp))
                    poi.admin?.let {
                        Text(
                            text = it,
                            color = Color.White
                        )
                    }
                }

                poi.phone?.let { phone ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Brown
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = phone,
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (canVisit && !isLocal) {
                        OutlinedButton(onClick = onVisitClick, border = BorderStroke(2.dp, Brown)) {
                            Text(text = "Visitar", color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                    }

                    Spacer(Modifier.width(10.dp))

                    if (!isLocal) {
                        Button(
                            onClick = onAddClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.width(120.dp).padding(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(text = "Guardar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewPoiBottomCard() {
    val fakePoi = InterestPointData(
        id = 1,
        name = "Café Central",
        type = "Restaurant",
        iconography = "",
        address = "Calle 123 #45-67",
        locality = "Bogotá",
        admin = "Administrador Ejemplo",
        phone = "3001234567",
        lat = 4.60971,
        lng = -74.08175,
        icon = null
    )

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PoiBottomCard(
                poi = fakePoi,
                onVisitClick = { },
                onAddClick = { },
                canVisit = true,
                isLocal = false,
                modifier = Modifier
                    .width(340.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
    }
}
