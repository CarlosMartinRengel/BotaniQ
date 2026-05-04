package com.botaniq.ui.components.navigationbar

import com.botaniq.R

sealed class BottomBarScreen(
    val route: String,
    val title: Int,
    val icon: Int,
    val icon_focused: Int
) {
    object Inventory : BottomBarScreen(
        "inventory",
        R.string.barnavigation_inventory,
        R.drawable.ic_bottom_plant,
        R.drawable.ic_bottom_plant_focused
    )

    object Add : BottomBarScreen(
        "plant_screen?plantId=0",
        R.string.barnavigation_add,
        R.drawable.ic_bottom_add,
        R.drawable.ic_bottom_add_focused
    )

    object Diagnostic : BottomBarScreen(
        "diagnostic",
        R.string.barnavigation_diagnostic,
        R.drawable.ic_bottom_camera,
        R.drawable.ic_bottom_camera_focused
    )

}