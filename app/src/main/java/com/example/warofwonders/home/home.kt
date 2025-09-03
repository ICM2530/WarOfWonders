package com.example.warofwonders.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.R

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(width = 120.dp, height = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bracketname),
                    contentDescription = "nombre",
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "User #11",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                )
            }

            Box(
                modifier = Modifier.size(width = 80.dp, height = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coinbracket),
                    contentDescription = "Coins",
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "000",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                )
            }

            Image(
                painter = painterResource(id = R.drawable.bell),
                contentDescription = "Notis",
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.map),
                contentDescription = "Mapa",
                modifier = Modifier.size(220.dp)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.mousebutton),
                contentDescription = "criaturas",
                modifier = Modifier.size(64.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.battlebutton),
                contentDescription = "Batalla",
                modifier = Modifier.size(64.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.clanbutton),
                contentDescription = "Clan",
                modifier = Modifier.size(64.dp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.shop),
            contentDescription = "tienda",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .size(48.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}