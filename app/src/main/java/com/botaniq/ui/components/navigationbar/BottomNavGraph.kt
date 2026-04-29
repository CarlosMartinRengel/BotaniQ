package com.botaniq.ui.components.navigationbar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.botaniq.ui.screens.CameraScreen
import com.botaniq.ui.screens.InventoryScreen
import com.botaniq.ui.screens.PlantScreen

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
            InventoryScreen(
                onPlantClick = { id ->
                    navController.navigate("plant_screen?plantId=$id")
                }
            )
        }
        composable(
            route = "plant_screen?plantId={plantId}&speciesName={speciesName}&photoUri={photoUri}",
            arguments = listOf(
                navArgument("plantId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("speciesName") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument("photoUri") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            val speciesName = backStackEntry.arguments?.getString("speciesName")
            val photoUri = backStackEntry.arguments?.getString("photoUri")

            PlantScreen(
                plantId = plantId,
                speciesName = speciesName,
                photoUri = photoUri,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = BottomBarScreen.Diagnostic.route)
        {
            CameraScreen()
        }
    }
}