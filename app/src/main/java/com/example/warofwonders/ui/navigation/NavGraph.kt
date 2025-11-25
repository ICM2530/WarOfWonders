package com.example.warofwonders.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.taller2.data.repository.GeoCoderRepository
import com.android.taller2.data.repository.RouteRepository
import com.example.warofwonders.data.repository.InterestPointRepository
import com.example.warofwonders.data.repository.LocationRepository
import com.example.warofwonders.data.source.hardware.BarometerSensorDataSource
import com.example.warofwonders.data.source.hardware.LightSensorDataSource
import com.example.warofwonders.data.source.hardware.TemperatureSensorDataSource
import com.example.warofwonders.data.source.hardware.StepDetectorDataSource
import com.example.warofwonders.data.source.hardware.MagnetometerDataSource
import com.example.warofwonders.ui.model.InventarioViewModel
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.MyUserViewModel
import com.example.warofwonders.ui.model.ShopViewModel
import com.example.warofwonders.ui.screens.camera.CameraScreen
import com.example.warofwonders.ui.screens.chat.ChatScreen
import com.example.warofwonders.ui.screens.clan.ClanScreen
import com.example.warofwonders.ui.screens.combat.CombatScreen
import com.example.warofwonders.ui.screens.contacts.ContactsScreen
import com.example.warofwonders.ui.screens.gallery.GalleryScreen
import com.example.warofwonders.ui.screens.home.HomeScreen
import com.example.warofwonders.ui.screens.inventory.InventoryScreen
import com.example.warofwonders.ui.screens.login.LogInScreen
import com.example.warofwonders.ui.screens.signup.SignUpScreen
import com.example.warofwonders.ui.screens.startup.StartUpScreen
import com.example.warofwonders.ui.screens.map.MapScreen
import com.example.warofwonders.ui.screens.map.MapViewModel
import com.example.warofwonders.ui.screens.settings.SettingsScreen
import com.example.warofwonders.ui.screens.shop.ShopScreen

import com.example.warofwonders.ui.shared.GenericViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    locationRepository: LocationRepository,
    geoCoderRepository: GeoCoderRepository,
    routeRepository: RouteRepository,
    lightSensorDataSource: LightSensorDataSource,
    barometerSensorDataSource: BarometerSensorDataSource,
    temperatureSensorDataSource: TemperatureSensorDataSource,
    stepDetectorDataSource: StepDetectorDataSource,
    magnetometerDataSource: MagnetometerDataSource,
    interestPointRepository: InterestPointRepository,
    startDestination: String
) {
    val inventarioVM: InventarioViewModel = viewModel()

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
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
            val mapViewModel: MapViewModel = viewModel(
                factory = GenericViewModelFactory {
                    MapViewModel(
                        locationRepository = locationRepository,
                        geoCoderRepository = geoCoderRepository,
                        routeRepository = routeRepository,
                        lightSensorDataSource = lightSensorDataSource,
                        barometerSensorDataSource = barometerSensorDataSource,
                        temperatureSensorDataSource = temperatureSensorDataSource,
                        magnetometerDataSource = magnetometerDataSource,
                        interestPointRepository = interestPointRepository,
                        inventarioVM = InventarioViewModel()
                    )
                }
            )
            InventoryScreen(navController = navController)
        }

        composable(route = AppScreens.Map.name) {
            val mapViewModel: MapViewModel = viewModel(
                factory = GenericViewModelFactory {
                    MapViewModel(
                        locationRepository = locationRepository,
                        geoCoderRepository = geoCoderRepository,
                        routeRepository = routeRepository,
                        lightSensorDataSource = lightSensorDataSource,
                        barometerSensorDataSource = barometerSensorDataSource,
                        temperatureSensorDataSource = temperatureSensorDataSource,
                        magnetometerDataSource = magnetometerDataSource,
                        interestPointRepository = interestPointRepository,
                        inventarioVM = InventarioViewModel()
                    )
                }
            )
            MapScreen(
                navController = navController,
                viewModel = mapViewModel
            )
        }

        composable(route = AppScreens.Clan.name) {
            ClanScreen()
        }

        composable(route = AppScreens.Chat.name) {
            ChatScreen(navController = navController)
        }

        composable(route = AppScreens.Settings.name) {
            SettingsScreen(navController = navController)
        }

        composable(route = AppScreens.Contacts.name) {
            ContactsScreen()
        }

        composable(route = AppScreens.Combat.name) {
            // ruta sin argumentos
            CombatScreen(navController = navController, attackerId = null, defenderId = null)
        }

        composable(route = AppScreens.Combat.name + "/{attackerId}/{defenderId}") { backStackEntry ->
            val attackerId = backStackEntry.arguments?.getString("attackerId")
            val defenderId = backStackEntry.arguments?.getString("defenderId")
            CombatScreen(navController = navController, attackerId = attackerId, defenderId = defenderId)
        }

        composable(route = AppScreens.Camera.name) {
            CameraScreen(navController = navController)
        }

        composable(route = AppScreens.Gallery.name) {
            GalleryScreen(navController = navController)
        }

        composable(route = AppScreens.Shop.name) {

            val userVM: MyUserViewModel = viewModel()
            val currentUser by userVM.currentUser.collectAsState()

            LaunchedEffect(Unit) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    userVM.loadUser(uid)
                }
            }

            ShopScreen(
                navController = navController,
                userState = currentUser ?: MyUserState(),
                inventarioVM = inventarioVM
            )
        }
    }
}