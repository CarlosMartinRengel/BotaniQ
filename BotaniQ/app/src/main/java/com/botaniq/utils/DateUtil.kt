package com.botaniq.utils

import com.botaniq.R
import java.text.SimpleDateFormat
import java.util.Locale

// Función auxiliar para formateo de fechas a partir de los timestamps
fun formatDate(timestamp: Long): UiText {
    // Si el timestamp es 0 -> Planta acaba de ser registrada y aún no se ha regado
    if (timestamp == 0L) return UiText.StringResource(R.string.date_not_registered)

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return UiText.DynamicString(sdf.format(java.sql.Date(timestamp)))
}