package com.botaniq.ui.components.navigationbar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.botaniq.ui.screens.AddScreen
import com.botaniq.ui.screens.CameraScreen
import com.botaniq.ui.screens.InventoryScreen

@Composable
fun BottomNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Inventory.route
    ) {
        composable(route = BottomBarScreen.Inventory.route)
        {
            InventoryScreen()
        }
        composable(route = BottomBarScreen.Add.route)
        {
            AddScreen()
        }
        composable(route = BottomBarScreen.Diagnostic.route)
        {
            CameraScreen()
        }
    }
}