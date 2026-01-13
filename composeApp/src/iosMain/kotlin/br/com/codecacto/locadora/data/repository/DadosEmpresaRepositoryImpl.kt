package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.DadosEmpresa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

class DadosEmpresaRepositoryImpl : DadosEmpresaRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    private object PreferencesKeys {
        const val NOME_EMPRESA = "dados_empresa_nome"
        const val TELEFONE = "dados_empresa_telefone"
        const val EMAIL = "dados_empresa_email"
        const val ENDERECO = "dados_empresa_endereco"
        const val DOCUMENTO = "dados_empresa_cnpj"
    }

    private val _dadosEmpresaFlow = MutableStateFlow(loadDadosEmpresa())

    private fun loadDadosEmpresa(): DadosEmpresa {
        return DadosEmpresa(
            nomeEmpresa = userDefaults.stringForKey(PreferencesKeys.NOME_EMPRESA) ?: "",
            telefone = userDefaults.stringForKey(PreferencesKeys.TELEFONE) ?: "",
            email = userDefaults.stringForKey(PreferencesKeys.EMAIL) ?: "",
            endereco = userDefaults.stringForKey(PreferencesKeys.ENDERECO) ?: "",
            documento = userDefaults.stringForKey(PreferencesKeys.DOCUMENTO) ?: ""
        )
    }

    override fun getDadosEmpresa(): Flow<DadosEmpresa> {
        return _dadosEmpresaFlow.asStateFlow()
    }

    override suspend fun saveDadosEmpresa(dados: DadosEmpresa) {
        userDefaults.setObject(dados.nomeEmpresa, PreferencesKeys.NOME_EMPRESA)
        userDefaults.setObject(dados.telefone, PreferencesKeys.TELEFONE)
        userDefaults.setObject(dados.email, PreferencesKeys.EMAIL)
        userDefaults.setObject(dados.endereco, PreferencesKeys.ENDERECO)
        userDefaults.setObject(dados.documento, PreferencesKeys.DOCUMENTO)
        userDefaults.synchronize()

        _dadosEmpresaFlow.value = dados
    }
}
