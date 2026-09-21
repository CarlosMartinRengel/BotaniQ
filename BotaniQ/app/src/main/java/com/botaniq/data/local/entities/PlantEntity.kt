package com.botaniq.data.local.entities

import androidx.compose.runtime.Immutable
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
@Immutable
data class PlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nickname: String,
    val speciesName: String,
    val photoUri: String?,
    val baseWaterFreq: Int,
    val lastWateredDate: Long,
    val nextWateringDate: Long
)
