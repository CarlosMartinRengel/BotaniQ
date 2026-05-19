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
                )
                    .addCallback(BotaniqDatabaseCallback(scope))
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
                            scientificName = "Alocasia spp",
                            commonName = "Oreja de Elefante",
                            category = "Interior",
                            defaultWateringDays = 5,
                            careTips = "Necesita humedad ambiental alta y luz indirecta brillante."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Aloe vera",
                            commonName = "Sábila",
                            category = "Suculenta",
                            defaultWateringDays = 15,
                            careTips = "Riego escaso y sustrato con gran drenaje. Evitar encharcamiento."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Begonia spp",
                            commonName = "Begonia",
                            category = "Interior",
                            defaultWateringDays = 4,
                            careTips = "Evitar mojar las hojas al regar para prevenir hongos. Prefiere semisombra."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Dionaea muscipula",
                            commonName = "Venus Atrapamoscas",
                            category = "Carnívora",
                            defaultWateringDays = 2,
                            careTips = "Usar solo agua destilada o de lluvia. Mantener el sustrato siempre húmedo."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Echeveria elegans",
                            commonName = "Rosa de Alabastro",
                            category = "Suculenta",
                            defaultWateringDays = 12,
                            careTips = "Resistente a la sequía. Necesita sol directo para mantener su forma compacta."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Ficus elastica",
                            commonName = "Árbol del Caucho",
                            category = "Interior",
                            defaultWateringDays = 7,
                            careTips = "Limpiar el polvo de las hojas con un paño húmedo. Sensible a corrientes."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Epipremnum aureum",
                            commonName = "Poto (Ivy Arum)",
                            category = "Interior",
                            defaultWateringDays = 6,
                            careTips = "Muy resistente. Tolera niveles bajos de luz, aunque prefiere luz indirecta."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Monstera deliciosa",
                            commonName = "Costilla de Adán",
                            category = "Interior",
                            defaultWateringDays = 7,
                            careTips = "Planta trepadora. Requiere tutores y riego cuando el sustrato esté seco."
                        ),
                        SpeciesInfoEntity(
                            scientificName = "Tulipa gesneriana",
                            commonName = "Tulipán",
                            category = "Exterior",
                            defaultWateringDays = 3,
                            careTips = "Requiere suelos frescos y mucha luz. Reducir riego tras la floración."
                        )
                    )
                    speciesDao.insertInitialSpecies(initialSpecies)

                    val testPlants = listOf(
                        PlantEntity(
                            nickname = "La del salón",
                            speciesName = "Monstera deliciosa",
                            photoUri = null,
                            baseWaterFreq = 7,
                            lastWateredDate = System.currentTimeMillis(),
                            nextWateringDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
                        ),
                        PlantEntity(
                            nickname = "Mi Aloe",
                            speciesName = "Aloe vera",
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

