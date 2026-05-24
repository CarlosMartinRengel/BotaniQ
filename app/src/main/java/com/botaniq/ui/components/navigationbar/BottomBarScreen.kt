package com.botaniq.ui.components.navigationbar

import com.botaniq.R
import com.botaniq.utils.UiText

sealed class BottomBarScreen(
    val route: String,
    val title: UiText,
    val icon: Int,
    val iconFocused: Int
) {
    object Inventory : BottomBarScreen(
        "inventory",
        UiText.StringResource(R.string.barnavigation_inventory),
        R.drawable.ic_bottom_plant,
        R.drawable.ic_bottom_plant_focused
    )

    object Add : BottomBarScreen(
        "add_plant",
        UiText.StringResource(R.string.barnavigation_add),
        R.drawable.ic_bottom_add,
        R.drawable.ic_bottom_add_focused
    )

    object Diagnostic : BottomBarScreen(
        "diagnostic",
        UiText.StringResource(R.string.barnavigation_scanner),
        R.drawable.ic_bottom_camera,
        R.drawable.ic_bottom_camera_focused
    )

}