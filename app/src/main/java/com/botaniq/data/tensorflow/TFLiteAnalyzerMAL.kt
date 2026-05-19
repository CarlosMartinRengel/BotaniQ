package com.botaniq.data.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.botaniq.ml.RecognitionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class TFLiteAnalyzerMAL(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var labels = listOf<String>()

    init {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        try {
            // Carga del modelo desde assets
            val model = FileUtil.loadMappedFile(context, "")
            interpreter = Interpreter(model, options)
            Log.d("TFLiteAnalyzer", "¡Modelo TFLite cargado con éxito!")
        } catch (e: Exception) {
            Log.e("TFLiteAnalyzer", "CRASH: No se encuentra 'model.tflite'", e)
        }
        // Carga y limpieza de etiquetas de Teachable Machine
        try {
            labels = context.assets.open("labels.txt")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
                .map { it.replace(Regex("^\\d+\\s+"), "") }
            Log.d("TFLiteAnalyzer", "¡Labels cargados con éxito!")
        } catch (e: Exception) {
            Log.e("TFLiteAnalyzer", "CRASH: No se encuentra 'labels.txt'", e)
        }
    }

    suspend fun analyzeSpecies(bitmap: Bitmap): RecognitionResult =
        withContext(Dispatchers.Default) {
            if (interpreter == null || labels.isEmpty()) {
                return@withContext RecognitionResult("Error de inicialización", 0f)
            }

            try {
                // 1. SOLUCIÓN AL CRASH DE MEMORIA: Forzar formato ARGB_8888
                val safeBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
                    bitmap.copy(Bitmap.Config.ARGB_8888, true)
                } else {
                    bitmap
                }

                // 2. LECTURA DINÁMICA DEL MODELO: Evita fallos de Buffer
                val inputTensor = interpreter!!.getInputTensor(0)
                val outputTensor = interpreter!!.getOutputTensor(0)

                // 3. SOLUCIÓN MATEMÁTICA: Preprocesamiento exacto para Teachable Machine
                val imageProcessorBuilder = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))

                // Si el modelo es Float32, Teachable Machine requiere (pixel - 127.5) / 127.5
                if (inputTensor.dataType() == DataType.FLOAT32) {
                    imageProcessorBuilder.add(NormalizeOp(127.5f, 127.5f))
                }

                var tensorImage = TensorImage(inputTensor.dataType())
                tensorImage.load(safeBitmap)
                tensorImage = imageProcessorBuilder.build().process(tensorImage)

                // 4. EJECUCIÓN
                // Usamos la forma exacta del outputTensor para evitar desbordamientos
                val probabilityBuffer =
                    TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())
                interpreter?.run(tensorImage.buffer, probabilityBuffer.buffer)

                // 5. OBTENER RESULTADO
                val results = TensorLabel(labels, probabilityBuffer).mapWithFloatValue
                val topResult = results.maxByOrNull { it.value }

                RecognitionResult(
                    speciesName = topResult?.key ?: "Desconocida",
                    confidence = topResult?.value ?: 0f
                )
            } catch (e: Exception) {
                // Si vuelve a fallar, revisa la pestaña "Logcat" en Android Studio para ver el error exacto
                Log.e("TFLiteAnalyzer", "Error interno de TensorFlow durante la inferencia", e)
                RecognitionResult("Error de inferencia", 0f)
            }
        }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}