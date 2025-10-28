package com.example.warofwonders.ui.screens.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.screens.inventory.components.ProfileUser
import com.example.warofwonders.ui.screens.inventory.components.SlotsSection
import com.example.warofwonders.ui.screens.map.MapViewModel

@Composable
fun InventoryScreen(navController: NavHostController, viewModel: MapViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
            modifier = Modifier
                .fillMaxSize(),

            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileUser(navController = navController)
            SlotsSection(
                title = "CLIMA MEDIO",
                items = listOf(R.drawable.oso)
            )

            if (uiState.coldCreatureCaptured) {
                SlotsSection(
                    title = "CLIMA FRIO",
                    items = listOf(R.drawable.pinguino)
                )
            }

            if (uiState.hotCreatureCaptured) {
                SlotsSection(
                    title = "CLIMA CALIDO",
                    items = listOf(R.drawable.fenix)
                )
            }

            if (uiState.pressureCreatureCaptured) {
                SlotsSection(
                    title = "PRESION ALTA",
                    items = listOf(R.drawable.radam)
                )
            }

                SlotsSection(
                    title = "ARMADURAS",
                    items = listOf(R.drawable.aradura1, R.drawable.armadura2)
                )
        }
    }
}