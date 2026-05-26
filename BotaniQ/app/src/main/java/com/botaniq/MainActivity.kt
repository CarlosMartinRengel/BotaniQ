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
                MainScreen()
            }
        }
    }
}
