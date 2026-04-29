package com.botaniq.ui.components.navigationbar

import com.botaniq.R

sealed class BottomBarScreen(

    val route: String,
    val title: Int,
    val icon: Int,
    val icon_focused: Int
) {
    // Inventario
    object Inventory : BottomBarScreen(
        route = "inventory",
        title = R.string.barnavigation_inventory,
        icon = R.drawable.ic_bottom_plant,
        icon_focused = R.drawable.ic_bottom_plant_focused
    )

    // Añadir/Registrar
    object Add : BottomBarScreen(
        route = "add",
        title = R.string.barnavigation_add,
        icon = R.drawable.ic_bottom_add,
        icon_focused = R.drawable.ic_bottom_add_focused
    )

    // Diagnostico
    object Diagnostic : BottomBarScreen(
        route = "diagnostic",
        title = R.string.barnavigation_diagnostic,
        icon = R.drawable.ic_bottom_camera,
        icon_focused = R.drawable.ic_bottom_camera_focused
    )

}