package com.example.warofwonders.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.battle.BattleScreen
import com.example.warofwonders.chat.ChatScreen
import com.example.warofwonders.clan.ClanScreen
import com.example.warofwonders.combat.CombatScreen
import com.example.warofwonders.creatures.CreaturesScreen
import com.example.warofwonders.home.HomeScreen
import com.example.warofwonders.items.ItemsScreen
import com.example.warofwonders.login.LogInScreen
import com.example.warofwonders.map.MapScreen
import com.example.warofwonders.profile.ProfileScreen
import com.example.warofwonders.signup.SignUpScreen
import com.example.warofwonders.settings.SettingsScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.Home.name) {
        composable(route = AppScreens.Home.name) {
            HomeScreen(navController)
        }
        composable(route = AppScreens.LogIn.name) {
            LogInScreen(navController)
        }
        composable(route = AppScreens.SignUp.name) {
            SignUpScreen(navController)
        }
        composable(route = AppScreens.Battle.name) {
            BattleScreen(navController)
        }
        composable(route = AppScreens.Map.name) {
            MapScreen(navController)
        }
        /*composable(route = AppScreens.Clan.name) {
            ClanScreen(navController)
        }
        composable(route = AppScreens.Chat.name) {
            ChatScreen(navController)
        }*/
        composable(route = AppScreens.Profile.name) {
            ProfileScreen(navController)
        }
        composable(route = AppScreens.Combat.name) {
            CombatScreen(navController)
        }
        composable(route = AppScreens.Creatures.name) {
            CreaturesScreen(navController)
        }
        composable(route = AppScreens.Items.name) {
            ItemsScreen(navController)
        }
        composable(route = AppScreens.Settings.name) {
            SettingsScreen(navController)
        }
    }
}