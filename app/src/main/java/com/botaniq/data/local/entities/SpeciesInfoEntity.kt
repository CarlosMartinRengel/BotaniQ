package com.botaniq.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "species_info")
data class SpeciesInfoEntity(
    @PrimaryKey
    val scientificName: String, // Nombre científico y PK
    val commonName: String, // Nombre común
    val category: String, // Categoría (interior, exterior...)
    val defaultWateringDays: Int, // Frecuencia de riego por defecto
    val careTips: String // Consejos para el cuidado
)
