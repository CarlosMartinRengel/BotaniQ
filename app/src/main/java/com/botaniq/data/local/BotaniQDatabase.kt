package com.botaniq.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.botaniq.data.local.dao.PlantDao
import com.botaniq.data.local.dao.SpeciesInfoDao
import com.botaniq.data.local.dao.WeatherCacheDao
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.data.local.entities.SpeciesInfoEntity
import com.botaniq.data.local.entities.WeatherCacheEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PlantEntity::class, SpeciesInfoEntity::class, WeatherCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BotaniQDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun speciesInfoDao(): SpeciesInfoDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: BotaniQDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BotaniQDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BotaniQDatabase::class.java,
                    "botaniq_database"
                ).addCallback(BotaniqDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BotaniqDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val speciesDao = database.speciesInfoDao()
                    val plantDao = database.plantDao()

                    val initialSpecies = listOf(
                        SpeciesInfoEntity(
                            scientificName = "Maranta leuconeura",
                            commonName = "Planta de la oración",
                            category = "Interior",
                            defaultWateringDays = 7,
                            careTips = "Mantener humedad alta y luz indirecta."
                        )
                        //, TODO añadir especies que se vayan a utilizar
                    )
                    speciesDao.insertInitialSpecies(initialSpecies)

                    val testPlants = listOf(
                        PlantEntity(
                            nickname = "La del salón",
                            speciesName = "Maranta leuconeura",
                            photoUri = null,
                            baseWaterFreq = 7,
                            lastWateredDate = System.currentTimeMillis(),
                            nextWateringDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        ),
                        PlantEntity(
                            nickname = "La del otro lado",
                            speciesName = "Maranta leuconeura",
                            photoUri = null,
                            baseWaterFreq = 7,
                            lastWateredDate = System.currentTimeMillis(),
                            nextWateringDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        ),
                        PlantEntity(
                            nickname = "La del salón",
                            speciesName = "Maranta leuconeura",
                            photoUri = null,
                            baseWaterFreq = 7,
                            lastWateredDate = System.currentTimeMillis(),
                            nextWateringDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        ),
                        PlantEntity(
                            nickname = "La del baño",
                            speciesName = "Maranta leuconeura",
                            photoUri = null,
                            baseWaterFreq = 7,
                            lastWateredDate = System.currentTimeMillis(),
                            nextWateringDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        ),
                        PlantEntity(
                            nickname = "Juanito pepillo",
                            speciesName = "Maranta leuconeura",
                            photoUri = null,
                            baseWaterFreq = 7,
                            lastWateredDate = System.currentTimeMillis(),
                            nextWateringDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        ),
                        PlantEntity(
                            nickname = "La del otro lado",
                            speciesName = "Maranta leuconeura",
                            photoUri = null,
                            baseWaterFreq = 7,
                            lastWateredDate = System.currentTimeMillis(),
                            nextWateringDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        )
                    )
                    testPlants.forEach { plantDao.insertPlant(it) }
                }
            }
        }
    }
}

