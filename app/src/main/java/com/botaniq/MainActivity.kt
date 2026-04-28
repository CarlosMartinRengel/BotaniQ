package com.botaniq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.botaniq.ui.screens.MainScreen
import com.botaniq.ui.theme.BotaniQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BotaniQTheme {
//                val navController = rememberNavController()
//
//                Scaffold(
//                    modifier = Modifier.fillMaxSize(),
//                    bottomBar = {
//                        BotaniqBottomNavigation(navController)
//                    }
//                ) { innerPadding ->
//                    NavHost(
//                        navController = navController,
//                        startDestination = "inventory", // Empezamos en el inventario [cite: 409]
//                        modifier = Modifier.padding(innerPadding)
//                    ) {
//                        composable("inventory") { /*InventoryScreen()*/ }
//                        composable("add_plant") { /*AddPlantScreen()*/ }
//                        composable("camera") { /*CameraScreen()*/ }
//                    }
//                }

                MainScreen()
            }
        }
    }
}

//@Composable
//fun BotaniqBottomNavigation(navController: NavController) {
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    // Llamamos a la UI pura
//    BotaniqBottomNavigationContent(
//        currentRoute = currentRoute,
//        onItemClick = { route ->
//            navController.navigate(route) {
//                // Esto evita que se acumulen pantallas en el historial
//                popUpTo(navController.graph.startDestinationId) { saveState = true }
//                launchSingleTop = true
//                restoreState = true
//            }
//        }
//    )
//}
//
//@Composable
//fun BotaniqBottomNavigationContent(
//    currentRoute: String?,
//    onItemClick: (String) -> Unit
//) {
//    NavigationBar(
//        containerColor = Color(0xFF4E342E),
//        contentColor = Color.White
//    ) {
//        val items = listOf(
//            "inventory" to ("Inventario" to R.drawable.ic_launcher_foreground),
//            "add_plant" to ("Añadir" to R.drawable.ic_launcher_foreground),
//            "camera" to ("Cámara" to R.drawable.ic_launcher_foreground)
//        )
//
//        items.forEach { (route, data) ->
//            val (label, icon) = data
//            val isSelected = currentRoute == route
//
//            val animatedOffset by animateDpAsState(
//                targetValue = if (isSelected) (-8).dp else 0.dp,
//                label = "iconOffset"
//            )
//
//            NavigationBarItem(
//                selected = isSelected,
//                onClick = { onItemClick(route) },
//                icon = {
//                    Icon(
//                        painter = painterResource(id = icon),
//                        contentDescription = label,
//                        modifier = Modifier.offset(y = animatedOffset)
//                    )
//                },
//                label = {
//                    Text(
//                        text = label,
//                        color = Color.White
//                    )
//                },
//                colors = NavigationBarItemDefaults.colors(
//                    indicatorColor = Color.White,
//                    selectedIconColor = GreenSelectedIcon,
//                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
//                    selectedTextColor = Color.White,
//                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
//                )
//            )
//        }
//
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun BotaniqBottomNavigationPreview() {
//    BotaniQTheme {
//        BotaniqBottomNavigationContent(
//            currentRoute = "inventory",
//            onItemClick = {} // No hace nada en el preview
//        )
//    }
//}

//@Composable
//fun BotaniqBottomNavigation(navController: NavController) {
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    NavigationBar(
//        containerColor = Color(0xFF4E342E), // El color marrón de tu prototipo
//        contentColor = Color.White
//    ) {
//        // Opción Inventario
//        NavigationBarItem(
//            selected = currentRoute == "inventory",
//            onClick = { navController.navigate("inventory") },
//            icon = { Icon(painterResource(id = R.drawable.ic_launcher_foreground), "Inventario") }
//        )
//        // Opción Añadir Planta
//        NavigationBarItem(
//            selected = currentRoute == "add_plant",
//            onClick = { navController.navigate("add_plant") },
//            icon = { Icon(painterResource(id = R.drawable.ic_launcher_foreground), "Añadir") }
//        )
//        // Opción Diagnóstico
//        NavigationBarItem(
//            selected = currentRoute == "camera",
//            onClick = { navController.navigate("camera") },
//            icon = { Icon(painterResource(id = R.drawable.ic_launcher_foreground), "Cámara") }
//        )
//    }
//}