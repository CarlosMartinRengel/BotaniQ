package com.botaniq.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants_inventory")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // ID autogenerado
    val nickname: String, // Apodo personalizado
    val speciesName: String, // Nombre de la especie y FK
    val photoUri: String?, // Ruta de la fotografía local
    val baseWaterFreq: Int, // Frecuencia base teórica de riego
    val lastWateredDate: Long, // Timestamp del último riego
    val nextWateringDate: Long // Próxima fecha de riego recomendada
)
