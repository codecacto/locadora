package br.com.codecacto.locadora.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.codecacto.locadora.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepositoryImpl(
    private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val HORARIO_NOTIFICACAO = stringPreferencesKey("horario_notificacao")
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return context.userPreferencesDataStore.data.map { preferences ->
            UserPreferences(
                horarioNotificacao = preferences[PreferencesKeys.HORARIO_NOTIFICACAO] ?: "08:00"
            )
        }
    }

    override suspend fun setHorarioNotificacao(horario: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.HORARIO_NOTIFICACAO] = horario
        }
    }

    override fun getHorarioNotificacao(): Flow<String> {
        return context.userPreferencesDataStore.data.map { preferences ->
            preferences[PreferencesKeys.HORARIO_NOTIFICACAO] ?: "08:00"
        }
    }
}
