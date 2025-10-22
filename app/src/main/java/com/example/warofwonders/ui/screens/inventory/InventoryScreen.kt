package com.example.warofwonders.ui.screens.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.screens.inventory.components.ProfileUser
import com.example.warofwonders.ui.screens.inventory.components.SlotsSection

@Composable
fun InventoryScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background_inventory),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileUser(navController = navController)

            SlotsSection(title = "CLIMA MEDIO", items = listOf(R.drawable.oso))

            SlotsSection(title = "CLIMA FRIO", items = listOf(R.drawable.pinguino))

            SlotsSection(title = "CLIMA CALIDO", items = listOf(R.drawable.rino, R.drawable.fenix))

            SlotsSection(title = "ARMADURAS", items = listOf(R.drawable.aradura1, R.drawable.armadura2))
        }
    }
}