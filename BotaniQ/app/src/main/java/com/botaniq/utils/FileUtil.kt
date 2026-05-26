package com.botaniq.utils

import android.content.Context
import android.net.Uri
import java.io.File

object FileUtil {
    fun saveImageToInternalStorage(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        // Nombre para que no se sobreescriba con fotos de otras plantas
        val fileName = "plant_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        // Esta ruta va a la bbdd
        return file.absolutePath
    }
}