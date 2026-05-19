package com.botaniq.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// INstanciar datastore como singleton
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferenceRepository(private val context: Context) {
    companion object {
        private val SELECTED_CITY_KEY = stringPreferencesKey("selected_city")
    }

    val selectedCityFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_CITY_KEY] ?: "Salamanca"
        }

    suspend fun saveSelectedCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CITY_KEY] = city
        }
    }
}