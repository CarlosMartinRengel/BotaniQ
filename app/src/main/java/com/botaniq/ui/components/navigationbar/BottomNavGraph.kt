package com.botaniq.ui.components.navigationbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.botaniq.di.AppModule
import com.botaniq.ui.plantdetail.PlantFormViewModel
import com.botaniq.ui.plantdetail.PlantFormViewModelFactory
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
                }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            val speciesName = backStackEntry.arguments?.getString("speciesName")
            val photoUri = backStackEntry.arguments?.getString("photoUri")

            val context = LocalContext.current.applicationContext
            val scope = rememberCoroutineScope()

            val db = AppModule.provideDatabase(context, scope)

            val viewModel: PlantFormViewModel = viewModel(
                factory = PlantFormViewModelFactory(
                    AppModule.providePlantRepository(
                        context = context,
                        plantDao = AppModule.providePlantDao(db),
                        speciesInfoDao = AppModule.provideSpeciesInfoDao(db),
                        weatherCacheDao = AppModule.provideWeatherCacheDao(db),
                        weatherApi = AppModule.provideWeatherApi()
                    )
                )
            )

            PlantScreen(
                plantId = plantId,
                speciesName = speciesName,
                photoUri = photoUri,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCamera = {
                    navController.navigate("${BottomBarScreen.Diagnostic.route}?isFromForm=true")
                },
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable(
            route = "${BottomBarScreen.Diagnostic.route}?isFromForm={isFromForm}",
            arguments = listOf(
                navArgument("isFromForm") {
                    type = NavType.BoolType
                    defaultValue =
                        false
                }
            )
        ) { backStackEntry ->
            val isFromForm = backStackEntry.arguments?.getBoolean("isFromForm") ?: false

            CameraScreen(
                isFromForm = isFromForm, // Pasamos el contexto a la pantalla
                onPhotoConfirmedForForm = { uri ->
                    // Se guarda la foto si la pantalla previa es la de formulario
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("returnedPhotoUri", uri.toString())
                    navController.popBackStack()
                },
                onAnalyzeWithAI = { uri, mode ->
                    // Desde el menú, entrada a IA
                    // TODO: Integrar aquí la lógica de TensorFlow Lite
                }
            )
        }
    }
}