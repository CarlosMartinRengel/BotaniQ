package com.botaniq.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.botaniq.R

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current

    // El permiso en tiempo de ejecución solo es necesario en Android 13 (API 33) o superior
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        val deniedMessage = stringResource(R.string.notification_permission_denied)

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (!isGranted) {
                    Toast.makeText(
                        context,
                        deniedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        LaunchedEffect(Unit) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                // Si no se tiene el permiso, aparece el cuadro de diálogo del sistema
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}