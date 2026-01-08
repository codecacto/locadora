package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.DadosEmpresa
import kotlinx.coroutines.flow.Flow

interface DadosEmpresaRepository {
    fun getDadosEmpresa(): Flow<DadosEmpresa>
    suspend fun saveDadosEmpresa(dados: DadosEmpresa)
}
