package com.botaniq.ml

data class DiagnosticResult(
    val isHealthy: Boolean,
    val anomalyDescription: String,
    val confidence: Float
)

