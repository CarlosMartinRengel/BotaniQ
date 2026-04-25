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
                }
            }
        }
    }
}

