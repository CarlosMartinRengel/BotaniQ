package com.botaniq.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {

    suspend fun uriToOptimizedBitmap(context: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                // Para Android 9 (API 28) o superior
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        // 1. Reducir el tamaño de muestreo (Target 800px max)
                        val targetSize = 800
                        val sampleSize = Integer.highestOneBit(
                            info.size.width.coerceAtLeast(info.size.height) / targetSize
                        ).coerceAtLeast(1)

                        decoder.setTargetSampleSize(sampleSize)

                        // 2. Evitar el Bitmap de Hardware
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    // Fallback seguro para versiones anteriores a Android 9
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                    // Leemos solo las dimensiones primero
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }

                    options.inSampleSize = calculateInSampleSize(options, 800, 800)
                    options.inJustDecodeBounds = false
                    options.inPreferredConfig =
                        Bitmap.Config.ARGB_8888 // Formato compatible con TFLite

                    // Se carga la imagen
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    // Calculadora para la reducción de imágenes en APIs antiguas
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}