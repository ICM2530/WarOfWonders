package com.example.warofwonders.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.navigation.AppScreens

@Composable
fun HomeTopBar(
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(250.dp).height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bracket_name),
                    contentDescription = "Usuario",
                    modifier = Modifier.fillMaxSize()
                        .clickable { navController.navigate(AppScreens.Profile.name) },
                    contentScale = ContentScale.FillBounds
                )

                Row(
                    modifier = Modifier.width(240.dp).fillMaxHeight(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "World Recoverer",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Teusaquillo amigos",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            Image(
                painter = painterResource(id = R.drawable.bell_icon),
                contentDescription = "Notification",
                modifier = Modifier.size(48.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Contacts.name) }
                    )
            )

            Image(
                painter =  painterResource(id = R.drawable.configbutton),
                contentDescription = "Config",
                modifier = Modifier.size(48.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Settings.name) }
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1F),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bracket_coin),
                    contentDescription = "Coins",
                    modifier = Modifier.width(120.dp).height(48.dp)
                )

                Text(
                    text = "000",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
            }

            Image(
                painter = painterResource(id = R.drawable.shop),
                contentDescription = "tienda",
                modifier = Modifier.size(48.dp)
                    .clickable(
                        onClick = { navController.navigate(AppScreens.Camera.name) }
                    )
            )
        }
    }
}