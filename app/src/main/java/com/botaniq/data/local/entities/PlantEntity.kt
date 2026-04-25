package com.botaniq.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plants_inventory",
    indices = [Index(value = ["speciesName"])],
    foreignKeys = [
        ForeignKey(
            entity = SpeciesInfoEntity::class,
            parentColumns = ["scientificName"],
            childColumns = ["speciesName"],
            onDelete = ForeignKey.CASCADE
        )]
)
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
