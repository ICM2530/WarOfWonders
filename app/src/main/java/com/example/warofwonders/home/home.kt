package com.example.warofwonders.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.navigation.AppScreens

@Composable
fun HomeScreen(navController: NavController) {
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
                modifier = Modifier.size(width = 250.dp, height = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bracketname),
                    contentDescription = "nombre",
                    modifier = Modifier.fillMaxSize()
                        .clickable(
                            onClick = { navController.navigate(AppScreens.Profile.name) }
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                   Column { Text(
                       text = "User #11",
                       style = TextStyle(
                           fontSize = 20.sp,
                           color = Color.White
                       )
                   )
                       Text(
                           text = "teusaquillo amigos",
                           style = TextStyle(
                               fontSize = 10.sp,
                               color = Color.White
                           )
                       ) }


                    Box(
                        modifier = Modifier.size(100.dp),
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
                                color = Color.White
                            )
                        )
                    }
                }
            }



            Image(
                painter = painterResource(id = R.drawable.bell),
                contentDescription = "Notis",
                modifier = Modifier.size(40.dp)
            )

            Image(
               painter =  painterResource(id = R.drawable.configbutton),
                contentDescription = "Config",
                modifier = Modifier.size(50.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Settings.name) }
                    )
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
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Map.name) }
                    )
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
                modifier = Modifier.size(100.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Creatures.name) }
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.battlebutton),
                contentDescription = "Batalla",
                modifier = Modifier.size(120.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Combat.name) }
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.clanbutton),
                contentDescription = "Clan",
                modifier = Modifier.size(100.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Chat.name) }
                    )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val navcontroller= rememberNavController()
    HomeScreen(navcontroller)
}