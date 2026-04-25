package com.botaniq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.botaniq.data.local.entities.SpeciesInfoEntity

@Dao
interface SpeciesInfoDao {
    @Query("SELECT * FROM species_info WHERE scientificName = :name")
    suspend fun getSpeciesByName(name: String): SpeciesInfoEntity?

    @Query("SELECT * FROM species_info")
    suspend fun getAllSpecies(): List<SpeciesInfoEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialSpecies(species: List<SpeciesInfoEntity>)
}