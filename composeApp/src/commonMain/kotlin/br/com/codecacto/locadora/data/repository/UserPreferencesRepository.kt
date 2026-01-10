package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun setHorarioNotificacao(horario: String)
    fun getHorarioNotificacao(): Flow<String>
}
