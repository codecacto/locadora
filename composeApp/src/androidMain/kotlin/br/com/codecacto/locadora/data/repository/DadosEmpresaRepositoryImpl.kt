package br.com.codecacto.locadora.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.codecacto.locadora.core.model.DadosEmpresa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dados_empresa")

class DadosEmpresaRepositoryImpl(
    private val context: Context
) : DadosEmpresaRepository {

    private object PreferencesKeys {
        val NOME_EMPRESA = stringPreferencesKey("nome_empresa")
        val TELEFONE = stringPreferencesKey("telefone")
        val EMAIL = stringPreferencesKey("email")
        val ENDERECO = stringPreferencesKey("endereco")
        val CNPJ = stringPreferencesKey("cnpj")
    }

    override fun getDadosEmpresa(): Flow<DadosEmpresa> {
        return context.dataStore.data.map { preferences ->
            DadosEmpresa(
                nomeEmpresa = preferences[PreferencesKeys.NOME_EMPRESA] ?: "",
                telefone = preferences[PreferencesKeys.TELEFONE] ?: "",
                email = preferences[PreferencesKeys.EMAIL] ?: "",
                endereco = preferences[PreferencesKeys.ENDERECO] ?: "",
                cnpj = preferences[PreferencesKeys.CNPJ] ?: ""
            )
        }
    }

    override suspend fun saveDadosEmpresa(dados: DadosEmpresa) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOME_EMPRESA] = dados.nomeEmpresa
            preferences[PreferencesKeys.TELEFONE] = dados.telefone
            preferences[PreferencesKeys.EMAIL] = dados.email
            preferences[PreferencesKeys.ENDERECO] = dados.endereco
            preferences[PreferencesKeys.CNPJ] = dados.cnpj
        }
    }
}
