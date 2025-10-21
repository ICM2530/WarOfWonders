package com.example.warofwonders.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.ui.screens.camera.CameraScreen
import com.example.warofwonders.ui.screens.chat.ChatScreen
import com.example.warofwonders.ui.screens.clan.ClanScreen
import com.example.warofwonders.ui.screens.combat.CombatScreen
import com.example.warofwonders.ui.screens.contacts.ContactsScreen
import com.example.warofwonders.ui.screens.home.HomeScreen
import com.example.warofwonders.ui.screens.inventory.InventoryScreen
import com.example.warofwonders.ui.screens.login.LogInScreen
import com.example.warofwonders.ui.screens.signup.SignUpScreen
import com.example.warofwonders.ui.screens.startup.StartUpScreen
import com.example.warofwonders.ui.screens.map.MapScreen
import com.example.warofwonders.ui.screens.map.MapViewModel
import com.example.warofwonders.ui.screens.profile.ProfileScreen
import com.example.warofwonders.ui.screens.settings.SettingsScreen
import com.example.warofwonders.ui.shared.GenericViewModelFactory

@Composable
fun NavGraph(
    locationRepository: LocationRepository
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreens.StartUp.name) {
        composable(route = AppScreens.StartUp.name) {
            StartUpScreen(navController = navController)
        }

        composable(route = AppScreens.LogIn.name) {
            LogInScreen(navController = navController)
        }

        composable(route = AppScreens.SignUp.name) {
            SignUpScreen(navController = navController)
        }

        composable(route = AppScreens.Home.name) {
            HomeScreen(navController = navController)
        }

        composable(route = AppScreens.Inventory.name) {
            InventoryScreen()
        }

        composable(route = AppScreens.Map.name) {
            val mapViewModel: MapViewModel = viewModel(
                factory = GenericViewModelFactory {
                    MapViewModel(locationRepository)
                }
            )
            MapScreen(navController = navController, viewModel = mapViewModel)
        }

        composable(route = AppScreens.Clan.name) {
            ClanScreen()
        }

        composable(route = AppScreens.Chat.name) {
            ChatScreen(navController = navController)
        }

        composable(route = AppScreens.Profile.name) {
            ProfileScreen(navController = navController)
        }

        composable(route = AppScreens.Settings.name) {
            SettingsScreen(navController = navController)
        }

        composable(route = AppScreens.Contacts.name) {
            ContactsScreen()
        }

        composable(route = AppScreens.Combat.name) {
            CombatScreen()
        }

        composable(route = AppScreens.Camera.name) {
            CameraScreen()
        }
    }
}