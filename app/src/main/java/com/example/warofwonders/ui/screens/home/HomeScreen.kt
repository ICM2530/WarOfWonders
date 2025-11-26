package com.example.warofwonders.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.navigation.AppScreens
import com.example.warofwonders.ui.screens.home.components.HomeTopBar

@Composable
fun HomeScreen(navController: NavHostController,) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeTopBar(

                navController = navController
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.60f)
                    .aspectRatio(1f)
                    .clickable { navController.navigate(AppScreens.Map.name) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map_image),
                    contentDescription = "Mapa",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ImageButton(
                    imageRes = R.drawable.button_mouse,
                    contentDescription = "Criaturas / Inventario",
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.4f),
                    onClick = { navController.navigate(AppScreens.Inventory.name) }
                )

                ImageButton(
                    imageRes = R.drawable.button_battle,
                    contentDescription = "Batalla amistosa",
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.4f),
                    onClick = { navController.navigate(AppScreens.FriendlyBattle.name) }
                )

                ImageButton(
                    imageRes = R.drawable.button_clan,
                    contentDescription = "Clan / Chat",
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.4f),
                    onClick = { navController.navigate(AppScreens.Chat.name) }
                )
            }

        }
    }
}