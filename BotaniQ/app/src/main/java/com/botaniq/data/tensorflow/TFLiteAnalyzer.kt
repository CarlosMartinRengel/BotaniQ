package com.botaniq.data.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.botaniq.ml.DiagnosticResult
import com.botaniq.ml.RecognitionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class TFLiteAnalyzer(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var labels = listOf<String>()
    private var diseaseInterpreter: Interpreter? = null
    private var diseaseLabels = listOf<String>()

    init {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }

        try {
            val model = FileUtil.loadMappedFile(context, "model.tflite")
            interpreter = Interpreter(model, options)

            labels = context.assets.open("labels.txt")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
                .map { it.replace(Regex("^\\d+\\s+"), "") }
            Log.d("TFLiteAnalyzer", "¡Modelo de especies cargado con éxito!")
        } catch (e: Exception) {
            Log.e("TFLiteAnalyzer", "Error crítico: No se pudo iniciar el modelo de especies", e)
        }

        try {
            val diseaseModel = FileUtil.loadMappedFile(context, "model_disease.tflite")
            diseaseInterpreter = Interpreter(diseaseModel, options)

            diseaseLabels = context.assets.open("labels_disease.txt")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
                .map { it.replace(Regex("^\\d+\\s+"), "") }
            Log.d("TFLiteAnalyzer", "¡Modelo de enfermedades cargado con éxito!")
        } catch (e: Exception) {
            Log.e(
                "TFLiteAnalyzer",
                "Error crítico: No se pudo iniciar el modelo de enfermedades",
                e
            )
        }
    }

    suspend fun analyzeSpecies(bitmap: Bitmap): RecognitionResult =
        withContext(Dispatchers.Default) {
            if (interpreter == null || labels.isEmpty()) {
                return@withContext RecognitionResult("Error de inicialización", 0f)
            }

            try {
                // Forzar formato ARGB_8888
                val safeBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
                    bitmap.copy(Bitmap.Config.ARGB_8888, true)
                } else {
                    bitmap
                }

                // Comprueba si el modelo es de 8 o 32 bits
                val inputTensor = interpreter!!.getInputTensor(0)
                val outputTensor = interpreter!!.getOutputTensor(0)

                // Preprocesamiento de Entrada
                val imageProcessorBuilder = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))

                if (inputTensor.dataType() == DataType.FLOAT32) {
                    imageProcessorBuilder.add(NormalizeOp(0f, 1f))

                } else if (inputTensor.dataType() == DataType.UINT8) {
                    imageProcessorBuilder.add(CastOp(DataType.UINT8))
                }

                var tensorImage = TensorImage(inputTensor.dataType())
                tensorImage.load(safeBitmap)
                tensorImage = imageProcessorBuilder.build().process(tensorImage)

                val probabilityBuffer =
                    TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())
                interpreter?.run(tensorImage.buffer, probabilityBuffer.buffer)

                // Postprocesado en la salida (Solucion para que no salgan porcentajes del 25500%)
                val tensorProcessor = TensorProcessor.Builder().apply {
                    if (outputTensor.dataType() == DataType.UINT8) {
                        add(NormalizeOp(0f, 255f))
                    }
                }.build()

                val processedBuffer = tensorProcessor.process(probabilityBuffer)
                val results = TensorLabel(labels, processedBuffer).mapWithFloatValue
                val topResult = results.maxByOrNull { it.value }

                RecognitionResult(
                    speciesName = topResult?.key ?: "Desconocida",
                    confidence = topResult?.value ?: 0f
                )
            } catch (e: Exception) {
                Log.e("TFLiteAnalyzer", "Error interno de TensorFlow durante la inferencia", e)
                RecognitionResult("Error de inferencia", 0f)
            }
        }

    suspend fun analyzeHealth(bitmap: Bitmap): DiagnosticResult =
        withContext(Dispatchers.Default) {
            if (diseaseInterpreter == null || diseaseLabels.isEmpty()) {
                return@withContext DiagnosticResult(false, "Error de inicialización médica", 0f)
            }

            try {
                val safeBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
                    bitmap.copy(Bitmap.Config.ARGB_8888, true)
                } else bitmap

                val inputTensor = diseaseInterpreter!!.getInputTensor(0)
                val outputTensor = diseaseInterpreter!!.getOutputTensor(0)

                val imageProcessorBuilder = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))

                if (inputTensor.dataType() == DataType.FLOAT32) {
                    imageProcessorBuilder.add(NormalizeOp(0f, 1f))
                }

                var tensorImage = TensorImage(inputTensor.dataType())
                tensorImage.load(safeBitmap)
                tensorImage = imageProcessorBuilder.build().process(tensorImage)

                val probabilityBuffer =
                    TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())
                diseaseInterpreter?.run(tensorImage.buffer, probabilityBuffer.buffer)

                val tensorProcessor = TensorProcessor.Builder().apply {
                    if (outputTensor.dataType() == DataType.UINT8) {
                        add(NormalizeOp(0f, 255f))
                    }
                }.build()

                val processedBuffer = tensorProcessor.process(probabilityBuffer)
                val results = TensorLabel(diseaseLabels, processedBuffer).mapWithFloatValue

                val topResult = results.maxByOrNull { it.value }

                val etiqueta = topResult?.key ?: "Desconocida"
                val confianza = topResult?.value ?: 0f

                val esSana = etiqueta.contains("Sana", ignoreCase = true)

                DiagnosticResult(
                    isHealthy = esSana,
                    anomalyDescription = etiqueta,
                    confidence = confianza
                )
            } catch (e: Exception) {
                Log.e("TFLiteAnalyzer", "Error interno en inferencia médica", e)
                DiagnosticResult(false, "Error de inferencia", 0f)
            }
        }

    fun close() {
        interpreter?.close()
        interpreter = null
        diseaseInterpreter?.close()
        diseaseInterpreter = null
    }

}

