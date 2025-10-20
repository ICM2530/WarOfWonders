package com.example.warofwonders.ui.screens.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.ui.screens.inventory.components.ProfileUser
import com.example.warofwonders.ui.screens.inventory.components.SlotsSection
import com.example.warofwonders.ui.theme.WarOfWondersTheme

@Composable
fun InventoryScreen(
    navController: NavController
) {
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
            ProfileUser()

            SlotsSection(title = "CLIMA MEDIO", items = listOf(R.drawable.oso))

            SlotsSection(title = "CLIMA FRIO", items = listOf(R.drawable.pinguino))

            SlotsSection(title = "CLIMA CALIDO", items = listOf(R.drawable.rino, R.drawable.fenix))

            SlotsSection(title = "ARMADURAS", items = listOf(R.drawable.aradura1, R.drawable.armadura2))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    WarOfWondersTheme {
        InventoryScreen(navController = rememberNavController())
    }
}