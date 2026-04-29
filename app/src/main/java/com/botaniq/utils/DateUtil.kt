package com.botaniq.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Función auxiliar para formateo de fechas a partir de los timestamps
fun formatDate(timestamp: Long): String {
    // Si el timestamp es 0 -> Planta acaba de ser registrada y aún no se ha regado
    if (timestamp == 0L) return "Aún no registrado"

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}